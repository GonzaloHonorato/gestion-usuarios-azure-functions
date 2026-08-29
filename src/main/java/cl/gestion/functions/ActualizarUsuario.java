package cl.gestion.functions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActualizarUsuario(
    String nombre,
    String apellido,
    String email,
    List<Long> roles
) {}
