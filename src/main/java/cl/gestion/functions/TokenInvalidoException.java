package cl.gestion.functions;

public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException() {
        super("El token no es valido o ya expiro");
    }
}
