package com.loan.approver;

import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loan.approver.LoanApplication;
import com.loan.approver.dto.LoanProcessRequest;
import com.loan.approver.repository.LoanProcessRepository;
import com.loan.approver.service.CreditScoreEngine;
import com.loan.approver.service.EncoderService;
import com.loan.approver.service.LoanProcessService;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.DEFINED_PORT,
    classes = LoanApplication.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles({"test"})
class LoanApplicationTestsIT {
  @Autowired private ApplicationContext applicationContext;
  @Autowired private LoanProcessService loanProcessService;
  @Autowired private EncoderService encoderService;
  @Autowired private CreditScoreEngine creditScoreEngine;
  @Autowired private MockMvc mockMvc;

  @Autowired private LoanProcessRepository loanProcessRepository;

  LoanProcessRequest loanProcessRequest;

  @LocalServerPort private int port;

  private static final String APPLY_LOAN_ENDPOINT = "/api/v0/loans/apply";

  @BeforeEach
  public void init() {
	  loanProcessRequest = new LoanProcessRequest();
	  loanProcessRequest.setLoanAmount(190000.00);
      loanProcessRequest.setCurrentAnnualIncome(90000.00);
      loanProcessRequest.setSsnNumber("018022021");
  }

  @Test
  void contextLoads() {
    assertNotNull("The application context should have loaded.", this.applicationContext);
  }

  @Test
  void applyLoan_withValidParameter_return200() throws Exception {
    mockMvc
        .perform(
            post(APPLY_LOAN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loanProcessRequest)))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  void applyLoan_withInvalidParameter_return400() throws Exception {
	  loanProcessRequest = new LoanProcessRequest();
	  loanProcessRequest.setSsnNumber("09032021");
    mockMvc
        .perform(
            post(APPLY_LOAN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loanProcessRequest)))
        .andExpect(status().isBadRequest());
  }
}
