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

lorem ipsum

1. Login
1. Usual resource request
1. JWT refresh
1. Logout
   1. Logout over SonarQube
   1. Single Logout (SLO)
1. Clean-up

### Login

1. User wants to get resource
   - enters URL in Browser: https://sonar.server.com/
1. ForceCasLoginFilter recognizes log-in use-case
   - redirect user to CAS log-in page
   - https://cas.server.com/cas/login?service=http://sonar.server.com/sessions/init/cas
1. User logs into CAS with credentials
1. CAS redirects user to back to SonarQube
   - adds service ticket parameter
1. CasIdentityProvider validates service ticket directly with CAS
   - this validation is independent from the browser
   - Sonar and CAS communicate on a direct channel
1. CAS replies with validity and user attributes
   - CasIdentityProvider authenticates against SonarQube
1. CasIdentityProvider fetches and stores JWT as well as service ticket
   - stored in session store
1. SonarQube delivers originally requested resource
   - user receives also authentication cookie with JWT

### Usual resource request

1. User wants to get resource
   - Browser contains valid and unexpired JWT cookie
1. ForceCasLoginFilter asks session store if JWT is expired
1. Session store replies that JWT is good
1. SonarQube delivers originally requested resource
   - JWT cookie is still the same

### JWT refresh

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

#### Logout over SonarQube

1. User logs in into SonarQube (as usual)
1. CasSonarSignOutInjectorFilter injects Javascript into the requested HTML file
1. User clicks menu > logout
1. Injected Javascript rewrites browser location and points to CAS logout page
1. CAS receives log out.
1. CAS sends back-channel logout request to all registered services
1. CasIdentityProvider receives logout with service ticket
1. CasIdentityProvider invalidates user's JWT
   - invalidated JWT updates the original JWT in the session store

#### Single Logout (SLO)

1. User logs in into SonarQube (as usual)
1. User changes to third party service which is registered with the CAS
1. User logs out
1. CAS receives log out.
1. CAS sends back-channel logout request to all registered services
1. CasIdentityProvider receives logout with service ticket
1. CasIdentityProvider invalidates user's JWT
   - invalidated JWT updates the original JWT in the session store

### Clean-up

This is done by a background task. It iterates all saved JWT and service ticket files (see the FileSessionStore section below for more information)

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

### CasIdentityProvider for browser-based requests

The `CasIdentityProvider` takes care of logging-in or logging-out. Logging-in is by far the more complex process of both
which is described above.

### CasAuthenticator for REST-based requests

The `CasAuthenticator` takes care of HTTP-API calls toward SonarQube.

### ForceCasLoginFilter

The `ForceCasLoginFilter` checks for every request if the request is permitted by checking the session store with the
JWT from the user's request.

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