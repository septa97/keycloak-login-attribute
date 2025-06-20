package fr.cnieg.keycloak.providers.resource.realm;

import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.common.ClientConnection;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

public class BeforeAuthRestResource {
    private static final Logger logger = Logger.getLogger(BeforeAuthRestResource.class);

    @Context
    private ClientConnection clientConnection;

    private final KeycloakSession session;

    public BeforeAuthRestResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("")
    public SearchByAttributeRestResource getSearchByAttributeRestResourceAuthenticated(@Context final HttpHeaders headers) {
        AdminAuth auth = authenticateRealmAdminRequest(headers);
        logger.debug("authenticated admin access for: " + auth.getUser().getUsername());

        SearchByAttributeRestResource searchByAttributeRestResource = new SearchByAttributeRestResource(session);
        ResteasyProviderFactory.getInstance().injectProperties(searchByAttributeRestResource);
        return searchByAttributeRestResource;
    }

    private AdminAuth authenticateRealmAdminRequest(HttpHeaders headers) {
        String tokenString = AppAuthManager.extractAuthorizationHeaderToken(headers);
        if (tokenString == null) throw new NotAuthorizedException("Bearer");
        AccessToken token;

        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            throw new NotAuthorizedException("Bearer token format error");
        }
        String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(session);
        RealmModel realm = realmManager.getRealmByName(realmName);
        if (realm == null) {
            throw new NotAuthorizedException("Unknown realm in token");
        }
        session.getContext().setRealm(realm);

        AuthenticationManager.AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session)
                .setRealm(realm)
                .setConnection(clientConnection)
                .setHeaders(headers)
                .authenticate();

        if (authResult == null) {
            logger.debug("Token not valid");
            throw new NotAuthorizedException("Bearer");
        }

        ClientModel client = realm.getClientByClientId(token.getIssuedFor());
        if (client == null) {
            throw new NotFoundException("Could not find client for authorization");
        }

        return new AdminAuth(realm, authResult.getToken(), authResult.getUser(), client);
    }
}
