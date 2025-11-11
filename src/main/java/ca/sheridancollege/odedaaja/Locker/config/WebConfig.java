package ca.sheridancollege.odedaaja.Locker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/contracts/**")
                .addResourceLocations("file:./contracts/");

        registry.addResourceHandler("/receipts/**")
                .addResourceLocations("file:./receipts/");
    }
}
