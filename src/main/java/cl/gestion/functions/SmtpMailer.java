package cl.gestion.functions;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class SmtpMailer implements Mailer {

    private static final String PUERTO_TLS_IMPLICITO = "465";

    private final Session sesion;
    private final String remitente;

    public SmtpMailer(String host, String puerto, String usuario, String password,
                      String remitente, boolean tlsImplicito) {
        this.remitente = remitente;
        this.sesion = Session.getInstance(
            propiedades(host, puerto, tlsImplicito),
            new Authenticator() {
                @Override protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(usuario, password);
                }
            });
    }

    static Properties propiedades(String host, String puerto, boolean tlsImplicito) {
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", host);
        propiedades.put("mail.smtp.port", puerto);
        propiedades.put("mail.smtp.auth", "true");
        if (tlsImplicito) {
            propiedades.put("mail.smtp.ssl.enable", "true");
        } else {
            propiedades.put("mail.smtp.starttls.enable", "true");
        }
        return propiedades;
    }

    static boolean usaTlsImplicito(String puerto, String ajusteExplicito) {
        if (ajusteExplicito != null && !ajusteExplicito.isBlank()) {
            return Boolean.parseBoolean(ajusteExplicito.trim());
        }
        return PUERTO_TLS_IMPLICITO.equals(puerto);
    }

    public static SmtpMailer desdeConfiguracion() {
        String puerto = Configuracion.valor("SMTP_PORT", "587");
        return new SmtpMailer(
            Configuracion.requerido("SMTP_HOST"),
            puerto,
            Configuracion.requerido("SMTP_USER"),
            Configuracion.requerido("SMTP_PASSWORD"),
            Configuracion.valor("SMTP_FROM", Configuracion.requerido("SMTP_USER")),
            usaTlsImplicito(puerto, System.getenv("SMTP_SSL")));
    }

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        try {
            MimeMessage mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(remitente));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto, "UTF-8");
            mensaje.setText(cuerpo, "UTF-8");
            Transport.send(mensaje);
        } catch (Exception ex) {
            throw new RuntimeException(
                "Fallo el envio SMTP a " + destinatario + ": " + ex.getMessage(), ex);
        }
    }
}
