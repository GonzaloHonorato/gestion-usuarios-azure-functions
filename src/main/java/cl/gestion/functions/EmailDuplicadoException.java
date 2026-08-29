package cl.gestion.functions;

public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException(String email) {
        super("Ya existe un usuario con el correo " + email);
    }
}
