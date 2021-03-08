package org.deb.loan.approver.service;

import com.jayway.jsonpath.JsonPath;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deb.loan.approver.dto.CreditRatingRequest;
import org.deb.loan.approver.dto.LoanApplicationRequest;
import org.deb.loan.approver.dto.LoanApplicationResponse;
import org.deb.loan.approver.enumeration.LoanApprovalStatus;
import org.deb.loan.approver.model.LoanApplication;
import org.deb.loan.approver.repository.LoanApplicationRepository;
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
public class LoanApplicationService {
  private final LoanApplicationRepository loanApplicationRepository;
  private final Clock clock;
  private final EncoderService encoderService;
  private final RestTemplate restTemplate;
  private static final String CREDIT_RATING_REQUEST_TEMPLATE = "{\"ssnNumber\":\"%s\"}";

  @Value("${creditScore.url:http://localhost:8080/api/v0/credit/score}")
  private String creditScoreURL;

  public LoanApplicationResponse process(final LoanApplicationRequest loanApplicationRequest) {
    LoanApplicationResponse loanApplicationResponse = null;

    boolean checkCreditRating = true;
    String message = "";
    String encodedSSN = encoderService.encode(loanApplicationRequest.getSsnNumber());
    Optional<List<LoanApplication>> existingApplications =
        loanApplicationRepository.findBySsnNumberOrderByApplicationDateTimeDesc(encodedSSN);
    if ((existingApplications.isPresent() && !existingApplications.get().isEmpty())
        && (!LocalDateTime.now(clock)
            .isAfter(existingApplications.get().get(0).getApplicationDateTime().plusDays(30)))) {
      message =
          String.format(
              "Last applied for loan on %s. There must be 30 days gap between two loan applications.",
              existingApplications.get().get(0).getApplicationDateTime());
      loanApplicationResponse = rejectLoanApplication(message);
      checkCreditRating = false;
    }

    if (checkCreditRating) {
      int creditRating = getCreditRating(loanApplicationRequest.getSsnNumber());
      log.info(String.format("Credit rating %d", creditRating));
      if (creditRating > 700) {
        loanApplicationResponse = processLoanApplication(loanApplicationRequest);
      } else {
        message =
            String.format(
                "Credit rating is %d. To approve required rating should be greater than 700.",
                creditRating);
        loanApplicationResponse = rejectLoanApplication(message);
      }
    }
    LoanApplication loanApplication =
        persistLoanApplication(loanApplicationRequest, checkCreditRating, encodedSSN, message);
    log.info("Loan application {}", loanApplication);
    loanApplicationResponse.setRequestId(loanApplication.getId());
    return loanApplicationResponse;
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

  private LoanApplication persistLoanApplication(
      LoanApplicationRequest loanApplicationRequest,
      boolean loanApproved,
      String encodedSSN,
      String message) {
    LoanApplication loanApplication = new LoanApplication();
    loanApplication.setId(UUID.randomUUID());
    loanApplication.setApplicationDateTime(LocalDateTime.now(clock));
    loanApplication.setCurrentAnnualIncome(loanApplicationRequest.getCurrentAnnualIncome());
    loanApplication.setSsnNumber(encodedSSN);
    loanApplication.setRequestedAmount(loanApplicationRequest.getLoanAmount());
    if (loanApproved) {
      loanApplication.setSanctionedAmount(loanApplicationRequest.getCurrentAnnualIncome() / 2);
      loanApplication.setLoanApprovalStatus(LoanApprovalStatus.APPROVED);
    } else {
      loanApplication.setSanctionedAmount(0.00);
      loanApplication.setLoanApprovalStatus(LoanApprovalStatus.REJECTED);
    }
    loanApplication.setMessage(message);
    return loanApplicationRepository.save(loanApplication);
  }

  private LoanApplicationResponse rejectLoanApplication(String errorMessage) {
    LoanApplicationResponse loanApplicationResponse = new LoanApplicationResponse();
    loanApplicationResponse.setLoanApprovalStatus(LoanApprovalStatus.REJECTED);
    loanApplicationResponse.setMessage(errorMessage);
    return loanApplicationResponse;
  }

  private LoanApplicationResponse processLoanApplication(
      LoanApplicationRequest loanApplicationRequest) {
    LoanApplicationResponse loanApplicationResponse = new LoanApplicationResponse();
    loanApplicationResponse.setLoanApprovalStatus(LoanApprovalStatus.APPROVED);
    loanApplicationResponse.setApprovalAmount(loanApplicationRequest.getCurrentAnnualIncome() / 2);
    loanApplicationResponse.setMessage("");
    return loanApplicationResponse;
  }
}
