package user.exception;

import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.*;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import user.util.CommonUtil;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, List<Object>> body = new HashMap<>();
        List<Object> errors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            ExceptionInfo exInfo =
                    CommonUtil.buildExceptionInfo(((FieldError) error).getField(),
                            HttpStatus.BAD_REQUEST.value(),
                            HttpStatus.BAD_REQUEST,
                            error.getDefaultMessage(),
                            ZonedDateTime.now(), ((ServletWebRequest) request).getRequest().getRequestURI());
            errors.add(exInfo);
            log.error(exInfo.toString());
        });
        body.put("errors", errors);

        return new ResponseEntity<>(body, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        Map<String, List<Object>> body = new HashMap<>();
        List<Object> errors = new ArrayList<>();

        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ifx = (InvalidFormatException) ex.getCause();
            if (ifx.getTargetType() != null && ifx.getTargetType().isEnum()) {
                String fieldName = ifx.getPath().get(ifx.getPath().size() - 1).getFieldName();
                String errorMessage = String.format("%s is invalid.", ifx.getValue());
                ZonedDateTime timestamp = ZonedDateTime.now();

                ExceptionInfo exInfo =
                        CommonUtil.buildExceptionInfo(fieldName,
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST, errorMessage,
                                timestamp, ((ServletWebRequest) request).getRequest().getRequestURI());
                errors.add(exInfo);
                log.error(exInfo.toString());
            }
        }
        body.put("errors", errors);

        return new ResponseEntity<>(body, status);

    }

    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<?> constraintViolationException(ConstraintViolationException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();

        ex.getConstraintViolations().forEach(cv -> errors.add(cv.getMessage()));

        Map<String, List<String>> result = new HashMap<>();

        result.put("errors", errors);

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<Object> handleUserServiceException(UserServiceException ex, WebRequest request) {
        log.error(ex.toString());
        return CommonUtil.buildUserErrorResponse(ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknownException(Exception ex, WebRequest request) {
        log.error(ex.toString());
        return CommonUtil.buildUserErrorResponse(ex, request);
    }

}
