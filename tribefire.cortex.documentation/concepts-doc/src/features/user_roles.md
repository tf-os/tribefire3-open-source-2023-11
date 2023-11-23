# User Roles

By default, we identify the following roles in tribefire:

* tf-admin
* tf-locksmith
* tf-internal

The role you use depends on the activity you want to perform. For instance, if you build an extension (like a `StateChangeProcessor` or a `ServiceProcessor`) in a cartridge you usually can choose to run certain operations on a regular user-based session or an internal-session which runs as `tf-internal`. Which one to use in which scenario is up to the cartridge developer and depending on the use case.

The `tf-admin` role has access to everything in the cortex database. If you need more granular permissions applied on the cortex model, this needs to be configured explicitly (e.g. via MetaData) and probably new roles need to be introduced.

> For more information on Metadata see [Metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/metadata.md).

If your application should support individual permissions based on user roles, you have to define that on the application level. This means you do not create the necessary users in tribefire, but in your application. When you create a new user, its username is exposed as a role in the following format: `$user-name`, where the **name** part is the value of the `name` parameter of the user.

> Note that the roles below all relate to tribefire itself, and not the apps built on tribefire.
### tf-admin

The tf-admin role is the most technical role. Users assigned this role have access to all cortex data, can make changes to cortex, and can access all system accesses from Control Center. Normally, you would not assign this role to users other than `cortex`.

An example of this role is the `cortex` user.

### tf-locksmith

The tf-locksmith role is a backup role used primarily to unlock a locked tf-admin user role. This role has less restrictions in terms of security. All constraints like mandatory fields, invisibility of entities and properties, property and entity protection (being able to edit) are inactive. It is not possible to login with this user unless an administrator changed/set the password for this user explicitly.

An example of this role is the `locksmith` user.

> There is no default password for this role.

### tf-internal

The tf-internal role is used internally in the server and in privileged cartridge-to-server communication. This role completely bypasses the `SecurityAspect`. You should not grant this role to any 'real' user.