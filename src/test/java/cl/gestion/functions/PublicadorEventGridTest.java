package cl.gestion.functions;

import com.azure.messaging.eventgrid.EventGridEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PublicadorEventGridTest {

    @Test
    void componeElEventoDeUsuarioCreado() {
        EventGridEvent evento = PublicadorEventGrid.eventoUsuarioCreado(
            12L, "ana@correo.cl", "Ana");

        assertThat(evento.getEventType()).isEqualTo(TipoEvento.USUARIO_CREADO);
        assertThat(evento.getSubject()).isEqualTo("usuarios/12");
        assertThat(evento.getDataVersion()).isEqualTo("1.0");
        assertThat(evento.getData().toString()).contains("ana@correo.cl").contains("Ana");
    }

    @Test
    void componeElEventoDeRecuperacion() {
        EventGridEvent evento = PublicadorEventGrid.eventoRecuperacionSolicitada(
            7L, "juan@correo.cl", "Juan", "tok-abc");

        assertThat(evento.getEventType()).isEqualTo(TipoEvento.RECUPERACION_SOLICITADA);
        assertThat(evento.getSubject()).isEqualTo("usuarios/7/recuperacion");
        assertThat(evento.getData().toString()).contains("tok-abc");
    }

    @Test
    void componeElEventoDeRolEliminado() {
        EventGridEvent evento = PublicadorEventGrid.eventoRolEliminado(5L, "SUPERVISOR", 3);

        assertThat(evento.getEventType()).isEqualTo(TipoEvento.ROL_ELIMINADO);
        assertThat(evento.getSubject()).isEqualTo("roles/5");
        assertThat(evento.getDataVersion()).isEqualTo("1.0");
        assertThat(evento.getData().toString()).contains("SUPERVISOR").contains("3");
    }

    @Test
    void elAsuntoIdentificaAlUsuarioAfectado() {
        assertThat(PublicadorEventGrid.eventoUsuarioCreado(1L, "a@b.cl", "A").getSubject())
            .isEqualTo("usuarios/1");
        assertThat(PublicadorEventGrid.eventoUsuarioCreado(999L, "a@b.cl", "A").getSubject())
            .isEqualTo("usuarios/999");
    }

    @Test
    void elEventoDeBienvenidaNoTransportaLaContrasena() {
        EventGridEvent evento = PublicadorEventGrid.eventoUsuarioCreado(
            12L, "ana@correo.cl", "Ana");

        assertThat(evento.getData().toString().toLowerCase())
            .doesNotContain("password")
            .doesNotContain("hash");
    }
}
