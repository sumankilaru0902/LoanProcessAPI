package org.deb.loan.approver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.deb.loan.approver.dto.LoanApplicationRequest;
import org.deb.loan.approver.dto.LoanApplicationResponse;
import org.deb.loan.approver.enumeration.LoanApprovalStatus;
import org.deb.loan.approver.model.LoanApplication;
import org.deb.loan.approver.repository.LoanApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

// @RunWith(SpringRunner.class)

@RunWith(SpringRunner.class)
@SpringBootTest
// @AutoConfigureMockMvc
class LoanApplicationServiceTest {

  public static final String SSN_NUMBER = "018-02-2020";
  public static final String CREDIT_ENGINE_URL = "http://localhost:8080/api/v0/credit/score";

  @Autowired private LoanApplicationService loanApplicationService;

  @MockBean private LoanApplicationRepository loanApplicationRepository;

  @MockBean private RestTemplate restTemplate;

  @Test
  void process_success() {

    String creditRatingResponse = "{\"creditRating\":701}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    when(loanApplicationRepository.findBySsnNumberOrderByApplicationDateTimeDesc(anyString()))
        .thenReturn(Optional.empty());
    when(loanApplicationRepository.save(Mockito.any())).thenReturn(mockApprovedLoanApplication());
    when(restTemplate.exchange(anyString(), Mockito.eq(GET), any(), Mockito.eq(String.class)))
        .thenReturn(ResponseEntity.ok(creditRatingResponse));

    LoanApplicationRequest loanApplicationRequest = new LoanApplicationRequest();
    loanApplicationRequest.setSsnNumber(SSN_NUMBER);
    loanApplicationRequest.setLoanAmount(100000.00);
    loanApplicationRequest.setCurrentAnnualIncome(144000.00);

    LoanApplicationResponse loanApplicationResponse =
        loanApplicationService.process(loanApplicationRequest);

    assertEquals(
        0, loanApplicationResponse.getLoanApprovalStatus().compareTo(LoanApprovalStatus.APPROVED));
    verify(loanApplicationRepository,times(1)).save(any());
  }

  @Test
  void process_applicatinWithinAMonth() {

    String creditRatingResponse = "{\"creditRating\":701}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ArrayList exitingLoanApplications = new ArrayList();
    exitingLoanApplications.add(mockApprovedLoanApplication());
    when(loanApplicationRepository.findBySsnNumberOrderByApplicationDateTimeDesc(anyString()))
        .thenReturn(Optional.of(exitingLoanApplications));
    when(loanApplicationRepository.save(Mockito.any())).thenReturn(mockApprovedLoanApplication());
    when(restTemplate.exchange(anyString(), Mockito.eq(GET), any(), Mockito.eq(String.class)))
        .thenReturn(ResponseEntity.ok(creditRatingResponse));

    LoanApplicationRequest loanApplicationRequest = new LoanApplicationRequest();
    loanApplicationRequest.setSsnNumber(SSN_NUMBER);
    loanApplicationRequest.setLoanAmount(100000.00);
    loanApplicationRequest.setCurrentAnnualIncome(144000.00);

    LoanApplicationResponse loanApplicationResponse =
        loanApplicationService.process(loanApplicationRequest);

    assertEquals(
        0, loanApplicationResponse.getLoanApprovalStatus().compareTo(LoanApprovalStatus.REJECTED));
    verify(loanApplicationRepository,times(1)).save(any());
  }

  @Test
  void process_lowCreditRating() {

    String creditRatingResponse = "{\"creditRating\":700}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    when(loanApplicationRepository.findBySsnNumberOrderByApplicationDateTimeDesc(anyString()))
        .thenReturn(Optional.empty());
    when(loanApplicationRepository.save(Mockito.any())).thenReturn(mockApprovedLoanApplication());
    when(restTemplate.exchange(anyString(), Mockito.eq(GET), any(), Mockito.eq(String.class)))
        .thenReturn(ResponseEntity.ok(creditRatingResponse));

    LoanApplicationRequest loanApplicationRequest = new LoanApplicationRequest();
    loanApplicationRequest.setSsnNumber(SSN_NUMBER);
    loanApplicationRequest.setLoanAmount(100000.00);
    loanApplicationRequest.setCurrentAnnualIncome(144000.00);

    LoanApplicationResponse loanApplicationResponse =
        loanApplicationService.process(loanApplicationRequest);

    assertEquals(
        0, loanApplicationResponse.getLoanApprovalStatus().compareTo(LoanApprovalStatus.REJECTED));
    verify(loanApplicationRepository,times(1)).save(any());
  }

  @Test
  void process_creditRestApiUnsuccessful() {

    String creditRatingResponse = "{\"creditRating\":700}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    when(loanApplicationRepository.findBySsnNumberOrderByApplicationDateTimeDesc(anyString()))
        .thenReturn(Optional.empty());
    when(loanApplicationRepository.save(Mockito.any())).thenReturn(mockApprovedLoanApplication());
    when(restTemplate.exchange(anyString(), Mockito.eq(GET), any(), Mockito.eq(String.class)))
        .thenReturn(ResponseEntity.badRequest().body(creditRatingResponse));

    LoanApplicationRequest loanApplicationRequest = new LoanApplicationRequest();
    loanApplicationRequest.setSsnNumber(SSN_NUMBER);
    loanApplicationRequest.setLoanAmount(100000.00);
    loanApplicationRequest.setCurrentAnnualIncome(144000.00);

    LoanApplicationResponse loanApplicationResponse =
        loanApplicationService.process(loanApplicationRequest);

    assertEquals(
        0, loanApplicationResponse.getLoanApprovalStatus().compareTo(LoanApprovalStatus.REJECTED));
    verify(loanApplicationRepository,times(1)).save(any());
  }

  public LoanApplication mockApprovedLoanApplication() {
    LoanApplication loanApplication = new LoanApplication();
    loanApplication.setCurrentAnnualIncome(60000.00);
    loanApplication.setRequestedAmount(40000.00);
    loanApplication.setId(UUID.randomUUID());
    loanApplication.setSsnNumber(SSN_NUMBER);
    loanApplication.setSanctionedAmount(30000.00);
    loanApplication.setLoanApprovalStatus(LoanApprovalStatus.APPROVED);
    loanApplication.setMessage("");
    loanApplication.setApplicationDateTime(LocalDateTime.now(Clock.systemUTC()));
    return loanApplication;
  }

  public LoanApplication mockRejectedLoanApplication() {
    LoanApplication loanApplication = new LoanApplication();
    loanApplication.setCurrentAnnualIncome(60000.00);
    loanApplication.setRequestedAmount(40000.00);
    loanApplication.setId(UUID.randomUUID());
    loanApplication.setSsnNumber(SSN_NUMBER);
    loanApplication.setSanctionedAmount(0.00);
    loanApplication.setLoanApprovalStatus(LoanApprovalStatus.REJECTED);
    loanApplication.setMessage("");
    loanApplication.setApplicationDateTime(LocalDateTime.now(Clock.systemUTC()));
    return loanApplication;
  }
}
