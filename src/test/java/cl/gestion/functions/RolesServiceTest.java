package cl.gestion.functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RolesServiceTest {

    private RolRepositorioMemoria repositorio;
    private PublicadorFalso publicador;
    private RolesService servicio;

    @BeforeEach
    void preparar() {
        repositorio = new RolRepositorioMemoria();
        publicador = new PublicadorFalso();
        servicio = new RolesService(repositorio, publicador);
    }

    @Test
    void creaUnRol() {
        Rol creado = servicio.crear(new DatosRol("supervisor", "Supervisa operaciones"));

        assertThat(creado.id()).isNotNull();
        assertThat(creado.descripcion()).isEqualTo("Supervisa operaciones");
    }

    @Test
    void normalizaElNombreAMayusculas() {
        assertThat(servicio.crear(new DatosRol("supervisor", null)).nombre()).isEqualTo("SUPERVISOR");
        assertThat(servicio.crear(new DatosRol("  auditor  ", null)).nombre()).isEqualTo("AUDITOR");
    }

    @Test
    void rechazaUnNombreRepetidoSinImportarMayusculas() {
        servicio.crear(new DatosRol("supervisor", null));

        assertThatThrownBy(() -> servicio.crear(new DatosRol("SUPERVISOR", null)))
            .isInstanceOf(NombreRolDuplicadoException.class);
    }

    @Test
    void rechazaUnNombreVacio() {
        assertThatThrownBy(() -> servicio.crear(new DatosRol("   ", null)))
            .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void listaYObtieneRoles() {
        Rol creado = servicio.crear(new DatosRol("supervisor", null));

        assertThat(servicio.listar()).hasSize(1);
        assertThat(servicio.obtener(creado.id())).isPresent();
        assertThat(servicio.obtener(999L)).isEmpty();
    }

    @Test
    void actualizaUnRol() {
        Rol creado = servicio.crear(new DatosRol("supervisor", "vieja"));

        assertThat(servicio.actualizar(creado.id(), new DatosRol("supervisor", "nueva"))).isTrue();
        assertThat(servicio.obtener(creado.id()).orElseThrow().descripcion()).isEqualTo("nueva");
    }

    @Test
    void informaCuandoElRolAActualizarNoExiste() {
        assertThat(servicio.actualizar(999L, new DatosRol("otro", null))).isFalse();
    }

    @Test
    void publicaElEventoAlPedirLaEliminacionDeUnRol() {
        Rol creado = servicio.crear(new DatosRol("supervisor", null));

        assertThat(servicio.eliminar(creado.id())).isTrue();
        assertThat(publicador.rolesEliminados).containsExactly(creado.id() + ":SUPERVISOR:0");
    }

    @Test
    void dejaElRolEnLaBaseHastaQueElConsumidorLoProcese() {
        Rol creado = servicio.crear(new DatosRol("supervisor", null));

        servicio.eliminar(creado.id());

        assertThat(servicio.obtener(creado.id())).isPresent();
    }

    @Test
    void anunciaCuantosUsuariosQuedaranSinEseRol() {
        Rol creado = servicio.crear(new DatosRol("supervisor", null));
        repositorio.asignados.put(creado.id(), 3);

        servicio.eliminar(creado.id());

        assertThat(publicador.rolesEliminados).containsExactly(creado.id() + ":SUPERVISOR:3");
    }

    @Test
    void informaCuandoElRolAEliminarNoExiste() {
        assertThat(servicio.eliminar(999L)).isFalse();
        assertThat(publicador.rolesEliminados).isEmpty();
    }
}
