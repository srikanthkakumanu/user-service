package user.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Advanced Password Encoder Configuration with multiple secure algorithms
 * and production-ready settings for maximum security
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Production-grade password encoder with Argon2 as default
     * Provides backward compatibility with existing passwords
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        // Create custom delegating encoder with Argon2 as default
        Map<String, PasswordEncoder> encoders = new HashMap<>();

        // Argon2 - Most secure, recommended for new passwords
        encoders.put("argon2", argon2PasswordEncoder());

        // BCrypt - Strong fallback with tuned cost
        encoders.put("bcrypt", bCryptPasswordEncoder());

        // SCrypt - Alternative strong option
        encoders.put("scrypt", sCryptPasswordEncoder());

        // Legacy support (for migration only)
        encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());

        // Set Argon2 as default for new passwords
        DelegatingPasswordEncoder delegatingEncoder =
                new DelegatingPasswordEncoder("argon2", encoders);

        // Enable upgrading from weaker algorithms
        delegatingEncoder.setDefaultPasswordEncoderForMatches(argon2PasswordEncoder());

        return delegatingEncoder;
    }

    /**
     * Argon2 Password Encoder - Winner of password hashing competition
     * Resistant to GPU/ASIC attacks, memory-hard function
     */
    @Bean
    public Argon2PasswordEncoder argon2PasswordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    /**
     * Custom Argon2 with production-tuned parameters
     * Use this for high-security applications
     */
    @Bean
    public Argon2PasswordEncoder customArgon2PasswordEncoder() {
        return new Argon2PasswordEncoder(
                16,    // Salt length
                32,    // Hash length
                1,     // Parallelism (threads)
                65536, // Memory cost (64MB)
                3      // Time cost (iterations)
        );
    }

    /**
     * BCrypt with an increased cost factor for enhanced security
     * Cost 12 = ~250ms on modern hardware (good for 2024)
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12); // Increased from default 10
    }

    /**
     * SCrypt - Memory-hard alternative to BCrypt
     * Good performance vs security balance
     */
    @Bean
    public SCryptPasswordEncoder sCryptPasswordEncoder() {
        return SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    /**
     * Custom SCrypt with tuned parameters for high security
     */
    @Bean
    public SCryptPasswordEncoder customSCryptPasswordEncoder() {
        return new SCryptPasswordEncoder(
                65536, // CPU cost (N)
                8,     // Memory cost (r)
                1,     // Parallelization (p)
                32,    // Key length
                64     // Salt length
        );
    }

    /**
     * Alternative configuration: Argon2 only (for new applications)
     * Use when you don't need backward compatibility
     */
    @Bean
    @ConditionalOnProperty(name = "security.password.encoder", havingValue = "argon2-only")
    public PasswordEncoder argon2OnlyEncoder() {
        // Production-tuned Argon2 parameters
        return new Argon2PasswordEncoder(
                16,     // Salt length
                32,     // Hash length
                2,      // Parallelism
                131072, // Memory cost (128MB)
                4       // Time cost
        );
    }

    /**
     * Password strength validator bean
     * Complements strong hashing with input validation
     */
    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator() {
            @Override
            public boolean isValid(String password) {
                return password != null
                        && password.length() >= 12
                        && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$");
            }
        };
    }
}

/**
 * Custom password validator interface
 */
interface PasswordValidator {
    boolean isValid(String password);
}