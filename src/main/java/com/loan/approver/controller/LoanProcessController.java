package com.loan.approver.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan.approver.dto.LoanApplicationRequest;
import com.loan.approver.dto.LoanApplicationResponse;
import com.loan.approver.service.LoanApplicationService;

@RestController
@RequestMapping("api/v0/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanProcessController {

  private final LoanApplicationService loanApplicationService;

  @PostMapping("/apply")
  public ResponseEntity<LoanApplicationResponse> process(
      @RequestBody @Valid final LoanApplicationRequest loanApplicationRequest) {

    return ResponseEntity.ok(loanApplicationService.process(loanApplicationRequest));
  }
}
