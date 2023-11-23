# Storing User Sessions Externally

You can configure Tribefire to store user sessions in an external database.

## General

A SMOOD access is used to store user sessions. This allows for a simple startup as a SMOOD access does not involve other technologies and doesn't require you to configure additional connections. There are cases where you want to change that behavior. You normally want to work with a scalable system where a database is better fitting.

Consider the following example. You have several tribefire instances and a load balancer decides which instance your request goes through. As tribefire stores sessions in SMOOD by default, it may happen that your request goes through an instance which does not have your open session stored. If you are running multiple instances you might run into user session errors, so it is best to consider storing the sessions externally. This way, the session is available for every instance, not just the one it was created in.

> The default wiring of tribefire Services supports only either SMOOD or a database for user session storage.

## Changing User Session Data Source

1. Go to `TRIBEFIRE_INSTALLATION_DIRECTORY/host/conf/` and open the `catalina.properties` file for editing.
2. Add the following property:

```bash
   TRIBEFIRE_CONFIGURATION_DIR=<TRIBEFIRE_INSTALLATION_DIRECTORY>/tribefire/conf/
```

   This property defines a path to an external configuration file where you can introduce custom databases.
   > The `<TRIBEFIRE_INSTALLATION_DIRECTORY>` phrase is a placeholder. Provide the path to your tribefire installation directory there. You can also specify an absolute location of this file. For more information, see [Configuring tribefire with configuration.json](configuring_with_json.md).
3. Go to `TRIBEFIRE_INSTALLATION_DIRECTORY/tribefire/conf/` and rename the `configuration.json.template` file to `configuration.json`.
4. Open the `configuration.json` file for editing. The file has skeleton code and is empty by default.
5. Provide your database configuration in the file, for example:

```json
   [
        {
         "_type" : "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
         "bindId" : "tribefire-user-sessions-db",
         "denotation" : {
           "_type" : "com.braintribe.model.deployment.database.pool.HikariCpConnectionPool",
           "externalId" : "tribefire-user-sessions-db",
           "name" : "Locking DB",
           "connectionDescriptor" : {
               "_type" : "com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor",
               "user" : "cortex",
               "password" : "cortex",
               "url" : "jdbc:mysql://localhost:3306/TF",
               "driver" : "com.mysql.jdbc.Driver"
           }
         }
        }
   ]
```

6. Save the file and restart your tribefire server.

## Password Protection

Needless to say, it is not good practice to have passwords stored as plain text.

There is a possibility to use an obfuscated value in the `configuration.json` file. A tool for obfuscating passwords is included in the `platform-api.jar` file.

> The tool does **NOT** encrypt the password, however an obfuscated password is still more secure than a password stored as plain text.

### Obfuscating a Password

To obfuscate the password `comeonchelsea`:

1. Run the following command:

   ```java
   java -cp /PATH_TO_YOUR_LOCAL_REPOSITORY/com/braintribe/common/codec-api/1.0.11/codec-api-1.0.11.jar:/PATH_TO_YOUR_LOCAL_REPOSITORY/com/braintribe/common/platform-api/1.0.48/platform-api-1.0.48.jar com.braintribe.utils.Obfuscation comeonchelsea
   ```

   This results in: 

   ```
   Password: comeonchelsea obfuscated to : OBF:1rwh1v2h1xfd1ugo1v2h1vgl1san1vg91v1x1uh21xfp1v1x1rwd deobfuscated to : comeonchelsea
   ```

   > The versions of the `codec-api` and `platform-api` .jar files might be different. Make sure to change the command above to reflect the versions you have in your local repository. If you're on Windows, you need to enclose the paths to the .jar files with `" "` and change the `:` separator to a `;` (semicolon).

2. In the `configuration.json` file, change the value of the `password` property to `OBF:1rwh1v2h1xfd1ugo1v2h1vgl1san1vg91v1x1uh21xfp1v1x1rwd`.

   > The `OBF:` prefix is vital for a successful deobfuscation of the password.
