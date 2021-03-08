package org.deb.loan.approver.dto;

import java.util.UUID;
import lombok.Data;
import org.deb.loan.approver.enumeration.LoanApprovalStatus;

@Data
public class LoanApplicationResponse {
  private UUID requestId;
  private LoanApprovalStatus loanApprovalStatus;
  private Double approvalAmount = 0.00;
  private String message;
}
