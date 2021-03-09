package com.loan.approver.service;

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

import com.loan.approver.dto.LoanProcessRequest;
import com.loan.approver.dto.LoanProcessResponse;
import com.loan.approver.enumeration.LoanApprovalStatus;
import com.loan.approver.model.LoanProcess;
import com.loan.approver.repository.LoanProcessRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
class LoanProcessServiceTest {

  public static final String SSN_NUMBER = "018022020";
  public static final String CREDIT_ENGINE_URL = "http://localhost:8080/api/v0/credit/score";

  @Autowired private LoanProcessService loanProcessService;

  @MockBean private LoanProcessRepository loanProcessRepository;

  @MockBean private RestTemplate restTemplate;

  @Test
  void process_success() {

    String creditRatingResponse = "{\"creditRating\":701}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    when(loanProcessRepository.findBySsnNumberOrderByApplicationDateTimeDesc(anyString()))
        .thenReturn(Optional.empty());
    when(loanProcessRepository.save(Mockito.any())).thenReturn(mockApprovedLoanProcess());
    when(restTemplate.exchange(anyString(), Mockito.eq(GET), any(), Mockito.eq(String.class)))
        .thenReturn(ResponseEntity.ok(creditRatingResponse));

    LoanProcessRequest loanProcessRequest =new LoanProcessRequest();
		   loanProcessRequest.setSsnNumber(SSN_NUMBER);
		   loanProcessRequest.setLoanAmount(100000.00);
		   loanProcessRequest.setCurrentAnnualIncome(144000.00);

    LoanProcessResponse loanProcessResponse =
    		loanProcessService.process(loanProcessRequest);

    assertEquals(
        0, loanProcessResponse.getLoanApprovalStatus().compareTo(LoanApprovalStatus.APPROVED));
    verify(loanProcessRepository,times(1)).save(any());
  }

  @Test
  void process_applicatinWithinAMonth() {

    String creditRatingResponse = "{\"creditRating\":701}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ArrayList<LoanProcess> exitingLoanData = new ArrayList<>();
    exitingLoanData.add(mockApprovedLoanProcess());
    when(loanProcessRepository.findBySsnNumberOrderByApplicationDateTimeDesc(anyString()))
        .thenReturn(Optional.of(exitingLoanData));
    when(loanProcessRepository.save(Mockito.any())).thenReturn(mockApprovedLoanProcess());
    when(restTemplate.exchange(anyString(), Mockito.eq(GET), any(), Mockito.eq(String.class)))
        .thenReturn(ResponseEntity.ok(creditRatingResponse));

    LoanProcessRequest loanProcessRequest = new LoanProcessRequest();
    loanProcessRequest.setSsnNumber(SSN_NUMBER);
    loanProcessRequest.setLoanAmount(100000.00);
    loanProcessRequest.setCurrentAnnualIncome(144000.00);

    LoanProcessResponse loanProcessResponse =
    		loanProcessService.process(loanProcessRequest);

    assertEquals(
        0, loanProcessResponse.getLoanApprovalStatus().compareTo(LoanApprovalStatus.REJECTED));
    verify(loanProcessRepository,times(1)).save(any());
  }

  @Test
  void process_lowCreditRating() {

    String creditRatingResponse = "{\"creditRating\":700}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    when(loanProcessRepository.findBySsnNumberOrderByApplicationDateTimeDesc(anyString()))
        .thenReturn(Optional.empty());
    when(loanProcessRepository.save(Mockito.any())).thenReturn(mockApprovedLoanProcess());
    when(restTemplate.exchange(anyString(), Mockito.eq(GET), any(), Mockito.eq(String.class)))
        .thenReturn(ResponseEntity.ok(creditRatingResponse));

    LoanProcessRequest loanProcessRequest = new LoanProcessRequest();
    loanProcessRequest.setSsnNumber(SSN_NUMBER);
    loanProcessRequest.setLoanAmount(100000.00);
    loanProcessRequest.setCurrentAnnualIncome(144000.00);

    LoanProcessResponse loanProcessResponse =
    		loanProcessService.process(loanProcessRequest);

    assertEquals(
        0, loanProcessResponse.getLoanApprovalStatus().compareTo(LoanApprovalStatus.REJECTED));
    verify(loanProcessRepository,times(1)).save(any());
  }

  @Test
  void process_creditRestApiUnsuccessful() {

    String creditRatingResponse = "{\"creditRating\":700}";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    when(loanProcessRepository.findBySsnNumberOrderByApplicationDateTimeDesc(anyString()))
        .thenReturn(Optional.empty());
    when(loanProcessRepository.save(Mockito.any())).thenReturn(mockApprovedLoanProcess());
    when(restTemplate.exchange(anyString(), Mockito.eq(GET), any(), Mockito.eq(String.class)))
        .thenReturn(ResponseEntity.badRequest().body(creditRatingResponse));

    LoanProcessRequest loanProcessRequest = new LoanProcessRequest();
    loanProcessRequest.setSsnNumber(SSN_NUMBER);
    loanProcessRequest.setLoanAmount(100000.00);
    loanProcessRequest.setCurrentAnnualIncome(144000.00);

    LoanProcessResponse loanProcessResponse =
    		loanProcessService.process(loanProcessRequest);

    assertEquals(
        0, loanProcessResponse.getLoanApprovalStatus().compareTo(LoanApprovalStatus.REJECTED));
    verify(loanProcessRepository,times(1)).save(any());
  }

  public LoanProcess mockApprovedLoanProcess() {
    LoanProcess loanApplication = new LoanProcess();
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

  public LoanProcess mockRejectedLoanProcess() {
    LoanProcess loanProcess = new LoanProcess();
    loanProcess.setCurrentAnnualIncome(60000.00);
    loanProcess.setRequestedAmount(40000.00);
    loanProcess.setId(UUID.randomUUID());
    loanProcess.setSsnNumber(SSN_NUMBER);
    loanProcess.setSanctionedAmount(0.00);
    loanProcess.setLoanApprovalStatus(LoanApprovalStatus.REJECTED);
    loanProcess.setMessage("");
    loanProcess.setApplicationDateTime(LocalDateTime.now(Clock.systemUTC()));
    return loanProcess;
  }
}
