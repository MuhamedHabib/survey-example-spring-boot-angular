package com.github.joumenharzli.surveypoc.web.error;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.github.joumenharzli.surveypoc.exception.QuestionNotFoundException;
import com.github.joumenharzli.surveypoc.exception.UserNotFoundException;

/**
 * Translate exceptions to {@link RestErrorsDto}
 *
 * @author Joumen HARZLI
 */
@ControllerAdvice
public class RestExceptionTranslator {

  private final static Logger LOGGER = LoggerFactory.getLogger(RestExceptionTranslator.class);

  private final MessageSource messageSource;

  public RestExceptionTranslator(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  /**
   * Handle validation errors
   *
   * @return validation error with the fields errors and a bad request
   */
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  @ResponseBody
  public RestErrorsDto handleValidationExceptions(MethodArgumentNotValidException exception) {

    LOGGER.error("Translating method arguments not valid", exception);

    BindingResult result = exception.getBindingResult();
    List<FieldError> fieldErrors = result.getFieldErrors();

    RestFieldsErrorsDto restFieldsErrors = new RestFieldsErrorsDto();

    fieldErrors.forEach(fieldError ->
        restFieldsErrors.addError(new RestFieldErrorDto(fieldError.getField(), fieldError.getCode(),
            getLocalizedMessageFromFieldError(fieldError))));

    return new RestErrorsDto(
        new RestErrorDto(RestErrorConstants.ERR_VALIDATION_ERROR,
            "Validation Error", restFieldsErrors));

  }

  /**
   * Handle Question Not Found
   *
   * @return 404 status with message telling that the question not found
   */
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(value = QuestionNotFoundException.class)
  @ResponseBody
  public RestErrorsDto handleQuestionNotFound(QuestionNotFoundException exception) {
    LOGGER.error("Translating question not found", exception);

    String errorMessage = "The question with id %s was not found";

    RestErrorsDto restErrors = new RestErrorsDto();

    exception.getNotFoundQuestionsIds().forEach(id ->
        restErrors.addError(new RestErrorDto(RestErrorConstants.ERR_QUESTION_NOT_FOUND_ERROR,
            String.format(errorMessage, id)))
    );

    return restErrors;
  }

  /**
   * Handle User Not Found
   *
   * @return 404 status with message telling that the user not found
   */
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(value = UserNotFoundException.class)
  @ResponseBody
  public RestErrorsDto handleUserNotFound(UserNotFoundException exception) {
    LOGGER.error("Translating user not found", exception);

    String errorMessage = "The user with id %s was not found";

    RestErrorsDto restErrors = new RestErrorsDto();

    exception.getNotFoundUsersIds().forEach(id ->
        restErrors.addError(new RestErrorDto(RestErrorConstants.ERR_USER_NOT_FOUND_ERROR,
            String.format(errorMessage, id)))
    );

    return restErrors;
  }

  /**
   * Handle all types of errors
   *
   * @return internal server error
   */
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(value = Exception.class)
  @ResponseBody
  public RestErrorsDto handleAllExceptions(Exception exception) {

    LOGGER.error("Translating internal Server Error", exception);

    return new RestErrorsDto(
        new RestErrorDto(RestErrorConstants.ERR_INTERNAL_SERVER_ERROR,
            "Internal Server Error"));

  }

  /**
   * Get the correspondent localized message for a field error
   *
   * @param fieldError error that will be used for search
   * @return the localized message if found or the default one
   */
  private String getLocalizedMessageFromFieldError(FieldError fieldError) {
    return messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
  }
}
