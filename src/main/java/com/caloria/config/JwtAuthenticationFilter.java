package com.caloria.config;

import com.caloria.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


/**
 * Filtro personalizado de seguridad que se ejecuta una sola vez por petición.
 *
 * Se encarga de:
 * - Extraer el token JWT del encabezado `Authorization`
 * - Validar su firma y expiración
 * - Establecer el contexto de seguridad (usuario autenticado) si es válido
 *
 * Excluye automáticamente las rutas que comienzan con `/auth/` para permitir el login y registro.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    
    
    /**
     * Intercepta cada petición HTTP para validar el token JWT.
     *
     * Si el token es válido:
     * - Extrae el `usuarioId` del token
     * - Crea un `UsernamePasswordAuthenticationToken` con ese ID
     * - Lo coloca en el `SecurityContextHolder`
     *
     * Si no hay token o no es válido, continúa la cadena de filtros normalmente.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
                                  throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        //log.debug("Authorization header: [{}]", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.isValid(token)) {
                String usuarioId = jwtService.getUsuarioId(token);
                User principal = new User(usuarioId, "", List.of());
                var auth = new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Define qué rutas deben excluirse del filtro de autenticación JWT.
     *
     * En este caso, se excluyen los endpoints de autenticación (`/auth/*`)
     * para permitir el acceso al login y registro sin necesidad de token.
     *
     * @param request Petición HTTP
     * @return true si la ruta debe omitirse del filtro
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/");
    }
}