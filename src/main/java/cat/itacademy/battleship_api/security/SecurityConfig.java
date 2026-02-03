package cat.itacademy.battleship_api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        // 👇 1. PERMITIR ACCESO A LA PÁGINA WEB Y RECURSOS
                        .requestMatchers(
                                "/",                // La raíz (localhost:8080)
                                "/index.html",      // El archivo HTML
                                "/*.css",           // Todos los estilos
                                "/*.js",            // Todos los scripts (game.js)
                                "/*.png",           // Imágenes
                                "/*.jpg",           // Imágenes
                                "/*.mp3",           // Sonidos
                                "/*.mp4",           // Video de fondo
                                "/favicon.ico"      // Icono del navegador
                        ).permitAll()

                        // 👇 2. PERMITIR CREAR PARTIDA (API)
                        .requestMatchers("/game/new").permitAll()

                        // 👇 3. CANDADO: Todo lo demás (disparar, turno CPU) requiere autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}