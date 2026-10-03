package cl.gestion.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class LectorEvento {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LectorEvento() {
    }

    public static EventoRecibido leer(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new DatosInvalidosException("El evento llego sin contenido");
        }
        try {
            JsonNode raiz = MAPPER.readTree(contenido);
            if (raiz.isArray()) {
                if (raiz.isEmpty()) {
                    throw new DatosInvalidosException("El arreglo de eventos venia vacio");
                }
                raiz = raiz.get(0);
            }
            return MAPPER.treeToValue(raiz, EventoRecibido.class);
        } catch (com.fasterxml.jackson.core.JacksonException ex) {
            throw new DatosInvalidosException("El evento no es un JSON valido");
        }
    }

    public static String texto(JsonNode datos, String campo) {
        if (datos == null) {
            return "";
        }
        JsonNode valor = datos.get(campo);
        return (valor == null || valor.isNull()) ? "" : valor.asText();
    }

    public static long numero(JsonNode datos, String campo) {
        if (datos == null) {
            return 0L;
        }
        JsonNode valor = datos.get(campo);
        return (valor == null || valor.isNull()) ? 0L : valor.asLong();
    }
}
