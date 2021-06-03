# Architektur

## Grundlegende Entitäten

### JWT - JSON Web Token

SonarQube gibt bei einer erfolgreichen Authentifizierung ein [JWT](https://tools.ietf.org/html/rfc7519) aus. Dieses JWT ist eine offene
Methode zur sicheren Darstellung von Ansprüchen zwischen zwei Parteien. Das JWT von SonarQube enthält unter anderem:

- eine ID,
- den Benutzernamen des Benutzers
- ein Verfallsdatum

### Service-Ticket

Das Service-Ticket ist ein Code, der verwendet wird, um die Authentifizierungsanfrage des Benutzers direkt mit dem
authentifizierenden System, nämlich dem CAS, zu validieren.

## Anwendungsfälle innerhalb des Authentifizierungslebenszyklus

Authentifizierung ist eine wackelige Sache, auch ohne ein externes Authentifizierungssystem. Dieses Plug-in konzentriert sich auf diese
Anwendungsfälle:

1. Lokales Log-in und Single Sign-on (SSO)
1. Gewöhnliche Ressourcenanforderung
1. JWT-Aktualisierung
1. Lokale Abmeldung und einmalige Abmeldung (SLO)
    1. Abmeldung über SonarQube
    1. Einzelne Abmeldung
1. Aufräumen

### Lokale Anmeldung und Single Sign-on (SSO)

Aufgrund der Natur des CAS ist eine Anmeldung am CAS so einfach wie 1-2-3. Dies beinhaltet SSO mit Diensten, die bei CAS registriert sind.

1. Benutzer möchte Ressource abrufen
    - gibt URL im Browser ein: `https://sonar.server.com/`
1. ForceCasLoginFilter erkennt den Anmelde-Nutzungsfall
    - leitet den Benutzer auf die CAS-Login-Seite um
    - `https://cas.server.com/cas/login?service=http://sonar.server.com/sessions/init/sonarqube`
1. Benutzer meldet sich mit Anmeldedaten bei CAS an
1. CAS leitet den Benutzer zurück zu SonarQube um
    - fügt Service-Ticket-Parameter hinzu
1. CasIdentityProvider validiert Service-Ticket direkt mit CAS
    - diese Validierung ist unabhängig vom Browser
    - Sonar und CAS kommunizieren über einen direkten Kanal
1. CAS antwortet mit Gültigkeit und Benutzerattributen
    - CasIdentityProvider authentifiziert sich gegen SonarQube
1. CasIdentityProvider holt und speichert JWT sowie das Service-Ticket
    - im Sitzungsspeicher gespeichert
1. SonarQube liefert die ursprünglich angeforderte Ressource
    - Benutzer erhält auch Authentifizierungs-Cookie mit JWT

### Gewöhnliche Ressourcenanforderung

Sobald der Benutzer eingeloggt ist, muss jede Anfrage mit der Blacklist geprüft werden.

1. Benutzer möchte Ressource erhalten
    - Browser enthält gültigen und nicht abgelaufenen JWT-Cookie
1. ForceCasLoginFilter fragt den Sitzungsspeicher, ob das JWT abgelaufen ist
1. Sitzungsspeicher antwortet, dass JWT gut ist
1. SonarQube liefert die ursprünglich angeforderte Ressource
    - JWT-Cookie ist immer noch derselbe

### JWT-Aktualisierung

Von Zeit zu Zeit gibt SonarQube ein aktualisiertes JWT aus, um sicherzustellen, dass ein angemeldeter Benutzer seine Arbeit fortsetzen kann.
Dieses JWT enthält aktualisierte Ablaufinformationen, die ebenfalls in den JWT-Sitzungsspeicher aufgenommen werden müssen.

1. Benutzer möchte Ressource abrufen
    - Browser enthält gültigen und nicht abgelaufenen JWT-Cookie
1. CasTokenRefreshFilter findet neues JWT-Cookie
1. CasTokenRefreshFilter aktualisiert Sitzungsspeicher mit Ablaufdatum
    - nur das Datum ändert sich im Cookie
1. ForceCasLoginFilter fragt den Sitzungsspeicher, ob das JWT abgelaufen ist
1. Sitzungsspeicher antwortet, dass JWT gut ist
1. SonarQube liefert die ursprünglich angeforderte Ressource
    - Benutzer erhält aktualisierten JWT-Cookie

### Abmelden

In Bezug auf das Sonar CAS-Plugin gibt es zwei Möglichkeiten, sich von SonarQube abzumelden.

1. Abmeldung über SonarQube
1. Einzelabmeldung (SLO)

An einem bestimmten Punkt sind sich beide ähnlich, da letztlich der Rückkanal-Logout-Mechanismus verwendet wird.

#### Abmeldung über SonarQube

1. Benutzer meldet sich in SonarQube an (wie gewohnt)
1. CasSonarSignOutInjectorFilter injiziert Javascript in die angeforderte HTML-Datei
1. Benutzer klickt auf Menü > Abmelden
1. Eingeschleustes Javascript schreibt die Browser-Position um und zeigt auf die CAS-Logout-Seite
1. CAS empfängt Abmeldung.
1. CAS sendet Back-Channel-Abmeldeanforderung an alle registrierten Dienste
1. CasIdentityProvider empfängt Logout mit Service-Ticket
1. CasIdentityProvider holt das JWT aus dem Sitzungsspeicher
    - der Sitzungsspeicher enthält eine Referenz vom Service-Ticket zur JWT-ID
    - mit der JWT-ID wird das gespeicherte JWT geholt
1. CasIdentityProvider invalidiert das JWT des Benutzers
    - das ungültig gemachte JWT aktualisiert das ursprüngliche JWT im Sitzungsspeicher

#### Einmaliges Abmelden (SLO)

1. Benutzer meldet sich in SonarQube an (wie gewohnt)
1. Benutzer wechselt zu einem Drittanbieterdienst, der beim CAS registriert ist
1. Benutzer meldet sich ab
1. CAS empfängt Abmeldung.
1. CAS sendet Back-Channel-Abmeldeanforderung an alle registrierten Dienste
1. CasIdentityProvider empfängt Logout mit Service-Ticket
1. CasIdentityProvider holt das JWT aus dem Sitzungsspeicher
    - der Sitzungsspeicher enthält eine Referenz vom Service-Ticket zur JWT-ID
    - mit der JWT-ID wird das gespeicherte JWT geholt
1. CasIdentityProvider invalidiert das JWT des Benutzers
    - das ungültig gemachte JWT aktualisiert das ursprüngliche JWT im Sitzungsspeicher

### Aufräumen

Dies wird von einer Hintergrundaufgabe erledigt. Sie durchläuft alle gespeicherten JWT- und Service-Ticket-Dateien (siehe den Abschnitt FileSessionStore weiter unten für weitere Informationen)

1. In festen Intervallen liest ein Hintergrund alle gespeicherten JWTs und zugehörigen Service-Tickets aus
2. alle JWTs werden auf ihr Ablaufdatum untersucht
3. abgelaufene JWTs und deren zugehöriges Service-Ticket werden entfernt


## Entscheidende Komponenten

Die Authentifizierung innerhalb von SonarQube ist ein komplexer Prozess, der verschiedene Klassen benötigt, um zu funktionieren. In diesem Abschnitt werden nur die
wichtigsten Komponenten grob beschrieben, sodass man eine Vorstellung davon bekommt, wie es funktioniert.

### Allgemeiner Mechanismus von `CasPlugin`

`CasPlugin` ist der Haupteinstiegspunkt, der den Plugin-Mechanismus von SonarQube nutzt. Alle Komponenten, die vom Sonar CAS
Plugin verwendet werden, müssen in dessen `collectExtensions()` Methode registriert werden. Auf diese Weise sind die Komponenten für SonarQube's
Injektion von Abhängigkeiten. Sofern sie nicht manuell instanziert werden, muss jede Komponente entweder einen Standardkonstruktor oder einen
Konstruktor haben, der aus registrierten Komponenten besteht.

In Bezug auf `ServletFilter`-Komponenten darf jeder Filter die angegebene `FilterChain` nicht mehr als einmal aufrufen. Andernfalls kann es passieren, dass Inhalte mehr als einmal geschrieben werden, was zu einer Fülle von seltsamen CSS/Script-Verhalten oder sichtbaren Inhalten führen kann.

### CasIdentityProvider für browserbasierte Anfragen

Der `CasIdentityProvider` kümmert sich um das Ein- und Ausloggen. Das Einloggen ist der bei weitem komplexere Prozess von beiden der oben beschrieben ist.

### CasAuthenticator für REST-basierte Anfragen

Der `CasAuthenticator` kümmert sich um HTTP-API-Aufrufe in Richtung SonarQube.

### ForceCasLoginFilter

Der `ForceCasLoginFilter` prüft bei jeder Anfrage, ob die Anfrage erlaubt ist, indem er den Session Store mit dem
JWT aus der Anfrage des Benutzers.

Anfragen auf statischen Ressourcen werden erlaubt, da diese u. U. asynchron vor einer Authentifizierung ausgeführt werden können. Innerhalb des Authentifizierungsprozesses werden Benutzende anhand des Feldes "LOGIN" erkannt. Ein leeres Feld oder ein Login mit dem Wert `-` bedeutet, dass noch keine Authentifizierung stattgefunden hat.

### FileSessionStore

Der `FileSessionStore` ist eine Implementierung eines Sitzungsspeichers. Der Sitzungsspeicher verwaltet eine White-/Blacklist mit allen
JWTs und deren zugehörigen Service-Tickets.

Der FileSessionStore speichert zwei Dateien pro Anmeldung:

- die JWT-Datei (Dateiname = JWT-ID)
   - speichert die JWT-ID
   - Verfallsdatum
   - die Information, ob das JWT ungültig ist (d. h. nach einer Abmeldung auf der schwarzen Liste steht)
- die Service-Ticket-Datei (Dateiname = Service-Ticket-ID)
   - speichert die JWT-ID

Sobald ein JWT abgelaufen ist, können sowohl die JWT-Datei als auch die Service-Ticket-Datei entfernt werden.

## Fehlerbehandlung

Die flexible Plugin-Architektur von SonarQube hat einen Nachteil, wenn es um die Fehlerbehandlung geht. Alle Fehler von Plugins scheinen ignoriert zu werden. In der Konsequenz bedeutet dies, dass alle Ausnahmen nicht an die Spitze des startenden Prozesses blubbern dürfen (ein üblicher Vorgang für Java-Anwendungen).

Exception Bubbling kann innerhalb von abhängigen Plugin-Klassen verwendet werden, mit der Einschränkung, dass stattdessen alle Top-Level-Funktionalität alle Ausnahmen fangen und sie mit dem Log-Level ERROR protokollieren **MUSS**, so wie hier:

```java
public class IhrNeuerFilter { 
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
        try {
            // irgendetwas tun
        } catch (Exception e) {
            LOG.error("YourNewFilter doFilter failed", e);
        }
    }
}
``` 

## Architekturänderungen mit SonarQube 8.x

Mit dem Versionssprung von SonarQube 7.x auf 8.x muss das Sonar-CAS-Plugin auf Neuerungen von SonarQube reagieren, um weiterhin funktionstüchtig zu bleiben. Dieser Abschnitt erläutert diese Änderungen vor dem Hintergrund von SonarQubes Arbeitsweise.

SonarQube hat mit Version 8.x die Ermittlung der Benutzer abgeändert:

Bei REST-Anfragen mit Basic Auth übernimmt SonarQube ausschließlich eigene, nicht erweiterbare Identity Provider. Ohne Änderungen am Sonar-CAS-Plugin führen solche Abfragen u. U. zu Authentifizierungsfehlern, die hauptsächlich auf potenzielle Dopplungen in der Emailadresse oder im Loginbezeichner beruhen. SonarQube ignoriert dabei den IdentityProvider des CAS-Plugins und benutzt stattdessen intern das Realm `sonarqube`. Dies zeigen Datenbankabfragen gegenüber SonarQubes `user`-Tabelle. Für Browser-Anfragen benutzt SonarQube allerdings wie gewohnt den CAS-IdentityProvider.

Wegen der mangelnden Erweiterbarkeit seitens SonarQube beruht die Lösung dieses Problems auf zwei grundlegenden Erkenntnissen:

1. Sonar-CAS-Plugin muss intern der Identity Provider `sonarqube` anstelle von `cas` verwendet werden.
2. Sonar-CAS-Plugin horcht nun auf Login-URLs, die auf den Identity Provider `sonarqube` deuten

SonarQube erkennt den Authentifizierungsbereich anhand des Realm-Identifizierers in der Login-URL `http://sonar.server.com/sessions/init/${realm}` und wählt anhand dessen einen passenden Identity Provider aus. Anstelle vom `cas`-Realm (`.../sessions/init/cas`) horcht das Sonar-CAS-Plugin nun auf URLs für das `sonarqube`-Realm (`.../sessions/init/sonarqube`) und behandelt hier die Web-Anfragen wie zuvor (siehe Abschnitt "Lokale Anmeldung und Single Sign-on (SSO)").

Durch die Identifikation des Sonar-CAS-Plugin als `sonarqube` werden nun Validierungsfehler für doppelte Emailadressen oder Logins umgangen. Dies ermöglicht wieder REST-Anfragen per Basic Authentication. In der Benutzerübersicht von SonarQube führt die Änderung des Realms dazu, dass die vom CAS-Plugin replizierten Benutzer nicht mehr mit `CAS` markiert werden. 

Authentifizierung per lokalem Benutzer oder Token sind hiervon nicht betroffen und können wie gewohnt eingesetzt werden. Siehe hierzu auch die Empfehlung von SonarSource zu [lokalen Benutzern für Sonar-Scanner](https://docs.sonarqube.org/latest/instance-administration/delegated-auth/).
