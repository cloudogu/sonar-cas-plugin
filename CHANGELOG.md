# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
Sonar CAS Plugin now supports Sonar 2025.x
- use Http instead of HttpServlet components
- retrieve request attributes with Reflection
- simplify cas logout procedure
    - move script to Sonar
- use SonarCookie instead of JavaX Cookie
- remove Http conversion
- Update Sonar Plugin API to 11.0.0.2664

## [v5.1.0](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v5.1.0) - 2024-09-18
### Changed
- Relicense to AGPL-3.0-only

## [v5.0.2](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v5.0.2) - 2023-05-15
### Fixed
- Set java-target-version to 11 to be compatible with Java-11 runtimes (#52)

## [v5.0.1](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v5.0.1) - 2023-05-05
### Changed
- Updated dependencies to remove CVEs

## [v5.0.0](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v5.0.0) - 2023-05-03
### Changed
- Upgrade Sonar-PluginAPI to v9.14.0.375
- Implement AutoCloseable in SessionStoreCleaner to close the CleanUp-Timer 
- Upgrade to JDK 17

## [v4.2.1](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v4.2.1) - 2022-08-22
### Changed
- Add `/static` to list of routes which do not need authentication (#44)

## [v4.2.0](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v4.2.0) - 2021-11-02
### Added
- Add properties key `sonar.cas.userSecureRedirectCookies` to configure the redirect cookie's `secure` flag
   - if not configured the cookie's secure flag defaults to `true`

### Fixed
- Fixed insecure redirect request after login (#39)

## [v4.1.0](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v4.1.0) - 2021-07-28
### Added
- Add Proxy ticketing against SonarQube API (#36)
   - you can find requirements and more information about this topic in the [documentation](docs/architecture_en.md)

## [v4.0.0](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v4.0.0) - 2021-06-03

Breaking change ahead.

### Fixed
- Fixes defective CAS group replication when the group list was empty (#34)
   - The group replication behaviour now defaults to CAS group replication.
   - The `sonar.properties` key `sonar.cas.groupReplication` must be set to `sonarqube` if local SonarQube groups are
     preferred.

## [v3.0.1](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v3.0.1) - 2021-06-03
### Fixed
- Fixes defective redirect behaviour for repeated unauthenticated requests (#32)
   - this fix also allows unauthenticated requests for static resources

## [v3.0.0](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v3.0.0) - 2021-06-02

Breaking change ahead.

### Changed
- this release of Sonar-CAS-Plugin only supports SonarQube v8.9 or later (#30)
   - user accounts which were replicated with CAS show now up without the CAS identity provider mark in the User
     overview

### Fixed
- switch name of security realm `cas` to `sonarqube` to ensure authentication via REST API with SonarQube 8.9 (#30)

## [v2.0.1](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v2.0.1) - 2019-12-19
### Changed
- Refactored code so it can be compiled with Java 8

## [v2.0.0](https://github.com/cloudogu/sonar-cas-plugin/releases/tag/v2.0.0) - 2019-12-11

Breaking change ahead.

### Changed
- Support SonarQube 7.9 LTS
   - In order to support Single Logout (SLO) the UI Menu
   - This plugin version does explicitly not work with the previous SonarQube 6.7 LTS version

### Fixed
- Fixed double content rendering of non-HTML resources
