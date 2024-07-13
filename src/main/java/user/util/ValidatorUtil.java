package user.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ValidatorUtil {

    @Getter
    private static List<String> apiKeys;

    public static ResponseEntity<?> validateApiKey(String apiKey) {

        final String BAD_API_KEY = "{\"status\":\"Authorization Failed\",\"message\":\"Invalid API Key\"}";

        if (apiKey == null || !apiKeys.contains(apiKey)) {
            // return invalid key
            log.error("Invalid API Key");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(BAD_API_KEY);
        }
        return null;
    }

    @Value("#{'${apiKey}'.split(',')}")
    public void setApiKeys(List<String> apiKeys) {
        ValidatorUtil.apiKeys = apiKeys;
    }

}
