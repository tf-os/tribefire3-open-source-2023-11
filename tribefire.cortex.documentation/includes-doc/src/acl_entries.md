## ACL Entries

It is the entries in the list that control the operation permissions for a given role. Each entry must contain:

* an [AclPermission](javadoc:com.braintribe.model.acl.AclPermission) - `GRANT` or `DENY`
* a role
* an [AclOperation](javadoc:com.braintribe.model.acl.AclOperation)

The ACL works by accumulating the effects of the single entries. Entries with an `AclPermission.DENY` permission are treated with priority over the entries with an `AclPermission.GRANT` permission for the same operation.

`Acl` instances are used on and shared amongst `HasAcl` instances to control access to them.

An `AclEntry` is a single immutable entry that either grants or a denies a concrete operation for a single role. The standard operations (it is possible to define your own and implement support for it) are given by the `AclOperation` enum (which is available as a property on `AclStandardEntry`). The individual constants have the following semantics:

`AclOperation` | Controls
-----   | --------
`READ` | read `HasAcl` entity
`WRITE` | modify existing `HasAcl` entity
`DELETE` | delete `HasAcl` entity
`MODIFY_ACL` | modify existing `Acl` entity
`ASSIGN_ACL` | assign values to `HasAcl.acl`

> Even though an `AclEntry` grants or denies a concrete operation for a single role, you can use `$all` as the role which applies the given entry to all users. Also bear in mind that when you assign a `WRITE` permission for a role, you must assign the same role a `READ` permission as well. Otherwise, that particular role will not have read access to the entity instance and the instance will not even be displayed in Control Center.

Note that with `ASSIGN_ACL` we check the old `Acl` value which is being overwritten. We do not however check the newly assigned `Acl` at all, so it is theoretically possible for a user to assign such `Acl` that the users themselves won't be able to access the entity later.