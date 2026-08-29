package cl.gestion.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Map;
import java.util.Optional;

public class NotificacionesFunction {

    @FunctionName("notificaciones")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.FUNCTION,
                route = "notificaciones")
            HttpRequestMessage<Optional<String>> peticion,
            final ExecutionContext contexto) {

        try {
            PeticionNotificacion datos = Json.leer(peticion.getBody().orElse(null),
                PeticionNotificacion.class);

            if (datos.destinatario() == null || datos.destinatario().isBlank()) {
                throw new DatosInvalidosException("Falta el destinatario");
            }

            String nombre = (datos.nombre() == null || datos.nombre().isBlank())
                ? "usuario" : datos.nombre();

            Correo correo = switch (datos.tipo() == null ? "" : datos.tipo().toUpperCase()) {
                case "BIENVENIDA" -> MensajesCorreo.bienvenida(nombre);
                case "RECUPERACION" -> MensajesCorreo.recuperacion(nombre, datos.token(),
                    Configuracion.entero("TOKEN_VIGENCIA_MINUTOS", 30));
                default -> throw new DatosInvalidosException("Tipo de notificacion no reconocido");
            };

            SmtpMailer.desdeConfiguracion()
                .enviar(datos.destinatario(), correo.asunto(), correo.cuerpo());

            contexto.getLogger().info("notificaciones: " + datos.tipo() + " enviada a " + datos.destinatario());
            return Respuestas.json(peticion, HttpStatus.OK, Map.of("enviado", true));

        } catch (DatosInvalidosException ex) {
            return Respuestas.error(peticion, HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (RuntimeException ex) {
            contexto.getLogger().severe("notificaciones: " + ex.getMessage());
            return Respuestas.error(peticion, HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo enviar la notificacion");
        }
    }
}
