package org.sonar.plugins.cas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.web.ServletFilter;
import org.sonar.plugins.cas.util.HttpStreams;
import org.sonar.plugins.cas.util.SonarCasProperties;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The {@link LogNoOpFilter} logs request infos and calls the filter chain without any modification.
 * SonarQube logging must be set to DEBUG or TRACE in order to facilitate the logging.
 */
public class LogNoOpFilter extends ServletFilter {
    private static final Logger LOG = LoggerFactory.getLogger(LogNoOpFilter.class);

    /**
     * called with injection by SonarQube during server initialization
     */
    public LogNoOpFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to init
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOG.debug("LogNoOpFilter: URL {}", request.toString());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }
}
