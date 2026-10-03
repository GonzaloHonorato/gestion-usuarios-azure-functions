package cl.gestion.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class AuditoriaFunction {

    @FunctionName("auditoria")
    public void run(
            @EventGridTrigger(name = "eventGridEvent") String contenido,
            final ExecutionContext contexto) {

        try {
            EventoRecibido evento = LectorEvento.leer(contenido);
            new AuditoriaRepositorioJdbc().registrar(evento);
            contexto.getLogger().info("auditoria: registrado " + evento.eventType()
                + " · " + evento.subject() + " · evento " + evento.id());

        } catch (DatosInvalidosException ex) {
            contexto.getLogger().severe("auditoria: " + ex.getMessage());
        } catch (RuntimeException ex) {
            contexto.getLogger().severe("auditoria: " + ex.getMessage());
            throw ex;
        }
    }
}
