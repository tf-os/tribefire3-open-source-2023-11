# GMQL Where
The where statement restricts results, returning only those that match the value comparison(s), or in the case of the negated value comparison, return only the results that do not match the value comparison.

After the use of the where statement you must also provide two operands for comparison, as well as an operator on which the two operands can be compared.

To create multiple comparisons you must use the statements `and` or `or`.
>For more information see [GMQL Conjunctions and Disjunctions](gmql_and_or.md).

The simplest use of the where keyword is to define one comparison consisting of two operands and one operator.

## Select Query
```
select ENTITY_PROPERTIES|* from TYPE_SIGNATURE where OPERAND OPERATOR OPERAND
```
The following query lists all instances of the `User` entity where the value of the property `lastName` is `Smith`.
```
select * from com.braintribe.model.user.User u where u.lastName like 'Smith'
```

The following query lists all instances of the Group entity where the value of the property `conflictPriority` is equal to `0.00`.
```
select * from com.braintribe.model.user.Group g where g.conflictPriority = 0.00
```

>Note the difference between the two select queries above. While the comparison on a string value requires the use of single quotation marks, the comparison on a number value does not. <br/> Additionally, because the second query above includes the decimal place (that is, `0.00`) no additional suffix is required. We could also declare: `g.conflictPriority = 0D`. <br/> This would have the same effect. For more information on the use of numbers and strings in a value comparison see [GMQL Syntax](gmql_syntax.md).

If you want to compare a complex property, one whose value represents another entity type, you need to use an entity reference. In the following query, only `User` entity instances are returned when their property picture contains an `AdaptiveIcon` instance with the ID of `2`.
```
select * from com.braintribe.model.user.User u where reference(com.braintribe.model.resource.AdaptiveIcon, 2L) = u.picture
```

## Entity Query
```
from TYPE_SIGNATURE where OPERAND OPERATOR OPERAND
```
The following query returns all instances of the `User` entity where the value of the property `lastName` is `Smith`
```
from com.braintribe.model.user.User where lastName like 'Smith'
```

> Because no alias was defined after the entity source declaration, when the property `lastName` is used, GMQL assumes that the source is `User`. Unlike the select query, aliases are not mandatory in entity queries. For more information, see [GMQL Syntax](gmql_syntax.md).

## Property Query
The where statement can only be used on properties that are of a collection type (either Set, List or Map), since these are the only property types that can be compared; all other types return a single value or instance.
```
property SOURCE_PROPERTY of reference(ENTITY_REFERENCE, ID) where OPERAND OPERATOR OPERAND
```
The following query returns a collection of groups where the name of the group is `admins`.
```
property groups g of reference(com.braintribe.model.user.User, 'john.smith') where g.name = 'admins'
```

## Operands
An operand can be many things, from a basic type (string, date, number, and so on) to entity references, enum references, collections or even functions. Available operands include:
* Simple types (Strings, Numbers, dates, booleans, and so on)
* null values
* entity type references
* enum type references
* property references (for example, using an alias: p.lastName)
* functions (Aggregate functions, string functions, boolean functions, and date functions)
* collections
* variables

## Operators

Operator    | Description | Example
------- | -----------     | ------
`in`| The operator in is used to compare one value against a set of values contained in a collection. If the value provided is contained within this collection the entity instance will be returned as part of the results set. The value is provided on the left-half side of the operator while the set is provided on the right-hand side. <br/> There are two specific ways that this operator can be used. You can either use this operator to search a collection property for the value defined, or you search on a specific property for one of several values passed as a set. <br/> Compare a value against those contained in a collection property (return only `Person` instances which have `Smith` contained in its collection property `nicknames`): `select * from com.braintribe.custom.model.Person where "Smith" in nicknames` <br/> or for complex collections (that is, collections whose elements are other entities) (return only `User` instances that contain the role `john.smith.role.b` in their collection property `roles`: `select * from com.braintribe.model.user.User u where reference(com.braintribe.model.user.Role, 'john.smith.role.b') in u.roles` <br/> Compare a property of an entity against a collection of values (return only `User` entities that have the second name of either `Cortex` or `Smith`): `select * from com.braintribe.model.user.User u where u.secondName in ('Cortex', 'Smith')` | `4 in favouriteNumbers` <br/> `secondName in ('Cortex', Smith')`
`like` | Used in string comparisons. For example, `firstName like "Rob"` returns any instance where the string value matches exactly the value given. In the case of the example only `Rob` is returned, not `Robert`. If you wish to match partial strings, you must use the `*` wildcard. For example, `firstName like "Rob*"` returns both `Rob` and `Robert`. | `secondName like 'Cortex'`
`ilike` | Used for non case sensitive comparisons. The functionality of `ilike` depends on the underlying repository that is connected to the Access. If you use <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.smood}}">SMOOD</a> database, the `ilike` operator is available by default. | `secondName like 'Cortex'`
`>=` | greater than or equal to
`>`	| greater than
`!=` | not equal to
`<	` | less than
`<=` | less than or equal to
`=	` | Whether the left-hand value equals the right-hand value
