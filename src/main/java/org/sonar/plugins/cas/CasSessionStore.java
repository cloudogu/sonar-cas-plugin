package org.sonar.plugins.cas;

import org.sonar.plugins.cas.util.SimpleJwt;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CasSessionStore {
    /**
     * This map contains the CAS granting ticket and the issued JWT. This map is only hit during back-channel logout.
     */
    private static Map<String, SimpleJwt> ticketToJwt = new HashMap<>();
    /**
     * This map provides the CAS plugin with information about a JWT's validity. This collection is hit on every Sonar
     * request and must be super-fast.
     */
    private static Map<String, SimpleJwt> jwtIdToJwt = new HashMap<>();

    static void store(String ticket, SimpleJwt jwt) {
        ticketToJwt.put(ticket, jwt);
        jwtIdToJwt.put(jwt.getJwtId(), jwt);
    }

    public static boolean isJwtStored(SimpleJwt jwt) {
        return jwtIdToJwt.containsKey(jwt.getJwtId());
    }

    public static SimpleJwt getJwtById(SimpleJwt jwt) {
        SimpleJwt result = jwtIdToJwt.get(jwt.getJwtId());
        if (result == null) {
            result = SimpleJwt.getNullObject();
        }
        return result;
    }

    /**
     * Render existing JWT invalid.
     * @param grantingTicketId the CAS granting ticket ID
     * @return the JWT id which is now invalid.
     */
    public static String invalidateJwt(String grantingTicketId) {
        SimpleJwt jwt = ticketToJwt.get(grantingTicketId);
        SimpleJwt invalidated = jwt.cloneAsInvalidated();

        jwtIdToJwt.replace(jwt.getJwtId(), invalidated);
        ticketToJwt.replace(grantingTicketId, invalidated);

        return invalidated.getJwtId();
    }

    public static Collection<String> pruneExpiredEntries() {
        return Collections.emptyList();
    }
}
