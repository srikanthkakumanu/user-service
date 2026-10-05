package com.users.interfaces.security;

import com.users.application.EnsureTokenActive;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Changes to another user's account, credentials and required actions additionally check the
 * caller's token with the identity provider, so a token from an ended session cannot be used for
 * them during the minutes it would still pass a signature check.
 */
@Configuration(proxyBeanMethods = false)
class SensitiveOperationInterceptor implements HandlerInterceptor, WebMvcConfigurer {

	private static final String[] SENSITIVE = { "/api/v1/users", "/api/v1/users/*", "/api/v1/users/*/enable",
			"/api/v1/users/*/disable", "/api/v1/users/*/lock", "/api/v1/users/*/unlock",
			"/api/v1/users/*/credentials/**", "/api/v1/users/*/actions/**" };

	private static final String[] NOT_SENSITIVE = { "/api/v1/users/register", "/api/v1/users/password-reset-requests",
			"/api/v1/users/me" };

	private final ObjectProvider<EnsureTokenActive> ensureTokenActive;

	SensitiveOperationInterceptor(ObjectProvider<EnsureTokenActive> ensureTokenActive) {
		this.ensureTokenActive = ensureTokenActive;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this).addPathPatterns(SENSITIVE).excludePathPatterns(NOT_SENSITIVE);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		boolean changesState = !request.getMethod().equals("GET") && !request.getMethod().equals("HEAD")
				&& !request.getMethod().equals("OPTIONS");
		if (changesState
				&& SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token) {
			ensureTokenActive.ifAvailable(check -> check.handle(token.getToken().getTokenValue()));
		}
		return true;
	}
}
