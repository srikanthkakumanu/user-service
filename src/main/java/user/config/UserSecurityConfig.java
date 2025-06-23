package user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import user.config.filters.JWTAuthenticationFilter;


@Configuration
public class UserSecurityConfig {


    private final JWTAuthEntryPoint authEntryPoint;

    public UserSecurityConfig(JWTAuthEntryPoint authEntryPoint) {
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ehc ->
                        ehc.authenticationEntryPoint(authEntryPoint))
                .authorizeHttpRequests(auth -> auth
                                // Swagger UI endpoints
                                .requestMatchers("/swagger-ui/**",
                                        "v3/api-docs/**",
                                        "/swagger-resources/**",
                                        "/webjars/**",
                                        "/configuration/ui/**")
                                .permitAll()
                                // Health check endpoint
                                .requestMatchers(HttpMethod.GET, "api/users/ping")
                                .permitAll()
                                // User registration/signup endpoint
                                .requestMatchers(HttpMethod.POST, "/api/users/signup")
                                .permitAll()
                                // User login/signin endpoint
                                .requestMatchers(HttpMethod.POST, "/api/users/signin")
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                        // To restrict the API calls coming only from API Gateway IP Address
//                                .access(new WebExpressionAuthorizationManager("hasIpAddress('10.0.0.12')"))
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // Or DISABLE
                .formLogin(AbstractHttpConfigurer::disable)
                // disables the default behavior of saving the security context in the session after each request
                .securityContext(scc ->
                        scc.requireExplicitSave(false))
                .addFilterBefore(jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
                //Allow HTTPS only;
//                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure())
//                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Add frontend domains to allow
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public  JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }

// Enable the below only if you want to handle CORS
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Allow specific origins (replace with your frontend URLs)
//        configuration.setAllowedOrigins(Arrays.asList(
//                "http://localhost:3000",  // React dev server
//                "http://localhost:4200",  // Angular dev server
//                "https://your-frontend-domain.com"
//        ));
//
//        // Allow specific HTTP methods
//        configuration.setAllowedMethods(Arrays.asList(
//                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
//        ));
//
//        // Allow specific headers
//        configuration.setAllowedHeaders(Arrays.asList(
//                "Authorization", "Content-Type", "X-Requested-With",
//                "Accept", "Origin", "Access-Control-Request-Method",
//                "Access-Control-Request-Headers"
//        ));
//
//        // Allow credentials (cookies, authorization headers)
//        configuration.setAllowCredentials(true);
//
//        // Cache preflight response for 1 hour
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

//    @Bean
//    public PasswordEncoder argon2PasswordEncoder() {
//        return new Argon2PasswordEncoder(16,
//                32,
//                1,
//                60000,
//                10);
//    }

}

