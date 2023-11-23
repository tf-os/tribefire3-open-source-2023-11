# GMQL Pagination
Pagination makes use of two keywords: `limit` and `offset`. `limit` determines the start index and `offset` determines the page size.

You may use both keywords together or individually.

## Limit
### Select Query
```
select ENTITY.PROPERTIES | * from TYPE_SIGNATURE ALIAS limit LIMIT_VALUE
```
Returns only the first 10 `User` instances.
```
select * from com.braintribe.model.user.User u limit 10
```

### Entity Query
```
from TYPE_SIGNATURE ALIAS limit LIMIT_VALUE
```
Returns only the first 5 `User` instances.
```
from com.braintribe.model.user.User u limit 05
```

### Property Query
The `limit` statement can only be used on properties that are of a collection type (either Set, List or Map), since these are the only property types that can be limited; all other types return a single value or instance.
```
property SOURCE_PROPERTY of reference(ENTITY_REFERENCE, ID) limit LIMIT_VALUE
```
Returns only the first 2 `Role` instances that are assigned to the roles property of the `User` `john.smith`.
```
property roles of reference(com.braintribe.model.user.User, 'john.smith') limit 2
```

## Offset
### Select Query
```
select ENTITY.PROPERTIES|* from TYPE_SIGNATURE ALIAS offset OFFSET_VALUE
```
This query returns all `User` instances after the first 10.
```
select * from com.braintribe.model.user.User u offset 10
```

### Entity Query
```
from TYPE_SIGNATURE ALIAS offset OFFSET_VALUE
```
Returns all `User` instances after the first 15:
```
from com.braintribe.model.user.User u offset 15
```

### Property Query
```
property SOURCE_PROPERTY of reference(ENTITY_REFERENCE, ID) offset OFFSET_VALUE
```
Returns all `Role` instances after the first 2 assigned to the roles property of `User` `mary.williams`:
```
property roles of reference(com.braintribe.model.user.User, 'mary.williams') offset 2
```

## Limit and Offset
### Select Query
```
select ENTITY.PROPERTIES|* from TYPE_SIGNATURE ALIAS limit LIMIT_VALUE offset OFFSET_VALUE
```
Returns 14 instances of `User` after the first 4 registered instances:
```
select * from com.braintribe.model.user.User u limit 14 offset 4
```

### Entity Query
```
from TYPE_SIGNATURE ALIAS limit LIMIT_VALUE limit LIMIT_VALUE offset OFFSET_VALUE
```
Returns 12 instances of `User` after the first 3 registered instances:
```
select * from com.braintribe.model.user.User u limit 12 offset 3
```

### Property Query
```
property SOURCE_PROPERTY of reference(ENTITY_REFERENCE, ID) limit LIMIT_VALUE offset OFFSET_VALUE
```
Returns 2 instances of `Role` after the first 2 registered instances belonging to the `roles` property of the `User` `cortex`.
```
property roles of reference(com.braintribe.model.user.User, 'cortex') limit 2 offset 2
```
