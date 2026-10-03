package cl.gestion.functions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AsignacionRepositorioJdbc implements AsignacionRepositorio {

    @Override
    public boolean tieneRoles(long usuarioId) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "SELECT 1 FROM USUARIO_ROL WHERE USUARIO_ID = ? FETCH FIRST 1 ROWS ONLY")) {
            sentencia.setLong(1, usuarioId);
            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next();
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("revisar los roles del usuario " + usuarioId, ex);
        }
    }

    @Override
    public void asignar(long usuarioId, long rolId) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID) VALUES (?, ?)")) {
            sentencia.setLong(1, usuarioId);
            sentencia.setLong(2, rolId);
            sentencia.executeUpdate();
        } catch (SQLException ex) {
            throw new AccesoDatosException(
                "asignar el rol " + rolId + " al usuario " + usuarioId, ex);
        }
    }

    @Override
    public int quitarRol(long rolId) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "DELETE FROM USUARIO_ROL WHERE ROL_ID = ?")) {
            sentencia.setLong(1, rolId);
            return sentencia.executeUpdate();
        } catch (SQLException ex) {
            throw new AccesoDatosException("quitar el rol " + rolId + " a sus usuarios", ex);
        }
    }
}
