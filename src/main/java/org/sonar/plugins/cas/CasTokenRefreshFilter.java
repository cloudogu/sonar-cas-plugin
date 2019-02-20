package org.sonar.plugins.cas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.web.ServletFilter;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;
import org.sonar.plugins.cas.util.JwtProcessor;
import org.sonar.plugins.cas.util.RequestUtil;
import org.sonar.plugins.cas.util.SimpleJwt;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import static org.sonar.plugins.cas.logout.LogoutHandler.JWT_SESSION_COOKIE;

public class CasTokenRefreshFilter extends ServletFilter {
    private static final Logger LOG = LoggerFactory.getLogger(ForceCasLoginFilter.class);
    private CasSessionStoreFactory sessionStoreFactory;

    public CasTokenRefreshFilter(CasSessionStoreFactory sessionStoreFactory) {
        this.sessionStoreFactory = sessionStoreFactory;
    }

    @Override
    public void init(FilterConfig filterConfig)  {
        // nothing to init
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain)
            throws IOException {

        final HttpServletRequest request = RequestUtil.toHttp(servletRequest);
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        SimpleJwt requestJwt = JwtProcessor.getJwtTokenFromCookies(request.getCookies());
        SimpleJwt responseJwt = getJwtFromResponse(response);

        // only during a refresh JWTs exist both in the same time in request AND response
        if (isTokenRefreshed(responseJwt, requestJwt)) {
            LOG.debug("Refresh JWT {} with updated expiration date", requestJwt.getJwtId());
            sessionStoreFactory.getInstance().refreshJwt(responseJwt);
        }
    }

    private SimpleJwt getJwtFromResponse(HttpServletResponse response) {
        Collection<String> headers = response.getHeaders("Set-Cookie");
        if (headers == null || headers.size() == 0) {
            return SimpleJwt.getNullObject();
        }

        String refreshedTokenHeader = null;
        for (String header : headers) {
            if (header.contains(JWT_SESSION_COOKIE)) {
                refreshedTokenHeader = header;
                break;
            }
        }
        if (refreshedTokenHeader == null) {
            return SimpleJwt.getNullObject();
        }

        return JwtProcessor.getJwtTokenFromResponseHeaders(headers);
    }

    boolean isTokenRefreshed(SimpleJwt responseJwt, SimpleJwt requestJwt) {
        LOG.debug("Found JWT refresh candidates: {} vs {}", responseJwt.toString(), requestJwt.toString());

        return responseJwt.getJwtId().equals(requestJwt.getJwtId());
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }
}
