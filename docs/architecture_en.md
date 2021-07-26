# Architecture and internals

## Basic entities

### JWT - JSON Web Tokens

SonarQube issues a [JWT](https://tools.ietf.org/html/rfc7519) upon a successful authentication. This JWT is an open
method for representing claims securely between two parties. SonarQube's JWT contains among other information:

- an ID, 
- the username of the user, 
- an expiration date

### Service Ticket

The service ticket is a code which is used to validate the user's authentication request directly with the 
authenticating system, namely the CAS.

## Use cases within the authentication lifecycle

Authentication is a fickle thing, even without having an external authentication system. This plug-in focuses on these
use cases:

1. Local Log-in and Single Sign-on (SSO)
1. Usual resource request
1. JWT refresh
1. Local Log-out and Single Log-out (SLO)
   1. Logout over SonarQube
   1. Single Log-out
1. Clean-up

### Local Log-in and Single Sign-on (SSO)

Due to the nature of CAS, signing in on the CAS is easy as 1-2-3. This includes SSO with services registered with
CAS.

1. User wants to get resource
   - enters URL in Browser: `https://sonar.server.com/`
1. ForceCasLoginFilter recognizes log-in use-case
   - redirect user to CAS log-in page
   - `https://cas.server.com/cas/login?service=http://sonar.server.com/sessions/init/sonarqube`
1. User logs into CAS with credentials
1. CAS redirects user to back to SonarQube
   - adds service ticket parameter
1. CasIdentityProvider validates service ticket directly with CAS
   - this validation is independent of the browser
   - Sonar and CAS communicate on a direct channel
1. CAS replies with validity and user attributes
   - CasIdentityProvider authenticates against SonarQube
1. CasIdentityProvider fetches and stores JWT as well as service ticket
   - stored in session store
1. SonarQube delivers originally requested resource
   - user receives also authentication cookie with JWT

### Usual resource request

Once the user is signed-in, every request must be checked with the blacklist.

1. User wants to get resource
   - Browser contains valid and unexpired JWT cookie
1. ForceCasLoginFilter asks session store if JWT is expired
1. Session store replies that JWT is good
1. SonarQube delivers originally requested resource
   - JWT cookie is still the same

### JWT refresh

Every now and then SonarQube issues an updated JWT in order to ensure that a signed-in user may continue her work.
This JWT contains updated expiration information which needs to go in the JWT session store as well. 

1. User wants to get resource
   - Browser contains valid and unexpired JWT cookie
1. CasTokenRefreshFilter finds new JWT cookie
1. CasTokenRefreshFilter updates session store with expiration date
   - only the date changes within the cookie
1. ForceCasLoginFilter asks session store if JWT is expired
1. Session store replies that JWT is good
1. SonarQube delivers originally requested resource
   - user receives update JWT cookie

### Logout

In terms of the Sonar CAS plugin, there are two ways of logging out from SonarQube.

1. Logout over SonarQube
1. Single Logout (SLO) 

At a certain point both are similar because in the end the back-channel logout mechanism is used.

#### Logout over SonarQube

1. User logs in into SonarQube (as usual)
1. CasSonarSignOutInjectorFilter injects Javascript into the requested HTML file
1. User clicks menu > logout
1. Injected Javascript rewrites browser location and points to CAS logout page
1. CAS receives log out.
1. CAS sends back-channel logout request to all registered services
1. CasIdentityProvider receives logout with service ticket
1. CasIdentityProvider fetches the JWT from the session store
   - the session store contains a reference from service ticket to JWT ID
   - with the JWT ID the stored JWT is fetched
1. CasIdentityProvider invalidates user's JWT
   - invalidated JWT updates the original JWT in the session store

#### Single Logout (SLO)

1. User logs in into SonarQube (as usual)
1. User changes to third party service which is registered with the CAS
1. User logs out
1. CAS receives log out.
1. CAS sends back-channel logout request to all registered services
1. CasIdentityProvider receives logout with service ticket
1. CasIdentityProvider fetches the JWT from the session store
   - the session store contains a reference from service ticket to JWT ID
   - with the JWT ID the stored JWT is fetched
1. CasIdentityProvider invalidates user's JWT
   - invalidated JWT updates the original JWT in the session store

#### Authentication with Proxy Tickets

Proxy tickets are a mechanism for indirect authentication of users, but without using their original password.

In addition to SonarQube, there may be other services that CAS authenticates to. The CAS specification speaks here of *
services*. A login via proxy ticket is then no longer done via SonarQube in interaction with CAS, but via another
service: A proxy service. The person using the ticket must instead authenticate himself in interaction with the proxy
service. This proxy service is then issued a proxy granting ticket, which in turn can be used to request proxy tickets.

Proxy tickets are very similar to CAS service tickets and have a short validity period. The proxy service makes the
requested request to SonarQube using the proxy ticket. Sonar-CAS plugin recognizes this process and checks the validity
against CAS. After successful validation, SonarQube processes the request and sends the response back to the proxy
service.

Since the content is a request against SonarQube's REST API, the entry point is the
class `org.sonar.plugins.cas.CasAuthenticator`.

![Authentication workflow with CAS proxy tickets](https://ecosystem.cloudogu.com/plantuml/svg/ZLB1RXGn3BtFLrZ31QJEIi1fzm2gMlN0gMZX0qpYpWRIP1exkmnVZprfTxUYbRZC9FPxVlQBqKaky9sf039K_NSJ5WakJ9W4-YjAKZ32PPMT7eD32Jd1bie-EEgDv92VSsvB_Zq3dq4cYpm7RNF2yhN-Q02s6xnPhszkrkiNWCFLvNQuZNKCgU7TT4HtrZKCdveAm0Pubmzm502FWl2s9ZpDGFvTr-3AM_XAA-H38ISW6LJlM5S71310pAeFXo0xS0gsMXYvi_onp0d7rJbYlgknIra8IXXtie6SugmVCjIWi4I6mZ9tgzNgFsTvRP9cumP6aePSUcrfHV_IyFRRyFx3n-7XG4N-8FkxSHMpmzWrhXLHRqtvN0Hmn91Op1S8o-GoM-6zNafdb9DHoWtygX3iCGR_-ScrfcQScVYYcPZmdg3_YObyVm4-y1HnVen-qIXSPzB4M7ATU0Ezfpt5F57H0a8ioy7kox9o_zHV6z6q5fdp0PMWqpWYtppB-beXQRU57ggMFDdJtBHjuKcBastB8wXHpVX_bsjvHqlz1G00.svg)

##### Configuration

Proxy services must be configured on the CAS side, otherwise there may be a security risk due to insufficient
restriction.

The SonarQube property `sonar.cas.proxyTicketing.services` must contain a Regular Expression that sufficiently fixes
proxy services.

Example:

```properties
sonar.cas.proxyTicketing.services=^https://myservices.company.com/.*$
```

##### Restrictions

- Proxy tickets only supported with CAS 3.0 protocol and higher
   - Extended user attributes are not provided
     until [CAS protocol specification 3.0](https://apereo.github.io/cas/5.1.x/protocol/CAS-Protocol-Specification.html)
     .
   - These attributes are necessary to perform user and group replication.
   - Sonar-CAS-plugin does not support other protocols and earlier CAS protocols for this use case.
- Proxy tickets are only possible and useful for REST requests
- In the HTTP request, the proxy ticket must be processed as follows:
   - Basic Auth header
   - User name as usual
   - Password is composed of keyword `ProxyTicket==:` and the proxy ticket

```
proxyTicket=ST-123-qwertzasdfg.local;
basicAuthCredentials=username + ":" + "ProxyTicket==:" + proxyTicket;
encodedCredentials=base64(plainCredentials);
headers={ "Authorization" : "Basic " + encodedCredentials }
http.get(headers, "http://sonarqube/api/endpoint")
```

### Clean-up

This is done by a background task. It iterates all saved JWT and service ticket files (see the FileSessionStore section
below for more information)

1. In fixed intervals a background reads all stored JWTs and associated service tickets
2. all JWTs are inspected for expiration date
3. expired JWTs and their associated service ticket are removed

## Crucial components

The authentication within SonarQube is a complex process which needs different classes to work. In this section only the
most important components are roughly described, so you get an idea of how things work.

### General mechanism of `CasPlugin`

`CasPlugin` is the main entry point using SonarQube's plug-in mechanism. All components used by the Sonar CAS
plugin must be registered in its `collectExtensions()` method. This way, the components are eligible for SonarQube's
dependency injection. Unless they are manually instantiated each component must either have a default constructor or a
constructor consisting of registered components.

In terms of `ServletFilter` components, each filter must call the given `FilterChain` not more than once. Otherwise content may be written more than once which may lead to a plethora of weird CSS/script behaviour or visible content.

### CasIdentityProvider for browser-based requests

The `CasIdentityProvider` takes care of logging-in or logging-out. Logging-in is by far the more complex process of both
which is described above.

### CasAuthenticator for REST-based requests

The `CasAuthenticator` takes care of HTTP-API calls toward SonarQube.

### ForceCasLoginFilter

The `ForceCasLoginFilter` checks for every request if the request is permitted by checking the session store with the
JWT from the user's request.

Requests on static resources are allowed, since these may be executed asynchronously before authentication. Within the authentication process, users are recognized by the "LOGIN" field. An empty field or a login with the value `-` means that no authentication has taken place yet.

### FileSessionStore

The `FileSessionStore` is an implementation of a session store. The session store maintains a white-/blacklist of all
JWTs and their associated service ticket.

The FileSessionStore stores two files per login:

- the JWT file (filename = JWT ID)
  - stores JWT ID
  - expiration date
  - information whether the JWT is invalid (i.e. blacklisted after a log-out)
- the Service Ticket file (filename = Service Ticket id)
  - stores the JWT ID

Once a JWT is expired both JWT file and service ticket file are eligible for removal.

## Error Handling

SonarQube's flexible plugin architecture has a drawback when it comes to error handling. All errors from plugins seem to be ignored. In consequence this means that all exceptions must not bubble to the top of the starting process (a usual process for Java applications).

Exception bubbling may be used inside dependent plugin classes with the restriction that instead all top level functionality **MUST CATCH** all exceptions and log them with ERROR log level instead, just like this:

```java
public class YourNewFilter { 
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
        try {
            // do somthing
        } catch (Exception e) {
            LOG.error("YourNewFilter doFilter failed", e);
        }
    }
}
``` 

## Architecture changes with SonarQube 8.x

With the version jump from SonarQube 7.x to 8.x, the Sonar CAS plugin must respond to SonarQube changes in order to remain functional. This section explains these changes against the background of SonarQube's way of working.

Starting with version 8.x, SonarQube changed the way of processing user authentication:

For REST requests via Basic Auth, SonarQube only accepts its own non-extensible identity providers. Without changes to the Sonar CAS plugin, such queries may lead to authentication errors, mainly due to potential duplicate email addresses or login identifiers. SonarQube ignores the IdentityProvider from the CAS plugin and uses the realm `sonarqube` internally instead. Database queries against SonarQube's `user` table show this. For browser queries, however, SonarQube uses the CAS IdentityProvider as usual.

Because of the lack of extensibility on the part of SonarQube, the solution to this problem is based on two basic realizations:

1. Sonar-CAS-Plugin must internally use the Identity Provider `sonarqube` instead of `cas`.
2. Sonar-CAS plugin now listens for login URLs pointing to identity provider `sonarqube`.

For many versions SonarQube already recognizes the authentication realm based on the realm identifier in the login URL `http://sonar.server.com/sessions/init/${realm}` and selects a matching identity provider based on that. Instead of the `cas` realm (`.../sessions/init/cas`), the Sonar CAS plugin now listens for URLs for the `sonarqube` realm (`.../sessions/init/sonarqube`) and handles web requests here as before (see section "Local Login and Single Sign-on (SSO)").

By identifying the Sonar CAS plugin as `sonarqube`, validation errors for duplicate email addresses or logins are now bypassed. This re-enables REST requests via Basic Authentication. Consequently, user accounts will miss the CAS identity provider mark in SonarQube's user overview even though they were replicated by Sonar-CAS-Plugin.  

Authentication via local user or token are not affected and can be used as usual. See also SonarSource's recommendation on [local users for Sonar scanners](https://docs.sonarqube.org/latest/instance-administration/delegated-auth/).
