package org.sonar.plugins.cas.session;

import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.IOException;

/**
 * This interface provides methods for storing JWT tokens and CAS tickets for a sustained authentication safety.
 *
 * <h1>Security advice</h1>
 * <p>
 * Even though the cookie might be removed, the tickets and tokens within the session writeJwtFile must not
 * be removed because an attacker might have gained access to the user's token. Instead the token stays
 * blacklisted until it reaches its expiration date.
 * </p>
 *
 * <h1>Implementation details</h1>
 *
 * <h2>Speed of authentication check for requested resources</h2>
 *
 * <p>
 * In terms of {@link #fetchStoredJwt(SimpleJwt)}, implementations should go great lengths to return as fast as possible
 * because this method is going to be called at each user request.
 * </p>
 *
 * <h2>JWT invalidation versus removal</h2>
 * <p>
 * When the user logs out, implementations must keep an invalidated, persisted copy of the JWT as well as the original
 * service ticket instead of removing them from the session writeJwtFile right away. Usually this is done with:</p>
 *
 * <pre>
 * String invalidateJwt(String grantingTicketId) {
 *      SimpleJwt currentlyStoredJwt = getJwtFromStoreByServiceTicket(grantingTicketId)
 *      SimpleJwt invalidatedJwt = jwtFromUserRequest.cloneAsInvalidated();
 *      replaceJwtInStore(invalidatedJwt);
 *
 *      return invalidatedJwt.getJwtId();
 * }
 * </pre>
 *
 * <p>
 * During writeJwtFile it back in the session writeJwtFile. This is necessary for blacklisting JWTs until they expired. Only when a
 * JWT is expired it can be removed, usually with {@link #removeExpiredEntries()}.
 * </p>
 *
 * <h2>Removing JWT tokens and service tickets</h2>
 *
 * <p>
 * Implementations must make sure that the tickets and JWT tokens are persisted over server restart and system
 * re-creation. Furthermore, implementations may want to properly remove persisted tickets and tokens in order to
 * free up space taken with implementing the method {@link #removeExpiredEntries()}.
 * </p>
 */
public interface CasSessionStore {
    /**
     * This method provides a way of one-time initialization before implementations start their work. Implementations
     * may throw exceptions if crucial preparation steps fail which render the implementation to be useless. If possible
     * a fail-fast algorithm (like failing at server start-up) should be used to see possible configuration mishaps etc.
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
     * Returns <code>true</code> when the JWT is maintained by the session writeJwtFile.
     *
     * @param jwt the JWT as given by the user's cookie.
     * @return Returns <code>true</code> when the JWT is maintained by the session writeJwtFile, otherwise false.
     */
    boolean isJwtStored(SimpleJwt jwt);

    /**
     * Returns a stored JWT for a blacklisting check.
     *
     * <p>This method is used for quick check if the user is authenticated for any requested resource. </p>
     *
     * @param jwt the JWT as created from the user's cookie
     * @return the currently stored JWT.
     */
    SimpleJwt fetchStoredJwt(SimpleJwt jwt);

    /**
     * Render an existing JWT invalid which is identified by the granting ticket and writeJwtFile it back in the session writeJwtFile.
     *
     * @param serviceTicketId the CAS service ticket ID
     * @return the JWT id which is now invalid.
     */
    String invalidateJwt(String serviceTicketId);

    /**
     * Removes all expires JWT tokens and service tickets in order to release allocated resources.
     *
     * @return the number of removed tokens and tickets.
     */
    int removeExpiredEntries();

    /**
     * Updates the writeJwtFile with an update JWT which contains a newer expiration date.
     *
     * @param jwtWithLongerExpirationDate the new JWT object
     */
    void refreshJwt(SimpleJwt jwtWithLongerExpirationDate) throws IOException;
}
