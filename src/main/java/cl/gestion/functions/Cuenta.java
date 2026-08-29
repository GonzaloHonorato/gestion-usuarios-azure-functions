package cl.gestion.functions;

import java.util.List;

public record Cuenta(
    Long id,
    String email,
    String nombre,
    String passwordHash,
    boolean activo,
    List<String> roles
) {}
