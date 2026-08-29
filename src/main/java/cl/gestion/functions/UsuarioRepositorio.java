package cl.gestion.functions;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio {

    List<Usuario> listar();

    Optional<Usuario> porId(long id);

    boolean existeEmail(String email);

    Usuario crear(CrearUsuario datos, String passwordHash);

    boolean actualizar(long id, ActualizarUsuario datos);

    boolean desactivar(long id);
}
