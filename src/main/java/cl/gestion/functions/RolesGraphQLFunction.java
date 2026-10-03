package cl.gestion.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class RolesGraphQLFunction {

    @FunctionName("rolesGraphQL")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.FUNCTION,
                route = "graphql/roles")
            HttpRequestMessage<Optional<String>> peticion,
            final ExecutionContext contexto) {

        RolesService servicio = new RolesService(
            new RolRepositorioJdbc(),
            PublicadorEventGrid.desdeConfiguracion(contexto.getLogger()));

        return EjecutorGraphQL.responder(
            peticion, EsquemaRoles.construir(servicio), "rolesGraphQL", contexto);
    }
}
