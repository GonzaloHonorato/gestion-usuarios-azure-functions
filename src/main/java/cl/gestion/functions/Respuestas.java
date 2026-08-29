package cl.gestion.functions;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

import java.util.Map;
import java.util.Optional;

public final class Respuestas {

    private Respuestas() {
    }

    public static HttpResponseMessage json(HttpRequestMessage<Optional<String>> peticion,
                                           HttpStatus estado, Object cuerpo) {
        return peticion.createResponseBuilder(estado)
            .header("Content-Type", "application/json; charset=utf-8")
            .body(Json.escribir(cuerpo))
            .build();
    }

    public static HttpResponseMessage sinContenido(HttpRequestMessage<Optional<String>> peticion) {
        return peticion.createResponseBuilder(HttpStatus.NO_CONTENT).build();
    }

    public static HttpResponseMessage error(HttpRequestMessage<Optional<String>> peticion,
                                            HttpStatus estado, String mensaje) {
        return json(peticion, estado, Map.of("estado", estado.value(), "mensaje", mensaje));
    }

    public static HttpResponseMessage noEncontrado(HttpRequestMessage<Optional<String>> peticion,
                                                   String recurso) {
        return error(peticion, HttpStatus.NOT_FOUND, "No existe " + recurso);
    }
}
