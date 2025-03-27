package ssafy.d210.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    // jwt 구현 하면 추가
    // 토큰 정보로 볼 수 있도록 구현할 것
    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
//                .components(new Components().addSecuritySchemes(securitySchemeName,
//                        new SecurityScheme()
//                                .type(SecurityScheme.Type.HTTP)
//                                .scheme("bearer")
//                                .bearerFormat("JWT")
//                ))
//                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .info(apiInfo()); // API 정보를 설정한다.
    }

    private Info apiInfo() {
        return new Info().title("LearnAUTH 정보 REST API 문서다냥!")
                .description("<h3>LearnAuth 고양이들을 위한 REST API 문서입니다옹.</h3>")
                .version("1.0.0");
    }

}
