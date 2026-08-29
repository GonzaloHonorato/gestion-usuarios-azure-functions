package cl.gestion.functions;

public class NombreRolDuplicadoException extends RuntimeException {

    public NombreRolDuplicadoException(String nombre) {
        super("Ya existe un rol con el nombre " + nombre);
    }
}
