package cl.gestion.functions;

public final class MensajesCorreo {

    private MensajesCorreo() {
    }

    public static Correo bienvenida(String nombre) {
        return new Correo(
            "Bienvenida a la plataforma",
            """
            Hola %s,

            Tu cuenta fue creada correctamente.

            Ya puedes iniciar sesion con el correo que registraste.

            Sistema de Gestion de Usuarios y Roles
            """.formatted(nombre));
    }

    public static Correo recuperacion(String nombre, String token, int vigenciaMinutos) {
        return new Correo(
            "Recuperacion de contrasena",
            """
            Hola %s,

            Recibimos una solicitud para restablecer tu contrasena.

            Tu codigo es:

              %s

            Usalo en el endpoint de restablecer dentro de los proximos %d minutos.
            Pasado ese plazo tendras que solicitar uno nuevo.

            Si no fuiste tu, ignora este mensaje.

            Sistema de Gestion de Usuarios y Roles
            """.formatted(nombre, token, vigenciaMinutos));
    }
}
