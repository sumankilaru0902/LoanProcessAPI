package org.deb.loan.approver.enumeration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LoanApprovalStatusTest {

  @Test
  void testStatus() {
    assertSame(LoanApprovalStatus.APPROVED, LoanApprovalStatus.valueOf("APPROVED"));
    assertSame(LoanApprovalStatus.REJECTED, LoanApprovalStatus.valueOf("REJECTED"));
  }
}
