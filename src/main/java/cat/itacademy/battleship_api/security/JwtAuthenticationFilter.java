package cat.itacademy.battleship_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils; // Utilidad de Spring muy útil
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

        // 1. Intentamos obtener el token limpio
        String token = getTokenFromRequest(request);

        // 2. Si no hay token, pasamos al siguiente filtro (usuario anónimo)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Si hay token, intentamos autenticar
        try {
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(token, username)) {
                    authenticateUser(request, username);
                    log.debug("Usuario autenticado: {}", username);
                }
            }
        } catch (Exception exception) {
            // Si el token expiró o está mal formado, solo logueamos y seguimos (el usuario quedará como no autenticado)
            log.warn("No se pudo autenticar el token: {}", exception.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT puro de la cabecera Authorization.
     * Devuelve null si no existe o no tiene el formato correcto.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * Establece la autenticación en el contexto de Spring Security.
     */
    private void authenticateUser(HttpServletRequest request, String username) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.emptyList()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}