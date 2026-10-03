package cl.gestion.functions;

public interface AsignacionRepositorio {

    boolean tieneRoles(long usuarioId);

    void asignar(long usuarioId, long rolId);

    int quitarRol(long rolId);
}
