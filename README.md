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

---
## What is the Cloudogu EcoSystem?
The Cloudogu EcoSystem is an open platform, which lets you choose how and where your team creates great software. Each service or tool is delivered as a Dogu, a Docker container. Each Dogu can easily be integrated in your environment just by pulling it from our registry.

We have a growing number of ready-to-use Dogus, e.g. SCM-Manager, Jenkins, Nexus Repository, SonarQube, Redmine and many more. Every Dogu can be tailored to your specific needs. Take advantage of a central authentication service, a dynamic navigation, that lets you easily switch between the web UIs and a smart configuration magic, which automatically detects and responds to dependencies between Dogus.

The Cloudogu EcoSystem is open source and it runs either on-premises or in the cloud. The Cloudogu EcoSystem is developed by Cloudogu GmbH under [AGPL-3.0-only](https://spdx.org/licenses/AGPL-3.0-only.html).

## License
Copyright © 2020 - present Cloudogu GmbH
This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License along with this program. If not, see https://www.gnu.org/licenses/.
See [LICENSE](LICENSE) for details.


---
MADE WITH :heart:&nbsp;FOR DEV ADDICTS. [Legal notice / Imprint](https://cloudogu.com/en/imprint/?mtm_campaign=ecosystem&mtm_kwd=imprint&mtm_source=github&mtm_medium=link)
