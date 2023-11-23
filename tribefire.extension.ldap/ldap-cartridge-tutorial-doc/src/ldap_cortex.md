# LDAP - Configuring Cortex
In a standard installation, the user registry is found in the `auth` access and is used to validate user credentials using an authentication service. 

The authentication access can be set via Control Center by configuring the entity `CortexConfiguration`, which allows you to configure the authentication access, authentication service, and CORS configuration. 

>For more information on configuring CORS, see [Configuring CORS](asset://tribefire.cortex.documentation:installation-doc/Configuration/configuring_cors.md).

In the scope of the LDAP Cartridge, only the authentication access needs to be changed. 

After configuring the Cortex configuration, you must restart the tribefire Server. On restart, the LDAP-compatible server is used as the source of authentication. 

## Configuring LDAP-compatible Server
You must make sure to assign the proper groups to users you want to access tribefire with. 
>For more information, see [Security Considerations](asset://tribefire.cortex.documentation:installation-doc/Configuration/security_considerations.md#default-user-roles)"%}

As only members of the `tf-admin` group have access to Control Center, make sure to add that usergroup to the user you want to access Control Center with.

## Configuring Cortex for LDAP Authentication

After creating the LDAP Connection and the LDAP User Acccess, the final step is to assign the authentication access. 

1. In Control Center, locate the **Quick Access** search bar, located at the top, search for `CortexConfiguration` and open it from the **Types** heading. A new tab opens.
2. In the new tab, right-click the **Authentication Access** property and click **Assign**. The Selection Constellation is displayed. 
3. Select the `LdapUserAccess` and click **Add**. 
4. Right-click the **Authentication Service** property and click **Assign**. The Selection Constellation is displayed.
5. Select the `LdapAuthenticationService` and click **Add**. The main Control Center screen is displayed once more. 
6. Commit your changes and restart the server. 

Once the server has restarted, the LDAP cartridge is now used to provide authentication.