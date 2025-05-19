package com.example.control_escolar_api.Security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("jwtFilterBean")
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getServletPath();
        String authHeader = req.getHeader("Authorization");

        // Permitir login sin token
        if (path.equals("/api/login") || path.startsWith("/error")) {
            chain.doFilter(request, response);
            return;
        }

        // Validar y autenticar token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validarToken(token)) {
                String username = jwtUtil.extraerUsuario(token);

                // Crear objeto de autenticación sin roles
                var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        username, null, java.util.Collections.emptyList()
                );

                // Registrar al usuario como autenticado en el contexto
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

                chain.doFilter(request, response);
                return;
            }
        }

        // Si no es válido
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o ausente");
    }


    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
