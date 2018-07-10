package org.sonar.plugins.cas;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.authentication.BaseIdentityProvider;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.authentication.UserIdentity;
import org.sonar.plugins.cas.util.Serializer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * The {@link CasIdentifyProvider} is responsible for the browser based cas sso authentication. The authentication
 * workflow for an unauthenticated user works as follows:
 *
 * - the {@link ForceCasLoginFilter} redirects the user to /sessions/new
 * - the {@link AuthenticationFilter} redirects the user to the cas server
 * - the user authenticates him self
 * - cas redirects bach to /cas/validate
 * - the configured cas validation filter ({@link org.sonar.plugins.cas.cas2.Cas2ValidationFilter},
 *   {@link org.sonar.plugins.cas.saml11.Saml11ValidationFilter} or
 *   {@link org.jasig.cas.client.validation.Cas10TicketValidationFilter}) fetches the assertions from the cas server
 * - the {@link AssertionFilter} gets the assertions from the request and redirects to /sessions/init/cas with the
 *   serialized assertions as query parameter
 * - The {@link CasIdentifyProvider} is called by sonarqube (InitFilter) and creates the user from the assertions and
 *   redirects the user to the root of sonarqube
 */
@ServerSide
public class CasIdentifyProvider implements BaseIdentityProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CasIdentifyProvider.class);

    private final CasAttributeSettings attributeSettings;

    public CasIdentifyProvider(CasAttributeSettings attributeSettings) {
        this.attributeSettings = attributeSettings;
    }

    @Override
    public void init(Context context) {
        try {
            handleAuthentication(context);
        } catch (IOException e) {
            LOG.error("authentication failed", e);
        }
    }

    private void handleAuthentication(Context context) throws IOException {
        String assertionAsString = context.getRequest().getParameter("assertion");
        if (!Strings.isNullOrEmpty(assertionAsString)) {
            Assertion assertion = Serializer.deserialize(Assertion.class, assertionAsString);
            if (assertion != null) {
                context.authenticate(createUserIdentity(assertion));
                // redirect back to start page
                // TODO what about opened page? lost?
                context.getResponse().sendRedirect(StringUtils.defaultIfEmpty(context.getRequest().getContextPath(), "/"));
            }
        }
    }

    private UserIdentity createUserIdentity(Assertion assertion) {
        AttributePrincipal principal = assertion.getPrincipal();
        Map<String, Object> attributes = principal.getAttributes();

        UserIdentity.Builder builder = UserIdentity.builder()
                .setLogin(principal.getName())
                .setProviderLogin(principal.getName());

        String displayName = attributeSettings.getDisplayName(attributes);
        if (!Strings.isNullOrEmpty(displayName)) {
            builder = builder.setName(displayName);
        }

        String email = attributeSettings.getEmail(attributes);
        if (!Strings.isNullOrEmpty(email)) {
            builder = builder.setEmail(email);
        }

        Set<String> groups = attributeSettings.getGroups(attributes);
        if (groups != null) {
            builder = builder.setGroups(groups);
        }

        return builder.build();
    }



    @Override
    public String getKey() {
        return "cas";
    }

    @Override
    public String getName() {
        return "CAS";
    }

    @Override
    public Display getDisplay() {
        return Display.builder().build();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean allowsUsersToSignUp() {
        return false;
    }
}
