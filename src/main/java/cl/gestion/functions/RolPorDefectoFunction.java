package cl.gestion.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class RolPorDefectoFunction {

    @FunctionName("rolPorDefecto")
    public void run(
            @EventGridTrigger(name = "eventGridEvent") String contenido,
            final ExecutionContext contexto) {

        try {
            EventoRecibido evento = LectorEvento.leer(contenido);
            if (!TipoEvento.USUARIO_CREADO.equals(evento.eventType())) {
                contexto.getLogger().info(
                    "rolPorDefecto: evento ignorado, tipo " + evento.eventType());
                return;
            }

            long usuarioId = LectorEvento.numero(evento.data(), "usuarioId");
            if (usuarioId == 0L) {
                contexto.getLogger().warning(
                    "rolPorDefecto: el evento " + evento.id() + " no trae usuarioId");
                return;
            }

            RolPorDefectoService.desdeConfiguracion().asignar(usuarioId)
                .ifPresentOrElse(
                    rol -> contexto.getLogger().info("rolPorDefecto: rol " + rol
                        + " asignado al usuario " + usuarioId + " · evento " + evento.id()),
                    () -> contexto.getLogger().info("rolPorDefecto: el usuario " + usuarioId
                        + " ya tenia roles, nada por hacer · evento " + evento.id()));

        } catch (DatosInvalidosException ex) {
            contexto.getLogger().severe("rolPorDefecto: " + ex.getMessage());
        } catch (RuntimeException ex) {
            contexto.getLogger().severe("rolPorDefecto: " + ex.getMessage());
            throw ex;
        }
    }
}
