package user.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * To Log every request and response
 */
@Slf4j
@Order(2)
public class ServiceLogInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {

        //HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        if (ex != null) {
            ex.printStackTrace();
        }
        log.info("[afterCompletion][" + request + "][exception: " + ex + "]");
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        //HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
        log.info("[postHandle][{}]", response);
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        //return HandlerInterceptor.super.preHandle(request, response, handler);
        log.info("[preHandle][{}][{}]{}{}",
                request, request.getMethod(),
                request.getRequestURI(), getParameters(request));
        return true;
    }

    /**
     * Get Request Parameters
     * @param request HTTP request
     * @return Request parameters string object
     */
    private String getParameters(HttpServletRequest request) {
        StringBuilder posted = new StringBuilder();
        Enumeration<?> e = request.getParameterNames();

        if (Objects.nonNull(e)) {
            posted.append("?");
        }
        while (e.hasMoreElements()) {
            if (posted.length() > 1) {
                posted.append("&");
            }
            String curr = (String) e.nextElement();
            posted.append(curr).append("=");
            if (curr.contains("password")
                    || curr.contains("pass")
                    || curr.contains("pwd")) {
                posted.append("*****");
            } else {
                posted.append(request.getParameter(curr));
            }
        }
        String ip = request.getHeader("X-FORWARDED-FOR");
        String ipAddr = (ip == null) ? getRemoteAddress(request) : ip;
        if (Objects.nonNull(ipAddr) && !ipAddr.isEmpty()) {
            posted.append("&_psip=").append(ipAddr);
        }
        return posted.toString();
    }

    /**
     * Get the source IP address of a HTTP request
     * @param request HTTP request
     * @return remote IP address
     */
    private String getRemoteAddress(HttpServletRequest request) {
        String ipFromHeader = request.getHeader("X-FORWARDED-FOR");
        if (Objects.nonNull(ipFromHeader) && !ipFromHeader.isEmpty()) {
            log.debug("ip from proxy - X-FORWARDED-FOR : {}", ipFromHeader);
            return ipFromHeader;
        }
        return request.getRemoteAddr();
    }
}
