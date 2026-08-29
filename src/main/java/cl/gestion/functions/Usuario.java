package cl.gestion.functions;

import java.util.List;

public record Usuario(
    Long id,
    String nombre,
    String apellido,
    String email,
    boolean activo,
    List<String> roles
) {}
