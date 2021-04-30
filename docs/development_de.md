## Entwicklung des SonarQube-CAS-Plugins

## CAS-Server-Installation (lokale Entwicklung)

### Was Sie benötigen

- Java JDK >= 11
    - getestet mit der `11.0.1-zulu` Distribution
- Maven
- Docker
- docker-compose

Bevor Sie beginnen, müssen Sie einen erreichbaren Hostnamen wählen. Dieser Hostname wird verwendet für
- Authentifizierungsinteraktion zwischen SonarQube und CAS
- Prüfung des SSL-Zertifikats innerhalb von CAS

1. Erzeugen Sie einen [Keystore für Ihren CAS-Hostnamen](../docker/README.md)
    - alternativ verwenden Sie den Hostnamen `cas.hitchhiker.com`, für den ein Keystore bereitgestellt wird
1. Ändern Sie die statische Benutzerliste und die Attribute
    - Derzeit ist nur ein Benutzer mit statischen Listen konfigurierbar

``Eigenschaften
cas.authn.attributeRepository.stub.attributes.mail=tricia.mcmillan@hitchhiker.com
cas.authn.attributeRepository.stub.attributes.displayName=Tricia McMillan
cas.authn.attributeRepository.stub.attributes.groups=admin

cas.authn.accept.users=admin::secretPassword
``` 

## Sonar CAS-Plugin-Installation (lokale Entwicklung)

1. Ordnen Sie Ihre lokale IP-Adresse den DNS-Namen für eine korrekte SonarQube ⇄ CAS-Interaktion zu
   - Fügen Sie Ihrer `/etc/hosts`-Datei eine Zeile wie diese hinzu:
   - `192.168.1.31 sonar.hitchhiker.com cas.hitchhiker.com`
   - Prüfen Sie, ob die Hostnamen erreichbar sind:
     - `ping cas.hitchhiker.com`
     - `ping sonar.hitchhiker.com`

1. Exportieren Sie Ihre lokale IP-Adresse in Umgebungsvariablen für docker-compose:
   - Entweder mit einem Shell `export` oder mit einer `.env` Datei, die `source`'d werden muss
   - `SONAR_CAS_LOCAL_IP=192.168.1.31`
   - `SONAR_SONAR_LOCAL_IP=192.168.1.31`
1. Kopieren Sie das Plugin
1. Fügen Sie die folgenden Eigenschaften zu `conf/sonar.properties` hinzu und starten Sie den Server neu
1. Erstellen Sie das CAS-Plugin und kopieren Sie es in das SonarQube-Plugin-Verzeichnis

```
mvn clean install
cp target/sonar-cas-plugin-<Version>.jar sonar-home/plugins
```

## SonarQube und CAS mit docker-compose starten

Dies ist so einfach wie 1,2,3, da docker-compose verwendet wird. Stellen Sie also sicher, dass Sie entweder die Images in Ihrem Docker-Cache haben oder eine funktionierende Internetverbindung haben.

Starten Sie beide Server auf einmal im Backup wie folgt:

``` 
docker-compose up -d
```

Und rufen Sie die konfigurierten URLs auf: 

- [CAS](http://cas.hitchhiker.com:9000/cas)
- [SonarQube](http://sonar.hitchhiker.com:9000/sonar)
  - aktuell muss ein Kontextpfad konfiguriert werden
  - konfigurieren Sie `sonar.config` mit diesem Wert `sonar.web.context=/sonar`


### Dateiberechtigungen von `sonar_home`

Wenn SonarQube sich an Ihren Dateisystemrechten zu schaffen macht, ändern Sie einfach die Rechte mit diesem Befehl: 

```
sudo chown 999:999 -R sonar_home/data sonar_home/temp sonar_home/plugins sonar_home/logs
``` 

## SonarQube während der Entwicklung neu starten

Für die CAS-Plugin-Entwicklung müssen Sie SonarQube neu starten, um die aktivierten Code-Änderungen vorzunehmen, etwa so:

```
cp target/sonar-cas-plugin-<Version>.jar sonar-home/plugins
docker-compose restart sonar
```

## Logs lesen

Mit diesen Befehlen können Sie die jeweiligen Log-Ausgaben einsehen:
```
docker-compose logs -f sonar
docker-compose logs -f cas
```

## Plugin-Konfiguration

Dieses Plugin ist auf verschiedene Arten konfigurierbar, indem die üblichen Eigenschaften in der Datei `sonar.properties` gesetzt werden.
Die Schlüssel und einige Erklärungen finden Sie auf der Seite [Plugin-Konfiguration](pluginConfiguration.md)

## Wie dieses Plugin funktioniert

Mehr über die Interna des Plugins finden Sie auf der Seite [Architektur und Interna](architecture.md).
