# Plugin-Konfiguration

Diese Eigenschaften werden in der typischen Datei `sonar.properties` konfiguriert.

### CAS-Plugin für die Authentifizierung konfigurieren

`sonar.security.realm=cas`

### Benutzern erlauben, sich anzumelden

Erlauben Sie noch unbekannten Benutzern, sich zu authentifizieren. Wenn auf `false` gesetzt, können sich nur bestehende Benutzer authentifizieren.

`sonar.authenticator.createUsers=true`

### CAS-Authentifizierung erzwingen (kein anonymer Zugriff erlaubt)

`sonar.cas.forceCasLogin=true`

### cas3, cas1, cas2 oder saml11

Wählen Sie das Authentifizierungsprotokoll zwischen CAS und SonarQube. Standard ist `cas3`.

`sonar.cas.protocol=cas3`

### Legen Sie die Root-URL des CAS-Servers fest

Sie sollten nach Möglichkeit HTTP/S verwenden. Ohne abschließenden Schrägstrich.

`sonar.cas.casServerUrlPrefix = https://cas.hitchhiker.com:8443/cas`

### Speicherort des CAS-Server-Anmeldeformulars

`sonar.cas.casServerLoginUrl=https://cas.hitchhiker.com:8443/cas/login`

### Root-URL des Sonar-Servers

Ohne endenden Schrägstrich.

`sonar.cas.sonarServerUrl=http://localhost:9000`

### CAS-Server-Abmelde-URL

Obligatorische CAS-Server-Abmelde-URL. Wenn gesetzt, wird die Sonar-Sitzung bei einer CAS-Logout-Anfrage gelöscht. 
Auch vom Logout-Button

`sonar.cas.casServerLogoutUrl=https://cas.hitchhiker.com:8443/cas/logout`

### Gibt an, ob gateway=true an den CAS-Server gesendet werden soll.

Voreinstellung ist false.

`sonar.cas.sendGateway=false`

### Pfad zum CAS-Sitzungsspeicher

SonarQube bietet keine Sitzung (außer durch Ausgabe von JWT-Tokens). Wenn sich der Benutzer abmeldet, wird das Cookie, 
das das notwendige JWT-Token enthält, entfernt.Trotzdem sorgt SonarQube *NICHT* dafür, dass das (nun nicht mehr gültige) 
JWT-Token ignoriert wird. Stattdessen wird das JWT Token weiterhin als gültig angesehen, so dass der Besitzer weiterhin mit 
SonarQube arbeiten kann.

Das CAS-Plugin sorgt dafür, dass vorhandene Token beim Abmelden des Benutzers auf eine schwarze Liste gesetzt werden.
Dazu müssen die Token persistent gespeichert werden, um Server- oder Container-Neustarts oder sogar Container-Neuerstellungen 
zu überdauern. Administratoren sollten dies möglicherweise als eigenes Volume mounten, um mit der Anzahl der nicht abgelaufenen 
Sitzungen skalieren zu können.

Das Verzeichnis sollte sich im Arbeitsverzeichnis von SonarQube befinden.

Das Verzeichnis sollte sich im Arbeitsverzeichnis von SonarQube befinden.

`sonar.cas.sessionStorePath = /opt/sonarqube/data/sonarcas/sessionstore`

## CAS Session Store Bereinigungsintervall

Der CAS-Sitzungsspeicher speichert JWT-Tokens, die ein Ablaufdatum haben. Diese werden aufbewahrt, um Deny- und Allowlisting für JWTs eines Benutzers durchzuführen, um Angreifern den Zugriff auf die alten JWT-Tokens eines Benutzers zu verwehren.

Sobald diese JWTs abgelaufen sind, müssen sie im Hintergrund aus dem Speicher entfernt werden ob. Diese Eigenschaft definiert das
Intervall in Sekunden zwischen den einzelnen Bereinigungsläufen. Stellen Sie das Intervall nicht zu kurz (dies könnte zu unnötiger
CPU-Last) oder zu lang (dies könnte zu unnötiger Belastung des Dateisystems führen).

Die Voreinstellung ist 30 Minuten, 0 deaktiviert die Bereinigung (dies SOLLTE in einer Produktionsumgebung NICHT geschehen)

`sonar.cas.sessionStore.cleanUpIntervalInSeconds = 1800`

### CAS-Rollen-Attribut(e) konfigurieren

Attribute, die die Autoritäten (Gruppen, Rollen usw.) enthalten, denen der Benutzer angehört. Mehrere
Werte sollten mit Kommas ohne weitere Leerzeichen getrennt werden (z.B. 'groups,roles').

sonar.cas.rolesAttributes=groups,roles`

### Attribut, das den vollständigen Namen des Benutzers enthält.

Wird derzeit aufgrund von Sonar-Einschränkungen nicht unterstützt, ist aber mit CAS2-Attributen gelöst. `displayName` ist das
entsprechende Feld im CAS-Ticket, das den vollen Namen des Benutzers enthält.

`sonar.cas.fullNameAttribute=displayName`

### Das Attribut enthält die E-Mail-Adresse des Benutzers.

`mail` ist das entsprechende Feld im CAS-Ticket, das die E-Mail-Adresse des Benutzers enthält.

`sonar.cas.eMailAttribute=mail`

### Konfigurieren Sie die Toleranz der Zeitverschiebung für SAML 1.1-Tickets.

Die Toleranz in Millisekunden für driftende Uhren bei der Validierung von SAML 1.1-Tickets.

Beachten Sie, dass 10 Sekunden für die meisten Umgebungen mit NTP-Zeitsynchronisation mehr als genug sein sollten. Der Standardwert ist 1000 Millisekunden.

`sonar.cas.saml11.toleranceMilliseconds=1000`

### Ignoriere Zertifizierungsprüfungsfehler.

**ACHTUNG! NIEMALS IN PRODUKTIONSUMGEBUNG VERWENDEN! SICHERHEITSRISIKO!**

Dies ist nur für Entwicklungsumgebungen, in denen eine korrekte Zertifikatskette nicht durchführbar ist.

`sonar.cas.disableCertValidation=false`

### Cookie-Alter, das eine Redirect-URL enthält

Wenn der Benutzer abgemeldet ist, kann er eine beliebige SonarQube-URL aufrufen und wird dann an das CAS weitergeleitet. Das CAS selbst kann keine
keine weiteren Informationen und leitet nur auf eine feste SonarQube-URL um. Die ursprüngliche URL, wie sie vom Benutzer aufgerufen wurde, wird
in einem Cookie gespeichert. Nach einer erfolgreichen Anmeldung leitet das System zu dieser URL weiter.

Diese Einstellung steuert, wie lange (in Sekunden) das Cookie gültig sein darf, bis es verworfen wird.

`sonar.cas.urlAfterCasRedirectCookieMaxAgeSeconds=300`
