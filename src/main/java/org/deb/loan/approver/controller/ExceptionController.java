package org.deb.loan.approver.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.deb.loan.approver.dto.BaseError;
import org.deb.loan.approver.dto.Error;
import org.deb.loan.approver.dto.ErrorResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Configuration
@ControllerAdvice
public class ExceptionController {

  private static final String APP_NAME = "LoanApprover";

  /**
   * Handle constraint violation in inputs.
   *
   * @param ex exception raised when an input causes constraint violation.
   * @return error response.
   */
  @ResponseBody
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> constraintViolationExceptionHandler(
      final ConstraintViolationException ex) {
    log.error("constraintViolationExceptionHandler exception : {}", ex.getMessage(), ex);
    List<BaseError> errors = new ArrayList<>();
    errors.add(
        BaseError.builder()
            .source(APP_NAME)
            .reasonCode(HttpStatus.BAD_REQUEST.toString())
            .description(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .recoverable(false)
            .build());
    return getErrorResponseEntity(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle unknown exception to prevent leakage of stack.
   *
   * @param ex exception that should be handled.
   * @return default error response.
   */
  @ExceptionHandler({Throwable.class})
  @ResponseBody
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> defaultExceptionHandler(final Throwable ex) {
    log.error("defaultExceptionHandler exception: {}", ex.getMessage(), ex);
    List<BaseError> errors = new ArrayList<>();
    errors.add(
        BaseError.builder()
            .source(APP_NAME)
            .reasonCode(HttpStatus.INTERNAL_SERVER_ERROR.toString())
            .description(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .recoverable(false)
            .build());
    return getErrorResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handle validation exceptions.
   *
   * @param ex the validation exception.
   * @return error response containing all the validation failures.
   */
  @ExceptionHandler({MethodArgumentNotValidException.class})
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      final MethodArgumentNotValidException ex) {
    log.error("handleMethodArgumentNotValidException exception: {}", ex.getMessage(), ex);
    List<BaseError> errors = new ArrayList<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            err ->
                errors.add(
                    BaseError.builder()
                        .source(APP_NAME)
                        .reasonCode(HttpStatus.BAD_REQUEST.toString())
                        .description(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .recoverable(false)
                        .details(
                            ((Objects.nonNull(err.getCodes())
                                        && err.getCodes().length > 0
                                        && err.getCodes()[0].contains(".")
                                        && err.getCodes()[0].split("\\.").length > 2)
                                    ? err.getCodes()[0].split("\\.")[2]
                                    : "")
                                + " - "
                                + err.getDefaultMessage())
                        .build()));
    return getErrorResponseEntity(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle improper inputs.
   *
   * @param ex the exception thrown due to the request.
   * @return error response.
   */
  @ExceptionHandler({HttpMessageNotReadableException.class})
  public ResponseEntity<ErrorResponse> handleMessageNotReadableException(
      final HttpMessageNotReadableException ex) {
    log.error("handleMessageNotReadableException exception: {}", ex.getMessage(), ex);
    List<BaseError> errors = new ArrayList<>();
    errors.add(
        BaseError.builder()
            .source(APP_NAME)
            .reasonCode(HttpStatus.BAD_REQUEST.toString())
            .description(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .recoverable(false)
            .details(ex.getMessage())
            .build());
    return getErrorResponseEntity(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle improper inputs.
   *
   * @param ex the exception thrown due to the request.
   * @return error response.
   */
  @ExceptionHandler({ServletRequestBindingException.class})
  public ResponseEntity<ErrorResponse> handleServletRequestBindingException(
      final ServletRequestBindingException ex) {
    log.error("handleServletRequestBindingException exception: {}", ex.getMessage(), ex);
    List<BaseError> errors = new ArrayList<>();
    errors.add(
        BaseError.builder()
            .source(APP_NAME)
            .reasonCode(HttpStatus.BAD_REQUEST.toString())
            .description(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .recoverable(false)
            .details(ex.getMessage())
            .build());
    return getErrorResponseEntity(errors, HttpStatus.BAD_REQUEST);
  }

  private ResponseEntity<ErrorResponse> getErrorResponseEntity(
      List<BaseError> errors, HttpStatus httpStatus) {
    return new ResponseEntity<>(
        ErrorResponse.builder().errors(Error.builder().errorList(errors).build()).build(),
        httpStatus);
  }
}
