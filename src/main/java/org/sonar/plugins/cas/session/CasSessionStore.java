package org.sonar.plugins.cas.session;

import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.IOException;

/**
 * This interface provides methods for storing JWT tokens and CAS tickets for a sustained authentication safety.
 *
 * <h3>Security advice:</h3>
 * <p>
 * Even though the cookie might be removed, the tickets and tokens within the session store must not
 * be removed because an attacker might have gained access to the user's token. Instead the token stays
 * blacklisted until it reaches its expiration date.
 * </p>
 */
public interface CasSessionStore {
    /**
     * This method provides a way of one-time initialization before implementations start their work. Implementations
     * may throw exceptions if crucial preparation steps fail which render the implementation to be useless.
     */
    void prepareForWork() throws IOException;

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
     * Removes all expires JWT tokens.
     * @return the number of removed entries
     */
    int removeExpiredEntries();

    /**
     * Updates the store with an update JWT which contains a newer expiration date.
     *
     * @param jwtWithLongerExpirationDate the new JWT object
     */
    void refreshJwt(SimpleJwt jwtWithLongerExpirationDate) throws IOException;
}
