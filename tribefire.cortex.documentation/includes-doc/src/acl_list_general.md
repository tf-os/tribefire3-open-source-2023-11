## General

The `access-control-model`'s `HasAcl` interface introduces the `acl` property where you can add individual ACL entries. To be able to manage permissions on instance level, make sure that your model depends on the `access-control-model` and the entity type you want to manage permissions for extends `HasAcl`.

If your entity type extends `HasAcl` you can set an owner and an ACL for it's instance. Instances inheriting after `HasAcl` are checked by the `SecurityAspect` when being queried or manipulated by the privileged roles defined by the individual ACLs. Other operations are checked by custom code.

The [Acl](javadoc:com.braintribe.model.acl.Acl) interface is the main force behind the ACL functionality.

The operation permissions are resolved based on the entries in the permission list, which is populated with [AclEntry](javadoc:com.braintribe.model.acl.AclEntry) instances.

> For an overview of all ACL-related classes, see [JavaDoc](javadoc:com.braintribe.model.acl.Acl).