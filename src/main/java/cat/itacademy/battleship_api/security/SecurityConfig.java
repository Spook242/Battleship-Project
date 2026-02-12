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
                        // 👇 1. RECURSOS ESTÁTICOS (Actualizado para tus nuevas carpetas)
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/styles.css",  // Tu CSS
                                "/game.js",     // Tu JS principal
                                "/favicon.ico",

                                // ✅ AQUI ESTÁ EL CAMBIO IMPORTANTE:
                                "/images/**",   // Permite todo lo de dentro de images/
                                "/sounds/**",   // Permite todo lo de dentro de sounds/
                                "/videos/**",   // Permite todo lo de dentro de videos/

                                // Extensiones sueltas por si acaso se te escapa alguno fuera
                                "/**/*.css", "/**/*.js",
                                "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.gif", "/**/*.svg",
                                "/**/*.mp3", "/**/*.wav", "/**/*.mp4",

                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 2. RUTAS PÚBLICAS (Login y Crear Partida)
                        .requestMatchers("/game/new", "/game/ranking", "/auth/**").permitAll()

                        // 3. RUTAS PROTEGIDAS (Jugar)
                        .requestMatchers("/game/**").authenticated() // El resto de /game/ requiere token

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}