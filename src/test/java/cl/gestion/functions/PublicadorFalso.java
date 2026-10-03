package cl.gestion.functions;

import java.util.ArrayList;
import java.util.List;

class PublicadorFalso implements PublicadorEventos {

    final List<String> creados = new ArrayList<>();
    final List<String> recuperaciones = new ArrayList<>();
    final List<String> rolesEliminados = new ArrayList<>();

    @Override public void usuarioCreado(long usuarioId, String email, String nombre) {
        creados.add(usuarioId + ":" + email);
    }

    @Override public void recuperacionSolicitada(long usuarioId, String email,
                                                 String nombre, String token) {
        recuperaciones.add(email);
    }

    @Override public void rolEliminado(long rolId, String nombre, int usuariosAfectados) {
        rolesEliminados.add(rolId + ":" + nombre + ":" + usuariosAfectados);
    }
}
