package cl.gestion.functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RolesServiceTest {

    private RolRepositorioMemoria repositorio;
    private RolesService servicio;

    @BeforeEach
    void preparar() {
        repositorio = new RolRepositorioMemoria();
        servicio = new RolesService(repositorio);
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
    void eliminaUnRolSinUsuariosAsignados() {
        Rol creado = servicio.crear(new DatosRol("supervisor", null));

        assertThat(servicio.eliminar(creado.id())).isTrue();
        assertThat(servicio.obtener(creado.id())).isEmpty();
    }

    @Test
    void impideEliminarUnRolAsignadoAUsuarios() {
        Rol creado = servicio.crear(new DatosRol("supervisor", null));
        repositorio.asignados.put(creado.id(), 3);

        assertThatThrownBy(() -> servicio.eliminar(creado.id()))
            .isInstanceOf(RolEnUsoException.class)
            .hasMessageContaining("3");
    }

    static class RolRepositorioMemoria implements RolRepositorio {
        private final List<Rol> roles = new ArrayList<>();
        final Map<Long, Integer> asignados = new HashMap<>();
        private long siguienteId = 1;

        @Override public List<Rol> listar() {
            return List.copyOf(roles);
        }

        @Override public Optional<Rol> porId(long id) {
            return roles.stream().filter(r -> r.id() == id).findFirst();
        }

        @Override public boolean existeNombre(String nombre) {
            return roles.stream().anyMatch(r -> r.nombre().equalsIgnoreCase(nombre));
        }

        @Override public Rol crear(String nombre, String descripcion) {
            Rol nuevo = new Rol(siguienteId++, nombre, descripcion);
            roles.add(nuevo);
            return nuevo;
        }

        @Override public boolean actualizar(long id, String nombre, String descripcion) {
            for (int i = 0; i < roles.size(); i++) {
                if (roles.get(i).id() == id) {
                    roles.set(i, new Rol(id, nombre, descripcion));
                    return true;
                }
            }
            return false;
        }

        @Override public int usuariosCon(long id) {
            return asignados.getOrDefault(id, 0);
        }

        @Override public boolean eliminar(long id) {
            return roles.removeIf(r -> r.id() == id);
        }
    }
}
