package com.company.approval_system.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.node.ObjectNode;

import javax.management.ObjectName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
public class RequestApprovalFlowIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long registerUser(String email, String password, String fullName,
                              String role, Long managerId) throws Exception{
        ObjectNode body = objectMapper.createObjectNode()
                .put("email", email)
                .put("password", password)
                .put("fullName", fullName)
                .put("role", role);
        if(managerId!=null){
            body.put("managerId", managerId);
        }

        String response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("userId").asLong();
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
        long managerId = registerUser(
                "manager.happy@integrationtest.com", "ManagerPass123",
                "Happy Path Manager", "Manager", null);
        String managerToken = login("manager.happy@integrationtest.com","ManagerPass123");

        registerUser(
                "employee.happy@integrationtest.com","EmployeePass123",
                "Happy Path Employee", "EMPLOYEE", managerId);
        String employeeToken = login("employee.happy@integrationtest.com", "EmployeePass123");

        long requestId = createLeaveRequest(employeeToken);

        mockMvc.perform(put("/api/v1/requests/{id}/approve", requestId)
                .header("Authorization", "Bearer "+ managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comments\": \"Approved by integration test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
