package com.loan.approver.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.loan.approver.enumeration.LoanApprovalStatus;

class LoanApprovalStatusTest {

  @Test
  void testStatus() {
    assertSame(LoanApprovalStatus.APPROVED, LoanApprovalStatus.valueOf("APPROVED"));
    assertSame(LoanApprovalStatus.REJECTED, LoanApprovalStatus.valueOf("REJECTED"));
  }
}
