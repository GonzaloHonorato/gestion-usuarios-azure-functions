package cl.gestion.functions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PeticionGraphQL(String query, Map<String, Object> variables, String operationName) {}
