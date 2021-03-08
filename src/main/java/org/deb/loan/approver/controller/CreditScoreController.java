package org.deb.loan.approver.controller;

import lombok.RequiredArgsConstructor;
import org.deb.loan.approver.dto.CreditRatingRequest;
import org.deb.loan.approver.service.CreditScoreEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v0/credit")
public class CreditScoreController {
  private final CreditScoreEngine creditScoreEngine;

  @GetMapping(value = "/score", produces = "application/json")
  public ResponseEntity<String> getCreditRating(final CreditRatingRequest creditRatingRequest) {
    return ResponseEntity.ok(
        String.format("{\"creditRating\":%d}", creditScoreEngine.getCreditScore()));
  }
}
