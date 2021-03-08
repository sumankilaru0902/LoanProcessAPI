package org.deb.loan.approver.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.deb.loan.approver.dto.LoanApplicationRequest;
import org.deb.loan.approver.dto.LoanApplicationResponse;
import org.deb.loan.approver.enumeration.LoanApprovalStatus;
import org.deb.loan.approver.service.LoanApplicationService;
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
@WebMvcTest(value = LoanApplicationController.class)
@ActiveProfiles("test")
class LoanApplicationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private LoanApplicationService loanApplicationService;

  @Test
  void test_success() throws Exception {
    when(loanApplicationService.process(Mockito.any())).thenReturn(mockedLoanApplicationResponse());

    LoanApplicationRequest loanApplicationRequest = new LoanApplicationRequest();
    loanApplicationRequest.setSsnNumber("018-02-2020");
    loanApplicationRequest.setCurrentAnnualIncome(10000.00);
    loanApplicationRequest.setLoanAmount(6000.00);

    String requestJson = new ObjectMapper().writeValueAsString(loanApplicationRequest);
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
    when(loanApplicationService.process(Mockito.any())).thenReturn(mockedLoanApplicationResponse());

    LoanApplicationRequest loanApplicationRequest = new LoanApplicationRequest();
    loanApplicationRequest.setSsnNumber("018-02-202");
    loanApplicationRequest.setCurrentAnnualIncome(10000.00);
    loanApplicationRequest.setLoanAmount(6000.00);

    String requestJson = new ObjectMapper().writeValueAsString(loanApplicationRequest);
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

  public LoanApplicationResponse mockedLoanApplicationResponse() {
    LoanApplicationResponse loanApplicationResponse = new LoanApplicationResponse();
    loanApplicationResponse.setRequestId(UUID.randomUUID());
    loanApplicationResponse.setLoanApprovalStatus(LoanApprovalStatus.APPROVED);
    loanApplicationResponse.setApprovalAmount(5000.00);
    loanApplicationResponse.setMessage("");
    return loanApplicationResponse;
  }
}
