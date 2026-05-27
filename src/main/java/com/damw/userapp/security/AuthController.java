package com.damw.userapp.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Controlador de autenticación — proporciona el endpoint de login para obtener el JWT.
//
// Flujo de uso:
//   1. POST /auth/login con {"username":"admin","password":"admin123"}
//      → responde {"token":"eyJhbGciOiJIUzI1NiJ9..."}
//   2. El cliente guarda el token (localStorage o variable de sesión)
//   3. Cada petición posterior incluye: Authorization: Bearer <token>
//   4. JwtFilter valida el token y Spring Security permite el acceso
//
// ¿Por qué está en el paquete security y no en controller?
//   Es lógica de autenticación, no lógica de negocio — no pertenece junto a
//   UserController, NotaController, etc. Separarlo mejora la legibilidad del proyecto.
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Inyección por constructor — Spring inyecta automáticamente ambas dependencias.
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // -------------------------------------------------------------------------
    // POST /auth/login
    // -------------------------------------------------------------------------
    // Request body esperado:
    //   { "username": "admin", "password": "admin123" }
    //
    // Response 200 OK (credenciales correctas):
    //   { "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdC..." }
    //
    // Response 401 Unauthorized (credenciales incorrectas):
    //   { "error": "Credenciales incorrectas" }
    //
    // Usamos Map<String, String> como body de entrada — no necesitamos un DTO
    // para solo 2 campos (didácticamente más simple para 1º DAM).
    // -------------------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String username = credenciales.get("username");
        String password = credenciales.get("password");

        try {
            // authenticationManager.authenticate() verifica las credenciales contra
            // el UserDetailsService configurado en SecurityConfig (usuarios en memoria).
            //
            // Si son correctas → devuelve un objeto Authentication (no lo necesitamos aquí).
            // Si son incorrectas → lanza BadCredentialsException (subclase de AuthenticationException).
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Credenciales correctas → generar el JWT y devolverlo al cliente
            String token = jwtUtil.generarToken(username);

            // Map.of() crea un Map inmutable con una sola entrada — simple y suficiente.
            return ResponseEntity.ok(Map.of("token", token));

        } catch (AuthenticationException e) {
            // Credenciales incorrectas → 401 Unauthorized.
            //
            // IMPORTANTE: no incluimos el mensaje de la excepción en la respuesta.
            // Dar detalles ("usuario no existe" vs "contraseña incorrecta") ayudaría
            // a un atacante a enumerar usuarios válidos — esto se llama user enumeration.
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Credenciales incorrectas"));
        }
    }
}
