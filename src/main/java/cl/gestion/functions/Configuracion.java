package cl.gestion.functions;

public final class Configuracion {

    private Configuracion() {
    }

    public static String valor(String clave, String porDefecto) {
        String valor = System.getenv(clave);
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }

    public static String requerido(String clave) {
        String valor = System.getenv(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la configuracion " + clave);
        }
        return valor;
    }

    public static int entero(String clave, int porDefecto) {
        try {
            return Integer.parseInt(valor(clave, String.valueOf(porDefecto)));
        } catch (NumberFormatException ex) {
            return porDefecto;
        }
    }
}
