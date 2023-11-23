# LDAP - Creating a New Connection
After deploying and installing the LDAP cartridge, the various components must be configured for a functioning LDAP authentication. This section deals with the configuration of the LDAP connection.

As the name suggests, it provides the connection to the LDAP-compatible server. Even though it is possible to use other solutions, we recommend you use Active Directory. 

It should be noted that the connection provides only that, a connection to the server, the configuration details for the authentication and the collection of the Users, their roles, and groups are provided by the LDAP User Access.

## Prerequisites
* deploying and synchronizing the LDAP cartridge

## Creating a New LDAP Connection
1. In Control Center, go to **Connections** and click **New**.
2. Double-click `LdapConnection` and enter the connection details for the connection to your LDAP-compatible server:

   Property | Description
   ---------| -----------
   `Name`	| The name of the connection.
   `ExternalID` |	The External ID is used to reference the connection in the associated implementations.
   `Description`	| A description for the connection.
   `Cartridge`	| The cartridge where the implementation for the LDAP connection can be found.
   `ID`	| The unique ID for this connection, it is automatically assigned when the connection is committed.
   `connectionUrl`	| The address where the LDAP-compatible server can be found.
   `connectionTimeout`	| How long the connection waits for a response, in milliseconds, before a timeout exception is thrown. Setting this value to 0 defines no timeout value.
   `dnsTimeoutinitial`	| Setting this property defines the initial timeout period in milliseconds. If this property has not been set, the default initial timeout is 1000 milliseconds
   `dnsTimeoutRetries`	| How often the connection attempts to retry the connection after a timeout.
   `environmentSettings`	| Defines any environment settings required for the LDAP connection. By standard, no settings are required. However, check with the person responsible for the LDAP server whether any are actually required.
   `initialContextFactory`	| The name of the class that provides the context for a LDAP connection. Generally, this is defined as: `com.sun.jndi.ldap.LdapCtxFactory`
   `password` |	The password for the user account that is used to validate the connection.
   `referralFollow` |	Defines how the LDAP connection should handle any referrals. Checking the checkbox instructs the connection to follow a referral, that is, from one connected server to another, while leaving it uncheck means the referrals are ignored.
   `username` |	The username of the account that is used to validate the connection.
   `useTLSExtension` | The TSL Extension is used to serialize secure, plain requests against an LDAP server on a single connection. To turn this connection on, click the checkbox.

3. Click **Apply** and **Commit**. The connection is displayed in a new tab.
4. Right-click the connection and click **More -> Deploy**.
   >You can view the connections by clicking on the **Connections** link on the menu to the left of Control Center.
