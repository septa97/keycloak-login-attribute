package fr.cnieg.keycloak.providers.resource.realm;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.admin.ComponentResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

public class SearchByAttributeResourceProvider implements RealmResourceProvider {
    private KeycloakSession session;
    // TODO: move to a constants file
    private static final String MIN = "min";
    ComponentResource

    public SearchByAttributeResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @GET
    @Path("/search-by-min/{min}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<UserRepresentation> getUsersByMIN(@PathParam(MIN) String min) {
        return session.users().searchForUserByUserAttributeStream(session.getContext().getRealm(), MIN, min)
                .map(userModel -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(), userModel))
                .collect(Collectors.toList());
    }

    @Override
    public void close() {

    }
}
