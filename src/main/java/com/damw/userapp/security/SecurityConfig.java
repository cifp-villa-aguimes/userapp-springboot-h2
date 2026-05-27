package com.damw.userapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration + @EnableWebSecurity activan y configuran Spring Security.
//
// IMPORTANTE — conflicto de nombres:
//   Este fichero importa org.springframework.security.core.userdetails.User,
//   que es la clase de Spring Security para representar usuarios autenticados.
//   NO confundir con com.damw.userapp.model.User (nuestra entidad JPA).
//   Al estar en el paquete security, no hay ningún import ambiguo.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Inyectamos el filtro JWT para añadirlo a la cadena de filtros de Spring Security.
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // -------------------------------------------------------------------------
    // Bean: SecurityFilterChain
    // -------------------------------------------------------------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth

                // ── Rutas siempre públicas ────────────────────────────────────

                // Endpoint de login — siempre público (donde se obtiene el token)
                .requestMatchers("/auth/login").permitAll()

                // Consola H2 — imprescindible para el trabajo en clase
                .requestMatchers("/h2-console/**").permitAll()

                // Ficheros HTML del dashboard del alumnado
                .requestMatchers("/", "/*.html").permitAll()

                // Actuator — monitorización pública en entorno de desarrollo
                .requestMatchers("/actuator/**").permitAll()

                // ── GET públicos — consultas sin autenticar ───────────────────
                .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()

                // ── POST, PUT, DELETE → requieren token JWT válido ────────────
                .anyRequest().authenticated()
            )

            // STATELESS: Spring Security NO crea ni usa sesiones HTTP (sin cookies de sesión).
            // Cada petición es independiente — el cliente debe enviar el token en cada request.
            // Sin STATELESS, Spring crearía una sesión tras el primer login exitoso y las
            // siguientes peticiones pasarían por la sesión (ignorando el token) — incorrecto.
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Deshabilitar HTTP Basic — en JWT usamos Bearer Token, no Basic Auth
            .httpBasic(basic -> basic.disable())

            // CSRF deshabilitado: sin sesiones no hay riesgo de CSRF.
            // CSRF solo afecta a formularios tradicionales con cookies de sesión.
            .csrf(csrf -> csrf.disable())

            // La consola H2 usa <iframe> — permitir frames del mismo origen
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )

            // Añadir JwtFilter ANTES del filtro de autenticación estándar de Spring.
            // Orden de ejecución de filtros relevantes:
            //   JwtFilter (extrae y valida el token) →
            //   UsernamePasswordAuthenticationFilter (Spring, normalmente no actúa) →
            //   ... → controlador
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Bean: UserDetailsService — usuarios en memoria para /auth/login
    // -------------------------------------------------------------------------
    // IMPORTANTE: este User es org.springframework.security.core.userdetails.User
    // (Spring Security) — NO es com.damw.userapp.model.User (nuestra entidad JPA).
    // -------------------------------------------------------------------------
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    // -------------------------------------------------------------------------
    // Bean: PasswordEncoder
    // -------------------------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // -------------------------------------------------------------------------
    // Bean: AuthenticationManager
    // -------------------------------------------------------------------------
    // AuthController lo necesita para validar usuario+contraseña en /auth/login.
    // Spring Boot 4 (Spring Security 7) no expone este bean automáticamente —
    // hay que obtenerlo explícitamente desde AuthenticationConfiguration.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
