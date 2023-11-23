### HasAcl

`HasAcl` is the super type for access-controlled entity types. It can optionally have an owner that has full access on the instance. All other permissions can be optional by assigning a sharable `Acl` to the `acl` property.

If an instance has an owner the absence of an `Acl` means that no one else has any access to the instance. If an instance has no owner, the absence of an `Acl` means that everybody has full access to the instance.

A security aspect configured to a persistence layer changes the queries on every type that extends `HasAcl` to filter by access rights within the persistence layer to properly support paging. Also, the updates on the persistence layer should be checked to happen within the limits of the assigned access rights.

Furthermore, the returned deep structure from the filtered top-level entities should be trimmed whenever the access control demands it. This can only happen after the initial query has already filtered the top level.

If we examine the `HasAcl` type, it has two different properties that offer two different ways how to protect an entity.

`HasAcl.owner` is a property which contains the name of the user who has an unlimited control over the entity. It is very important to remember that the `owner` property can only be set when creating the entity or by the user who currently owns the entity.

`HasAcl.acl` is a property which can be assigned with an `Acl` instance, which is an object that aggregates access information based on user roles (i.e. describes which role is allowed to perform what kind of an operation). This property can only be assigned when creating the entity, but the current user, or if the current user currently has the right to replace or modify the `Acl`, which is explained later.

`Acl` is just a collection of `AclEntries` which are stored in the `Acl.entries` property, and contains another property called `accessibility` which cannot be modified by a user but is managed automatically, and contains pre-processed information which reflects read-access defined by entries. This is only used as an optimization to make querying faster. Every time an `Acl` is modified, this `accessibility` is updated automatically.
