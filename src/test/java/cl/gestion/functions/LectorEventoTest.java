package cl.gestion.functions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LectorEventoTest {

    private static final String USUARIO_CREADO = """
        {
          "id": "abc-123",
          "eventType": "GestionUsuarios.UsuarioCreado",
          "subject": "usuarios/12",
          "eventTime": "2026-10-03T14:05:00Z",
          "dataVersion": "1.0",
          "data": { "usuarioId": 12, "email": "ana@correo.cl", "nombre": "Ana" }
        }
        """;

    private static final String EN_ARREGLO = "[" + USUARIO_CREADO + "]";

    @Test
    void leeUnEventoSuelto() {
        EventoRecibido evento = LectorEvento.leer(USUARIO_CREADO);

        assertThat(evento.eventType()).isEqualTo(TipoEvento.USUARIO_CREADO);
        assertThat(evento.subject()).isEqualTo("usuarios/12");
        assertThat(evento.data().get("email").asText()).isEqualTo("ana@correo.cl");
    }

    @Test
    void leeUnEventoEnvueltoEnUnArreglo() {
        EventoRecibido evento = LectorEvento.leer(EN_ARREGLO);

        assertThat(evento.eventType()).isEqualTo(TipoEvento.USUARIO_CREADO);
        assertThat(evento.data().get("usuarioId").asLong()).isEqualTo(12L);
    }

    @Test
    void rechazaUnCuerpoVacio() {
        assertThatThrownBy(() -> LectorEvento.leer(null))
            .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> LectorEvento.leer("  "))
            .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void rechazaUnJsonMalFormado() {
        assertThatThrownBy(() -> LectorEvento.leer("{esto no es json"))
            .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void rechazaUnArregloVacio() {
        assertThatThrownBy(() -> LectorEvento.leer("[]"))
            .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void entregaTextoVacioCuandoElCampoNoExiste() {
        EventoRecibido evento = LectorEvento.leer(USUARIO_CREADO);

        assertThat(LectorEvento.texto(evento.data(), "noExiste")).isEmpty();
        assertThat(LectorEvento.texto(evento.data(), "nombre")).isEqualTo("Ana");
    }
}
