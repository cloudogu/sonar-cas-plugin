package org.sonar.plugins.cas;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas10TicketValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.authentication.BaseIdentityProvider;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.authentication.UserIdentity;
import org.sonar.plugins.cas.cas2.Cas2ValidationFilter;
import org.sonar.plugins.cas.saml11.Saml11ValidationFilter;
import org.sonar.plugins.cas.util.Serializer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * The {@link CasIdentifyProvider} is responsible for the browser based cas sso authentication. The authentication
 * workflow for an unauthenticated user is as follows:
 *
 * <ol>
 * <li>the {@link ForceCasLoginFilter} redirects the user to /sessions/new</li>
 * <li>the {@link AuthenticationFilter} redirects the user to the CAS Server</li>
 * <li>the user authenticates to the CAS Server</li>
 * <li>the CAS Server redirects back to /cas/validate</li>
 * <li>the configured CAS validation filter fetches the assertions from the CAS Server. Depends of the actual CAS Server and can be one of
 *  <ul>
 *      <li>{@link Cas2ValidationFilter}</li>
 *      <li>{@link Saml11ValidationFilter} </li>
 *      <li>{@link Cas10TicketValidationFilter}</li>
 *  </ul>
 * </li>
 * <li>the {@link AssertionFilter} gets the assertion from the request and redirects to /sessions/init/cas with the serialized assertion as query parameter
 * <li>the {@link CasIdentifyProvider} is called by sonarqube (InitFilter) and creates the user from the assertions and
 * redirects the user to the root of sonarqube</li>
 *</ol>
 *
 * Note: While most steps are tied to the general CAS workflow, passing the assertions from the {@link AssertionFilter} back to the {@link CasIdentifyProvider} is needed in order to  circumvent the restrictions imposed by the plugin architecture of SonarQube, which would otherwise inhibit authentication via CAS.
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
