package cl.gestion.functions;

public interface Mailer {

    void enviar(String destinatario, String asunto, String cuerpo);
}
