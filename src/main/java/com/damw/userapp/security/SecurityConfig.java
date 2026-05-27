package com.damw.userapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// @Configuration indica a Spring que esta clase contiene definiciones de beans.
// @EnableWebSecurity activa el módulo de seguridad web de Spring Security.
//
// IMPORTANTE — conflicto de nombres:
//   Este fichero importa org.springframework.security.core.userdetails.User,
//   que es la clase de Spring Security para representar usuarios autenticados.
//   NO confundir con com.damw.userapp.model.User (nuestra entidad JPA).
//   Al estar en el paquete security, no hay ningún import ambiguo.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // -------------------------------------------------------------------------
    // Bean: SecurityFilterChain
    // -------------------------------------------------------------------------
    // Define QUÉ rutas son públicas y cuáles requieren autenticación.
    // Spring Security intercepta cada petición HTTP y aplica estas reglas en orden.
    // La primera regla que coincide con la URL gana — el orden importa.
    // -------------------------------------------------------------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth

                // ── Rutas siempre públicas (sin autenticación) ──────────────

                // Consola H2 — imprescindible para el trabajo en clase.
                // Hay que permitir toda la ruta /h2-console/** (no solo /h2-console/).
                .requestMatchers("/h2-console/**").permitAll()

                // Ficheros HTML estáticos del dashboard del alumnado.
                // /*.html cubre index.html, users.html, notas.html, etiquetas.html, etc.
                .requestMatchers("/", "/*.html").permitAll()

                // Actuator — monitorización sin autenticar (solo en entorno de desarrollo).
                .requestMatchers("/actuator/**").permitAll()

                // ── GET públicos — el alumnado puede consultar sin credenciales ──
                // HttpMethod.GET aplica solo a peticiones GET en esas rutas.
                // POST/PUT/DELETE sobre las mismas rutas quedan protegidas.
                .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()

                // ── Resto (POST, PUT, DELETE) → requieren Basic Auth ──────────
                .anyRequest().authenticated()
            )

            // HTTP Basic: el navegador muestra un diálogo usuario/contraseña.
            // En Postman: pestaña Authorization → Basic Auth → admin / admin123.
            // En una API real usaríamos JWT — esto es para enseñar el concepto.
            .httpBasic(httpBasic -> {})

            // CSRF deshabilitado para APIs REST sin estado.
            // CSRF protege formularios HTML tradicionales (session-based).
            // Con Basic Auth + JSON no hay riesgo de CSRF porque el navegador
            // no envía automáticamente la cabecera Authorization cross-origin.
            .csrf(csrf -> csrf.disable())

            // La consola H2 usa <iframe> internamente.
            // Sin esta configuración, el navegador bloquea el iframe por X-Frame-Options.
            // sameOrigin() permite frames del mismo origen (localhost:8080).
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Bean: UserDetailsService
    // -------------------------------------------------------------------------
    // Define los usuarios en memoria para este ejemplo educativo.
    // En una aplicación real los usuarios vendrían de la base de datos
    // con un UserDetailsService que consulte un repositorio JPA.
    //
    // IMPORTANTE: este User es org.springframework.security.core.userdetails.User
    // (Spring Security), NO com.damw.userapp.model.User (nuestra entidad JPA de la BD).
    // -------------------------------------------------------------------------
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // User.withUsername() es el builder de Spring Security para usuarios en memoria.
        // .roles("ADMIN") añade automáticamente el prefijo "ROLE_" → "ROLE_ADMIN".
        var admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    // -------------------------------------------------------------------------
    // Bean: PasswordEncoder
    // -------------------------------------------------------------------------
    // BCrypt es el algoritmo de hashing de contraseñas estándar en aplicaciones web.
    // Nunca almacenamos contraseñas en texto plano — siempre hasheadas.
    // Spring Security usa este encoder automáticamente al comparar credenciales.
    // -------------------------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
