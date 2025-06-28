package or.sopt.houme.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 명세를 설정하여 Swagger UI에서 하우미 API 문서를 제공합니다.
     *
     * JWT Bearer 인증 스키마("JWTToken")를 포함하여 API 메타데이터(제목, 설명, 버전)와 서버 URL("/")을 구성합니다.
     *
     * @return 하우미 API의 OpenAPI 명세 인스턴스
     */
    @Bean
    public OpenAPI HOUME_API(){
        Info info=new Info()
                .title("하우미_API")
                .description("HOUME API입니다")
                .version("1.0");

        String jwtSchemeName="JWTToken";

        SecurityRequirement securityRequirement=new SecurityRequirement().addList(jwtSchemeName);

        Components components=new Components()
                .addSecuritySchemes(jwtSchemeName,new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);

    }

}
