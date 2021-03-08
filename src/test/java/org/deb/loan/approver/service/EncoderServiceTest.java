package org.deb.loan.approver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class EncoderServiceTest {

  @Autowired private EncoderService encoderService;

  @Test
  void encode() {
    String realValue = "198-50-0013";
    assertNotSame(realValue, encoderService.encode(realValue));
  }

  @Test
  void decode() {
    String realValue = "198-50-0013";
    String encodedValue = encoderService.encode(realValue);
    assertNotSame(realValue, encodedValue);
    assertEquals(realValue, encoderService.decode(encodedValue));
  }
}
