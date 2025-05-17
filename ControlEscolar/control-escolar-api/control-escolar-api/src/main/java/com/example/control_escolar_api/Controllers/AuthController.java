package com.example.control_escolar_api.Controllers;

import com.example.control_escolar_api.Security.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> cred) {
        System.out.println(">>> Entrando al endpoint /api/login");
        String usuario = cred.get("usuario");
        String password = cred.get("password");

        if ("admin".equals(usuario) && "admin123".equals(password)) {
            String token = jwtUtil.generarToken(usuario);
            return Map.of("token", token);
        } else {
            throw new RuntimeException("Credenciales inválidas");
        }
    }
}
