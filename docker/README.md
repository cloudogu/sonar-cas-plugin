# Generate new keystore

```bash
keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass changeit  -dname "CN=cas.hitchhiker.com,OU=Planet Manufacturing,O=Magrathea,C=ME" -validity 3600 -keysize 2048
 ```
