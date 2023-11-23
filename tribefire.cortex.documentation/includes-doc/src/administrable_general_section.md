## Administrable

This MD can be configured on `HasAcl` or `Acl` types, and is typically configured with a `RoleSelector` and a `UseCaseSelector` with use-case being `"acl"`. Configuring this MD on `HasAcl` grants rights on any `HasAcl` sub-type, while configuring it on `Acl` grants rights to modify the `Acl.entries` property.