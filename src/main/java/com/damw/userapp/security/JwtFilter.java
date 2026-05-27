package com.damw.userapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// JwtFilter intercepta CADA petición HTTP y, si viene con un JWT válido,
// autentica al usuario en el contexto de seguridad de Spring Security.
//
// Flujo de una petición protegida con JWT:
//   1. Cliente hace POST /api/v1/users con cabecera: Authorization: Bearer eyJhbGc...
//   2. JwtFilter extrae el token de la cabecera
//   3. JwtUtil verifica la firma HMAC-SHA256 y la fecha de expiración
//   4. Si es válido → registrar autenticación en SecurityContextHolder
//   5. SecurityConfig permite el acceso (la petición ya está autenticada)
//   6. Si el token es inválido → no registramos nada; Spring rechaza con 401
//
// @Component → Spring registra este bean y lo añade a la cadena de filtros.
// SecurityConfig lo posiciona explícitamente ANTES del filtro de Spring Security.
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Inyección por constructor: permite testear el filtro sin levantar Spring completo
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ── Paso 1: leer la cabecera Authorization ────────────────────────────
        // El estándar Bearer Token (RFC 6750) usa el formato:
        //   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIi...
        String authHeader = request.getHeader("Authorization");

        // Sin cabecera o sin prefijo "Bearer " → la petición llega sin autenticación.
        // SecurityConfig decidirá si el endpoint es público (GET) o requiere auth (POST...).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── Paso 2: extraer el token (quitar "Bearer ") ───────────────────────
        // "Bearer " tiene 7 caracteres → el token JWT empieza en la posición 7
        String token = authHeader.substring(7);

        // ── Paso 3: validar y autenticar ──────────────────────────────────────
        if (jwtUtil.esValido(token)) {
            String username = jwtUtil.extraerUsername(token);

            // UsernamePasswordAuthenticationToken representa al usuario autenticado.
            // Parámetros: (principal, credenciales, authorities)
            //   principal    → nombre de usuario (el "sub" del JWT)
            //   credenciales → null (no necesitamos la contraseña; ya validamos el token)
            //   authorities  → lista de roles. Asignamos ROLE_USER a todos los tokens válidos.
            var auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

            // Registrar la autenticación en el contexto de seguridad de esta petición.
            // Desde este momento Spring Security considera la petición como autenticada
            // y permitirá el acceso a los endpoints que requieren autenticación.
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        // Token inválido: no hacemos nada. La petición llega sin autenticación y
        // Spring Security la rechazará con 401 si el endpoint lo requiere.

        // ── Paso 4: continuar con la cadena de filtros ────────────────────────
        filterChain.doFilter(request, response);
    }
}
