package com.safa.loanapi.installment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safa.loanapi.installment.dao.Installment;
import com.safa.loanapi.installment.dto.PayInstallment;
import com.safa.loanapi.loan.dto.LoanPaymentInfo;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InstallmentControllerTests {

    @Mock
    private InstallmentService installmentService;

    @InjectMocks
    private InstallmentController installmentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(installmentController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listInstallments_Success() throws Exception {
        Long loanId = 1L;
        Long customerId = 2L;
        List<Installment> mockInstallments = List.of(
                new Installment(loanId, BigDecimal.valueOf(500.0), BigDecimal.valueOf(500.0), LocalDate.now()),
                new Installment(loanId, BigDecimal.valueOf(500.0), BigDecimal.valueOf(500.0), LocalDate.now().plusMonths(1))
        );

        when(installmentService.listInstallments(customerId, loanId)).thenReturn(mockInstallments);

        mockMvc.perform(get("/customers/2/loans/{loan_id}/installments", loanId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].loanId").value(1))
                .andExpect(jsonPath("$[0].amount").value(500.0));

        verify(installmentService, times(1)).listInstallments(customerId, loanId);
    }

    @Test
    void PayInstallment_Success() throws Exception {
        Long loanId = 1L;
        Long customerId = 2L;

        Installment paid = new Installment(loanId, BigDecimal.valueOf(500.0), BigDecimal.valueOf(500.0), LocalDate.now());
        paid.setId(5L);

        PayInstallment request = new PayInstallment();
        request.setAmount(500.0);

        LoanPaymentInfo mockPaymentInfo = new LoanPaymentInfo();
        mockPaymentInfo.setPaidInstallments(List.of(paid));
        mockPaymentInfo.setTotalAmountSpent(450.0);
        mockPaymentInfo.setLoanPaidCompletely(true);

        when(installmentService.payInstallment(customerId, loanId, request.getAmount()))
                .thenReturn(mockPaymentInfo);

        mockMvc.perform(post("/customers/2/loans/{loan_id}/payment", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidInstallments.length()").value(1))
                .andExpect(jsonPath("$.paidInstallments[0].id").value(5L))
                .andExpect(jsonPath("$.loanPaidCompletely").value(true))
                .andExpect(jsonPath("$.totalAmountSpent").value(450.0));

        verify(installmentService, times(1)).payInstallment(customerId, loanId, request.getAmount());
    }
}
