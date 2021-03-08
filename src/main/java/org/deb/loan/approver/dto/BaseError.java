package org.deb.loan.approver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseError {
  @JsonProperty("Source")
  private String source;

  @JsonProperty("ReasonCode")
  private String reasonCode;

  @JsonProperty("Description")
  private String description;

  @JsonProperty("Recoverable")
  private Boolean recoverable;

  @JsonProperty("Details")
  private String details;
}
