package com.trackflow.bootstrap.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara el esquema de token en la documentación, para que Swagger muestre el botón
 * "Authorize" y se puedan probar los endpoints protegidos desde el navegador.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA = "TokenDeOperador";

    @Bean
    OpenAPI trackflowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TrackFlow API")
                        .version("v1")
                        .description("""
                                Registro y seguimiento de envíos.

                                Las operaciones de escritura se admiten de forma asíncrona: responden 202 y el \
                                registro ocurre al consumir el mensaje de la cola.

                                La consulta de estado es pública. Para registrar envíos o eventos hay que \
                                obtener un token en POST /api/auth/login y usarlo con el botón Authorize."""))
                .components(new Components().addSecuritySchemes(ESQUEMA,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // Sin este requisito, el botón Authorize no adjunta el token a las peticiones.
                // Los endpoints públicos siguen funcionando sin él: quien decide es Spring Security.
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
    }
}
