package com.system.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("http://localhost:3000");
        registry.addMapping("/auth/**").allowedOrigins("http://localhost:3000");
        registry.addMapping("/city/**").allowedOrigins("http://localhost:3000");
        registry.addMapping("/district/**").allowedOrigins("http://localhost:3000");
        registry.addMapping("/ward/**").allowedOrigins("http://localhost:3000");
        registry.addMapping("/drug-addict/**").allowedOrigins("http://localhost:3000");
        registry.addMapping("/police/**").allowedOrigins("http://localhost:3000");
        registry.addMapping("/treatment_place/**").allowedOrigins("http://localhost:3000");
    }
}