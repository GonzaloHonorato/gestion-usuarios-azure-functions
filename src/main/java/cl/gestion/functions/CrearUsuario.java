package cl.gestion.functions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CrearUsuario(
    String nombre,
    String apellido,
    String email,
    String password,
    List<Long> roles
) {}
