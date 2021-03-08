package com.loan.approver.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.approver.controller.LoanProcessController;
import com.loan.approver.dto.LoanProcessRequest;
import com.loan.approver.dto.LoanProcessResponse;
import com.loan.approver.enumeration.LoanApprovalStatus;
import com.loan.approver.service.LoanProcessService;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(value = LoanProcessController.class)
@ActiveProfiles("test")
class LoanProcessControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LoanProcessService loanProcessService;

  @Test
  void test_success() throws Exception {
    when(loanProcessService.process(Mockito.any())).thenReturn(mockedLoanProcessResponse());

    LoanProcessRequest loanProcessRequest = new LoanProcessRequest();
    loanProcessRequest.setSsnNumber("018022020");
    loanProcessRequest.setCurrentAnnualIncome(10000.00);
    loanProcessRequest.setLoanAmount(6000.00);

    String requestJson = new ObjectMapper().writeValueAsString(loanProcessRequest);
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v0/loans/apply")
                .contentType("application/json")
                .content(requestJson))
        .andDo(print())
        .andExpect(jsonPath("$.requestId", notNullValue()))
        .andExpect(jsonPath("$.loanApprovalStatus", notNullValue()))
        .andExpect(jsonPath("$.approvalAmount", greaterThanOrEqualTo(0.00)))
        .andExpect(jsonPath("$.message", notNullValue()))
        .andExpect(status().is2xxSuccessful())
        .andReturn()
        .getResponse();
  }

  @Test
  void test_bad_request() throws Exception {
    when(loanProcessService.process(Mockito.any())).thenReturn(mockedLoanProcessResponse());

    LoanProcessRequest loanProcessRequest = new LoanProcessRequest();
    loanProcessRequest.setSsnNumber("018022020");
    loanProcessRequest.setCurrentAnnualIncome(10000.00);
    loanProcessRequest.setLoanAmount(6000.00);

    String requestJson = new ObjectMapper().writeValueAsString(loanProcessRequest);
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/v0/loans/apply")
                .contentType("application/json")
                .content(requestJson))
        .andDo(print())
        .andExpect(jsonPath("$..Source").value("LoanApprover"))
        .andExpect(jsonPath("$..ReasonCode").value("400 BAD_REQUEST"))
        .andExpect(jsonPath("$..Description").value("Bad Request"))
        .andExpect(jsonPath("$..Details").value("ssnNumber - Please provide proper SSN"))
        .andExpect(jsonPath("$..Recoverable").value(false))
        .andExpect(status().is4xxClientError())
        .andReturn()
        .getResponse();
  }

  public LoanProcessResponse mockedLoanProcessResponse() {
    LoanProcessResponse loanProcessResponse = new LoanProcessResponse();
    loanProcessResponse.setRequestId(UUID.randomUUID());
    loanProcessResponse.setLoanApprovalStatus(LoanApprovalStatus.APPROVED);
    loanProcessResponse.setApprovalAmount(5000.00);
    loanProcessResponse.setMessage("");
    return loanProcessResponse;
  }
}
