package user.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

//@Data
//@AllArgsConstructor
//@Builder
public record ExceptionInfo(String guid,
                            String entityName,
                            Integer code,
                            HttpStatus status,
                            String message,
                            ZonedDateTime timestamp,
                            String path) {}
