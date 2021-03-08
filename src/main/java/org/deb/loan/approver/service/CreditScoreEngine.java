package org.deb.loan.approver.service;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class CreditScoreEngine {

  public int getCreditScore() {
    return ThreadLocalRandom.current().nextInt(300, 850);
  }
}
