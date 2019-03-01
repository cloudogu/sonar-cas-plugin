# Plugin configuration

These properties are configured within the typical `sonar.properties` file.

### configure CAS plugin to handle authentication

`sonar.security.realm=cas`

### Allow Users to sign up

`sonar.authenticator.createUsers=true`

### force CAS authentication (no anonymous access allowed)

`sonar.cas.forceCasLogin=true`

### cas3, cas1, cas2 or saml11

Select the authentication protocol between CAS and SonarQube. Default is `cas3`.

`sonar.cas.protocol=cas3`

### Set the root URL of the CAS server

You should use HTTP/S where possible. Without an ending slash.

`sonar.cas.casServerUrlPrefix = https://cas.hitchhiker.com:8443/cas`

### Location of the CAS server login form

`sonar.cas.casServerLoginUrl=https://cas.hitchhiker.com:8443/cas/login`

### Sonar server root URL

Without ending slash.

`sonar.cas.sonarServerUrl=http://localhost:9000`

### CAS server logout URL

mandatory CAS server logout URL. If set, sonar session will be deleted on CAS logout request. Also from the logout-button

`sonar.cas.casServerLogoutUrl=https://cas.hitchhiker.com:8443/cas/logout`

### Specifies whether gateway=true should be sent to the CAS server.

Default is false.

`sonar.cas.sendGateway=false`

### Path to CAS Session Store

As SonarQube does not provide a session (other than by issueing JWT tokens). When the user
logs out, the cookie containing the necessary JWT token is removed. WhatBut SonarQube *DOES
NOT* ensure that the JWT token (which is now no-longer valid) is ignored. Instead the JWT
token is still valid, enabling anyone to use SonarQube. 
  
The CAS plugin makes sure to blacklist existing tokens when the user logs out. In order to do
this the tokens must be stored persistently in order to outlive server or container restarts or even container recreations. Administrators may want to mount this as its own volume in order to scale with number of unexpired sessions.

The directory should live in SonarQube's working directory.

`sonar.cas.sessionStorePath = /opt/sonarqube/data/sonarcas/sessionstore`

## CAS Session Store clean up interval

The CAS session store stores JWT tokens which have an expiration date. These are kept for black- and whitelisting
JWTs from a user in order to prohibit attackers which gained access to a user's old JWT tokens.
 
Once these JWTs are expired they need to be removed from the store in a background ob. This property defines the
interval in seconds between each clean up run. Do not set the interval too short (this could lead to unnecessary
CPU load) or too long (this could lead to unnecessary filesystem load).
  
Default is 30 minutes, 0 disables the cleanup (this SHOULD NOT be done in a production environment)

`sonar.cas.sessionStore.cleanUpIntervalInSeconds = 1800`

### Configure CAS Attribute(s) 

Attributes holding the authorities (groups, roles, etc.) the user belongs to. Multiple
values should be separated with commas without further whitespace (e.g. 'groups,roles').

`sonar.cas.rolesAttributes=groups,roles`

### Attribute holding the user's full name.

Currently not supported related to Sonar limitations but is solved with CAS2 attributes. `displayName` is the 
appropriate field in the CAS ticket which contains the user's full name.

`sonar.cas.fullNameAttribute=displayName`

### Attribute holding the user's email address.

`mail` is the appropriate field in the CAS ticket which contains the user's email address.

`sonar.cas.eMailAttribute=mail`

### Configure clock drifting tolerance for SAML 1.1 tickets.

The tolerance in milliseconds for drifting clocks when validating SAML 1.1 tickets.

 Note that 10 seconds should be more than enough for most environments that have NTP time synchronization. Default is 1000 milliseconds.

`sonar.cas.saml11.toleranceMilliseconds=1000`

### Ignore certification validation errors.

**CAUTION! NEVER USE IN PROD! SECURITY RISK!**

This is only for development environments where a proper certificate chain is unfeasible. 

`sonar.cas.disableCertValidation=false`