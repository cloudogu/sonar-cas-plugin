HOW TO INSTALL CAS SERVER

* Download sources from http://www.jasig.org/cas
* Build with mvn install -DskipTests
* Deploy cas-server-webapp/target/cas.war to web server


HOW TO INSTALL PLUGIN

* Copy the plugin
* Add the following properties to conf/sonar.properties then restart the server


PLUGIN PROPERTIES

sonar.authenticator.createUsers=true
sonar.security.realm=cas

# cas2 or saml
sonar.cas.protocol=cas2

sonar.cas.casServerLoginUrl=http://localhost:8080/cas/login
sonar.cas.casServerUrlPrefix=http://localhost:8080/cas
sonar.cas.sonarServerUrl=http://localhost:9000
sonar.cas.sendGateway=true
sonar.cas.saml1.toleranceMilliseconds=1000