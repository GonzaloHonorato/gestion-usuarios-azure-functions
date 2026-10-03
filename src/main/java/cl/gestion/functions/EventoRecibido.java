package cl.gestion.functions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoRecibido(
    String id,
    String eventType,
    String subject,
    String eventTime,
    String dataVersion,
    JsonNode data
) {}
