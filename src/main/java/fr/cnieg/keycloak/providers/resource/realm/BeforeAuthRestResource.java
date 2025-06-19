package fr.cnieg.keycloak.providers.resource.realm;

import fr.cnieg.keycloak.providers.login.attribute.authenticator.AttributeUsernamePasswordForm;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;

public class BeforeAuthRestResource {
    private static final Logger logger = Logger.getLogger(BeforeAuthRestResource.class);
    private final KeycloakSession session;
    private final AuthenticationManager.AuthResult auth;

    public BeforeAuthRestResource(KeycloakSession session) {
        this.session = session;
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
    }

    @Path("")
    public SearchByAttributeRestResource getSearchByAttributeRestResourceAuthenticated() {
        checkRealmAdmin();
        return new SearchByAttributeRestResource(session);
    }

    private void checkRealmAdmin() {
        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        } else if (auth.getToken().getRealmAccess() == null || !auth.getToken().getRealmAccess().isUserInRole("admin")) {
            if (auth.getToken().getRealmAccess() != null) {
                for (String role : auth.getToken().getRealmAccess().getRoles()) {
                    logger.info("role: " + role);
                }
            }
            throw new ForbiddenException("Does not have realm admin role");
        }
    }
}
