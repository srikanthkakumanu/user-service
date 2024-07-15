package user.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import user.exception.ExceptionInfo;
import user.exception.UserServiceException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

public class CommonUtil {

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }

    public static Timestamp toSQLTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime.toString());
    }

    public static ResponseEntity<Object> buildUserErrorResponse(Exception re, WebRequest request) {

        Map<String, List<Object>> body = new HashMap<>();
        List<Object> errors = new ArrayList<>();

        ExceptionInfo info = switch (re) {
            case UserServiceException use ->
                buildExceptionInfo(use.getEntityName(), use.getStatus().value(),
                        use.getStatus(), use.getMessage(), ZonedDateTime.now(),
                        ((ServletWebRequest) request).getRequest().getRequestURI());
            default ->
                    buildExceptionInfo("user", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected User Service Error",
                            ZonedDateTime.now(), ((ServletWebRequest) request).getRequest().getRequestURI());
        };
        errors.add(info);
        body.put("errors", errors);
        return new ResponseEntity<>(body, info.status());
    }

    public static ExceptionInfo buildExceptionInfo(String entityName,
                                                   int code,
                                                   HttpStatus status,
                                                   String message,
                                                   ZonedDateTime timestamp, String path) {
        return new ExceptionInfo(UUID.randomUUID().toString(), entityName, code, status, message, ZonedDateTime.now(), path);
    }
}
