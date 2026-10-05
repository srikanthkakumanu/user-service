package com.users.infrastructure.keycloak;

import java.util.Map;

import com.users.domain.exception.IdentityProviderUnavailableException;
import com.users.domain.port.TokenStatusPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Token introspection at Keycloak, authenticated as this service's own client. */
@Component
class KeycloakTokenStatusAdapter implements TokenStatusPort {

	private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {
	};

	private final RestClient http;
	private final KeycloakProperties properties;

	KeycloakTokenStatusAdapter(RestClient.Builder builder, KeycloakProperties properties) {
		this.http = builder.baseUrl(properties.serverUrl() + "/realms/" + properties.realm() + "/protocol/openid-connect").build();
		this.properties = properties;
	}

	@Override
	public boolean isActive(String token) {
		var form = new LinkedMultiValueMap<String, String>();
		form.add("client_id", properties.clientId());
		form.add("client_secret", properties.clientSecret());
		form.add("token", token);
		try {
			Map<String, Object> body = http.post().uri("/token/introspect")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(JSON);
			return body != null && Boolean.TRUE.equals(body.get("active"));
		}
		catch (RestClientException ex) {
			// Failing closed: without an answer the sensitive operation does not go ahead.
			throw new IdentityProviderUnavailableException("Identity provider failed to introspect token", ex);
		}
	}
}
