package cl.gestion.functions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RolRepositorioJdbc implements RolRepositorio {

    @Override
    public List<Rol> listar() {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT ID, NOMBRE, DESCRIPCION FROM ROLES ORDER BY ID");
             ResultSet filas = sentencia.executeQuery()) {
            List<Rol> roles = new ArrayList<>();
            while (filas.next()) {
                roles.add(mapear(filas));
            }
            return roles;
        } catch (SQLException ex) {
            throw new AccesoDatosException("listar roles", ex);
        }
    }

    @Override
    public Optional<Rol> porId(long id) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT ID, NOMBRE, DESCRIPCION FROM ROLES WHERE ID = ?")) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next() ? Optional.of(mapear(filas)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("obtener el rol " + id, ex);
        }
    }

    @Override
    public boolean existeNombre(String nombre) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT 1 FROM ROLES WHERE UPPER(NOMBRE) = UPPER(?)")) {
            sentencia.setString(1, nombre);
            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next();
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("verificar el nombre del rol", ex);
        }
    }

    @Override
    public Rol crear(String nombre, String descripcion) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "INSERT INTO ROLES (NOMBRE, DESCRIPCION) VALUES (?, ?)", new String[]{"ID"})) {
            sentencia.setString(1, nombre);
            sentencia.setString(2, descripcion);
            sentencia.executeUpdate();
            try (ResultSet generado = sentencia.getGeneratedKeys()) {
                generado.next();
                return new Rol(generado.getLong(1), nombre, descripcion);
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("crear el rol", ex);
        }
    }

    @Override
    public boolean actualizar(long id, String nombre, String descripcion) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "UPDATE ROLES SET NOMBRE = ?, DESCRIPCION = ? WHERE ID = ?")) {
            sentencia.setString(1, nombre);
            sentencia.setString(2, descripcion);
            sentencia.setLong(3, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new AccesoDatosException("actualizar el rol " + id, ex);
        }
    }

    @Override
    public int usuariosCon(long id) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT COUNT(*) FROM USUARIO_ROL WHERE ROL_ID = ?")) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                filas.next();
                return filas.getInt(1);
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("contar usuarios del rol " + id, ex);
        }
    }

    @Override
    public boolean eliminar(long id) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM ROLES WHERE ID = ?")) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new AccesoDatosException("eliminar el rol " + id, ex);
        }
    }

    private static Rol mapear(ResultSet filas) throws SQLException {
        return new Rol(filas.getLong("ID"), filas.getString("NOMBRE"), filas.getString("DESCRIPCION"));
    }
}
