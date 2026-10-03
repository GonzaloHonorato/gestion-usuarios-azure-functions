package cl.gestion.functions;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;

import java.util.logging.Logger;

public class PublicadorEventGrid implements PublicadorEventos {

    private static final String VERSION_DATOS = "1.0";

    private final EventGridPublisherClient<EventGridEvent> cliente;
    private final Logger log;

    public PublicadorEventGrid(String endpoint, String clave, Logger log) {
        this.log = log;
        this.cliente = new EventGridPublisherClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(clave))
            .buildEventGridEventPublisherClient();
    }

    public static PublicadorEventGrid desdeConfiguracion(Logger log) {
        return new PublicadorEventGrid(
            Configuracion.requerido("EVENTGRID_ENDPOINT"),
            Configuracion.requerido("EVENTGRID_KEY"),
            log);
    }

    static EventGridEvent eventoUsuarioCreado(long usuarioId, String email, String nombre) {
        return new EventGridEvent(
            "usuarios/" + usuarioId,
            TipoEvento.USUARIO_CREADO,
            BinaryData.fromObject(new EventoUsuarioCreado(usuarioId, email, nombre)),
            VERSION_DATOS);
    }

    static EventGridEvent eventoRecuperacionSolicitada(long usuarioId, String email,
                                                       String nombre, String token) {
        return new EventGridEvent(
            "usuarios/" + usuarioId + "/recuperacion",
            TipoEvento.RECUPERACION_SOLICITADA,
            BinaryData.fromObject(
                new EventoRecuperacionSolicitada(usuarioId, email, nombre, token)),
            VERSION_DATOS);
    }

    @Override
    public void usuarioCreado(long usuarioId, String email, String nombre) {
        publicar(eventoUsuarioCreado(usuarioId, email, nombre), TipoEvento.USUARIO_CREADO);
    }

    @Override
    public void recuperacionSolicitada(long usuarioId, String email, String nombre, String token) {
        publicar(eventoRecuperacionSolicitada(usuarioId, email, nombre, token),
            TipoEvento.RECUPERACION_SOLICITADA);
    }

    private void publicar(EventGridEvent evento, String tipo) {
        try {
            cliente.sendEvent(evento);
            log.info("Evento publicado: " + tipo + " · " + evento.getSubject());
        } catch (RuntimeException ex) {
            log.severe("No se pudo publicar el evento " + tipo + ": " + ex.getMessage());
        }
    }
}
