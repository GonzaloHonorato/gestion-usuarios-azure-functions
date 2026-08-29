package cl.gestion.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private Json() {
    }

    public static <T> T leer(String cuerpo, Class<T> tipo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            throw new DatosInvalidosException("El cuerpo de la peticion esta vacio");
        }
        try {
            return MAPPER.readValue(cuerpo, tipo);
        } catch (JsonProcessingException ex) {
            throw new DatosInvalidosException("El cuerpo no es un JSON valido");
        }
    }

    public static String escribir(Object valor) {
        try {
            return MAPPER.writeValueAsString(valor);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar la respuesta", ex);
        }
    }
}
