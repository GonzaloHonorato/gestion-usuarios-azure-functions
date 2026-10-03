package cl.gestion.functions;

import java.util.Optional;

public class RolPorDefectoService {

    public static final String NOMBRE_POR_DEFECTO = "USUARIO";

    private final RolRepositorio roles;
    private final AsignacionRepositorio asignaciones;
    private final String nombreRol;

    public RolPorDefectoService(RolRepositorio roles, AsignacionRepositorio asignaciones,
                                String nombreRol) {
        this.roles = roles;
        this.asignaciones = asignaciones;
        this.nombreRol = nombreRol;
    }

    public static RolPorDefectoService desdeConfiguracion() {
        return new RolPorDefectoService(
            new RolRepositorioJdbc(),
            new AsignacionRepositorioJdbc(),
            Configuracion.valor("ROL_POR_DEFECTO", NOMBRE_POR_DEFECTO));
    }

    public Optional<String> asignar(long usuarioId) {
        if (asignaciones.tieneRoles(usuarioId)) {
            return Optional.empty();
        }
        Rol rol = roles.porNombre(nombreRol).orElseThrow(() -> new DatosInvalidosException(
            "El rol por defecto " + nombreRol + " no existe en la base"));
        asignaciones.asignar(usuarioId, rol.id());
        return Optional.of(rol.nombre());
    }
}
