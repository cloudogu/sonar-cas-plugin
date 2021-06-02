# Releasing Sonar-CAS-Plugin

1. `git checkout master && git pull && git checkout develop && git pull` 
1. `git flow release start NEWVERSION`
1. bump version
    1. update `pom.xml` with NEWVERSION
    1. `git add pom.xml.md && git commit -m "Bump version"`
1. update changelog
    1. update `CHANGELOG.md` in fashion of [keepachangelog.com](keepachangelog.com) 
    1. `git add CHANGELOG.md && git commit -m "update changelog"`
1. `git flow release finish -s NEWVERSION`
1. `git push origin master`
1. `git push origin develop --tags`
1. prepare Github release   
    1. `git checkout NEWVERSION`
    1. `mvn clean package verify`
    1. edit release for NEWVERSION and upload binary/pom/checksum files from `target/`
