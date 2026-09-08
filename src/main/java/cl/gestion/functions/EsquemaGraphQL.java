package cl.gestion.functions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class EsquemaGraphQL {

    private EsquemaGraphQL() {
    }

    public static String leer(String recurso) {
        try (InputStream entrada = EsquemaGraphQL.class.getClassLoader().getResourceAsStream(recurso)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontro el esquema " + recurso);
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer el esquema " + recurso, ex);
        }
    }

    public static long identificador(Object valor) {
        if (valor instanceof Number numero) {
            return numero.longValue();
        }
        return Long.parseLong(String.valueOf(valor));
    }
}
