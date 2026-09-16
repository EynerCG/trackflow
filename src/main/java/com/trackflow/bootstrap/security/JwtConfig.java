package com.trackflow.bootstrap.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Firma y verificación de los tokens con un secreto compartido (HMAC-SHA256).
 *
 * Se eligió firma simétrica porque quien emite y quien valida son la misma aplicación.
 * Si en el futuro otros servicios tuvieran que validar tokens sin poder firmarlos,
 * habría que pasar a un par de claves asimétricas.
 */
@Configuration
public class JwtConfig {

    private final SecretKey clave;

    public JwtConfig(SecurityProperties propiedades) {
        this.clave = new SecretKeySpec(
                propiedades.secretoEfectivo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(clave).build();
    }
}
