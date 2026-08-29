package cl.gestion.functions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RutasTest {

    @Test
    void devuelveNuloCuandoNoHayIdentificador() {
        assertThat(Rutas.identificador("/api/roles", "roles")).isNull();
        assertThat(Rutas.identificador("/api/roles/", "roles")).isNull();
    }

    @Test
    void extraeElIdentificadorDeLaRuta() {
        assertThat(Rutas.identificador("/api/roles/12", "roles")).isEqualTo("12");
        assertThat(Rutas.identificador("/api/usuarios/5", "usuarios")).isEqualTo("5");
    }

    @Test
    void toleraUnaBarraFinal() {
        assertThat(Rutas.identificador("/api/roles/12/", "roles")).isEqualTo("12");
    }

    @Test
    void devuelveNuloSiElRecursoNoApareceEnLaRuta() {
        assertThat(Rutas.identificador("/api/otracosa/12", "roles")).isNull();
        assertThat(Rutas.identificador(null, "roles")).isNull();
    }
}
