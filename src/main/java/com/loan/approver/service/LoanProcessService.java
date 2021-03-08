package com.loan.approver.service;

import com.jayway.jsonpath.JsonPath;
import com.loan.approver.dto.CreditRatingRequest;
import com.loan.approver.dto.LoanProcessRequest;
import com.loan.approver.dto.LoanProcessResponse;
import com.loan.approver.enumeration.LoanApprovalStatus;
import com.loan.approver.model.LoanProcess;
import com.loan.approver.repository.LoanProcessRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanProcessService {
  private final LoanProcessRepository loanProcessRepository;
  private final Clock clock;
  private final EncoderService encoderService;
  private final RestTemplate restTemplate;

  @Value("${creditScore.url:http://localhost:8080/api/v0/credit/score}")
  private String creditScoreURL;

  public LoanProcessResponse process(final LoanProcessRequest loanProcessRequest) {
    LoanProcessResponse loanProcessResponse = null;

    boolean checkCreditRating = true;
    String message = "";
    String encodedSSN = encoderService.encode(loanProcessRequest.getSsnNumber());
    Optional<List<LoanProcess>> existingApplications =
    		loanProcessRepository.findBySsnNumberOrderByApplicationDateTimeDesc(encodedSSN);
    if ((existingApplications.isPresent() && !existingApplications.get().isEmpty())
        && (!LocalDateTime.now(clock)
            .isAfter(existingApplications.get().get(0).getApplicationDateTime().plusDays(30)))) {
      message =
          String.format(
              "Last applied for loan on %s. There must be 30 days gap between two loan applications.",
              existingApplications.get().get(0).getApplicationDateTime());
      loanProcessResponse = rejectLoanProcess(message);
      checkCreditRating = false;
    }

    if (checkCreditRating) {
      int creditRating = getCreditRating(loanProcessRequest.getSsnNumber());
      log.info(String.format("Credit rating %d", creditRating));
      if (creditRating > 700) {
    	  loanProcessResponse = approveLoanProcess(loanProcessRequest);
      } else {
        message =
            String.format(
                "Credit rating is %d. To approve required rating should be greater than 700.",
                creditRating);
        loanProcessResponse = rejectLoanProcess(message);
      }
    }
    LoanProcess loanProcess =
    		saveLoanProcess(loanProcessRequest, checkCreditRating, encodedSSN, message);
    log.info("Loan application {}", loanProcess);
    loanProcessResponse.setRequestId(loanProcess.getId());
    return loanProcessResponse;
  }

  private int getCreditRating(String ssnNumber) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CreditRatingRequest> entity =
        new HttpEntity<>(new CreditRatingRequest(ssnNumber), headers);

    log.info(String.format("Going to call credit score : %s", creditScoreURL));
    ResponseEntity<String> creditRatingResponse =
        restTemplate.exchange(creditScoreURL, HttpMethod.GET, entity, String.class);
    if (creditRatingResponse.getStatusCode().is2xxSuccessful()) {
      return JsonPath.read(creditRatingResponse.getBody(), "$.creditRating");
    } else {
      log.error("Call to credit rating is not successful");
      return -1;
    }
  }

  private LoanProcess saveLoanProcess(
      LoanProcessRequest loanProcessRequest,
      boolean loanApproved,
      String encodedSSN,
      String message) {
    LoanProcess loanProcess = new LoanProcess();
    loanProcess.setId(UUID.randomUUID());
    loanProcess.setApplicationDateTime(LocalDateTime.now(clock));
    loanProcess.setCurrentAnnualIncome(loanProcessRequest.getCurrentAnnualIncome());
    loanProcess.setSsnNumber(encodedSSN);
    loanProcess.setRequestedAmount(loanProcessRequest.getLoanAmount());
    if (loanApproved) {
    	loanProcess.setSanctionedAmount(loanProcessRequest.getCurrentAnnualIncome() / 2);
    	loanProcess.setLoanApprovalStatus(LoanApprovalStatus.APPROVED);
    } else {
    	loanProcess.setSanctionedAmount(0.00);
    	loanProcess.setLoanApprovalStatus(LoanApprovalStatus.REJECTED);
    }
    loanProcess.setMessage(message);
    return loanProcessRepository.save(loanProcess);
  }

  private LoanProcessResponse rejectLoanProcess(String errorMessage) {
    LoanProcessResponse loanProcessResponse = new LoanProcessResponse();
    loanProcessResponse.setLoanApprovalStatus(LoanApprovalStatus.REJECTED);
    loanProcessResponse.setMessage(errorMessage);
    return loanProcessResponse;
  }

  private LoanProcessResponse approveLoanProcess(
      LoanProcessRequest loanProcessRequest) {
    LoanProcessResponse loanProcessResponse = new LoanProcessResponse();
    loanProcessResponse.setLoanApprovalStatus(LoanApprovalStatus.APPROVED);
    loanProcessResponse.setApprovalAmount(loanProcessRequest.getCurrentAnnualIncome() / 2);
    loanProcessResponse.setMessage("Loan Request Approved ");
    return loanProcessResponse;
  }
}
