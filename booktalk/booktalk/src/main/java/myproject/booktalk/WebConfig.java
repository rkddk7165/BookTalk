package myproject.booktalk;

import lombok.RequiredArgsConstructor;
import myproject.booktalk.user.session.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",            // ✅ 로그인 요청 허용
                        "/login/**",         // 혹시 path variable 있을 경우
                        "/join",
                        "/logout",
                        "/users", "/users/**", // ← 만약 join이 /users로 들어온다면 이거 필요함
                        "/css/**",
                        "/js/**",
                        "/favicon.ico"
                );
    }
}