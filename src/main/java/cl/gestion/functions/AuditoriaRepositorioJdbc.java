package cl.gestion.functions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaRepositorioJdbc implements AuditoriaRepositorio {

    @Override
    public void registrar(EventoRecibido evento) {
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(
                 "INSERT INTO EVENTO_AUDITORIA (ID_EVENTO, TIPO, SUBJECT, INSTANTE_EVENTO, DATOS) "
                 + "VALUES (?, ?, ?, ?, ?)")) {
            sentencia.setString(1, evento.id());
            sentencia.setString(2, evento.eventType());
            sentencia.setString(3, evento.subject());
            sentencia.setString(4, evento.eventTime());
            sentencia.setString(5, evento.data() == null ? "{}" : evento.data().toString());
            sentencia.executeUpdate();
        } catch (SQLException ex) {
            throw new AccesoDatosException("registrar el evento en auditoria", ex);
        }
    }

    @Override
    public List<EventoAuditado> ultimos(int cantidad) {
        String sql = """
            SELECT ID, ID_EVENTO, TIPO, SUBJECT, INSTANTE_EVENTO, DATOS
              FROM EVENTO_AUDITORIA
             ORDER BY ID DESC
             FETCH FIRST ? ROWS ONLY
            """;
        try (Connection conexion = Conexion.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, cantidad);
            try (ResultSet filas = sentencia.executeQuery()) {
                List<EventoAuditado> eventos = new ArrayList<>();
                while (filas.next()) {
                    eventos.add(new EventoAuditado(
                        filas.getLong("ID"),
                        filas.getString("ID_EVENTO"),
                        filas.getString("TIPO"),
                        filas.getString("SUBJECT"),
                        filas.getString("INSTANTE_EVENTO"),
                        filas.getString("DATOS")));
                }
                return eventos;
            }
        } catch (SQLException ex) {
            throw new AccesoDatosException("consultar la auditoria", ex);
        }
    }
}
