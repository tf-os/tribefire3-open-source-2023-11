# GMQL Conjunction and Disjunctions

Conjunctions and disjunctions can be invoked by using the `and` & `or` keywords respectively.
* conjunctions require that **all** value comparisons in the statement resolve to `true`, before any instance is returned.
* disjunctions require that **one** value comparison in the statement resolves to `true`, before any instance is returned.

You can define as many value comparisons are you wish, make use of both `and` & `or` in the same query statement.

The use of parentheses is also allowed and they have the highest precedence when being evaluated, meaning that anything contained within them will be evaluated first. After which, the order of evaluation is `not`, `and` and finally `or`.


## Conjunction
### Select Query
```
select ENTITY_PROPERTIES|* from TYPE_SIGNATURE ALIAS where OPERAND OPERATOR OPERAND and OPERAND OPERATOR OPERAND
```
The following query returns only instances of the `User` entry where the values of the property `firstName` is `C.` and `lastName` is `Cortex`.
```
select * from com.braintribe.model.user.User u where u.firstName like 'C.' and u.lastName like 'Cortex'
```

### Entity Query
The following query lists all instances of the `Group` entity where the value of the property `conflictPriority` is equal to `0.00`.
```
from com.braintribe.model.user.Group g where g.conflictPriority = 0.00
```

The following query returns only instances of the `User` entity where the values of the property `firstName` is `C.` and `lastName` is `Cortex`.
```
from com.braintribe.model.user.User u where u.firstName like 'C.' and u.lastName like 'Cortex'
```

### Property Query
The where statement can only be used on properties that are of a collection type (either Set, List or Map), since these are the only property types that can be compared; all other types return a single value or instance.
```
property SOURCE_PROPERTY of reference(ENTITY_REFERENCE,ID) where OPERAND OPERATOR OPERAND and OPERAND OPERATOR OPERAND
```

For example:
```
property groups g of reference(com.braintribe.model.user.User, 'john.smith') where g.name = 'admins' and g.conflictPriority = 0.00
```

## Disjunction
### Select Query
```
select ENTITY_PROPERTIES | * from TYPE_SIGNATURE ALIAS where OPERAND OPERATOR OPERAND or OPERAND OPERATOR OPERAND
```
The following query returns all instances of the entity `Group` where either the property value for `roles` is not null or `users` is not null.
```
select * from com.braintribe.model.user.Group g where g.roles != null or g.users != null
```

### Entity Query
```
from TYPE_SIGNATURE where OPERAND OPERATOR OPERAND or OPERAND OPERATOR OPERAND
```

The following query returns all instances of the entity `Group` where either the property value for `roles` is not null or `users` is not null.
```
from com.braintribe.model.user.Group g where g.roles != null or g.users != null
```

### Property Query
The where statement can only be used on properties that are of a collection type (either Set, List or Map), since these are the only property types that can be compared; all other types return a single value or instance.
```
property SOURCE_PROPERTY of reference(ENTITY_REFERENCE,ID) where OPERAND OPERATOR OPERAND or OPERAND OPERATOR OPERAND
```

The following query returns the property when the `User` instance with the id `5` has either the name `cortex` or `locksmith`.
```
property groups g of reference(com.braintribe.model.user.User, 'john.smith') where  g.name = 'admins' or g.name = 'guests'
```

## Multiple Value Comparisons
The evaluation rule of multiple comparisons follow the precedence of `not`, `and` and `or` during evaluating the statement. However, you can use parentheses to override the ordering of precedence.

Consider the following query:
```
select * from com.braintribe.model.user.User u where u.name like 'cortex' or u.name = 'locksmith' and reference(com.braintribe.model.user.Role, 'ec740d11-ba85-4e16-909b-3c80b948d340') in u.roles
```

Because `and` has a higher precedence as `or`, it means that the following will be evaluated first:
```
u.name = 'locksmith' and reference(com.braintribe.model.user.Role, 'ec740d11-ba85-4e16-909b-3c80b948d340') in u.roles
```
before `u.name like 'cortex'` is evaluated.

This query will produce results of all `User` instances where either the value of the property `name` is `cortex` or all instances where the value of `name` is `locksmith` and contains `tf-admin` as a role. You could rewrite this statement as:
```
select * from com.braintribe.model.user.User u where u.name like 'cortex' or (u.name = 'locksmith' and u.name = 'locksmith' and reference(com.braintribe.model.user.Role, 'ec740d11-ba85-4e16-909b-3c80b948d340') in u.roles)
```

You can use parentheses, however, to override this ordering of  precedence. Consider the same query as from above, but this time with parentheses around the `or` comparison:
```
select * from com.braintribe.model.user.User u where (u.name like 'cortex' or u.name = 'locksmith') and u.name = 'locksmith' and reference(com.braintribe.model.user.Role, 'ec740d11-ba85-4e16-909b-3c80b948d340') in u.roles
```
This means that the `or` comparison will be evaluated first:
```
u.name like 'cortex' or name = 'locksmith'
```
This makes up the first half of the and statement, before the other half of the comparison `tf-admin` in `u.roles` is also evaluated. The instances returned, therefore, are all instances with `tf-admin` contained in the `roles` property and have either the name `cortex` or `locksmith`.

In the following query we have introduced a `not` logical operator:
```
select * from com.braintribe.model.Person p where p.name = p.indexedName or p.phoneNumber != p.name and not p.company = p.indexedCompany
```
Since `not` has the highest precedence, then `and`, and lastly `or`, we can write the same query using parentheses to show the ordering of this precedence:
```
select * from com.braintribe.model.Person p where p.name = p.indexedName or (p.phoneNumber != p.name and (not p.company = p.indexedCompany))
```
Adding these parentheses shows that we have again two value comparisons here, with ultimately an `or` operator.

The query will return instances where:
* the value of `p.name` is equal to `p.indexed` name
* the value of `p.phoneNumber` is not equal to `p.name` and `p.company` does not equal `p.indexedCompany`
