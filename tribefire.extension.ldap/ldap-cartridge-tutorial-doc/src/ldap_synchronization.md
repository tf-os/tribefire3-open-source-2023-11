# LDAP - Disabling User Synchronization

tribefire has a built-in mechanism where on startup it searches the user registry for the two default user accounts that are used to set up the tribefire platform. These users are `Locksmith` and `Cortex`, which are used to fix any security problems and to configure administrative tasks of tribefire respectively. It is recommended that as part of the configuration of your tribefire installation that new users are defined (either in the default "auth" access or via a directory service, such as using the LDAP cartridge).

The idea behind this protective mechanism is that if all users are deleted by accident or lost through some error, restarting the server will recreate these users – Locksmith and Cortex – so that it is possible to login and regain access to the platform. However, one problem is that when swapping the authentication source generally these users are not be found in the user registry, and since the LDAP Cartridge is read-only, tribefire will attempt to recreate these users but will fail, causing an error.

There is a switch for this mechanism, so that on startup tribefire will not check for these users. This is a required step when setting up the LDAP cartridge and can be done by editing the `catalina.properties` of your Tomcat container.

>If you are using another J2EE container, please refer to its documentation for how to define an environmental variable for the container.

## Disabling User Synchronization on Startup for LDAP Authentication
Adding the variable is a simple process and requires only one line added to the `catalina.properties` file. 

1. Navigate to the `conf` folder of your Tomcat container, generally found at `/host/conf`.
2. Open the file `catalina.properties` and, at the bottom of the file, add the following: `TRIBEFIRE_USER_SYNCHRONIZE_ON_STARTUP=false`. This stops tribefire from synchronizing users on startup. 
3. Save the file and restart tribefire.