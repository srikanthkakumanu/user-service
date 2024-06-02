package user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public sealed class BaseEntity permits User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(value = Types.VARBINARY)
    @Column(columnDefinition = "VARBINARY(16)", updatable = false, nullable = false)
    private UUID id;
    private final LocalDateTime created = LocalDateTime.now();
    private LocalDateTime updated;

}
