# Using Access Control

The access control list allows you to grant or deny CRUD and custom operation permissions to different roles on instance level. To use ACL in your model, make sure to have the `access-control-model` as a dependency.

> For more information, see [Access Control List](asset://tribefire.cortex.documentation:concepts-doc/features/access_control.md)

## Creating an ACL Model

You can use Control Center to create a model whose entities extend the `HasAcl` entity. This entity allows you to add a list of ACL entries which control instance-level permissions.

1. In Control Center, create a new model which has a dependency to `access-control-model`. For more information on how to model in Control Center, see [Using Modeler](asset://tribefire.cortex.documentation:tutorials-doc/control-center/using_modeler.md).
2. Add a new entity type, for example `SecureEntity`.
3. Click the **Go To** button, search for `HasAcl` and add the `HasAcl` entity type to the Modeler view.
4. Create a derivation relation from `SecureEntity` to the `HasAcl` entity type. This effectively makes your entity extend the `HasAcl` entity, inheriting all of its properties.
5. Commit your changes.

## Adding ACL Entries

[](asset://tribefire.cortex.documentation:includes-doc/acl_entries.md?INCLUDE)

To add an ACL entry:

1. Create an access with the model which has a dependency to `access-control-model` as the metamodel.
2. Switch to the access in Explorer.
3. Create a new instance of an entity type that derives from `HasAcl`. 
4. Click the **Assign** link in the **acl** line and assign a new instance of `acl`. A new window opens.
5. In the new window, provide a name for your ACL entry and click the **Add** link in the **entries** section. A new view is displayed.
6. Depending on whether you want to assign ACL to a custom or to a standard operation, select **AclCustomEntry** or **AclStandardEntry**. 
7. In the ACL entry window, assign the `operation`, `permission`, and `role`. Apply and commit your changes.

### Deleting `Acl` and `AclEntries`

Because of the special utilization of these two types, it is only possible to delete them with the `DeleteMode` `failIfReferenced`.

## `Administrable` Metadata

All security checks (except for the `Acl` and `AclEntries` deletion) can be overridden with the `Administrable` meta data. This MD can be configured on `HasAcl` or `Acl` types, and is typically configured with a `RoleSelector` and a `UseCaseSelector` with use-case being `"acl"`. Obviously, configuring this MD on `HasAcl` grants rights on any `HasAcl` sub-type, while on `Acl` it grants rights to modify the `Acl.entries` property.

> Note that configuring this MD on any sub-type of `HasAcl` has no effect, the MD is only resolved on `HasAcl` level. For more information, see [Administrable](asset://tribefire.cortex.documentation:concepts-doc/metadata/administrable.md).

## Quick Summary

The following table summarizes who is authorized to perform a given operation based on how the `HasAcl` (and in the last row then `Acl`) entity is configured.

`Operation` | `acl` = `owner` = null | Only `acl` set | Only `owner` set | Both `acl`+`owner` set
----   | ----- | ----- | ------- | ------
R/W `HasAcl.custom` | [GRANTED] | READ/WRITE | isOwner | isOwner or READ/WRITE
DELETE `HasAcl` | [GRANTED] | DELETE | isOwner | isOwner or READ/WRITE
SET `HasAcl.owner` | [DENIED] | - | isOwner | isOwner
SET `HasAcl.acl` | [GRANTED] | REPLACE_ACL | isOwner | isOwner or REPLACE_ACL
CHANGE `Acl.entries` | N/A | MODIFY_ACL | N/A | N/A 

* MODIFY_ACL implicitly grants the REPLACE_ACL for convenience, but not the others to keep the implementation more maintainable
* We assume `Acl` always has some entries. If an instance is created with no entries by a mistake, only a user with administrative privileges (see [Administrable](asset://tribefire.cortex.documentation:concepts-doc/metadata/administrable.md)) can modify it.

`Annotation` | meaning
------   | --------
[GRANTED] | access is allowed to everyone
[DENIED] | access is denied to everyone
READ/WRITE/... | granted if current user has a role which grants given operation, based on the relevant `Acl`
isOwner | granted if current user is the owner of given `HasAcl` entity
N/A | not a relevant use-case
