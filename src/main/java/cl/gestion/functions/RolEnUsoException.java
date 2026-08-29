package cl.gestion.functions;

public class RolEnUsoException extends RuntimeException {

    public RolEnUsoException(int usuarios) {
        super("El rol esta asignado a " + usuarios + " usuario(s) y no se puede eliminar");
    }
}
