package org.sonar.plugins.cas;

import org.sonar.api.web.ServletFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The {@link AuthenticationFilter} always redirects to the CAS Server.
 *
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
public class AuthenticationFilter extends ServletFilter {

    @Override
    public UrlPattern doGetPattern() {
        return UrlPattern.create("/sessions/new");
    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        ((HttpServletResponse)response).sendRedirect("https://cas.hitchhiker.com:8443/cas/login?service=http://sonar.hitchhiker.com:9000/sessions/init/cas");
    }

    @Override
    public void destroy() {

    }
}
