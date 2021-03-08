package org.deb.loan.approver.model;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.deb.loan.approver.enumeration.LoanApprovalStatus;

@Entity
@Data
public class LoanApplication {
  @Id private UUID id;

  private LocalDateTime applicationDateTime;

  @NotNull(message = "Loan approval status cannot be null")
  private LoanApprovalStatus loanApprovalStatus;

  @ToString.Exclude
  @NotNull(message = "ssnNumber cannot be null")
  private String ssnNumber;

  @NotNull(message = "requested amount cannot be null")
  private Double requestedAmount;

  @NotNull(message = "sanctioned amount cannot be null")
  private Double sanctionedAmount;

  @NotNull(message = "current annual income cannot be null")
  private double currentAnnualIncome;

  private String message;
}
