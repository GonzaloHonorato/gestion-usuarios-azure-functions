package cl.gestion.functions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepositorioJdbc implements UsuarioRepositorio {

    private static final String SELECCION = """
        SELECT u.ID, u.NOMBRE, u.APELLIDO, u.EMAIL, u.ACTIVO,
               LISTAGG(r.NOMBRE, ',') WITHIN GROUP (ORDER BY r.NOMBRE) AS ROLES
          FROM USUARIOS u
          LEFT JOIN USUARIO_ROL ur ON ur.USUARIO_ID = u.ID
          LEFT JOIN ROLES r ON r.ID = ur.ROL_ID
        """;

    @Override
    public List<Usuario> listar() {
        String sql = SELECCION + " GROUP BY u.ID, u.NOMBRE, u.APELLIDO, u.EMAIL, u.ACTIVO ORDER BY u.ID";
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet filas = sentencia.executeQuery()) {
            List<Usuario> usuarios = new ArrayList<>();
            while (filas.next()) {
                usuarios.add(mapear(filas));
            }
            return usuarios;
        } catch (SQLException ex) {
            throw new AccesoDatosException("listar usuarios", ex);
        }
    }

    @Override
    public Optional<Usuario> porId(long id) {
        String sql = SELECCION + " WHERE u.ID = ? GROUP BY u.ID, u.NOMBRE, u.APELLIDO, u.EMAIL, u.ACTIVO";
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next() ? Optional.of(mapear(filas)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("obtener el usuario " + id, ex);
        }
    }

    @Override
    public boolean existeEmail(String email) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT 1 FROM USUARIOS WHERE LOWER(EMAIL) = LOWER(?)")) {
            sentencia.setString(1, email);
            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next();
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("verificar el correo", ex);
        }
    }

    @Override
    public Usuario crear(CrearUsuario datos, String passwordHash) {
        try (Connection conexion = Conexion.abrir()) {
            conexion.setAutoCommit(false);
            try {
                long id = insertarUsuario(conexion, datos, passwordHash);
                asignarRoles(conexion, id, datos.roles());
                conexion.commit();
                return porIdEnConexion(conexion, id);
            } catch (SQLException ex) {
                conexion.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("crear el usuario", ex);
        }
    }

    @Override
    public boolean actualizar(long id, ActualizarUsuario datos) {
        try (Connection conexion = Conexion.abrir()) {
            conexion.setAutoCommit(false);
            try (PreparedStatement sentencia = conexion.prepareStatement(
                "UPDATE USUARIOS SET NOMBRE = ?, APELLIDO = ?, EMAIL = ? WHERE ID = ?")) {
                sentencia.setString(1, datos.nombre());
                sentencia.setString(2, datos.apellido());
                sentencia.setString(3, datos.email());
                sentencia.setLong(4, id);
                if (sentencia.executeUpdate() == 0) {
                    conexion.rollback();
                    return false;
                }
            }
            reemplazarRoles(conexion, id, datos.roles());
            conexion.commit();
            return true;
        } catch (SQLException ex) {
            throw new AccesoDatosException("actualizar el usuario " + id, ex);
        }
    }

    @Override
    public boolean desactivar(long id) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "UPDATE USUARIOS SET ACTIVO = 0 WHERE ID = ?")) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new AccesoDatosException("desactivar el usuario " + id, ex);
        }
    }

    public Optional<Credenciales> credencialesPorEmail(String email) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT ID, PASSWORD_HASH, ACTIVO FROM USUARIOS WHERE LOWER(EMAIL) = LOWER(?)")) {
            sentencia.setString(1, email);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Credenciales(
                    filas.getLong("ID"), filas.getString("PASSWORD_HASH"), filas.getInt("ACTIVO") == 1));
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("obtener credenciales", ex);
        }
    }

    private long insertarUsuario(Connection conexion, CrearUsuario datos, String passwordHash)
            throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
            "INSERT INTO USUARIOS (NOMBRE, APELLIDO, EMAIL, PASSWORD_HASH) VALUES (?, ?, ?, ?)",
            new String[]{"ID"})) {
            sentencia.setString(1, datos.nombre());
            sentencia.setString(2, datos.apellido());
            sentencia.setString(3, datos.email());
            sentencia.setString(4, passwordHash);
            sentencia.executeUpdate();
            try (ResultSet generado = sentencia.getGeneratedKeys()) {
                generado.next();
                return generado.getLong(1);
            }
        }
    }

    private void asignarRoles(Connection conexion, long usuarioId, List<Long> roles)
            throws SQLException {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        try (PreparedStatement sentencia = conexion.prepareStatement(
            "INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID) VALUES (?, ?)")) {
            for (Long rolId : roles) {
                sentencia.setLong(1, usuarioId);
                sentencia.setLong(2, rolId);
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }

    private void reemplazarRoles(Connection conexion, long usuarioId, List<Long> roles)
            throws SQLException {
        try (Statement borrado = conexion.createStatement()) {
            borrado.executeUpdate("DELETE FROM USUARIO_ROL WHERE USUARIO_ID = " + usuarioId);
        }
        asignarRoles(conexion, usuarioId, roles);
    }

    private Usuario porIdEnConexion(Connection conexion, long id) throws SQLException {
        String sql = SELECCION + " WHERE u.ID = ? GROUP BY u.ID, u.NOMBRE, u.APELLIDO, u.EMAIL, u.ACTIVO";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                filas.next();
                return mapear(filas);
            }
        }
    }

    private static Usuario mapear(ResultSet filas) throws SQLException {
        String roles = filas.getString("ROLES");
        return new Usuario(
            filas.getLong("ID"),
            filas.getString("NOMBRE"),
            filas.getString("APELLIDO"),
            filas.getString("EMAIL"),
            filas.getInt("ACTIVO") == 1,
            (roles == null || roles.isBlank()) ? List.of() : List.of(roles.split(",")));
    }

    public record Credenciales(long usuarioId, String passwordHash, boolean activo) {}
}
