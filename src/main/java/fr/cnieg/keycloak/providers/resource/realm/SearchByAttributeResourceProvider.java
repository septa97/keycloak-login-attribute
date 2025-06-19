package fr.cnieg.keycloak.providers.resource.realm;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class SearchByAttributeResourceProvider implements RealmResourceProvider {
    private KeycloakSession session;

    public SearchByAttributeResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new BeforeAuthRestResource(session);
    }

    @Override
    public void close() {

    }
}
