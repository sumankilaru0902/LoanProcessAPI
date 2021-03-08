package com.loan.approver.enumeration;

public enum LoanApprovalStatus {
  APPROVED("APPROVED"),
  REJECTED("REJECTED");

  private final String status;

  private LoanApprovalStatus(String status) {
    this.status = status;
  }
}
