package com.damw.userapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// @Component → Spring registra este bean. Lo inyectaremos en JwtFilter y AuthController.
//
// JwtUtil encapsula toda la lógica de JWT:
//   - Generar tokens al hacer login
//   - Validar tokens en cada petición (firma + expiración)
//   - Extraer el nombre de usuario del token
//
// ¿Qué es un JWT?
//   Un JWT (JSON Web Token) tiene 3 partes separadas por puntos:
//     HEADER.PAYLOAD.SIGNATURE
//
//   Header:    {"alg":"HS256","typ":"JWT"}  → codificado en Base64
//   Payload:   {"sub":"admin","iat":...}    → claims, codificado en Base64
//   Signature: HMAC-SHA256(header+payload, secretKey) → verifica integridad
//
//   Ejemplo real: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.Xk7...
//
// HS256 (HMAC-SHA256): algoritmo simétrico — la misma clave firma y verifica.
// Para tokens asimétricos (RS256) se usaría un par de claves pública/privada.
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiracion;

    // Construimos la SecretKey a partir del String de application.properties.
    // Keys.hmacShaKeyFor() crea una clave HMAC compatible con HS256.
    // StandardCharsets.UTF_8 garantiza la conversión String→bytes consistente
    // en cualquier sistema operativo (evita problemas con el charset por defecto del SO).
    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiracion
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracion = expiracion;
    }

    // -------------------------------------------------------------------------
    // generarToken(username) → crea y devuelve un JWT firmado
    // -------------------------------------------------------------------------
    // Claims estándar incluidos:
    //   sub (subject)     → identificador del usuario (nombre de login)
    //   iat (issued at)   → fecha de creación del token (timestamp Unix)
    //   exp (expiration)  → fecha de expiración del token (timestamp Unix)
    //
    // El token viaja en la cabecera HTTP de cada petición:
    //   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
    // -------------------------------------------------------------------------
    public String generarToken(String username) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracion);

        return Jwts.builder()
                .subject(username)      // claim "sub" — quién es el usuario
                .issuedAt(ahora)        // claim "iat" — cuándo se emitió
                .expiration(expira)     // claim "exp" — cuándo caduca
                .signWith(secretKey)    // firma con HS256 (JJWT infiere el algoritmo de la clave)
                .compact();             // genera el String final "xxxxx.yyyyy.zzzzz"
    }

    // -------------------------------------------------------------------------
    // extraerUsername(token) → obtiene el claim "sub" del JWT
    // -------------------------------------------------------------------------
    // parseSignedClaims() verifica la firma Y la expiración automáticamente.
    // Si el token está manipulado o expirado, lanza JwtException (capturada en esValido).
    // -------------------------------------------------------------------------
    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    // -------------------------------------------------------------------------
    // esValido(token) → true si el token tiene firma correcta y no ha expirado
    // -------------------------------------------------------------------------
    // Usamos try-catch en lugar de lanzar excepción para simplificar el código
    // del filtro — puede usar un simple if en lugar de try-catch.
    // -------------------------------------------------------------------------
    public boolean esValido(String token) {
        try {
            extraerClaims(token); // lanza excepción si es inválido o expirado
            return true;
        } catch (Exception e) {
            // JwtException (firma incorrecta, expirado, malformado) → token inválido
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Método privado: extrae y verifica todos los claims del token
    // -------------------------------------------------------------------------
    // verifyWith(secretKey) configura la clave para verificar la firma HMAC-SHA256.
    // Si la firma no coincide con nuestra clave → SignatureException.
    // Si el token ha expirado → ExpiredJwtException.
    // -------------------------------------------------------------------------
    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)          // configura la clave de verificación
                .build()
                .parseSignedClaims(token)       // verifica firma y fecha de expiración
                .getPayload();                  // devuelve los claims (sub, iat, exp, ...)
    }
}
