package user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
public class UserSecurityConfig {


//    private JWTAuthEntryPoint authEntryPoint;

//    public UserSecurityConfig(JWTAuthEntryPoint authEntryPoint) {
//        this.authEntryPoint = authEntryPoint;
//    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/*", "/swagger-ui/*")
                        // To restrict the API calls coming only from API Gateway IP Address
//                        .access(new WebExpressionAuthorizationManager("hasIpAddress('10.0.0.12')"))
                        .permitAll()
                )
                .sessionManagement(smc -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h.frameOptions(
                        HeadersConfigurer.FrameOptionsConfig::sameOrigin))
//                .requiresChannel(rcc -> rcc.anyRequest().requiresSecure()) //Allow HTTPS only; // REST APIs are stateless
                .formLogin(AbstractHttpConfigurer::disable)
                .securityContext(cc -> cc.requireExplicitSave(false)) // disables the default behavior of saving the security context in the session after each request
//                .exceptionHandling(c -> c.authenticationEntryPoint(authEntryPoint))
        ;
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();

    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

//    @Bean
//    public PasswordEncoder argon2PasswordEncoder() {
//        return new Argon2PasswordEncoder(16,
//                32,
//                1,
//                60000,
//                10);
//    }

}

//@Bean
//SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//    http
//            .requiresChannel(rcc -> rcc.anyRequest().requiresSecure()) //Allow HTTPS only
//            .formLogin(AbstractHttpConfigurer::disable)
//            .sessionManagement(smc -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // REST APIs are stateless
//            .securityContext(cc -> cc.requireExplicitSave(false))
//            .exceptionHandling(c -> c.authenticationEntryPoint(authEntryPoint))
//
//            .cors(AbstractHttpConfigurer::disable)
////                .cors ( config -> config.configurationSource(new CorsConfigurationSource() {
////                    @Override
////                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
////                        CorsConfiguration cfg = new CorsConfiguration();
////                        cfg.setAllowedOrigins(Collections.singletonList("http://localhost:4200")); // URL of UI app
////                        cfg.setAllowedMethods(Collections.singletonList("*"));
////                        cfg.setAllowCredentials(true);
////                        cfg.setAllowedHeaders(Collections.singletonList("*"));
////                        cfg.setMaxAge(3600L);
////                        return cfg;
////                    }
////                }))
//            .csrf( csrf -> csrf.disable()) // CSRF is disabled for REST APIs
//            .csrf(AbstractHttpConfigurer::disable)
////                .csrf(config ->
////                        config.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
////                                .ignoringRequestMatchers( "/contact","/register") // Ignoring specific paths for CSRF attacks
////                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
////                    .addFilterAfter(new CSRFCookieFilter(), BasicAuthenticationFilter.class)
//
//            .authorizeHttpRequests(auth -> auth.requestMatchers(
//                    "/",
//                    "/webjars/**",
//                    "/api-docs/**",
//                    "/swagger-resources/**",
//                    "configuration/ui/**",
//                    "api/users/",
//                    ).permitAll()
//            ) // "/api/users/**"
//            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
//
//    http.httpBasic(Customizer.withDefaults());
//    return http.build();
//}
