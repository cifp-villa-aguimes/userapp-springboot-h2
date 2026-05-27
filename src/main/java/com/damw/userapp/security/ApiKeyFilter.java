package com.damw.userapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

// OncePerRequestFilter — filtro que Spring garantiza que se ejecuta exactamente
// UNA vez por petición HTTP (evita dobles ejecuciones con includes/forwards).
//
// @Component → Spring lo detecta automáticamente y lo registra en la cadena de filtros.
// No necesitamos ninguna otra configuración adicional.
//
// Esta rama NO usa Spring Security — es un filtro puro del servlet Jakarta EE.
// Objetivo didáctico: demostrar que la seguridad no siempre requiere un framework
// completo. Un filtro simple puede proteger una API de escritura de forma efectiva.
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    // @Value inyecta el valor de la propiedad app.api-key desde application.properties.
    // Si la propiedad no existe, Spring lanza un error al arrancar — fail fast.
    private final String apiKey;

    // Inyección por constructor: @Value funciona en constructores igual que en campos.
    // Es preferible a la inyección directa en campo para poder testear la clase
    // sin levantar el contexto de Spring completo.
    public ApiKeyFilter(@Value("${app.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    // ── Métodos que MODIFICAN datos → requieren X-API-KEY ────────────────────
    // Los GET son públicos: el alumnado puede consultar la API sin credenciales.
    // Usamos un Set para la búsqueda en O(1) en lugar de múltiples if-else.
    private static final Set<String> METODOS_PROTEGIDOS = Set.of("POST", "PUT", "DELETE");

    // -------------------------------------------------------------------------
    // shouldNotFilter — excluye ciertas rutas del filtro completamente
    // -------------------------------------------------------------------------
    // La consola H2 usa POST internamente (para ejecutar queries SQL).
    // Si no la excluimos, el filtro bloqueará la consola H2 con 401.
    // Devolver true → el filtro NO se aplica a esa petición.
    // -------------------------------------------------------------------------
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/h2-console");
    }

    // -------------------------------------------------------------------------
    // doFilterInternal — lógica principal del filtro
    // -------------------------------------------------------------------------
    // Se ejecuta en cada petición HTTP (excepto las excluidas por shouldNotFilter).
    //
    // Flujo:
    //   1. ¿Es POST, PUT o DELETE? → verificar cabecera X-API-KEY
    //   2. ¿La clave coincide? → dejar pasar (chain.doFilter)
    //   3. Clave incorrecta o ausente → 401 Unauthorized y terminar aquí
    //   4. Es GET u otro método de solo lectura → dejar pasar siempre
    // -------------------------------------------------------------------------
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ── Solo aplicar la validación a métodos que modifican datos ──────────
        if (METODOS_PROTEGIDOS.contains(request.getMethod())) {

            // Recuperar la cabecera X-API-KEY de la petición.
            // getHeader() devuelve null si la cabecera no existe.
            String claveRecibida = request.getHeader("X-API-KEY");

            // Comparamos usando apiKey.equals() y no claveRecibida.equals() para
            // evitar NullPointerException si claveRecibida es null.
            if (claveRecibida == null || !apiKey.equals(claveRecibida)) {
                // WWW-Authenticate informa al cliente de qué tipo de autenticación se espera.
                response.setHeader("WWW-Authenticate", "ApiKey realm=\"userapp\"");

                // 401 Unauthorized — la petición no tiene credenciales válidas.
                // sendError termina la respuesta aquí, sin llamar a chain.doFilter().
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "API Key requerida. Añade la cabecera X-API-KEY a tu petición."
                );
                // return — la petición no llega al controlador.
                return;
            }
        }

        // ── Dejar pasar la petición al siguiente filtro o controlador ─────────
        filterChain.doFilter(request, response);
    }
}
