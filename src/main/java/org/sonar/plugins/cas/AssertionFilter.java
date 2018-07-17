package org.sonar.plugins.cas;

import org.jasig.cas.client.validation.Assertion;
import org.sonar.api.web.ServletFilter;
import org.sonar.plugins.cas.util.Serializer;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The {@link AssertionFilter} reads the CAS assertion from the request, serializes it and then redirects back to the
 * {@link CasIdentifyProvider} with the serialized assertion as query parameter.
 *
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
public class AssertionFilter extends ServletFilter {

    @Override
    public UrlPattern doGetPattern() {
        return UrlPattern.create("/cas/validate");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        Assertion assertion = (Assertion) request.getAttribute(Assertion.class.getName());
        if (assertion != null) {
            // TODO SECURITY => encrypt or sign, short living?
            String assertionAsString = Serializer.serialize(assertion);
            response.sendRedirect("/sessions/init/cas?assertion=" + assertionAsString);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {

    }
}
