# Developing the SonarQube CAS plugin

## CAS server installation (local development)

### What you need

- Java JDK >= 11
  - tested with the `11.0.1-zulu` distribution
- Maven
- docker
- docker-compose

Before you start, you need to pick a reachable host name. This host name is used for
- Authentication interaction between SonarQube and CAS
- SSL certificate check within CAS  

1. Generate a [keystore for your CAS host name](../docker/README.md) 
   - alternatively go with the host name `cas.hitchhiker.com` for which a keystore is provided
1. Modify the static user list and attributes
   - currently only one user is configurable with static lists

```properties
cas.authn.attributeRepository.stub.attributes.mail=tricia.mcmillan@hitchhiker.com
cas.authn.attributeRepository.stub.attributes.displayName=Tricia McMillan
cas.authn.attributeRepository.stub.attributes.groups=admin

cas.authn.accept.users=admin::secretPassword
``` 

## Sonar CAS plugin installation (local development)

1. Map your local IP address to DNS names for proper SonarQube ⇄ CAS interaction
   - add a line to your `/etc/hosts` file like this:
   - `192.168.1.31 sonar.hitchhiker.com cas.hitchhiker.com`
   - check if the host names are reachable:
     - `ping cas.hitchhiker.com`
     - `ping sonar.hitchhiker.com`

1. Export your local IP address to environment variables for docker-compose:
   - either with a shell `export` or with `.env` file to be `source`'d
   - `SONAR_CAS_LOCAL_IP=192.168.1.31`
   - `SONAR_SONAR_LOCAL_IP=192.168.1.31`
1. Copy the plugin
1. Add the following properties to `conf/sonar.properties` then restart the server
1. build the CAS plugin and copy it into the SonarQube plugins directory

```
mvn clean install
cp target/sonar-cas-plugin-<version>.jar sonar-home/plugins
```

## Start SonarQube and CAS with docker-compose

This is easy as 1,2,3 because docker-compose is used. So make sure you have either the images in your docker cache or have a working internet connection.

Start both servers at once in the backup like this:

``` 
docker-compose up -d
```

And hit the configured URLs: 

- [CAS](http://cas.hitchhiker.com:9000/cas)
- [SonarQube](http://sonar.hitchhiker.com:9000/sonar)
  - currently it must be configured to have a context path
  - configure `sonar.config` with this value `sonar.web.context=/sonar`


### File Permissions of `sonar_home`

If SonarQube acts up on your filesystem permission just change the permissions with this command: 

```
sudo chown 999:999 -R sonar-home/data sonar-home/temp sonar-home/plugins sonar-home/logs
``` 

## Restart SonarQube during development

For CAS plugin development you need to restart SonarQube in order to make the activate code changes, like this:

```
cp target/sonar-cas-plugin-<version>.jar sonar-home/plugins
docker-compose restart sonar
```

## Reading logs

You can view the respective log output with these commands:
```
docker-compose logs -f sonar
docker-compose logs -f cas
```

## Plugin configuration

This plugin is configurable in several ways by means of setting the usual properties in the `sonar.properties` file.
You can find the keys and some explanation in the [Plugin Configuration](pluginConfiguration.md) page

## How this plugin works

You can find more about plugin internals in the [Architecture and Internals](architecture.md) page.