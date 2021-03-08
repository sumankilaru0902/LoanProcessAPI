package org.deb.loan.approver.controller;

import java.util.Collections;
import javax.validation.ConstraintViolationException;
import lombok.val;
import org.deb.loan.approver.dto.BaseError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.json.JsonParseException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;

class ExceptionControllerTest {

  @Test
  void constraintViolation_returnBadRequest() {
    val exceptionController = new ExceptionController();
    val response =
        exceptionController.constraintViolationExceptionHandler(
            new ConstraintViolationException(Collections.emptySet()));
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void defaultException_returnInternalServerError() {
    val exceptionController = new ExceptionController();
    val response = exceptionController.defaultExceptionHandler(new NumberFormatException());
    Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void handleMethodArgumentNotValidException_returnBadRequestError() throws NoSuchMethodException {
    BeanPropertyBindingResult errors =
        new BeanPropertyBindingResult(
            BaseError.builder()
                .description("test description")
                .details("test details")
                .reasonCode("400")
                .recoverable(false)
                .build(),
            "testBean");
    errors.rejectValue("source", "invalid", "must not be null");
    MethodParameter parameter = new MethodParameter(this.getClass().getMethod("toString"), -1);
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, errors);
    val exceptionController = new ExceptionController();
    val response = exceptionController.handleMethodArgumentNotValidException(ex);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Assertions.assertEquals(
        "source - must not be null",
        response.getBody().getErrors().getErrorList().get(0).getDetails());
  }

  @Test
  void handleMethodArgumentNotValidException_returnBadRequestErrorWithEmptyFieldName()
      throws NoSuchMethodException {
    BeanPropertyBindingResult errors =
        new BeanPropertyBindingResult(
            BaseError.builder()
                .description("test description")
                .details("test details")
                .reasonCode("400")
                .recoverable(false)
                .build(),
            "testBean");
    errors.rejectValue(null, "invalid", "must not be null");
    MethodParameter parameter = new MethodParameter(this.getClass().getMethod("toString"), -1);
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, errors);
    val exceptionController = new ExceptionController();
    val response = exceptionController.handleMethodArgumentNotValidException(ex);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Assertions.assertEquals(
        " - must not be null", response.getBody().getErrors().getErrorList().get(0).getDetails());
  }

  @Test
  void handleMethodArgumentNotValidException_multipleFieldErrors_success()
      throws NoSuchMethodException {
    BeanPropertyBindingResult errors =
        new BeanPropertyBindingResult(
            BaseError.builder()
                .description("test description")
                .details("test details")
                .reasonCode("400")
                .recoverable(false)
                .build(),
            "testBean");
    errors.addError(new ObjectError("Error1", "Error 1"));
    errors.addError(new ObjectError("Error2", "Error 2"));
    MethodParameter parameter = new MethodParameter(this.getClass().getMethod("toString"), -1);
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, errors);
    val exceptionController = new ExceptionController();
    val response = exceptionController.handleMethodArgumentNotValidException(ex);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void handleMethodArgumentNotValidException_returnBadRequestErrorWithEmptyErrorCodes()
      throws NoSuchMethodException {
    BeanPropertyBindingResult errors =
        new BeanPropertyBindingResult(
            BaseError.builder()
                .description("test description")
                .details("test details")
                .reasonCode("400")
                .recoverable(false)
                .build(),
            "testBean");
    errors.rejectValue(null, "", "must not be null");
    MethodParameter parameter = new MethodParameter(this.getClass().getMethod("toString"), -1);
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, errors);
    val exceptionController = new ExceptionController();
    val response = exceptionController.handleMethodArgumentNotValidException(ex);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Assertions.assertEquals(
        " - must not be null", response.getBody().getErrors().getErrorList().get(0).getDetails());
  }

  @Test
  void handleMethodArgumentNotValidException_returnBadRequestErrorWithoutErrorCodes()
      throws NoSuchMethodException {
    BeanPropertyBindingResult errors =
        new BeanPropertyBindingResult(
            BaseError.builder()
                .description("test description")
                .details("test details")
                .reasonCode("400")
                .recoverable(false)
                .build(),
            "testBean");
    MethodParameter parameter = new MethodParameter(this.getClass().getMethod("toString"), -1);
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, errors);
    val exceptionController = new ExceptionController();
    val response = exceptionController.handleMethodArgumentNotValidException(ex);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Assertions.assertEquals(0, response.getBody().getErrors().getErrorList().size());
  }

  @Test
  void handleMethodArgumentNotValidException_returnBadRequestErrorWithEmptyDefaultErrorMessage()
      throws NoSuchMethodException {
    BeanPropertyBindingResult errors =
        new BeanPropertyBindingResult(
            BaseError.builder()
                .description("test description")
                .details("test details")
                .reasonCode("400")
                .recoverable(false)
                .build(),
            "testBean");
    errors.rejectValue("source", "invalid", null);
    MethodParameter parameter = new MethodParameter(this.getClass().getMethod("toString"), -1);
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, errors);
    val exceptionController = new ExceptionController();
    val response = exceptionController.handleMethodArgumentNotValidException(ex);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Assertions.assertEquals(
        "source - null", response.getBody().getErrors().getErrorList().get(0).getDetails());
  }

  @Test
  void handleBadRequestHttpMessageNotReadableException() {
    val httpMessageNotReadableException =
        new HttpMessageNotReadableException("Could not read JSON: ", new JsonParseException());
    val exceptionController = new ExceptionController();
    val response =
        exceptionController.handleMessageNotReadableException(httpMessageNotReadableException);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Assertions.assertEquals(
        "Could not read JSON: ; nested exception is org.springframework.boot.json.JsonParseException: Cannot parse JSON",
        response.getBody().getErrors().getErrorList().get(0).getDetails());
  }

  @Test
  void handleBadRequestServletRequestBindingException() {
    val servletRequestBindingException =
        new ServletRequestBindingException("Missing Path Parameter");
    val exceptionController = new ExceptionController();
    val response =
        exceptionController.handleServletRequestBindingException(servletRequestBindingException);
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Assertions.assertEquals(
        "Missing Path Parameter",
        response.getBody().getErrors().getErrorList().get(0).getDetails());
  }
}
