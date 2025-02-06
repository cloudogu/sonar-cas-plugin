package org.sonar.plugins.cas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.api.server.http.HttpResponse;
import org.sonar.api.web.FilterChain;
import org.sonar.api.web.HttpFilter;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;
import org.sonar.plugins.cas.util.JwtProcessor;
import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.IOException;
import java.util.Collection;

import static org.sonar.plugins.cas.util.Cookies.JWT_SESSION_COOKIE;

/**
 * This class updates the JWT with a newer expiration date in the session store when SonarQube sends a newer JWT.
 */
public class CasTokenRefreshFilter extends HttpFilter {
    private static final Logger LOG = LoggerFactory.getLogger(CasTokenRefreshFilter.class);
    private final CasSessionStoreFactory sessionStoreFactory;

    public CasTokenRefreshFilter(CasSessionStoreFactory sessionStoreFactory) {
        this.sessionStoreFactory = sessionStoreFactory;
    }

    @Override
    public void init() {
        // nothing to init
    }

    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain)
            throws IOException {

        SimpleJwt requestJwt = JwtProcessor.getJwtTokenFromCookies(request.getCookies());
        SimpleJwt responseJwt = getJwtFromResponse(response);

        // only during a refresh JWTs exist both in the same time in request AND response
        if (isTokenRefreshed(responseJwt, requestJwt)) {
            LOG.debug("Refresh JWT {} with updated expiration date", requestJwt.getJwtId());
            sessionStoreFactory.getInstance().refreshJwt(responseJwt);
        }
        chain.doFilter(request, response);
    }

    private SimpleJwt getJwtFromResponse(HttpResponse response) {
        Collection<String> headers = response.getHeaders("Set-Cookie");
        if (headers == null || headers.size() == 0) {
            return SimpleJwt.getNullObject();
        }

        if (!containsRefreshJwtCookie(headers)) {
            return SimpleJwt.getNullObject();
        }

        return JwtProcessor.getJwtTokenFromResponseHeaders(headers);
    }

    private boolean containsRefreshJwtCookie(Collection<String> headers) {
        String refreshedTokenHeader = null;
        for (String header : headers) {
            if (header.contains(JWT_SESSION_COOKIE)) {
                refreshedTokenHeader = header;
                break;
            }
        }
        return refreshedTokenHeader == null;
    }

    boolean isTokenRefreshed(SimpleJwt responseJwt, SimpleJwt requestJwt) {
        if (responseJwt.isNullObject() || requestJwt.isNullObject()) {
            return false;
        }

        LOG.debug("Found JWT refresh candidates: {} vs {}", responseJwt.toString(), requestJwt.toString());

        boolean equalIds = responseJwt.getJwtId().equals(requestJwt.getJwtId());
        boolean equalJwts = responseJwt.equals(requestJwt);

        return equalIds && !equalJwts;
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }
}
