package cat.itacademy.battleship_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders; // Importante para la constante
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 1. Si no hay cabecera o no empieza por Bearer, dejamos pasar la petición (será anónima)
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extraer el token y el usuario
            final String jwt = authHeader.substring(TOKEN_PREFIX.length());
            final String username = jwtService.extractUsername(jwt);

            // 3. Validar: Que haya usuario y que no esté ya autenticado en el contexto actual
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.isTokenValid(jwt, username)) {
                    // Autenticamos al usuario en el contexto de seguridad de Spring
                    authenticateUser(request, username);
                    log.debug("Token válido. Usuario autenticado: {}", username);
                }
            }

        } catch (Exception e) {
            // Usamos WARN o DEBUG para no ensuciar los logs con errores si alguien manda un token basura
            log.warn("Token inválido o expirado recibida en la petición: {}", e.getMessage());
        }

        // 4. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Crea y establece el token de autenticación en el contexto.
     */
    private void authenticateUser(HttpServletRequest request, String username) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.emptyList() // Aquí irían los roles/autoridades si los tuvieras
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}