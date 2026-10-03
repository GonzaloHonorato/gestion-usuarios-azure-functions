package cl.gestion.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class UsuariosGraphQLFunction {

    @FunctionName("usuariosGraphQL")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.FUNCTION,
                route = "graphql/usuarios")
            HttpRequestMessage<Optional<String>> peticion,
            final ExecutionContext contexto) {

        UsuariosService servicio = new UsuariosService(
            new UsuarioRepositorioJdbc(),
            PublicadorEventGrid.desdeConfiguracion(contexto.getLogger()));

        return EjecutorGraphQL.responder(
            peticion, EsquemaUsuarios.construir(servicio), "usuariosGraphQL", contexto);
    }
}
