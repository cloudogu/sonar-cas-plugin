# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

[Unreleased]

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
  - user accounts which were replicated with CAS show now up without the CAS identity provider mark in the User overview

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
