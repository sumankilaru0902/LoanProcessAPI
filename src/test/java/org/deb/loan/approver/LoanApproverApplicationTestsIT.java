package org.deb.loan.approver;

import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.deb.loan.approver.dto.LoanApplicationRequest;
import org.deb.loan.approver.repository.LoanApplicationRepository;
import org.deb.loan.approver.service.CreditScoreEngine;
import org.deb.loan.approver.service.EncoderService;
import org.deb.loan.approver.service.LoanApplicationService;
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

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.DEFINED_PORT,
    classes = LoanApprovalApplication.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles({"test"})
class LoanApproverApplicationTestsIT {
  @Autowired private ApplicationContext applicationContext;
  @Autowired private LoanApplicationService loanApplicationService;
  @Autowired private EncoderService encoderService;
  @Autowired private CreditScoreEngine creditScoreEngine;
  @Autowired private MockMvc mockMvc;

  @Autowired private LoanApplicationRepository loanApplicationRepository;

  LoanApplicationRequest loanApplicationRequest;

  @LocalServerPort private int port;

  private static final String APPLY_LOAN_ENDPOINT = "/api/v0/loans/apply";

  @BeforeEach
  public void init() {
    loanApplicationRequest = new LoanApplicationRequest();
    loanApplicationRequest.setLoanAmount(190000.00);
    loanApplicationRequest.setCurrentAnnualIncome(90000.00);
    loanApplicationRequest.setSsnNumber("018-02-2021");
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
                .content(new ObjectMapper().writeValueAsString(loanApplicationRequest)))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  void applyLoan_withInvalidParameter_return400() throws Exception {
    loanApplicationRequest.setSsnNumber("22-02-2021");
    mockMvc
        .perform(
            post(APPLY_LOAN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loanApplicationRequest)))
        .andExpect(status().isBadRequest());
  }
}
