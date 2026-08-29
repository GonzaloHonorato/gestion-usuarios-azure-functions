package cl.gestion.functions;

import java.util.Optional;

public interface CuentaRepositorio {

    Optional<Cuenta> porEmail(String email);

    void guardarToken(TokenRecuperacion token);

    Optional<TokenRecuperacion> porToken(String token);

    void marcarTokenUsado(String token);

    void cambiarPassword(long usuarioId, String passwordHash);
}
