package cl.gestion.functions;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

public class AutenticacionService {

    private static final int LARGO_MINIMO_PASSWORD = 8;
    private static final int BYTES_TOKEN = 32;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final CuentaRepositorio repositorio;
    private final PublicadorEventos publicador;
    private final int vigenciaMinutos;

    public AutenticacionService(CuentaRepositorio repositorio, PublicadorEventos publicador,
                                int vigenciaMinutos) {
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.vigenciaMinutos = vigenciaMinutos;
    }

    public Autenticado autenticar(String email, String password) {
        Cuenta cuenta = repositorio.porEmail(email).orElseThrow(CredencialesInvalidasException::new);
        if (!Passwords.coincide(password, cuenta.passwordHash())) {
            throw new CredencialesInvalidasException();
        }
        if (!cuenta.activo()) {
            throw new CuentaDesactivadaException();
        }
        return new Autenticado(cuenta.id(), cuenta.email(), cuenta.nombre(), cuenta.roles());
    }

    public void solicitarRecuperacion(String email) {
        Optional<Cuenta> cuenta = repositorio.porEmail(email);
        if (cuenta.isEmpty() || !cuenta.get().activo()) {
            return;
        }
        String token = generarToken();
        repositorio.guardarToken(new TokenRecuperacion(
            token, cuenta.get().id(), Instant.now().plus(vigenciaMinutos, ChronoUnit.MINUTES), false));
        publicador.recuperacionSolicitada(
            cuenta.get().id(), cuenta.get().email(), cuenta.get().nombre(), token);
    }

    public void restablecer(String token, String passwordNueva) {
        if (passwordNueva == null || passwordNueva.length() < LARGO_MINIMO_PASSWORD) {
            throw new DatosInvalidosException(
                "La contrasena debe tener al menos " + LARGO_MINIMO_PASSWORD + " caracteres");
        }
        TokenRecuperacion recuperacion = repositorio.porToken(token)
            .filter(TokenRecuperacion::vigente)
            .orElseThrow(TokenInvalidoException::new);

        repositorio.cambiarPassword(recuperacion.usuarioId(), Passwords.cifrar(passwordNueva));
        repositorio.marcarTokenUsado(token);
    }

    private static String generarToken() {
        byte[] bytes = new byte[BYTES_TOKEN];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
