package cl.gestion.functions;

public interface PublicadorEventos {

    void usuarioCreado(long usuarioId, String email, String nombre);

    void recuperacionSolicitada(long usuarioId, String email, String nombre, String token);
}
