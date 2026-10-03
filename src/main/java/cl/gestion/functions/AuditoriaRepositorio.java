package cl.gestion.functions;

import java.util.List;

public interface AuditoriaRepositorio {

    void registrar(EventoRecibido evento);

    List<EventoAuditado> ultimos(int cantidad);
}
