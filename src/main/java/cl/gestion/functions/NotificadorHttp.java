package cl.gestion.functions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Logger;

public class NotificadorHttp implements Notificador {

    private final String url;
    private final HttpClient http;
    private final Logger log;

    public NotificadorHttp(String url, Logger log) {
        this.url = url;
        this.log = log;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public void bienvenida(String email, String nombre) {
        enviar(Map.of("tipo", "BIENVENIDA", "destinatario", email, "nombre", nombre));
    }

    @Override
    public void recuperacion(String email, String nombre, String token) {
        enviar(Map.of("tipo", "RECUPERACION", "destinatario", email, "nombre", nombre, "token", token));
    }

    private void enviar(Map<String, String> cuerpo) {
        if (url == null || url.isBlank()) {
            log.warning("NOTIFICACIONES_URL sin configurar, se omite el aviso a " + cuerpo.get("destinatario"));
            return;
        }
        HttpRequest peticion = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(Json.escribir(cuerpo)))
            .build();
        try {
            HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() >= 300) {
                log.warning("La notificacion respondio " + respuesta.statusCode());
            }
        } catch (java.io.IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warning("No se pudo enviar la notificacion: " + ex.getMessage());
        }
    }
}
