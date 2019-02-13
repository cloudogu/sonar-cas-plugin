package org.sonar.plugins.cas;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.authentication.BaseIdentityProvider;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.authentication.UserIdentity;
import org.sonar.plugins.cas.logout.LogoutHandler;
import org.sonar.plugins.cas.util.JwtProcessor;
import org.sonar.plugins.cas.util.SimpleJwt;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
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
 * <li>the CAS Server redirects back to /sessions/init/cas</li>
 *
 * <li>the {@link CasIdentifyProvider} is called by sonarqube (InitFilter) and creates the user from the assertions and
 * redirects the user to the root of sonarqube</li>
 * </ol>
 * <p>
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
            // case login
            if (isLogin(context.getRequest())) {
                handleAuthentication(context);
            } else if (isLogout(context.getRequest())) {
                handleLogout(context);
            } else {
                LOG.debug("Found a different case than expected");
            }
            // CASE logout
            // post mit application/x-www-form-urlencoded und BODY param "logoutRequest" XML
        } catch (Exception e) {
            LOG.error("authentication failed", e);
        }
    }

    private void handleAuthentication(Context context) throws IOException, TicketValidationException {
        LOG.debug("Found internal login case");

        String grantingTicket = context.getRequest().getParameter("ticket");
        Cas30ProxyTicketValidator validator = new Cas30ProxyTicketValidator("https://cas.hitchhiker.com:8443/cas");
        Assertion assertion = validator.validate(grantingTicket, "http://sonar.hitchhiker.com:9000/sessions/init/cas");

        context.authenticate(createUserIdentity(assertion));

        Collection<String> headers = context.getResponse().getHeaders("Set-Cookie");
        SimpleJwt jwt = JwtProcessor.getJwtTokenFromRequestHeaders(headers);
        CasSessionStore.store(grantingTicket, jwt);

        // redirect back to start page
        // TODO what about opened page? lost?
        context.getResponse().sendRedirect(StringUtils.defaultIfEmpty(context.getRequest().getContextPath(), "/"));
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

    private void handleLogout(Context context) {
        LOG.debug("Found external logout case");
        String logoutAttributes = context.getRequest().getParameter("logoutRequest");
        new LogoutHandler().logout(logoutAttributes);
    }

    private boolean isLogout(HttpServletRequest request) {
        String requestMethod = request.getMethod();
        String logoutAttributes = request.getParameter("logoutRequest");
        return "POST".equals(requestMethod) && logoutAttributes != null && !logoutAttributes.isEmpty();
    }

    private boolean isLogin(HttpServletRequest request) {
        String requestMethod = request.getMethod();
        String ticket = request.getParameter("ticket");

        return "GET".equals(requestMethod) && ticket != null && !ticket.isEmpty();
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
        // TODO configurable because normal != CES
        return true;
    }
}
