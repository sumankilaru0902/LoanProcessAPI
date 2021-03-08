package com.loan.approver.dto;

import java.util.UUID;

import com.loan.approver.enumeration.LoanApprovalStatus;

import lombok.Data;

@Data
public class LoanApplicationResponse {
  private UUID requestId;
  private LoanApprovalStatus loanApprovalStatus;
  private Double approvalAmount = 0.00;
  private String message;
}
