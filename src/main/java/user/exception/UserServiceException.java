package user.exception;


import lombok.*;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class UserServiceException extends RuntimeException {
    private final String entityName;
    private final HttpStatus status;
    private final String message;
}
