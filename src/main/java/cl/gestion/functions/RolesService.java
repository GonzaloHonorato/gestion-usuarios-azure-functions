package cl.gestion.functions;

import java.util.List;
import java.util.Optional;

public class RolesService {

    private final RolRepositorio repositorio;

    public RolesService(RolRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<Rol> listar() {
        return repositorio.listar();
    }

    public Optional<Rol> obtener(long id) {
        return repositorio.porId(id);
    }

    public Rol crear(DatosRol datos) {
        String nombre = normalizar(datos.nombre());
        if (repositorio.existeNombre(nombre)) {
            throw new NombreRolDuplicadoException(nombre);
        }
        return repositorio.crear(nombre, datos.descripcion());
    }

    public boolean actualizar(long id, DatosRol datos) {
        return repositorio.actualizar(id, normalizar(datos.nombre()), datos.descripcion());
    }

    public boolean eliminar(long id) {
        int usuarios = repositorio.usuariosCon(id);
        if (usuarios > 0) {
            throw new RolEnUsoException(usuarios);
        }
        return repositorio.eliminar(id);
    }

    private static String normalizar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("El nombre del rol es obligatorio");
        }
        return nombre.trim().toUpperCase();
    }
}
