package cl.gestion.functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RolPorDefectoServiceTest {

    private RolRepositorioMemoria roles;
    private AsignacionRepositorioMemoria asignaciones;
    private RolPorDefectoService servicio;

    @BeforeEach
    void preparar() {
        roles = new RolRepositorioMemoria();
        asignaciones = new AsignacionRepositorioMemoria();
        roles.crear("USUARIO", "Acceso a su propio perfil");
        servicio = new RolPorDefectoService(roles, asignaciones, "USUARIO");
    }

    @Test
    void asignaElRolPorDefectoAlUsuarioQueLlegaSinRoles() {
        Optional<String> asignado = servicio.asignar(5L);

        assertThat(asignado).contains("USUARIO");
        assertThat(asignaciones.asignaciones)
            .containsExactly(new AsignacionRepositorioMemoria.Asignacion(5L, 1L));
    }

    @Test
    void noAsignaNadaSiElUsuarioYaTieneRoles() {
        Rol admin = roles.crear("ADMIN", null);
        asignaciones.asignar(5L, admin.id());

        assertThat(servicio.asignar(5L)).isEmpty();
        assertThat(asignaciones.asignaciones).hasSize(1);
    }

    @Test
    void respetaElNombreDeRolConfigurado() {
        roles.crear("OPERADOR", null);

        Optional<String> asignado =
            new RolPorDefectoService(roles, asignaciones, "OPERADOR").asignar(9L);

        assertThat(asignado).contains("OPERADOR");
    }

    @Test
    void reclamaSiElRolPorDefectoNoExisteEnLaBase() {
        RolPorDefectoService sinRol =
            new RolPorDefectoService(roles, asignaciones, "INEXISTENTE");

        assertThatThrownBy(() -> sinRol.asignar(5L))
            .isInstanceOf(DatosInvalidosException.class)
            .hasMessageContaining("INEXISTENTE");
    }

    @Test
    void esIdempotenteFrenteAUnaReentregaDelEvento() {
        servicio.asignar(5L);
        servicio.asignar(5L);

        assertThat(asignaciones.asignaciones).hasSize(1);
    }
}
