package org.deb.loan.approver.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.deb.loan.approver.dto.CreditRatingRequest;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class CreditScoreControllerTest {
  @Autowired private MockMvc mockMvc;

  @Test
  void getCreditRating() throws Exception {
    CreditRatingRequest createRatingRequest = new CreditRatingRequest("598-07-7771");
    String requestJson = new ObjectMapper().writeValueAsString(createRatingRequest);
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/v0/credit/score")
                .contentType("application/json")
                .content(requestJson))
        .andDo(print())
        .andExpect(jsonPath("$.creditRating", greaterThan(299)))
        .andExpect(jsonPath("$.creditRating", lessThan(851)))
        .andExpect(status().is2xxSuccessful())
        .andReturn()
        .getResponse();
  }
}
