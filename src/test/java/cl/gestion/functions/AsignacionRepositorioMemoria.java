package cl.gestion.functions;

import java.util.ArrayList;
import java.util.List;

class AsignacionRepositorioMemoria implements AsignacionRepositorio {

    record Asignacion(long usuarioId, long rolId) {}

    final List<Asignacion> asignaciones = new ArrayList<>();
    final List<String> operaciones;

    AsignacionRepositorioMemoria() {
        this(new ArrayList<>());
    }

    AsignacionRepositorioMemoria(List<String> operaciones) {
        this.operaciones = operaciones;
    }

    @Override public boolean tieneRoles(long usuarioId) {
        return asignaciones.stream().anyMatch(asignacion -> asignacion.usuarioId() == usuarioId);
    }

    @Override public void asignar(long usuarioId, long rolId) {
        asignaciones.add(new Asignacion(usuarioId, rolId));
    }

    @Override public int quitarRol(long rolId) {
        operaciones.add("quitar-rol:" + rolId);
        int antes = asignaciones.size();
        asignaciones.removeIf(asignacion -> asignacion.rolId() == rolId);
        return antes - asignaciones.size();
    }
}
