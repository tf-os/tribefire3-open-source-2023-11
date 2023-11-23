# LDAP - Creating a New User Access
The LDAP Cartridge contains the LDAP User Access, which uses the User Model to map information retrieved from the LDAP server. This means that the `User` entity, contained in the User Model, is used to display the information found in the LDAP server, with the `User` entity's properties `name`, `roles`, `groups` used to display this information.

## Creating an LDAP User Access
1. In Control Center, go to **Custom Accesses** and click **New**.
2. Double-click `LdapUserAccess` and enter the mandatory details for the connection to your LDAP-compatible server:

   Property | Description
   ---------| -----------
   `Name`	| The name of the connection.
   `ExternalID` |	The External ID is used to reference the connection in the associated implementations.
   `Description`	| A description for the connection.
   `Cartridge`	| The cartridge where the implementation for the LDAP connection can be found.
   `Meta Model`	| The model which the access should map to, in order to display the information in a tribefire context. <br/> <br/> When using the LDAP User Access, the User Model should always assigned to this property.
   `groupBase`	| The name of the Group Base DN (Distinguished Name) in your LDAP server. For example: `ou=groups, dc=yourorg,dc=example,dc=com`
   `groupIdAttribute`	| The name of the attribute in LDAP server that corresponds to the Group ID.
   `groupMemberAttribute`	| The attribute in LDAP server that determines the `groupMember`.
   `groupNameAttribute` |	The attribute in the LDAP server that determines the group name.
   `groupObjectClasses` |	The name of the class that represents the LDAP group object.
   `groupsAreRoles` |	Determines whether groups should also be treated as roles or not.
   `ldapConnection` |	The connection you created to a LDAP-compatible server.
   `memberAttribute` | Determines the member attribute in the LDAP server.
   `roleIdAttribute` | The name of the attribute in LDAP server that corresponds to the role ID.
   `roleNameAttribute` | The attribute in LDAP server that determines the role name.
   `searchPageSize` |	The amount of results that should be returned at one time.
   `userBase` | The name of the User Base DN in your LDAP server. For example: `dc=exampleorg, dc=com`
   `userDescriptionAttribute` | The attribute in LDAP server that maps to the description property in the `User` entity.
   `userEmailAttribute` |	The attribute in LDAP server that maps to the email property in the `User` entity.
   `userFilter` |	This allows the LDAP to restrict users according to the filter give here. For example: `(sAMAccountName=%s)`
   `userFirstNameAttribute` |	The attribute in LDAP server that maps to the First Name property in the `User` entity.
   `userIdAttribute` | The attribute in the LDAP server that maps to the User ID property in the `User` entity.
   `userLastLoginAttribute` |	The attribute in the LDAP server that maps to the Last Login property in the `User` entity.
   `userLastNameAttribute` |	The attribute in the LDAP server that maps to the last name property in the `User` entity.
   `userMemberOfAttribute` |	The attribute in the LDAP server that determines the userMemberOf value.
   `userNameAttribute`	| The attribute in the LDAP server that maps to the `Name` property in the `User` entity.
   `userObjectClasses` |	The name of the class that represents the LDAP user object.

3. Click **Apply** and **Commit**. The access is displayed in a new tab.
4. Right-click the access and click **More -> Deploy**.
   >"You can view the accesses by clicking on the **Custom Accesses** link on the menu to the left of Control Center.
