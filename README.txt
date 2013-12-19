HOW TO INSTALL CAS SERVER

* Download sources from http://www.jasig.org/cas
* Build with mvn install -DskipTests
* Deploy cas-server-webapp/target/cas.war to web server
* Any user with login equal to password is authenticated, for example login 'simon' and password 'simon'


HOW TO INSTALL PLUGIN

* Copy the plugin
* Add the following properties to conf/sonar.properties then restart the server


PLUGIN PROPERTIES

# enable CAS plugin
sonar.security.realm=cas

# this property must be set to true
sonar.authenticator.createUsers=true

# force CAS authentication (no anonymous access allowed)
sonar.cas.forceCasLogin=true

# cas1, cas2 or saml11
sonar.cas.protocol=cas2

# Location of the CAS server login form, i.e. https://localhost:8443/cas/login
sonar.cas.casServerLoginUrl=http://localhost:8080/cas/login

# CAS server root URL, i.e. https://localhost:8443/cas
sonar.cas.casServerUrlPrefix=http://localhost:8080/cas

# Sonar server root URL, without ending slash
sonar.cas.sonarServerUrl=http://localhost:9000

# Optional CAS server logout URL. If set, sonar session will be deleted on CAS logout request.
#sonar.cas.casServerLogoutUrl=http://localhost:8080/cas/logout

# Specifies whether gateway=true should be sent to the CAS server. Default is false.
#sonar.cas.sendGateway=false

# The tolerance in milliseconds for drifting clocks when validating SAML 1.1 tickets.
# Note that 10 seconds should be more than enough for most environments that have NTP time synchronization.
# Default is 1000 milliseconds.
#sonar.cas.saml11.toleranceMilliseconds=1000

# Ignore certification validation errors. CAUTION! NEVER USE IN PROD! SECURITY RISK!
sonar.cas.disableCertValidation=false