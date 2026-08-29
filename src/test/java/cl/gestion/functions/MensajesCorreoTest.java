package cl.gestion.functions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MensajesCorreoTest {

    @Test
    void elCorreoDeBienvenidaSaludaPorNombre() {
        Correo correo = MensajesCorreo.bienvenida("Ana");

        assertThat(correo.asunto()).contains("Bienvenida");
        assertThat(correo.cuerpo()).contains("Ana");
    }

    @Test
    void elCorreoDeBienvenidaNoIncluyeContrasenas() {
        Correo correo = MensajesCorreo.bienvenida("Ana");

        assertThat(correo.cuerpo().toLowerCase())
            .doesNotContain("contrasena:")
            .doesNotContain("password");
    }

    @Test
    void elCorreoDeRecuperacionLlevaElToken() {
        Correo correo = MensajesCorreo.recuperacion("Ana", "tok-123", 30);

        assertThat(correo.cuerpo()).contains("tok-123");
        assertThat(correo.cuerpo()).contains("30");
    }

    @Test
    void elCorreoDeRecuperacionExplicaComoUsarlo() {
        Correo correo = MensajesCorreo.recuperacion("Ana", "tok-123", 30);

        assertThat(correo.cuerpo()).contains("restablecer");
    }

    @Test
    void elPuerto465UsaTlsImplicito() {
        assertThat(SmtpMailer.usaTlsImplicito("465", null)).isTrue();
        assertThat(SmtpMailer.usaTlsImplicito("587", null)).isFalse();
        assertThat(SmtpMailer.usaTlsImplicito("587", "true")).isTrue();
    }
}
