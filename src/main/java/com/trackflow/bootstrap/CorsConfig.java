package com.trackflow.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Solo los frontends declarados pueden llamar a la API desde el navegador. Con "*"
 * cualquier página de internet podría hacerlo en nombre de quien la visite; por eso
 * los orígenes se listan uno a uno en {@code trackflow.cors.origenes-permitidos}.
 */
@Configuration
public class CorsConfig {

    private final String[] origenesPermitidos;

    public CorsConfig(@Value("${trackflow.cors.origenes-permitidos}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(origenesPermitidos)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
