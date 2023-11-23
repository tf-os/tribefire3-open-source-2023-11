# General GMQL Query Types
GMQL supports three standard query types:
* Select Query
* Entity Query
* Property Query

## Select Query
The most powerful query of the three, it can be used similar to SQL in that you can select specific properties, use various aggregation and string functions, and create joins on related entities.

The Select Query uses a system of aliases to determine the source of properties and are required for a valid select statement. They are defined after the entity reference of the from or join keywords. They are then affixed, along with a period (`.`), before a property declaration:
```
//u is defined as the alias for User
select u.firstName, u.lastName from com.braintribe.user.User u
```

>The select query statement must begin with the keyword **select**.

## Entity Query
Similar to the select query, in that it is used to query entities and return instances that match the query statement, the entity query can only return the full <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_instance}}">entity instance</a>. This means that all properties are returned – equivalent to the `select *` statement of a select query. Although the entity query doesn't support aggregate functions or joins, it can still filter results using the `where` statement to add restrictions to the query, as well as ordering and pagination.

It is also possible to use an alias in an entity query, but it is not mandatory. If they are not used the default source is assumed according to the entity reference given. This means for example if you define the entity reference as `com.braintribe.model.user.User` and then refer to a property, in a `where` comparison, for example, it will be assumed that this property will be found in the `User` entity.
```
//Both query statements return the same results. In the first query the alias is assumed
from com.braintribe.user.User where firstName like 'John'
from com.braintribe.user.User where u.firstName like 'John'
```

In GME, this query has the following properties:

Property | Description
--- | ---
distinct | When selected, the query will only return distinct values (no repetitions allowed).
entityTypeSignature | This signature defines which entity types are queried.
noAbsenceInformation | `absenceInformation` can be provided as a replacement for empty properties. With this flag selected, it is skipped by the query.
ordering | Defines how the query results are ordered.
queryContext | Allows you to narrow down the context of the query to specific properties of an entity.
restriction | Allows you to restrict the query to only return specific data, using a number of standard operators (for example `userName` **like** `John`).
traversingCriterion | Allows you to assign a [traversing criterion](../features/traversing_criteria_in_detail.md) to the query.
evaluationExcludes | You can assign entity types to be excluded from the query.
froms | Allows you to add `from` statements to the query, for example narrowing down the entity types being queried.
ignorePriviledgedRoles | Deprecated.

>The entity query statement must begin with the keyword **from**.

## Property Query
The property query, unlike entity and select queries, is used to return the value of a specific property belonging to an entity instance. An entity reference is used to determine the exact entity instance, while the `property` keyword is used to determine the value returned.
```
//Returns the value of the firstName property of User instance with the id 'cortex'
property firstName of reference(com.braintribe.model.user.User, 'cortex')
```
If the property type queried is a collection, the elements of that collection can be sorted or filtered in the same manner as an entity query.

It is also possible to use aliases, but it is not mandatory. If used, the alias is defined after the property declaration.
```
//Same property query as above but with the use of an alias
property firstName fn of reference(com.braintribe.model.user.User,'cortex')
```

> The property query statement must begin with the keyword **property.**
