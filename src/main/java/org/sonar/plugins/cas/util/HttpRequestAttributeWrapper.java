package org.sonar.plugins.cas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import org.sonar.api.server.http.HttpRequest;

/**
 * Wrapper for JakartaHttpRequest
 * due to Sonar-Server Api restrictions the Attributes are no longer accessible
 * The JakartaHttpRequestAttributeWrapper take use of reflections to access known fields
 *
 * @author Marco Bergen
 */
public class HttpRequestAttributeWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestAttributeWrapper.class);

    private final HttpRequest delegate;

    public HttpRequestAttributeWrapper(HttpRequest request) {
        delegate = request;
    }

    public Object getAttribute(String key) throws ReflectiveOperationException {
        Method delegateMethod = delegate.getClass().getDeclaredMethod("getDelegate");
        Object jakartadelegate = delegateMethod.invoke(this.delegate);
        delegateMethod = jakartadelegate.getClass().getMethod("getAttribute", String.class);
        return delegateMethod.invoke(jakartadelegate, key);
    }

}
