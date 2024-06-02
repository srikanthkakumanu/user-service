package user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BaseDTO {
    private UUID id;
    private LocalDateTime created;
    private LocalDateTime updated;
}
