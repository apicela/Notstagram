package apicela.notstagram.exceptions;


import apicela.notstagram.models.responses.DefaultApiResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MissingRequestValueException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler(SaveException.class)
    public ResponseEntity<Object> handleSaveException(SaveException ex) {
        log.error("handleSaveException {}", ex);
        var response = new DefaultApiResponse<>(ex.getMessage(), 400);
        return (ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(UpdateException.class)
    public ResponseEntity<Object> handleUpdateException(UpdateException ex) {
        log.error("handleUpdateException {}", ex);
        var response = new DefaultApiResponse<>(ex.getMessage(), 400);
        return (ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<Object> handleUpdateException(EmailAlreadyInUseException ex) {
        log.error("handleEmailAlreadyInUseException {}", ex);
        var response = new DefaultApiResponse<>(ex.getMessage(), 409);
        return (ResponseEntity.status(HttpStatus.CONFLICT).body(response));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        log.error("handleNotFound {}", ex);
        var response = new DefaultApiResponse<>(ex.getMessage(), 404);
        return (ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }


    @ExceptionHandler(MissingRequestValueException.class)
    public ResponseEntity<Object> handleMissingRequestValueException(MissingRequestValueException ex) {
        log.error("handleMissingRequestValueException {}", ex);
        var response = new DefaultApiResponse<>(ex.getMessage(), 400);
        return (ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        log.error("handleException {}", ex);
        var response = new DefaultApiResponse<>(ex.getMessage(), 401);
        return (ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception ex) {
        log.error("handleException {}", ex);
        var response = new DefaultApiResponse<>(ex.getMessage(), 500);
        return (ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}