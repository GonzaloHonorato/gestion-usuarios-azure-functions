package cl.gestion.functions;

import java.util.List;
import java.util.Optional;

public interface RolRepositorio {

    List<Rol> listar();

    Optional<Rol> porId(long id);

    Optional<Rol> porNombre(String nombre);

    boolean existeNombre(String nombre);

    Rol crear(String nombre, String descripcion);

    boolean actualizar(long id, String nombre, String descripcion);

    int usuariosCon(long id);

    boolean eliminar(long id);
}
