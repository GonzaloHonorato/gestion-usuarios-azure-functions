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

public class UsuariosFunction {

    @FunctionName("usuarios")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE},
                authLevel = AuthorizationLevel.FUNCTION,
                route = "usuarios/{id?}")
            HttpRequestMessage<Optional<String>> peticion,
            final ExecutionContext contexto) {

        String id = Rutas.identificador(peticion.getUri().getPath(), "usuarios");

        UsuariosService servicio = new UsuariosService(
            new UsuarioRepositorioJdbc(),
            PublicadorEventGrid.desdeConfiguracion(contexto.getLogger()));

        try {
            return switch (peticion.getHttpMethod()) {
                case GET -> id == null ? listar(peticion, servicio) : obtener(peticion, servicio, id);
                case POST -> crear(peticion, servicio);
                case PUT -> actualizar(peticion, servicio, id);
                case DELETE -> desactivar(peticion, servicio, id);
                default -> Respuestas.error(peticion, HttpStatus.METHOD_NOT_ALLOWED, "Metodo no soportado");
            };
        } catch (DatosInvalidosException ex) {
            return Respuestas.error(peticion, HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (EmailDuplicadoException ex) {
            return Respuestas.error(peticion, HttpStatus.CONFLICT, ex.getMessage());
        } catch (RuntimeException ex) {
            contexto.getLogger().severe("usuarios: " + ex.getMessage());
            return Respuestas.error(peticion, HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private HttpResponseMessage listar(HttpRequestMessage<Optional<String>> peticion,
                                       UsuariosService servicio) {
        return Respuestas.json(peticion, HttpStatus.OK, servicio.listar());
    }

    private HttpResponseMessage obtener(HttpRequestMessage<Optional<String>> peticion,
                                        UsuariosService servicio, String id) {
        return servicio.obtener(identificador(id))
            .map(usuario -> Respuestas.json(peticion, HttpStatus.OK, usuario))
            .orElseGet(() -> Respuestas.noEncontrado(peticion, "el usuario " + id));
    }

    private HttpResponseMessage crear(HttpRequestMessage<Optional<String>> peticion,
                                      UsuariosService servicio) {
        CrearUsuario datos = Json.leer(peticion.getBody().orElse(null), CrearUsuario.class);
        return Respuestas.json(peticion, HttpStatus.CREATED, servicio.crear(datos));
    }

    private HttpResponseMessage actualizar(HttpRequestMessage<Optional<String>> peticion,
                                           UsuariosService servicio, String id) {
        ActualizarUsuario datos = Json.leer(peticion.getBody().orElse(null), ActualizarUsuario.class);
        return servicio.actualizar(identificador(id), datos)
            ? Respuestas.json(peticion, HttpStatus.OK, servicio.obtener(identificador(id)).orElseThrow())
            : Respuestas.noEncontrado(peticion, "el usuario " + id);
    }

    private HttpResponseMessage desactivar(HttpRequestMessage<Optional<String>> peticion,
                                           UsuariosService servicio, String id) {
        return servicio.desactivar(identificador(id))
            ? Respuestas.sinContenido(peticion)
            : Respuestas.noEncontrado(peticion, "el usuario " + id);
    }

    private static long identificador(String id) {
        if (id == null || id.isBlank()) {
            throw new DatosInvalidosException("Falta el identificador del usuario");
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new DatosInvalidosException("El identificador debe ser numerico");
        }
    }
}
