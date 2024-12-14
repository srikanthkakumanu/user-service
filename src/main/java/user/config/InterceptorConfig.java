package user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import user.interceptor.AuthInterceptor;
import user.interceptor.ServiceLogInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new ServiceLogInterceptor());
            registry.addInterceptor(new AuthInterceptor())
                    .addPathPatterns("/**")
                    .excludePathPatterns("/api-docs/**", "**/swagger-ui/**");
        }
}
