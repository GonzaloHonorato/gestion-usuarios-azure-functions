package cl.gestion.functions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class CuentaRepositorioJdbc implements CuentaRepositorio {

    @Override
    public Optional<Cuenta> porEmail(String email) {
        String sql = """
            SELECT u.ID, u.EMAIL, u.NOMBRE, u.PASSWORD_HASH, u.ACTIVO,
                   LISTAGG(r.NOMBRE, ',') WITHIN GROUP (ORDER BY r.NOMBRE) AS ROLES
              FROM USUARIOS u
              LEFT JOIN USUARIO_ROL ur ON ur.USUARIO_ID = u.ID
              LEFT JOIN ROLES r ON r.ID = ur.ROL_ID
             WHERE LOWER(u.EMAIL) = LOWER(?)
             GROUP BY u.ID, u.EMAIL, u.NOMBRE, u.PASSWORD_HASH, u.ACTIVO
            """;
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, email);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    return Optional.empty();
                }
                String roles = filas.getString("ROLES");
                return Optional.of(new Cuenta(
                    filas.getLong("ID"),
                    filas.getString("EMAIL"),
                    filas.getString("NOMBRE"),
                    filas.getString("PASSWORD_HASH"),
                    filas.getInt("ACTIVO") == 1,
                    (roles == null || roles.isBlank()) ? List.of() : List.of(roles.split(","))));
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("obtener la cuenta", ex);
        }
    }

    @Override
    public void guardarToken(TokenRecuperacion token) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "INSERT INTO TOKEN_RECUPERACION (USUARIO_ID, TOKEN, EXPIRA_EN) VALUES (?, ?, ?)")) {
            sentencia.setLong(1, token.usuarioId());
            sentencia.setString(2, token.token());
            sentencia.setTimestamp(3, Timestamp.from(token.expiraEn()));
            sentencia.executeUpdate();
        } catch (SQLException ex) {
            throw new AccesoDatosException("guardar el token de recuperacion", ex);
        }
    }

    @Override
    public Optional<TokenRecuperacion> porToken(String token) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT USUARIO_ID, TOKEN, EXPIRA_EN, USADO FROM TOKEN_RECUPERACION WHERE TOKEN = ?")) {
            sentencia.setString(1, token);
            try (ResultSet filas = sentencia.executeQuery()) {
                if (!filas.next()) {
                    return Optional.empty();
                }
                return Optional.of(new TokenRecuperacion(
                    filas.getString("TOKEN"),
                    filas.getLong("USUARIO_ID"),
                    filas.getTimestamp("EXPIRA_EN").toInstant(),
                    filas.getInt("USADO") == 1));
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("obtener el token de recuperacion", ex);
        }
    }

    @Override
    public void marcarTokenUsado(String token) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "UPDATE TOKEN_RECUPERACION SET USADO = 1 WHERE TOKEN = ?")) {
            sentencia.setString(1, token);
            sentencia.executeUpdate();
        } catch (SQLException ex) {
            throw new AccesoDatosException("marcar el token como usado", ex);
        }
    }

    @Override
    public void cambiarPassword(long usuarioId, String passwordHash) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "UPDATE USUARIOS SET PASSWORD_HASH = ? WHERE ID = ?")) {
            sentencia.setString(1, passwordHash);
            sentencia.setLong(2, usuarioId);
            sentencia.executeUpdate();
        } catch (SQLException ex) {
            throw new AccesoDatosException("cambiar la contrasena", ex);
        }
    }
}
