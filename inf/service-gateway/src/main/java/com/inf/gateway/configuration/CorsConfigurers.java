package com.inf.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.time.Duration;

@Configuration
public class CorsConfigurers {

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); //是 否允许携带cookie
        config.addAllowedOrigin("*"); // 可接受的域，是一个具体域名或者*（代表任意域名）
        config.addAllowedHeader("*"); // 允许携带的头
        config.addAllowedMethod("*"); // 允许访问的方式
        config.setMaxAge(Duration.ofDays(2));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
