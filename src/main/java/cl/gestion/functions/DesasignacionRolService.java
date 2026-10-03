package cl.gestion.functions;

public class DesasignacionRolService {

    private final RolRepositorio roles;
    private final AsignacionRepositorio asignaciones;

    public DesasignacionRolService(RolRepositorio roles, AsignacionRepositorio asignaciones) {
        this.roles = roles;
        this.asignaciones = asignaciones;
    }

    public static DesasignacionRolService desdeConfiguracion() {
        return new DesasignacionRolService(new RolRepositorioJdbc(), new AsignacionRepositorioJdbc());
    }

    public int ejecutar(long rolId) {
        int usuarios = asignaciones.quitarRol(rolId);
        roles.eliminar(rolId);
        return usuarios;
    }
}
