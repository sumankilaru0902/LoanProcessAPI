package org.deb.loan.approver.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deb.loan.approver.dto.LoanApplicationRequest;
import org.deb.loan.approver.dto.LoanApplicationResponse;
import org.deb.loan.approver.service.LoanApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v0/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationController {

  private final LoanApplicationService loanApplicationService;

  @PostMapping("/apply")
  public ResponseEntity<LoanApplicationResponse> process(
      @RequestBody @Valid final LoanApplicationRequest loanApplicationRequest) {

    return ResponseEntity.ok(loanApplicationService.process(loanApplicationRequest));
  }
}
