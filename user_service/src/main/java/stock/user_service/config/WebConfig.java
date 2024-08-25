package stock.user_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 로컬 파일 시스템의 'uploads/profile_images/' 경로를 '/uploads/profile_images/**' URL 패턴으로 노출
        registry.addResourceHandler("/uploads/profile_images/**")
                .addResourceLocations("file:uploads/profile_images/");
    }
}
