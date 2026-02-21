package cat.itacademy.battleship_api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                        // 👇 1. RECURSOS ESTÁTICOS
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/game_old.css",
                                "/game_old.js",
                                "/favicon.ico",
                                "/images/**",
                                "/sounds/**",
                                "/videos/**",
                                "/js/**",
                                "/css/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 2. RUTAS PÚBLICAS (Login, Registro y Crear Partida)
                        // 🚨 CAMBIO 1: Actualizado a "/api/auth/**" para coincidir con tu AuthController
                        .requestMatchers("/game/new", "/game/ranking", "/api/auth/**").permitAll()

                        // 3. RUTAS PROTEGIDAS (Jugar)
                        .requestMatchers("/game/**").authenticated() // El resto de /game/ requiere token

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🚨 CAMBIO 2: Le decimos a Spring cómo vamos a encriptar y validar las contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}