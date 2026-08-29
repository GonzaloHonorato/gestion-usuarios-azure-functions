package cl.gestion.functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuariosServiceTest {

    private UsuarioRepositorioMemoria repositorio;
    private NotificadorFalso notificador;
    private UsuariosService servicio;

    @BeforeEach
    void preparar() {
        repositorio = new UsuarioRepositorioMemoria();
        notificador = new NotificadorFalso();
        servicio = new UsuariosService(repositorio, notificador);
    }

    private CrearUsuario datosValidos() {
        return new CrearUsuario("Ana", "Soto", "ana@correo.cl", "clave-segura-123", List.of(1L));
    }

    @Test
    void creaUnUsuarioConSuIdentificador() {
        Usuario creado = servicio.crear(datosValidos());

        assertThat(creado.id()).isNotNull();
        assertThat(creado.email()).isEqualTo("ana@correo.cl");
        assertThat(creado.activo()).isTrue();
    }

    @Test
    void guardaLaContrasenaCifrada() {
        servicio.crear(datosValidos());

        String hash = repositorio.hashPorEmail("ana@correo.cl").orElseThrow();
        assertThat(hash).isNotEqualTo("clave-segura-123");
        assertThat(Passwords.coincide("clave-segura-123", hash)).isTrue();
    }

    @Test
    void envíaCorreoDeBienvenidaAlCrear() {
        servicio.crear(datosValidos());

        assertThat(notificador.bienvenidas).containsExactly("ana@correo.cl");
    }

    @Test
    void rechazaUnEmailYaRegistrado() {
        servicio.crear(datosValidos());

        assertThatThrownBy(() -> servicio.crear(datosValidos()))
            .isInstanceOf(EmailDuplicadoException.class);
    }

    @Test
    void rechazaDatosIncompletos() {
        assertThatThrownBy(() -> servicio.crear(
            new CrearUsuario("", "Soto", "ana@correo.cl", "clave-segura-123", List.of())))
            .isInstanceOf(DatosInvalidosException.class);

        assertThatThrownBy(() -> servicio.crear(
            new CrearUsuario("Ana", "Soto", "no-es-email", "clave-segura-123", List.of())))
            .isInstanceOf(DatosInvalidosException.class);

        assertThatThrownBy(() -> servicio.crear(
            new CrearUsuario("Ana", "Soto", "ana@correo.cl", "corta", List.of())))
            .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void listaLosUsuariosRegistrados() {
        servicio.crear(datosValidos());
        servicio.crear(new CrearUsuario("Juan", "Perez", "juan@correo.cl", "otra-clave-123", List.of()));

        assertThat(servicio.listar()).hasSize(2);
    }

    @Test
    void obtieneUnUsuarioPorSuIdentificador() {
        Usuario creado = servicio.crear(datosValidos());

        assertThat(servicio.obtener(creado.id())).isPresent();
        assertThat(servicio.obtener(9999L)).isEmpty();
    }

    @Test
    void actualizaLosDatosDeUnUsuario() {
        Usuario creado = servicio.crear(datosValidos());

        boolean actualizado = servicio.actualizar(creado.id(),
            new ActualizarUsuario("Ana Maria", "Soto Vera", "ana.maria@correo.cl", List.of(2L)));

        assertThat(actualizado).isTrue();
        assertThat(servicio.obtener(creado.id()).orElseThrow().nombre()).isEqualTo("Ana Maria");
    }

    @Test
    void informaCuandoElUsuarioAActualizarNoExiste() {
        boolean actualizado = servicio.actualizar(9999L,
            new ActualizarUsuario("Ana", "Soto", "ana@correo.cl", List.of()));

        assertThat(actualizado).isFalse();
    }

    @Test
    void laBajaEsLogicaYNoEliminaElRegistro() {
        Usuario creado = servicio.crear(datosValidos());

        assertThat(servicio.desactivar(creado.id())).isTrue();

        Usuario tras = servicio.obtener(creado.id()).orElseThrow();
        assertThat(tras.activo()).isFalse();
    }

    static class UsuarioRepositorioMemoria implements UsuarioRepositorio {
        private final List<Usuario> usuarios = new ArrayList<>();
        private final List<String> hashes = new ArrayList<>();
        private long siguienteId = 1;

        @Override public List<Usuario> listar() {
            return List.copyOf(usuarios);
        }

        @Override public Optional<Usuario> porId(long id) {
            return usuarios.stream().filter(u -> u.id() == id).findFirst();
        }

        @Override public boolean existeEmail(String email) {
            return usuarios.stream().anyMatch(u -> u.email().equalsIgnoreCase(email));
        }

        @Override public Usuario crear(CrearUsuario datos, String passwordHash) {
            Usuario nuevo = new Usuario(siguienteId++, datos.nombre(), datos.apellido(),
                datos.email(), true, List.of());
            usuarios.add(nuevo);
            hashes.add(datos.email() + "::" + passwordHash);
            return nuevo;
        }

        @Override public boolean actualizar(long id, ActualizarUsuario datos) {
            return reemplazar(id, u -> new Usuario(u.id(), datos.nombre(), datos.apellido(),
                datos.email(), u.activo(), List.of()));
        }

        @Override public boolean desactivar(long id) {
            return reemplazar(id, u -> new Usuario(u.id(), u.nombre(), u.apellido(),
                u.email(), false, u.roles()));
        }

        private boolean reemplazar(long id, java.util.function.UnaryOperator<Usuario> cambio) {
            for (int i = 0; i < usuarios.size(); i++) {
                if (usuarios.get(i).id() == id) {
                    usuarios.set(i, cambio.apply(usuarios.get(i)));
                    return true;
                }
            }
            return false;
        }

        Optional<String> hashPorEmail(String email) {
            return hashes.stream()
                .filter(h -> h.startsWith(email + "::"))
                .map(h -> h.substring(email.length() + 2))
                .findFirst();
        }
    }

    static class NotificadorFalso implements Notificador {
        final List<String> bienvenidas = new ArrayList<>();

        @Override public void bienvenida(String email, String nombre) {
            bienvenidas.add(email);
        }

        @Override public void recuperacion(String email, String nombre, String token) {
        }
    }
}
