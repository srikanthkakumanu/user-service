package user.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import user.exception.ExceptionInfo;

import java.util.*;

@Slf4j
@Order(1)
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Getter
    private static List<String> apiKeys;

    @Value("#{'${apiKey}'.split(',')}")
    public void setApiKeys(List<String> apiKeys) {
        AuthInterceptor.apiKeys = apiKeys;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String apiKey = request.getHeader("apiKey");
        log.debug("apiKey: {}", apiKey);
        String message = "apiKey is empty/Null or Invalid.";

        if (Objects.isNull(apiKey) || !getApiKeys().contains(apiKey)) {

            log.error(message);

            ObjectMapper mapper = new ObjectMapper();
            ExceptionInfo exInfo =
                    new ExceptionInfo(UUID.randomUUID().toString(),
                            "apiKey",
                            HttpStatus.FORBIDDEN.value(),
                            HttpStatus.FORBIDDEN,
                            message,
                            null,
                            request.getRequestURI());

            Map<String, List<Object>> body = new HashMap<>();
            List<Object> errors = new ArrayList<>();
            errors.add(exInfo);
            body.put("errors", errors);

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            String json = mapper.writeValueAsString(body);
            response.getWriter().write(mapper.writeValueAsString(body));
            return false;
        }
        return true;
    }
}
