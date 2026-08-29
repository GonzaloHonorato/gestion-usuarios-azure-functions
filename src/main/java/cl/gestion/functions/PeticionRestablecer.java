package cl.gestion.functions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PeticionRestablecer(String token, String password) {}
