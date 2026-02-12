package cat.itacademy.battleship_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 1. Importante para logs
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // Opcional si usaras UserDetails real
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j // 2. Habilita el logger 'log' automáticamente
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final String HEADER_AUTH = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(HEADER_AUTH);
        final String jwt;
        final String username;

        // 3. Lógica simplificada: Si NO hay token válido, pasamos al siguiente filtro y salimos.
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 4. Extraer token
            jwt = authHeader.substring(TOKEN_PREFIX.length());
            username = jwtService.extractUsername(jwt);

            // 5. Validar solo si hay usuario y no está autenticado ya
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.isTokenValid(jwt, username)) {
                    setAuthenticationContext(request, username);
                    log.debug("Usuario autenticado correctamente: {}", username);
                }
            }

        } catch (Exception e) {
            // 6. Logueamos el error pero NO detenemos la petición.
            // Si la ruta era pública, debe seguir funcionando. Si era privada, Spring Security devolverá 403.
            log.error("No se pudo establecer la autenticación del usuario: {}", e.getMessage());
        }

        // 7. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Método auxiliar para extraer la lógica de creación del token y limpiar el método principal.
     */
    private void setAuthenticationContext(HttpServletRequest request, String username) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.emptyList() // Aquí podrías cargar roles si los tuvieras
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}