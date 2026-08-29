package cl.gestion.functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTest {

    private static String zipEnBase64(Map<String, String> archivos) throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(salida)) {
            for (Map.Entry<String, String> archivo : archivos.entrySet()) {
                zip.putNextEntry(new ZipEntry(archivo.getKey()));
                zip.write(archivo.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return Base64.getEncoder().encodeToString(salida.toByteArray());
    }

    @Test
    void extraeLosArchivosDeLaWallet(@TempDir Path destino) throws Exception {
        String base64 = zipEnBase64(Map.of(
            "tnsnames.ora", "gestionusuarios_high = (description=...)",
            "cwallet.sso", "contenido-binario",
            "ojdbc.properties", "oracle.net.wallet_location=..."));

        Wallet.extraer(base64, destino);

        assertThat(destino.resolve("tnsnames.ora")).exists();
        assertThat(destino.resolve("cwallet.sso")).exists();
        assertThat(Files.readString(destino.resolve("tnsnames.ora")))
            .contains("gestionusuarios_high");
    }

    @Test
    void ignoraLasRutasQueEscapanDelDirectorio(@TempDir Path destino) throws Exception {
        String base64 = zipEnBase64(Map.of("../../fuera.txt", "contenido"));

        assertThatThrownBy(() -> Wallet.extraer(base64, destino))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ruta");

        assertThat(destino.getParent().resolve("fuera.txt")).doesNotExist();
    }

    @Test
    void rechazaUnaWalletVacia(@TempDir Path destino) {
        assertThatThrownBy(() -> Wallet.extraer("", destino))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Wallet.extraer(null, destino))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rechazaUnBase64Invalido(@TempDir Path destino) {
        assertThatThrownBy(() -> Wallet.extraer("esto-no-es-base64-valido!!!", destino))
            .isInstanceOf(IllegalStateException.class);
    }
}
