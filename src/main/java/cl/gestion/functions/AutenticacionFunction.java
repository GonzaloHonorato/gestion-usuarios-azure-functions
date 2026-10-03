package cl.gestion.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Map;
import java.util.Optional;

public class AutenticacionFunction {

    @FunctionName("autenticacion")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.FUNCTION,
                route = "auth/{accion}")
            HttpRequestMessage<Optional<String>> peticion,
            @BindingName("accion") String accion,
            final ExecutionContext contexto) {

        int vigencia = Configuracion.entero("TOKEN_VIGENCIA_MINUTOS", 30);
        AutenticacionService servicio = new AutenticacionService(
            new CuentaRepositorioJdbc(),
            PublicadorEventGrid.desdeConfiguracion(contexto.getLogger()),
            vigencia);

        String cuerpo = peticion.getBody().orElse(null);

        try {
            return switch (accion == null ? "" : accion.toLowerCase()) {
                case "login" -> login(peticion, servicio, cuerpo);
                case "recuperar" -> recuperar(peticion, servicio, cuerpo);
                case "restablecer" -> restablecer(peticion, servicio, cuerpo);
                default -> Respuestas.error(peticion, HttpStatus.NOT_FOUND, "Accion no reconocida");
            };
        } catch (DatosInvalidosException ex) {
            return Respuestas.error(peticion, HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (CredencialesInvalidasException | TokenInvalidoException ex) {
            return Respuestas.error(peticion, HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (CuentaDesactivadaException ex) {
            return Respuestas.error(peticion, HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (RuntimeException ex) {
            contexto.getLogger().severe("autenticacion: " + ex.getMessage());
            return Respuestas.error(peticion, HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private HttpResponseMessage login(HttpRequestMessage<Optional<String>> peticion,
                                      AutenticacionService servicio, String cuerpo) {
        PeticionLogin datos = Json.leer(cuerpo, PeticionLogin.class);
        return Respuestas.json(peticion, HttpStatus.OK,
            servicio.autenticar(datos.email(), datos.password()));
    }

    private HttpResponseMessage recuperar(HttpRequestMessage<Optional<String>> peticion,
                                          AutenticacionService servicio, String cuerpo) {
        PeticionRecuperacion datos = Json.leer(cuerpo, PeticionRecuperacion.class);
        servicio.solicitarRecuperacion(datos.email());
        return Respuestas.json(peticion, HttpStatus.ACCEPTED, Map.of(
            "mensaje", "Si el correo esta registrado, recibiras las instrucciones"));
    }

    private HttpResponseMessage restablecer(HttpRequestMessage<Optional<String>> peticion,
                                            AutenticacionService servicio, String cuerpo) {
        PeticionRestablecer datos = Json.leer(cuerpo, PeticionRestablecer.class);
        servicio.restablecer(datos.token(), datos.password());
        return Respuestas.json(peticion, HttpStatus.OK, Map.of(
            "mensaje", "La contrasena fue actualizada"));
    }
}
