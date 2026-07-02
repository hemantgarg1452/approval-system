package com.company.approval_system.service;

import com.company.approval_system.dto.CreateRequestDto;
import com.company.approval_system.dto.RequestResponse;
import com.company.approval_system.entity.Request;
import com.company.approval_system.entity.User;
import com.company.approval_system.enums.RequestStatus;
import com.company.approval_system.enums.RequestType;
import com.company.approval_system.enums.Role;
import com.company.approval_system.exception.InvalidRequestException;
import com.company.approval_system.repository.RequestRepository;
import com.company.approval_system.repository.UserRepository;
import com.company.approval_system.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private RequestService requestService;
    private User employee;
    private User manager;
    private UserPrincipal employeePrincipal;

    @BeforeEach
    void setUp(){
        manager = User.builder()
                .id(1L)
                .email("manager@company.com")
                .fullName("Manager User")
                .role(Role.MANAGER)
                .build();

        employee = User.builder()
                .id(2L)
                .email("employee@company.com")
                .fullName("Employee User")
                .role(Role.EMPLOYEE)
                .manager(manager)
                .build();

        employeePrincipal = new UserPrincipal(
                employee.getId(),
                employee.getEmail(),
                "password",
                employee.getRole(),
                true
        );
    }

    @Test
    void createRequest_WithValidLeaveRequest_ShouldSucceed(){
        //Arrange
        CreateRequestDto dto = new CreateRequestDto();
        dto.setRequestType(RequestType.LEAVE);
        dto.setTitle("Four days Leave");
        dto.setDescription("Family Emergency!");
        dto.setStartDate(LocalDate.now().plusDays(4));
        dto.setEndDate(LocalDate.now().plusDays(8));

        Request savedRequest = Request.builder()
                .id(1L)
                .requestType(dto.getRequestType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(RequestStatus.PENDING)
                .createdBy(employee)
                .approver(manager)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);

        //Act
        RequestResponse response = requestService.createRequest(dto, employeePrincipal);

        //Assert
        assertNotNull(response);
        assertEquals(RequestType.LEAVE, response.getRequestType());
        assertEquals(RequestStatus.PENDING, response.getStatus());
        assertEquals(employee.getId(), response.getCreatedById());
        assertEquals(manager.getId(), response.getApproverId());
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    @Test
    void createRequest_WithMissingDatesForLeave_ShouldThrowException(){
        //Arrange
        CreateRequestDto dto = new CreateRequestDto();
        dto.setRequestType(RequestType.LEAVE);
        dto.setTitle("Annual Leave");
        dto.setDescription("Taking annual leave for vacation");
        //Missing start and end dates

        //when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        //Act & Assert
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                ()->requestService.createRequest(dto, employeePrincipal)
        );

        assertTrue(exception.getMessage().contains("require start date and end date"));
        verify(requestRepository, never()).save(any());
    }


    @Test
    void createRequest_WhenUserHasNoManager_ShouldThrowException(){
        //Arrange
        employee.setManager(null);

        CreateRequestDto dto = new CreateRequestDto();
        dto.setRequestType(RequestType.LEAVE);
        dto.setTitle("Annual Leave");
        dto.setDescription("Taking annual Leave");
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(14));

        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        //Act & Assert
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                ()->requestService.createRequest(dto, employeePrincipal)
        );

        assertTrue(exception.getMessage().contains("No manager assigned"));
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void createRequest_WithEndDateBeforeStartDate_ShouldThrowException(){
        //Arrange
        CreateRequestDto dto = new CreateRequestDto();
        dto.setRequestType(RequestType.LEAVE);
        dto.setTitle("Annual Leave");
        dto.setDescription("Invalid date range");
        dto.setStartDate(LocalDate.now().plusDays(14));
        dto.setEndDate(LocalDate.now().plusDays(7)); //before start date

        //when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        //Act & Assert
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                ()->requestService.createRequest(dto, employeePrincipal)
        );

        assertTrue(exception.getMessage().contains("End date cannot be before start date"));
        verify(requestRepository, never()).save(any());
    }
}
