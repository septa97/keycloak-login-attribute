package fr.cnieg.keycloak.providers.resource.realm;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

public class SearchByAttributeRestResource {
    // TODO: move to a constants file
    private static final String MIN = "min";

    private final KeycloakSession session;

    public SearchByAttributeRestResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Path("/min/{min}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<UserRepresentation> getUsersByMIN(@PathParam(MIN) String min) {
        return session.users().searchForUserByUserAttributeStream(session.getContext().getRealm(), MIN, min)
                .map(userModel -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(), userModel))
                .collect(Collectors.toList());
    }

    @GET
    @Produces("text/plain; charset=utf-8")
    public String getHello() {
        String name = session.getContext().getRealm().getDisplayName();
        if (name == null) {
            name = session.getContext().getRealm().getName();
        }

        return "Hello " + name;
    }
}
