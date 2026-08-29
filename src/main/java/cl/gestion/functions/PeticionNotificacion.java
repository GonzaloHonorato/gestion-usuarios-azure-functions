package cl.gestion.functions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PeticionNotificacion(String tipo, String destinatario, String nombre, String token) {}
