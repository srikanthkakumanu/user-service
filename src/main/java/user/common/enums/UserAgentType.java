package user.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UserAgentType {
    @JsonProperty("API")
    API,
    @JsonProperty("WEB")
    WEB,
    @JsonProperty("MOBILE")
    MOBILE;
}
