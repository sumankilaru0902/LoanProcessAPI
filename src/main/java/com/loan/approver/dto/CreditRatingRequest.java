package com.loan.approver.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreditRatingRequest {
  @NotBlank(message = "Please provide 'ssnNumber'")
  private String ssnNumber;
}
