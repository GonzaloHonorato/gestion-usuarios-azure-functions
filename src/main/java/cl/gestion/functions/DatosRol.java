package cl.gestion.functions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosRol(String nombre, String descripcion) {}
