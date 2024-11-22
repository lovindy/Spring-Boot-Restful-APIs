package com.example.springrestful.config; // Adjust the package to your project structure

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Overriding addCorsMappings to allow cross-origin requests
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply this CORS policy to all paths
                .allowedOrigins("http://localhost:8081") // Allow requests from Vue.js frontend (adjust port as needed)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow specific methods
                .allowedHeaders("*"); // Allow any header
    }
}
