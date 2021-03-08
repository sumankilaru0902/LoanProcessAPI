package com.loan.approver.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.loan.approver.service.CreditScoreEngine;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class CreditScoreEngineTest {

  @Autowired CreditScoreEngine creditRatingService;

  @Test
  void getCreditScore() {
    int creditRating = creditRatingService.getCreditScore();
    assertTrue(creditRating >= 300 && creditRating <= 850);
  }
}
