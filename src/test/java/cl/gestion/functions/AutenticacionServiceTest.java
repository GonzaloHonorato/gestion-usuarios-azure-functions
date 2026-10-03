package cl.gestion.functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutenticacionServiceTest {

    private CuentaRepositorioMemoria repositorio;
    private PublicadorFalso publicador;
    private AutenticacionService servicio;

    @BeforeEach
    void preparar() {
        repositorio = new CuentaRepositorioMemoria();
        publicador = new PublicadorFalso();
        servicio = new AutenticacionService(repositorio, publicador, 30);
        repositorio.registrar(1L, "ana@correo.cl", "Ana", "clave-segura-123", true, List.of("ADMIN"));
        repositorio.registrar(2L, "inactivo@correo.cl", "Juan", "clave-segura-123", false, List.of("USUARIO"));
    }

    @Test
    void autenticaConCredencialesCorrectas() {
        Autenticado resultado = servicio.autenticar("ana@correo.cl", "clave-segura-123");

        assertThat(resultado.usuarioId()).isEqualTo(1L);
        assertThat(resultado.roles()).containsExactly("ADMIN");
    }

    @Test
    void rechazaUnaContrasenaIncorrecta() {
        assertThatThrownBy(() -> servicio.autenticar("ana@correo.cl", "otra-clave"))
            .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void rechazaUnCorreoInexistenteConElMismoMensaje() {
        assertThatThrownBy(() -> servicio.autenticar("nadie@correo.cl", "clave-segura-123"))
            .isInstanceOf(CredencialesInvalidasException.class)
            .hasMessage("Credenciales invalidas");
    }

    @Test
    void rechazaUnaCuentaDesactivada() {
        assertThatThrownBy(() -> servicio.autenticar("inactivo@correo.cl", "clave-segura-123"))
            .isInstanceOf(CuentaDesactivadaException.class);
    }

    @Test
    void generaUnTokenDeRecuperacionYNotifica() {
        servicio.solicitarRecuperacion("ana@correo.cl");

        assertThat(repositorio.tokens).hasSize(1);
        assertThat(publicador.recuperaciones).containsExactly("ana@correo.cl");
    }

    @Test
    void noRevelaSiElCorreoExisteAlSolicitarRecuperacion() {
        servicio.solicitarRecuperacion("nadie@correo.cl");

        assertThat(repositorio.tokens).isEmpty();
        assertThat(publicador.recuperaciones).isEmpty();
    }

    @Test
    void restableceLaContrasenaConUnTokenValido() {
        servicio.solicitarRecuperacion("ana@correo.cl");
        String token = repositorio.tokens.get(0).token();

        servicio.restablecer(token, "clave-nueva-456");

        assertThat(servicio.autenticar("ana@correo.cl", "clave-nueva-456").usuarioId()).isEqualTo(1L);
    }

    @Test
    void invalidaElTokenTrasUsarlo() {
        servicio.solicitarRecuperacion("ana@correo.cl");
        String token = repositorio.tokens.get(0).token();
        servicio.restablecer(token, "clave-nueva-456");

        assertThatThrownBy(() -> servicio.restablecer(token, "otra-clave-789"))
            .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void rechazaUnTokenExpirado() {
        repositorio.tokens.add(new TokenRecuperacion(
            "vencido", 1L, Instant.now().minusSeconds(60), false));

        assertThatThrownBy(() -> servicio.restablecer("vencido", "clave-nueva-456"))
            .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void rechazaUnaContrasenaNuevaDemasiadoCorta() {
        servicio.solicitarRecuperacion("ana@correo.cl");
        String token = repositorio.tokens.get(0).token();

        assertThatThrownBy(() -> servicio.restablecer(token, "corta"))
            .isInstanceOf(DatosInvalidosException.class);
    }

    static class CuentaRepositorioMemoria implements CuentaRepositorio {
        final List<TokenRecuperacion> tokens = new ArrayList<>();
        private final Map<String, Object[]> cuentas = new HashMap<>();

        void registrar(long id, String email, String nombre, String password,
                       boolean activo, List<String> roles) {
            cuentas.put(email.toLowerCase(),
                new Object[]{id, nombre, Passwords.cifrar(password), activo, roles});
        }

        @Override public Optional<Cuenta> porEmail(String email) {
            Object[] datos = cuentas.get(email.toLowerCase());
            if (datos == null) {
                return Optional.empty();
            }
            return Optional.of(new Cuenta((Long) datos[0], email, (String) datos[1],
                (String) datos[2], (Boolean) datos[3], castRoles(datos[4])));
        }

        @SuppressWarnings("unchecked")
        private static List<String> castRoles(Object valor) {
            return (List<String>) valor;
        }

        @Override public void guardarToken(TokenRecuperacion token) {
            tokens.add(token);
        }

        @Override public Optional<TokenRecuperacion> porToken(String token) {
            return tokens.stream().filter(t -> t.token().equals(token)).findFirst();
        }

        @Override public void marcarTokenUsado(String token) {
            tokens.replaceAll(t -> t.token().equals(token)
                ? new TokenRecuperacion(t.token(), t.usuarioId(), t.expiraEn(), true) : t);
        }

        @Override public void cambiarPassword(long usuarioId, String passwordHash) {
            cuentas.replaceAll((email, datos) -> {
                if (datos[0].equals(usuarioId)) {
                    datos[2] = passwordHash;
                }
                return datos;
            });
        }
    }

    static class PublicadorFalso implements PublicadorEventos {
        final List<String> recuperaciones = new ArrayList<>();

        @Override public void usuarioCreado(long usuarioId, String email, String nombre) {
        }

        @Override public void recuperacionSolicitada(long usuarioId, String email,
                                                     String nombre, String token) {
            recuperaciones.add(email);
        }
    }
}
