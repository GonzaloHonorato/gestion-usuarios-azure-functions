package cl.gestion.functions;

public class CuentaDesactivadaException extends RuntimeException {

    public CuentaDesactivadaException() {
        super("La cuenta esta desactivada");
    }
}
