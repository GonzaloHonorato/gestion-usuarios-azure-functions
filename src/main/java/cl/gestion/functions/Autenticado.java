package cl.gestion.functions;

import java.util.List;

public record Autenticado(Long usuarioId, String email, String nombre, List<String> roles) {}
