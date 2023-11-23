# Setting Up Shiro Module
Shiro module enables you to support different authentication types like: google auth, facebook auth, instagram auth, keycloak auth, and others.
The Shiro module is based on [Apache Shiro](https://www.infoq.com/articles/apache-shiro/).

To set up Shiro module in your project:
1. Add the following dependency to your project aggregator `pom.xml`:
  ```
  <dependency>
  <groupId>tribefire.extension.shiro</groupId>
  <artifactId>shiro-setup</artifactId>
  <version>$

  {V.tribefire.extension.shiro}
  </version>
  <classifier>asset</classifier>
  <type>man</type>
  <?tag asset?>
  </dependency>
  ```
2. Build your project with jinni:

3. Get the `clientId` and `secretKey` from the authentication provider (Google, Facebook, etc.).
4. Encript `clientId` and `secreteKey` from the previous step with `jinni encrypt --value <clientId>`
5. Set the following properties in tribefire.properties files:
    (example just for Google)

	 ```
	     SHIRO_GOOGLE_CLIENTID_ENCRYPTED= {encrypted_google_client_id}
	     SHIRO_GOOGLE_SECRET_ENCRYPTED= {encrypted_google_secret}
	     SHIRO_ADD_SESSION_PARAMETER_ON_REDIRECT=true
	     SHIRO_LOGIN_USERROLESMAP=.*=bbone-outh-user
	     SHIRO_ENABLE_GOOGLE=true
	     SHIRO_ENABLE_TWITTER=false
	     SHIRO_ENABLE_FACEBOOK=false
	     SHIRO_ENABLE_GITHUB=false
	     SHIRO_ENABLE_AZUREAD=false
	     SHIRO_ENABLE_COGNITO=false
	     SHIRO_ENABLE_OKTA=false
	     SHIRO_ENABLE_INSTAGRAM=false
	     SHIRO_SHOW_STANDARD_LOGIN_FORM=true
	     ```

6. change `TRIBEFIRE_PUBLIC_SERVICES_URL` to `http://<host>:<port>/tribefire-services
`
7. Start Tribefire.

8. go to http://localhost:8080/tribefire-services/component/remote-login/ to test it your implementation.
