package cl.gestion.functions;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.util.List;
import java.util.Map;

public final class EsquemaUsuarios {

    private EsquemaUsuarios() {
    }

    public static GraphQL construir(UsuariosService servicio) {
        TypeDefinitionRegistry tipos = new SchemaParser().parse(
            EsquemaGraphQL.leer("esquema-usuarios.graphqls"));
        GraphQLSchema esquema = new SchemaGenerator()
            .makeExecutableSchema(tipos, cableado(servicio));
        return GraphQL.newGraphQL(esquema).build();
    }

    private static RuntimeWiring cableado(UsuariosService servicio) {
        DataFetcher<List<Usuario>> usuarios = entorno -> servicio.listar();

        DataFetcher<Usuario> usuario = entorno ->
            servicio.obtener(EsquemaGraphQL.identificador(entorno.getArgument("id"))).orElse(null);

        DataFetcher<Usuario> crear = entorno -> {
            Map<String, Object> entrada = entorno.getArgument("entrada");
            return servicio.crear(new CrearUsuario(
                texto(entrada.get("nombre")),
                texto(entrada.get("apellido")),
                texto(entrada.get("email")),
                texto(entrada.get("password")),
                identificadores(entrada.get("roles"))));
        };

        DataFetcher<Usuario> actualizar = entorno -> {
            long id = EsquemaGraphQL.identificador(entorno.getArgument("id"));
            Map<String, Object> entrada = entorno.getArgument("entrada");
            boolean actualizado = servicio.actualizar(id, new ActualizarUsuario(
                texto(entrada.get("nombre")),
                texto(entrada.get("apellido")),
                texto(entrada.get("email")),
                identificadores(entrada.get("roles"))));
            return actualizado ? servicio.obtener(id).orElse(null) : null;
        };

        DataFetcher<Boolean> desactivar = entorno ->
            servicio.desactivar(EsquemaGraphQL.identificador(entorno.getArgument("id")));

        return RuntimeWiring.newRuntimeWiring()
            .type("Query", constructor -> constructor
                .dataFetcher("usuarios", usuarios)
                .dataFetcher("usuario", usuario))
            .type("Mutation", constructor -> constructor
                .dataFetcher("crearUsuario", crear)
                .dataFetcher("actualizarUsuario", actualizar)
                .dataFetcher("desactivarUsuario", desactivar))
            .build();
    }

    private static String texto(Object valor) {
        return valor == null ? null : String.valueOf(valor);
    }

    private static List<Long> identificadores(Object valor) {
        if (!(valor instanceof List<?> lista)) {
            return List.of();
        }
        return lista.stream().map(EsquemaGraphQL::identificador).toList();
    }
}
