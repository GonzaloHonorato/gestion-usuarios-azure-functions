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

public final class EsquemaRoles {

    private EsquemaRoles() {
    }

    public static GraphQL construir(RolesService servicio) {
        TypeDefinitionRegistry tipos = new SchemaParser().parse(
            EsquemaGraphQL.leer("esquema-roles.graphqls"));
        GraphQLSchema esquema = new SchemaGenerator()
            .makeExecutableSchema(tipos, cableado(servicio));
        return GraphQL.newGraphQL(esquema).build();
    }

    private static RuntimeWiring cableado(RolesService servicio) {
        DataFetcher<List<Rol>> roles = entorno -> servicio.listar();

        DataFetcher<Rol> rol = entorno ->
            servicio.obtener(EsquemaGraphQL.identificador(entorno.getArgument("id"))).orElse(null);

        DataFetcher<Rol> crear = entorno -> servicio.crear(datos(entorno.getArgument("entrada")));

        DataFetcher<Rol> actualizar = entorno -> {
            long id = EsquemaGraphQL.identificador(entorno.getArgument("id"));
            return servicio.actualizar(id, datos(entorno.getArgument("entrada")))
                ? servicio.obtener(id).orElse(null)
                : null;
        };

        DataFetcher<Boolean> eliminar = entorno ->
            servicio.eliminar(EsquemaGraphQL.identificador(entorno.getArgument("id")));

        return RuntimeWiring.newRuntimeWiring()
            .type("Query", constructor -> constructor
                .dataFetcher("roles", roles)
                .dataFetcher("rol", rol))
            .type("Mutation", constructor -> constructor
                .dataFetcher("crearRol", crear)
                .dataFetcher("actualizarRol", actualizar)
                .dataFetcher("eliminarRol", eliminar))
            .build();
    }

    private static DatosRol datos(Map<String, Object> entrada) {
        return new DatosRol(
            entrada.get("nombre") == null ? null : String.valueOf(entrada.get("nombre")),
            entrada.get("descripcion") == null ? null : String.valueOf(entrada.get("descripcion")));
    }
}
