package cl.gestion.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class RolesFunction {

    @FunctionName("roles")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE},
                authLevel = AuthorizationLevel.FUNCTION,
                route = "roles/{id?}")
            HttpRequestMessage<Optional<String>> peticion,
            final ExecutionContext contexto) {

        String id = Rutas.identificador(peticion.getUri().getPath(), "roles");

        RolesService servicio = new RolesService(new RolRepositorioJdbc());

        try {
            return switch (peticion.getHttpMethod()) {
                case GET -> id == null
                    ? Respuestas.json(peticion, HttpStatus.OK, servicio.listar())
                    : servicio.obtener(identificador(id))
                        .map(rol -> Respuestas.json(peticion, HttpStatus.OK, rol))
                        .orElseGet(() -> Respuestas.noEncontrado(peticion, "el rol " + id));
                case POST -> Respuestas.json(peticion, HttpStatus.CREATED,
                    servicio.crear(Json.leer(peticion.getBody().orElse(null), DatosRol.class)));
                case PUT -> servicio.actualizar(identificador(id),
                        Json.leer(peticion.getBody().orElse(null), DatosRol.class))
                    ? Respuestas.json(peticion, HttpStatus.OK,
                        servicio.obtener(identificador(id)).orElseThrow())
                    : Respuestas.noEncontrado(peticion, "el rol " + id);
                case DELETE -> servicio.eliminar(identificador(id))
                    ? Respuestas.sinContenido(peticion)
                    : Respuestas.noEncontrado(peticion, "el rol " + id);
                default -> Respuestas.error(peticion, HttpStatus.METHOD_NOT_ALLOWED, "Metodo no soportado");
            };
        } catch (DatosInvalidosException ex) {
            return Respuestas.error(peticion, HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (NombreRolDuplicadoException | RolEnUsoException ex) {
            return Respuestas.error(peticion, HttpStatus.CONFLICT, ex.getMessage());
        } catch (RuntimeException ex) {
            contexto.getLogger().severe("roles: " + ex.getMessage());
            return Respuestas.error(peticion, HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private static long identificador(String id) {
        if (id == null || id.isBlank()) {
            throw new DatosInvalidosException("Falta el identificador del rol");
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new DatosInvalidosException("El identificador debe ser numerico");
        }
    }
}
