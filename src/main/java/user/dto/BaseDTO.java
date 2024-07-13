package user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@SuperBuilder
public class BaseDTO {

    @JsonInclude
    private UUID id;
    @JsonInclude
    private LocalDateTime created;
    @JsonInclude
    private LocalDateTime updated;
}
