package org.sonar.plugins.cas;

import org.sonar.plugins.cas.util.SimpleJwt;

import java.util.HashMap;
import java.util.Map;

public class CasSessionStore {
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
        return jwtIdToJwt.get(jwt.getJwtId());
    }

    public static void invalidateJwt(String grantingTicketId) {
        SimpleJwt jwt = ticketToJwt.get(grantingTicketId);
        SimpleJwt invalidated = jwt.cloneAsInvalidated();

        jwtIdToJwt.replace(jwt.getJwtId(), invalidated);
        ticketToJwt.replace(grantingTicketId, invalidated);
    }
}
