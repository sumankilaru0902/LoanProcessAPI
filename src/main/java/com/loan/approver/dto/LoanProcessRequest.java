package com.loan.approver.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;

@Data
public class LoanProcessRequest {
  @NotNull(message = "Please provide 'ssnNumber'")
  private String ssnNumber;

  @NotNull(message = "Please provide 'loanAmount''")
  private Double loanAmount;

  @NotNull(message = "Please provide 'currentAnnualIncome'")
  private Double currentAnnualIncome;
}
