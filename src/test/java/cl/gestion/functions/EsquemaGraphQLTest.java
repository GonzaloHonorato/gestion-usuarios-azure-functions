package cl.gestion.functions;

import graphql.ExecutionResult;
import graphql.GraphQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EsquemaGraphQLTest {

    private GraphQL usuarios;
    private GraphQL roles;
    private UsuariosServiceTest.UsuarioRepositorioMemoria repoUsuarios;
    private RolesServiceTest.RolRepositorioMemoria repoRoles;

    @BeforeEach
    void preparar() {
        repoUsuarios = new UsuariosServiceTest.UsuarioRepositorioMemoria();
        repoRoles = new RolesServiceTest.RolRepositorioMemoria();
        usuarios = EsquemaUsuarios.construir(new UsuariosService(repoUsuarios, new PublicadorNulo()));
        roles = EsquemaRoles.construir(new RolesService(repoRoles));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object valor) {
        return (Map<String, Object>) valor;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> lista(Object valor) {
        return (List<Map<String, Object>>) valor;
    }

    private Map<String, Object> ejecutar(GraphQL motor, String consulta) {
        ExecutionResult resultado = motor.execute(consulta);
        assertThat(resultado.getErrors()).isEmpty();
        return resultado.getData();
    }

    @Test
    void creaUnUsuarioMedianteMutacion() {
        Map<String, Object> datos = ejecutar(usuarios, """
            mutation {
              crearUsuario(entrada: {
                nombre: "Ana", apellido: "Soto",
                email: "ana@correo.cl", password: "clave-segura-123", roles: [1]
              }) { id nombre email activo }
            }
            """);

        Map<String, Object> creado = mapa(mapa(datos.get("crearUsuario")));
        assertThat(creado).containsEntry("nombre", "Ana").containsEntry("activo", true);
        assertThat(creado.get("id")).isNotNull();
    }

    @Test
    void consultaLosUsuariosCreados() {
        ejecutar(usuarios, """
            mutation { crearUsuario(entrada: {
              nombre: "Ana", apellido: "Soto", email: "ana@correo.cl",
              password: "clave-segura-123", roles: []
            }) { id } }
            """);

        Map<String, Object> datos = ejecutar(usuarios, "{ usuarios { nombre email } }");

        assertThat(lista(datos.get("usuarios"))).hasSize(1);
    }

    @Test
    void devuelveNuloParaUnUsuarioInexistente() {
        Map<String, Object> datos = ejecutar(usuarios, "{ usuario(id: 999) { nombre } }");

        assertThat(datos.get("usuario")).isNull();
    }

    @Test
    void actualizaUnUsuarioMedianteMutacion() {
        Map<String, Object> creado = mapa(ejecutar(usuarios, """
            mutation { crearUsuario(entrada: {
              nombre: "Ana", apellido: "Soto", email: "ana@correo.cl",
              password: "clave-segura-123", roles: []
            }) { id } }
            """).get("crearUsuario"));

        Map<String, Object> datos = ejecutar(usuarios, """
            mutation { actualizarUsuario(id: %s, entrada: {
              nombre: "Ana Maria", apellido: "Soto Vera", email: "ana@correo.cl", roles: []
            }) { nombre apellido } }
            """.formatted(creado.get("id")));

        assertThat(mapa(datos.get("actualizarUsuario"))).containsEntry("nombre", "Ana Maria");
    }

    @Test
    void desactivaUnUsuarioMedianteMutacion() {
        Map<String, Object> creado = mapa(ejecutar(usuarios, """
            mutation { crearUsuario(entrada: {
              nombre: "Ana", apellido: "Soto", email: "ana@correo.cl",
              password: "clave-segura-123", roles: []
            }) { id } }
            """).get("crearUsuario"));

        Map<String, Object> datos = ejecutar(usuarios, """
            mutation { desactivarUsuario(id: %s) }
            """.formatted(creado.get("id")));

        assertThat(datos).containsEntry("desactivarUsuario", true);
    }

    @Test
    void informaElErrorDeCorreoDuplicado() {
        String mutacion = """
            mutation { crearUsuario(entrada: {
              nombre: "Ana", apellido: "Soto", email: "ana@correo.cl",
              password: "clave-segura-123", roles: []
            }) { id } }
            """;
        ejecutar(usuarios, mutacion);

        ExecutionResult resultado = usuarios.execute(mutacion);

        assertThat(resultado.getErrors()).isNotEmpty();
        assertThat(resultado.getErrors().get(0).getMessage()).contains("ana@correo.cl");
    }

    @Test
    void creaYConsultaRoles() {
        ejecutar(roles, """
            mutation { crearRol(entrada: { nombre: "supervisor", descripcion: "Supervisa" })
              { id nombre descripcion } }
            """);

        Map<String, Object> datos = ejecutar(roles, "{ roles { nombre descripcion } }");

        assertThat(lista(datos.get("roles"))).hasSize(1);
        assertThat(lista(datos.get("roles")).get(0)).containsEntry("nombre", "SUPERVISOR");
    }

    @Test
    void eliminaUnRolMedianteMutacion() {
        Map<String, Object> creado = mapa(ejecutar(roles, """
            mutation { crearRol(entrada: { nombre: "temporal" }) { id } }
            """).get("crearRol"));

        Map<String, Object> datos = ejecutar(roles, """
            mutation { eliminarRol(id: %s) }
            """.formatted(creado.get("id")));

        assertThat(datos).containsEntry("eliminarRol", true);
    }

    @Test
    void impideEliminarUnRolEnUsoDesdeGraphQL() {
        Map<String, Object> creado = mapa(ejecutar(roles, """
            mutation { crearRol(entrada: { nombre: "enuso" }) { id } }
            """).get("crearRol"));
        repoRoles.asignados.put(Long.valueOf(String.valueOf(creado.get("id"))), 2);

        ExecutionResult resultado = roles.execute(
            "mutation { eliminarRol(id: %s) }".formatted(creado.get("id")));

        assertThat(resultado.getErrors()).isNotEmpty();
    }

    @Test
    void rechazaUnCampoQueNoExisteEnElEsquema() {
        ExecutionResult resultado = usuarios.execute("{ usuarios { campoInventado } }");

        assertThat(resultado.getErrors()).isNotEmpty();
    }

    static class PublicadorNulo implements PublicadorEventos {
        @Override public void usuarioCreado(long usuarioId, String email, String nombre) { }
        @Override public void recuperacionSolicitada(long usuarioId, String email,
                                                     String nombre, String token) { }
    }
}
