package cl.gestion.functions;

public class AccesoDatosException extends RuntimeException {

    public AccesoDatosException(String operacion, Throwable causa) {
        super("Fallo al " + operacion + ": " + causa.getMessage(), causa);
    }
}
