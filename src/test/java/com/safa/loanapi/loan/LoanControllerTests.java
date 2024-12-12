package com.safa.loanapi.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safa.loanapi.WithMockCustomUser;
import com.safa.loanapi.loan.dao.Loan;
import com.safa.loanapi.loan.dto.CreateLoan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoanControllerTests {

    @Mock
    private LoanService loanService;

    @InjectMocks
    private LoanController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockCustomUser()
    void CreateLoan_Success() throws Exception {
        CreateLoan request = new CreateLoan(1000.0, 0.1, 12);
        Loan mockLoan = new Loan(1L, BigDecimal.valueOf(1000.0), BigDecimal.valueOf(1050.0), 12, false);
        mockLoan.setId(1L);

        when(loanService.createLoan(anyLong(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(mockLoan);

        mockMvc.perform(post("/customers/1/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value("1000.0"))
                .andExpect(jsonPath("$.id").value(1L));

        verify(loanService, times(1)).createLoan(eq(1L), eq(1000.0), eq(0.1), eq(12));
    }

    @Test
    void ListLoans_Success() throws Exception {
        // Arrange
        Loan loan1 = new Loan(1L, BigDecimal.valueOf(1000.0), BigDecimal.valueOf(1050.0), 12, false);
        Loan loan2 = new Loan(2L, BigDecimal.valueOf(2000.0), BigDecimal.valueOf(2100.0), 24, false);
        List<Loan> mockLoans = Arrays.asList(loan1, loan2);

        when(loanService.listLoans(1L)).thenReturn(mockLoans);

        // Act & Assert
        mockMvc.perform(get("/customers/1/loans")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].amount").value(1000.0))
                .andExpect(jsonPath("$[1].amount").value(2000.0));

        verify(loanService, times(1)).listLoans(1L);
    }

    @Test
    void CreateLoan_ValidationFailure() throws Exception {
        // Arrange
        CreateLoan invalidRequest = new CreateLoan(0.0, -0.01, 0); // Invalid data

        // Act & Assert
        mockMvc.perform(post("/customers/1/loans")
                        .principal(() -> "testUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
