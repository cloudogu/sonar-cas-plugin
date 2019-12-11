# SonarQube CAS plugin

This plugin allows for authentication with [Apereo CAS](https://apereo.github.io/cas/6.0.x/protocol/Protocol-Overview.html) 
authentication.

## Installation

To install this plugin the plugin artifact must be copied to SonarQube's plugin directory. After that SonarQube must be
restarted in order to take affect.

## Resources

Prior knowledge about 

- authentication mechanisms in general,
- authentication with CAS specifically
- authentication inside SonarQube 

are highly recommended.

You should checkout the these resources for further reading:

- [Apereo CAS Protocol Overview](https://apereo.github.io/cas/6.0.x/protocol/Protocol-Overview.html)
- [SonarQube Plugin development](https://docs.sonarqube.org/display/DEV/Developing+a+Plugin)
- [SonarQube authentication](https://docs.sonarqube.org/latest/instance-administration/security/#header-2)

## Plugin configuration

This plugin is configurable in several ways by means of setting the usual properties in the `sonar.properties` file.
You can find the keys and some explanation in the [Plugin Configuration](docs/pluginConfiguration.md) page

## How this plugin works

You can find more about plugin internals in the [Architecture and Internals](docs/architecture.md) page.