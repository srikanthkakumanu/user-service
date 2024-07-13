package user.common.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserStatus {
    @JsonProperty("NEW_USER")
    NEW_USER,
    @JsonProperty("ACTIVE_USER")
    ACTIVE_USER,
    @JsonProperty("IN_ACTIVE_USER")
    IN_ACTIVE_USER;
}
