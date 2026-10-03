package cl.gestion.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class NotificacionesFunction {

    @FunctionName("notificaciones")
    public void run(
            @EventGridTrigger(name = "eventGridEvent") String contenido,
            final ExecutionContext contexto) {

        try {
            EventoRecibido evento = LectorEvento.leer(contenido);
            String destinatario = LectorEvento.texto(evento.data(), "email");
            String nombre = LectorEvento.texto(evento.data(), "nombre");
            if (nombre.isBlank()) {
                nombre = "usuario";
            }

            if (destinatario.isBlank()) {
                contexto.getLogger().warning(
                    "notificaciones: evento " + evento.eventType() + " sin destinatario");
                return;
            }

            Correo correo = switch (evento.eventType()) {
                case TipoEvento.USUARIO_CREADO -> MensajesCorreo.bienvenida(nombre);
                case TipoEvento.RECUPERACION_SOLICITADA -> MensajesCorreo.recuperacion(
                    nombre,
                    LectorEvento.texto(evento.data(), "token"),
                    Configuracion.entero("TOKEN_VIGENCIA_MINUTOS", 30));
                default -> null;
            };

            if (correo == null) {
                contexto.getLogger().info(
                    "notificaciones: evento ignorado, tipo " + evento.eventType());
                return;
            }

            SmtpMailer.desdeConfiguracion().enviar(destinatario, correo.asunto(), correo.cuerpo());
            contexto.getLogger().info("notificaciones: " + evento.eventType()
                + " enviada a " + destinatario + " · evento " + evento.id());

        } catch (DatosInvalidosException ex) {
            contexto.getLogger().severe("notificaciones: " + ex.getMessage());
        } catch (RuntimeException ex) {
            contexto.getLogger().severe("notificaciones: " + ex.getMessage());
            throw ex;
        }
    }
}
