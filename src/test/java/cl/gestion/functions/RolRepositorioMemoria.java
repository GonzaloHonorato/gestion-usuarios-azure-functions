package cl.gestion.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class RolRepositorioMemoria implements RolRepositorio {

    private final List<Rol> roles = new ArrayList<>();
    final Map<Long, Integer> asignados = new HashMap<>();
    final List<String> operaciones;
    private long siguienteId = 1;

    RolRepositorioMemoria() {
        this(new ArrayList<>());
    }

    RolRepositorioMemoria(List<String> operaciones) {
        this.operaciones = operaciones;
    }

    @Override public List<Rol> listar() {
        return List.copyOf(roles);
    }

    @Override public Optional<Rol> porId(long id) {
        return roles.stream().filter(rol -> rol.id() == id).findFirst();
    }

    @Override public Optional<Rol> porNombre(String nombre) {
        return roles.stream().filter(rol -> rol.nombre().equalsIgnoreCase(nombre)).findFirst();
    }

    @Override public boolean existeNombre(String nombre) {
        return porNombre(nombre).isPresent();
    }

    @Override public Rol crear(String nombre, String descripcion) {
        Rol nuevo = new Rol(siguienteId++, nombre, descripcion);
        roles.add(nuevo);
        return nuevo;
    }

    @Override public boolean actualizar(long id, String nombre, String descripcion) {
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).id() == id) {
                roles.set(i, new Rol(id, nombre, descripcion));
                return true;
            }
        }
        return false;
    }

    @Override public int usuariosCon(long id) {
        return asignados.getOrDefault(id, 0);
    }

    @Override public boolean eliminar(long id) {
        operaciones.add("eliminar-rol:" + id);
        return roles.removeIf(rol -> rol.id() == id);
    }
}
