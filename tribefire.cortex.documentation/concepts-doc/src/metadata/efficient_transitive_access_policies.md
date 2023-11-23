# Efficient Transitive Access Policies

This metadata is used to activate Efficient (Transitive) Instance Level Security (either ELIS or ETILS) on an entity type or an entity hierarchy.

Metadata Property Name  | Type Signature  
------- | -----------
`EfficientTransitiveAccessPolicies` | `com.braintribe.model.meta.data.security.EfficientTransitiveAccessPolicies`

## General

After configuring this metadata, any security permissions (either Efficient Grant or Deny, or simple Grant or Deny) configured on an instance of an entity are checked, and tribefire then acts accordingly.

This metadata is only used when you wish to affect security permissions on instances of an entity. If you wish to create security permissions for the entity type itself, then you should use the various metadata available in conjunction with a role selector.

> For more information on role selectors, see [Role Selector](selectors/role_selector.md).

Before using this metadata, you must configure the entities that you wish to add security permissions to by ensuring that the aforementioned entities have as a supertype either `HasEfficientTransitiveAccessPolicies` or `HasEfficientAccessPolicies`, depending on the security protocol you wish to implement.

The Efficient Transitive Access Policies metadata has only one configurable property called `ilsStrategy`:

`ilsStrategy` Value | Description
------| ---------
`denyByDefault` | This property enables the `denyByDefault` policy, which means if nothing is stated the access is denied, but if the user has one of the grant roles, the access is granted, as long as he does not have any of the denied roles.
`disabled` | This property turns this metadata off.
`grantByDefault` | This property enables the `grantByDefault` policy, which means every instance is accessible if no security information is configured. To deny an access for a given role, you must explicitly state that a given role is denied the access. However, this is not definitive, we might also specify other role(s) which override the deny and allow the user an access.

In other words, an access for given policy is granted if:

* `grantByDefault`: user has a role from the grant list OR has no role from the deny list
* `denyByDefault`: user has a role from the grant list AND has no role from the deny list

You select a strategy by selecting an entry from the `ilsStrategy` drop-down list.

## Example

You must first ensure that the entity you wish to activate EILS or ETILS security on has `HasEfficientAccessPolicies` (for EILS) or `HasEfficientTransitiveAccessPolicies` (for ETILS) as a super type. You can configure it in Control Center.

![](../images/EfficientTransitiveAccessPolicies01.png)

This means that the entity configured with this super type inherits two (or four, if `HasEfficientTransitiveAccessPolicies` is used a supertype) properties. You can use these properties to enter roles that grant or deny permission for this particular instance of the entity.

Once you have configured the permissions, you can now configure the metadata. This metadata has only one configurable property: `ilsStrategy`. Select one of the available strategies from the drop-down menu.