package cl.gestion.functions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Conexion {

    private static volatile Path directorioWallet;

    private Conexion() {
    }

    public static Connection abrir() {
        Properties propiedades = new Properties();
        propiedades.put("user", Configuracion.requerido("ORACLE_USER"));
        propiedades.put("password", Configuracion.requerido("ORACLE_PASSWORD"));
        propiedades.put("oracle.net.tns_admin", walletPreparada().toString());
        propiedades.put("oracle.net.ssl_server_dn_match", "true");
        try {
            return DriverManager.getConnection(Configuracion.requerido("ORACLE_URL"), propiedades);
        } catch (SQLException ex) {
            throw new IllegalStateException(
                "No se pudo conectar a la base de datos: " + ex.getMessage(), ex);
        }
    }

    private static Path walletPreparada() {
        Path actual = directorioWallet;
        if (actual != null && Files.isDirectory(actual)) {
            return actual;
        }
        synchronized (Conexion.class) {
            if (directorioWallet == null || !Files.isDirectory(directorioWallet)) {
                Path destino = Path.of(System.getProperty("java.io.tmpdir"), "oracle-wallet");
                Wallet.extraer(Configuracion.requerido("ORACLE_WALLET_BASE64"), destino);
                directorioWallet = destino;
            }
            return directorioWallet;
        }
    }
}
