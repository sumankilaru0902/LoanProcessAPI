package org.deb.loan.approver.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreditRatingRequest {
  @NotBlank(message = "Please provide 'ssnNumber'")
  @Pattern(regexp = "^[0-9]{3}-[0-9]{2}-[0-9]{4}$", message = "Please provide proper SSN")
  private String ssnNumber;
}
