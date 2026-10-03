package cl.gestion.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class DesasignacionRolFunction {

    @FunctionName("desasignacionRol")
    public void run(
            @EventGridTrigger(name = "eventGridEvent") String contenido,
            final ExecutionContext contexto) {

        try {
            EventoRecibido evento = LectorEvento.leer(contenido);
            if (!TipoEvento.ROL_ELIMINADO.equals(evento.eventType())) {
                contexto.getLogger().info(
                    "desasignacionRol: evento ignorado, tipo " + evento.eventType());
                return;
            }

            long rolId = LectorEvento.numero(evento.data(), "rolId");
            if (rolId == 0L) {
                contexto.getLogger().warning(
                    "desasignacionRol: el evento " + evento.id() + " no trae rolId");
                return;
            }

            int usuarios = DesasignacionRolService.desdeConfiguracion().ejecutar(rolId);
            contexto.getLogger().info("desasignacionRol: rol " + rolId + " retirado a "
                + usuarios + " usuario(s) y eliminado · evento " + evento.id());

        } catch (DatosInvalidosException ex) {
            contexto.getLogger().severe("desasignacionRol: " + ex.getMessage());
        } catch (RuntimeException ex) {
            contexto.getLogger().severe("desasignacionRol: " + ex.getMessage());
            throw ex;
        }
    }
}
