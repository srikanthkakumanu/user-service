package user.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import user.exception.ExceptionInfo;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static user.util.CommonUtil.buildExceptionInfo;

@Component
public class JWTAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {


        Map<String, List<Object>> body = new HashMap<>();
        List<Object> errors = new ArrayList<>();

        ExceptionInfo info = buildExceptionInfo("user",
                HttpServletResponse.SC_UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED,
                authException.getMessage(),
                ZonedDateTime.now(),
                ((ServletWebRequest) request).getRequest().getRequestURI());

        errors.add(info);
        body.put("errors", errors);

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, body.toString());
    }
}
