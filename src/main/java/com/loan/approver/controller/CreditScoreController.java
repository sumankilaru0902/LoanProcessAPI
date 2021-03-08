package com.loan.approver.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan.approver.dto.CreditRatingRequest;
import com.loan.approver.service.CreditScoreEngine;

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
