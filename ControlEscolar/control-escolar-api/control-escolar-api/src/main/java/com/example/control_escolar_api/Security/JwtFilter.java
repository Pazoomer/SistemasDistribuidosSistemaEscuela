package com.example.control_escolar_api.Security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("jwtFilterBean")  // Este nombre será usado en SecurityConfig
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

        String uri = req.getRequestURI();
        String authHeader = req.getHeader("Authorization");

        if ("/api/login".equals(uri)) {
            chain.doFilter(request, response);
            return;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validarToken(token)) {
                chain.doFilter(request, response);
                return;
            }
        }

        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o ausente");
    }

    @Override public void init(FilterConfig filterConfig) {}
    @Override public void destroy() {}
}
