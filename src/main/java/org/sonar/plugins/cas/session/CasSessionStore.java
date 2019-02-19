package org.sonar.plugins.cas.session;

import org.sonar.plugins.cas.util.SimpleJwt;

/**
 *
 */
public interface CasSessionStore {
    /**
     * Stores a CAS granting ticket and the associated JWT token after a successful CAS login. Tickets and tokens
     * are held for black/white listing of existing authentications because SonarQube does not provide a session
     * management.
     *
     * @param ticket the CAS granting ticket ID
     * @param jwt    the JWT token ID
     */
    void store(String ticket, SimpleJwt jwt);

    /**
     * @param jwt
     * @return
     */
    boolean isJwtStored(SimpleJwt jwt);

    /**
     * @param jwt
     * @return
     */
    SimpleJwt getJwtById(SimpleJwt jwt);

    /**
     * Render existing JWT invalid.
     *
     * @param grantingTicketId the CAS granting ticket ID
     * @return the JWT id which is now invalid.
     */
    String invalidateJwt(String grantingTicketId);

    /**
     * Removes all expires tickets and tokens.
     */
    void pruneExpiredEntries();
}
