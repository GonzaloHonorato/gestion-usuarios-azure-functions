package cl.gestion.functions;

public interface Notificador {

    void bienvenida(String email, String nombre);

    void recuperacion(String email, String nombre, String token);
}
