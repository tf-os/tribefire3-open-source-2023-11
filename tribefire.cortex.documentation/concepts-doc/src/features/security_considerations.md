# Security Considerations

Although Tribefire is designed to be as secure as possible, there are certain standard measures you might take to make tribefire even more intrusion-proof.

## General

The document described below serves as a best practice guide into using tribefire in a more secure way. As tribefire runs on Tomcat one of the most critical aspects is to obey best practices for the Tomcat installation and run-time that either your company uses internally or one of the many best practices that are available online. In addition, available online are benchmarking tools that can help identify weak spots in any Tomcat installation.

> Best practices mentioned below are not entirely tribefire-specific, as you can apply them to virtually any web application in the industry. Our guidelines do not present a ready-made solution - consider them more suggestions and hints.

## Best Practices

* Make sure to change the default password for the `cortex` user.
* Create a password for the `locksmith` user in case you lock the `cortex` account.
* As tribefire runs on Tomcat, the `cortex` user is also the one used in Tomcat. Make sure to change that password to a strong password.
* Consider using the Java Cryptography Extension, enable only specific cyphers and create your own certificate.
* Don't use JKS keystores, use a `PKCS12` file instead.
    > Installer provides a default `keystore.p12` file. To add this certificate to Java’s local truststore, execute the following commands. <br/> <br/> For Linux:<br/> `openssl pkcs12 -in keystore.p12 -clcerts -nokeys -out /tmp/extracted.pem` and `$JAVA_HOME/jre/lib/security: keytool -import -alias ca -file  /tmp/extracted.pem -keystore cacerts -storepass changeit` <br/> <br/> For Windows:<br/> `keytool.exe -exportcert -keystore keystore.p12 -storetype PKCS12 -storepass cortex -alias tribefire -file c:\extracted.crt` and `keytool.exe -import -alias ca -file c:\extracted.crt -keystore cacerts -storepass changeit` <br/> 
* Create a user specifically for running Tomcat. Ensure that the Tomcat user only has the absolutely necessary privileges (i.e. as restricted as possible) e.g. accessing the database or accessing files within the tribefire installation path.
  > Tomcat must not run as root.
* The server which tribefire is deployed and runs on should only be accessible via a secure HTTPS protocol. Make sure to use a valid certificate and not use a self-signed certificate. Even though tribefire comes with a self-signed certificate by default, we recommend not to use it in any production environment.
* It is considered good practice to use a firewall that blocks all IP addresses that are not known and are supposed to have access to the server.
* It is considered good practice to use a reverse proxy which forwards requests to tribefire.
* It is considered good practice to remove the Webapp Manager.
* Make sure that all password fields have the Confidential metadata assigned. Password fields must not be visible by the client.
    > For more information, see [Confidential and NonConfidential](asset://tribefire.cortex.documentation:concepts-doc/metadata/prompt/confidential.md).
* Do not provide `tf-admin` role to non-admin users.
* Randomize the shutdown passcode in the `server.xml` file (`shutdown="..."`).
* Always use HTTPS, also internally. HTTP traffic should be redirected to HTTPS.

> For information about user roles, see [User Roles](asset://tribefire.cortex.documentation:concepts-doc/features/user_roles.md).