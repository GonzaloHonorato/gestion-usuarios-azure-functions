package cl.gestion.functions;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class EjecutorGraphQL {

    private EjecutorGraphQL() {
    }

    public static HttpResponseMessage responder(HttpRequestMessage<Optional<String>> peticion,
                                                GraphQL motor, String nombre,
                                                ExecutionContext contexto) {
        String cuerpo = peticion.getBody().orElse(null);
        if (cuerpo == null || cuerpo.isBlank()) {
            return Respuestas.error(peticion, HttpStatus.BAD_REQUEST,
                "Se espera un cuerpo JSON con el campo query");
        }

        PeticionGraphQL entrada;
        try {
            entrada = Json.leer(cuerpo, PeticionGraphQL.class);
        } catch (DatosInvalidosException ex) {
            return Respuestas.error(peticion, HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        if (entrada.query() == null || entrada.query().isBlank()) {
            return Respuestas.error(peticion, HttpStatus.BAD_REQUEST, "El campo query es obligatorio");
        }

        try {
            ExecutionResult resultado = motor.execute(ExecutionInput.newExecutionInput()
                .query(entrada.query())
                .variables(entrada.variables() == null ? Map.of() : entrada.variables())
                .operationName(entrada.operationName())
                .build());

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("data", resultado.getData());
            if (!resultado.getErrors().isEmpty()) {
                respuesta.put("errors", resultado.getErrors().stream()
                    .map(error -> Map.of("message", error.getMessage()))
                    .toList());
            }
            return Respuestas.json(peticion, HttpStatus.OK, respuesta);

        } catch (RuntimeException ex) {
            contexto.getLogger().severe(nombre + ": " + ex.getMessage());
            return Respuestas.error(peticion, HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor");
        }
    }
}
