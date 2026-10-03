package cl.gestion.functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DesasignacionRolServiceTest {

    private final List<String> operaciones = new ArrayList<>();
    private RolRepositorioMemoria roles;
    private AsignacionRepositorioMemoria asignaciones;
    private DesasignacionRolService servicio;
    private Rol supervisor;

    @BeforeEach
    void preparar() {
        roles = new RolRepositorioMemoria(operaciones);
        asignaciones = new AsignacionRepositorioMemoria(operaciones);
        supervisor = roles.crear("SUPERVISOR", null);
        servicio = new DesasignacionRolService(roles, asignaciones);
    }

    @Test
    void quitaElRolATodosLosUsuariosQueLoTenian() {
        asignaciones.asignar(1L, supervisor.id());
        asignaciones.asignar(2L, supervisor.id());

        assertThat(servicio.ejecutar(supervisor.id())).isEqualTo(2);
        assertThat(asignaciones.asignaciones).isEmpty();
    }

    @Test
    void eliminaElRolUnaVezLiberadoDeUsuarios() {
        asignaciones.asignar(1L, supervisor.id());

        servicio.ejecutar(supervisor.id());

        assertThat(roles.porId(supervisor.id())).isEmpty();
    }

    @Test
    void quitaLasAsignacionesAntesDeEliminarElRol() {
        asignaciones.asignar(1L, supervisor.id());

        servicio.ejecutar(supervisor.id());

        assertThat(operaciones).containsExactly(
            "quitar-rol:" + supervisor.id(), "eliminar-rol:" + supervisor.id());
    }

    @Test
    void noAfectaLasAsignacionesDeOtrosRoles() {
        Rol auditor = roles.crear("AUDITOR", null);
        asignaciones.asignar(1L, supervisor.id());
        asignaciones.asignar(1L, auditor.id());

        servicio.ejecutar(supervisor.id());

        assertThat(asignaciones.asignaciones)
            .containsExactly(new AsignacionRepositorioMemoria.Asignacion(1L, auditor.id()));
    }

    @Test
    void esIdempotenteSiElRolYaFueEliminado() {
        servicio.ejecutar(supervisor.id());

        assertThat(servicio.ejecutar(supervisor.id())).isZero();
    }
}
