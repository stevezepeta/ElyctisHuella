package gruposantoro.elyctishuella.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite CORS para todas las rutas
                .allowedOrigins("*") // Agrega aquí los dominios permitidos
                .allowedMethods("*") // Métodos permitidos
                .allowedHeaders("*") // Permite todas las cabeceras
                .allowCredentials(false); // Permite envío de credenciales (cookies, authorization headers, etc.)
    }
}
