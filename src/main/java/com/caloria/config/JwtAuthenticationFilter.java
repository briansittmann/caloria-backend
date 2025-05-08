package com.caloria.config;

import com.caloria.service.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Intercepta cada request, valida el JWT y coloca el usuarioId en el SecurityContext.
 */
@Slf4j
@Component          // se registra como bean automáticamente
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

    private final JwtService jwtService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String auth = request.getHeader("Authorization");
        log.debug("Authorization header raw: [{}]", auth);

        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);

            if (jwtService.isValid(token)) {
                String usuarioId = jwtService.getUsuarioId(token);
                User principal = new User(usuarioId, "", List.of());

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
            }
        }
        chain.doFilter(req, res);
    }
}