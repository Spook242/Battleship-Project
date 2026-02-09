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
                        // 👇 1. PERMITIR ACCESO A LA PÁGINA WEB Y RECURSOS (Optimizado)
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/**/*.css",    // Captura subcarpetas
                                "/**/*.js",     // Captura subcarpetas
                                "/**/*.png",
                                "/**/*.jpg",
                                "/**/*.jpeg",
                                "/**/*.mp3",
                                "/**/*.mp4",
                                "/**/*.gif",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 👇 2. PERMITIR CREAR PARTIDA (API)
                        .requestMatchers("/game/new").permitAll()
                        .requestMatchers("/game/new", "/game/ranking", "/auth/**").permitAll()
                        .requestMatchers("/game/**").permitAll()

                        // 👇 3. CANDADO: Todo lo demás (disparar, turno CPU) requiere autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}