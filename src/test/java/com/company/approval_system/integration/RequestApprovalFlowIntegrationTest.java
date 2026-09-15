package com.company.approval_system.integration;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class RequestApprovalFlowIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private long createUserAsAdmin(String email, String password, String fullName,
                              String role, Long managerId, String adminToken) throws Exception{
        ObjectNode body = objectMapper.createObjectNode()
                .put("email", email)
                .put("password", password)
                .put("fullName", fullName)
                .put("role", role);
        if(managerId!=null){
            body.put("managerId", managerId);
        }

        String response = mockMvc.perform(post("/api/v1/users")
                .header("Authorization", "Bearer "+ adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private String login(String email, String password) throws Exception{
        String body = objectMapper.createObjectNode()
                .put("email", email)
                .put("password", password)
                .toString();

        String response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private long createLeaveRequest(String employeeToken) throws Exception{
        String body = objectMapper.createObjectNode()
                .put("requestType", "LEAVE")
                .put("title", "Integration test leave request")
                .put("description","Created by an automated integration test")
                .put("startDate", "2026-09-01")
                .put("endDate","2026-09-05")
                .toString();

        String response = mockMvc.perform(post("/api/v1/requests")
                .header("Authorization","Bearer "+employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    //automated testing of request flow.
    @Test
    void employeeCanCreateRequestAndManagerCanApproveIt() throws Exception{
        String adminToken = login("admin@test.com", "AdminPass123");
        long managerId = createUserAsAdmin(
                "manager.happy@integrationtest.com", "ManagerPass123",
                "Happy Path Manager", "MANAGER", null, adminToken);
        String managerToken = login("manager.happy@integrationtest.com","ManagerPass123");

        createUserAsAdmin(
                "employee.happy@integrationtest.com","EmployeePass123",
                "Happy Path Employee", "EMPLOYEE", managerId, adminToken);
        String employeeToken = login("employee.happy@integrationtest.com", "EmployeePass123");

        long requestId = createLeaveRequest(employeeToken);

        mockMvc.perform(put("/api/v1/requests/{id}/approve", requestId)
                .header("Authorization", "Bearer "+ managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "action":"APPROVED",
                            "comments":"Approved by integration test"
                        }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    //Negative Test - RBAC. (6a)
    @Test
    void employeeCannotApproveRequest() throws Exception{
        String adminToken = login("admin@test.com", "AdminPass123");
        long managerId = createUserAsAdmin(
                "manager.rbac@integrationtest.com", "ManagerPass123",
                "RBAC Manager", "MANAGER",null, adminToken);

        createUserAsAdmin(
                "employee.rbac@integrationtest.com", "EmployeePass123",
                "RBAC Employee", "EMPLOYEE", managerId, adminToken);
        String employeeToken = login("employee.rbac@integrationtest.com", "EmployeePass123");

        long requestId = createLeaveRequest(employeeToken);

        mockMvc.perform(put("/api/v1/requests/{id}/approve", requestId)
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comments\": \"trying to self-approve\"}"))
                .andExpect(status().isForbidden());
    }

    //Negative Test - Business rule enforcement. (6b)
    @Test
    void managerCannotApproveSameRequestTwice() throws Exception{
        String adminToken = login("admin@test.com", "AdminPass123");
        long managerId = createUserAsAdmin(
                "manager.double@integrationtest.com","ManagerPass123",
                "Double Approve Manager","MANAGER", null, adminToken);
        String managerToken = login("manager.double@integrationtest.com","ManagerPass123");

        createUserAsAdmin(
                "employee.double@integrationtest.com", "EmployeePass123",
                "Double Approve Employee", "EMPLOYEE", managerId, adminToken);
        String employeeToken = login("employee.double@integrationtest.com", "EmployeePass123");

        long requestId = createLeaveRequest(employeeToken);

        mockMvc.perform(put("/api/v1/requests/{id}/approve", requestId)
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "action": "APPROVED",
                            "comments": "first approval"
                        }
                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/requests/{id}/approve", requestId)
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "action": "APPROVED",
                            "comments": "second approval attempt"
                        }
                """))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    //Negative Test - no token at all.
    @Test
    void cannotAccessRequestWithoutToken() throws Exception{
        mockMvc.perform(get("/api/v1/requests/my-requests"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
