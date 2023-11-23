## GMQL Order by
You can use single or multiple properties to order by. You can also use `asc` (ascending) or `desc` (descending) to influence the direction of ordering.

## Select Query
```
select ENTITY_PROPERTIES | * from TYPE_SIGNATURE ALIAS order by ENTITY_PROPERTY
```
You can sort your results on more than one instance by adding each property to a comma separated list. The first property entered will be sorted first, the second after that, and so on.
```
select ENTITY_PROPERTIES | * from TYPE_SIGNATURE ALIAS order by ENTITY_PROPERTY_1, ENTITY_PROPERTY_2, ENTITY_PROPERTY_N
```

You can also sort the direction of ordering by using the `asc` (ascending) or `desc` (descending) keywords, which are added after the property declaration.
```
select ENTITY_PROPERTIES | * from TYPE_SIGNATURE ALIAS order by ENTITY_PROPERTY_1 asc, ENTITY_PROPERTY_2 desc
```

This query will produce results for instances of a `Person` entity sorted according to the property `lastName`.
```
select * from com.braintribe.model.user.User u order by u.lastName
```

This query will produce the same results as the previous query, except that the direction of ordering is now reversed, `descending`.
```
select * from com.braintribe.model.user.User u order by u.lastName desc
```

This query will produce results for instances of a `Person` entity sorted according to three properties: `lastName` (ascending), `firstName` (descending), `email` (default ordering is `ascending`).
```
select * from com.braintribe.model.user.User u order by u.lastName asc, u.firstName desc, u.email
```

## Entity Query
```
from TYPE_SIGNATURE ALIAS order by ENTITY_PROPERTY
```

You can sort your results on more than one instance by adding each property to a comma separated list. The first property entered will be sorted first, the second after that, and so on.
```
from TYPE_SIGNATURE ALIAS order by ENTITY_PROPERTY_1, ENTITY_PROPERTY_2, ENTITY_PROPERTY_N
```

You can also sort the direction of ordering by using the `asc` (ascending) or `desc` (descending) keywords. They should be added after the property declaration.
```
from TYPE_SIGNATURE ALIAS order by ENTITY_PROPERTY_1 asc, ENTITY_PROPERTY_2 desc
```

This query will produce results for instances of a `Person` entity sorted according to the property `lastName`.
```
from com.braintribe.model.user.User order by lastName
```

This query will produce the same results as the previous query, except that the direction of ordering is now reversed, `descending`.
```
from com.braintribe.model.user.User order by lastName desc
```

This query will produce results for instances of a `Person` entity sorted according to three properties: `lastName` (ascending), `firstName` (descending), `email` (default ordering is `ascending`).
```
from com.braintribe.model.user.User order by lastName asc, firstName desc, email
```

## Property Query
The Order By statement can only be used on properties that are of a collection type (either Set, List or Map), since these are the only property types that can be compared; all other types return a single value or instance.
```
property SOURCE_PROPERTY of reference(ENTITY_REFERENCE , ENTITY_ID) order by ENTITY_PROPERTY
```
You can sort your results on more than one instance by adding each property to a comma separated list. The first property entered will be sorted first, the second after that, and so on.
```
property SOURCE_PROPERTY of reference(ENTITY_REFERENCE , ENTITY_ID) order by ENTITY_PROPERTY_1, ENTITY_PROPERTY_2, ENTITY_PROPERTY_N
```
You can also sort the direction of ordering by using the `asc` (ascending) or `desc` (descending) keywords. They should be added after the property declaration.
```
property SOURCE_PROPERTY of reference(ENTITY_REFERENCE , ENTITY_ID) order by ENTITY_PROPERTY_1 asc , ENTITY_PROPERTY_2 desc
```

This query will produce results for instances of a `Person` entity sorted according to the property `lastName`.
```
property roles r of reference(com.braintribe.model.user.User , 'mary.williams') order by r.name
```
This query will produce the same results as the previous query, except that the direction of ordering is now reversed, descending.
```
property roles r of reference(com.braintribe.model.user.User , 'mary.williams') order by r.name desc
```
