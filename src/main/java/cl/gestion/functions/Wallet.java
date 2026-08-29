package cl.gestion.functions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Wallet {

    private Wallet() {
    }

    public static void extraer(String base64, Path destino) {
        if (base64 == null || base64.isBlank()) {
            throw new IllegalStateException("La wallet no fue proporcionada");
        }

        byte[] contenido;
        try {
            contenido = Base64.getDecoder().decode(base64.replaceAll("\\s", ""));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("La wallet no esta codificada en base64 valido", ex);
        }

        Path raiz = destino.toAbsolutePath().normalize();
        try {
            Files.createDirectories(raiz);
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(contenido))) {
                ZipEntry entrada;
                boolean alguno = false;
                while ((entrada = zip.getNextEntry()) != null) {
                    if (entrada.isDirectory()) {
                        continue;
                    }
                    Path archivo = raiz.resolve(entrada.getName()).normalize();
                    if (!archivo.startsWith(raiz)) {
                        throw new IllegalStateException(
                            "La wallet contiene una ruta fuera del directorio: " + entrada.getName());
                    }
                    Files.createDirectories(archivo.getParent());
                    copiar(zip, archivo);
                    alguno = true;
                }
                if (!alguno) {
                    throw new IllegalStateException("La wallet no contiene archivos");
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo extraer la wallet: " + ex.getMessage(), ex);
        }
    }

    private static void copiar(InputStream origen, Path destino) throws IOException {
        Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
    }
}
