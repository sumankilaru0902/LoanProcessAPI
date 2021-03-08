package com.loan.approver.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan.approver.dto.LoanProcessRequest;
import com.loan.approver.dto.LoanProcessResponse;
import com.loan.approver.service.LoanProcessService;

@RestController
@RequestMapping("api/v0/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanProcessController {

  private final LoanProcessService loanProcessService;

  @PostMapping("/apply")
  public ResponseEntity<LoanProcessResponse> process(
      @RequestBody @Valid final LoanProcessRequest loanProcessRequest) {
    return ResponseEntity.ok(loanProcessService.process(loanProcessRequest));
  }
}
