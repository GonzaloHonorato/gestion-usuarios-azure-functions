package cl.gestion.functions;

public final class Rutas {

    private Rutas() {
    }

    public static String identificador(String ruta, String recurso) {
        if (ruta == null) {
            return null;
        }
        String marca = "/" + recurso;
        int posicion = ruta.indexOf(marca);
        if (posicion < 0) {
            return null;
        }
        String resto = ruta.substring(posicion + marca.length());
        if (!resto.startsWith("/")) {
            return null;
        }
        String valor = resto.substring(1);
        int barra = valor.indexOf('/');
        if (barra >= 0) {
            valor = valor.substring(0, barra);
        }
        return valor.isBlank() ? null : valor;
    }
}
