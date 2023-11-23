# GMQL Examples

## String Restriction
Returns all instances of the `User` entity where the value of the property `lastName` is `Smith`:
```
//Entity Query
from com.braintribe.model.user.User where lastName = 'Smith'

//Select Query
select * from com.braintribe.model.user.User u where u.lastName = 'Smith'
```

## Null Restriction
Returns all instances of the `User` entity where the value the property name is not null.
```
//Entity Query
from com.braintribe.model.user.User where name != null

//Select Query
select * from com.braintribe.model.user.User u where u.name != null
```

## Operator Restrictions
The following contains several examples with different comparison operators that will return instances of `Group` depending on which are matched by the value comparison.
```
// Instances of Group will be returned when the value of conflictPriority is greater that 0.10

//Entity Query
from com.braintribe.model.user.Group where conflictPriority > 0.10

//Select Query
select * from com.braintribe.model.user.Group g where g.conflictPriority > 0.10

//Instances of Group will be returned when the value of conflictPriority is greater or equal to 1.00

//Entity Query
from com.braintribe.model.user.Group where conflictPriority >= 1.00

//Select Query
select * from com.braintribe.model.user.Group g where g.conflictPriority >= 1.00

//Instances of Group will be returned when the value of conflictPriority is less than 0.75

//Entity Query
from com.braintribe.model.user.Group where conflictPriority < 0.75

//Select Query
select * from com.braintribe.model.user.Group g where g.conflictPriority < 0.75

//Instances of Group will be returned when the value of conflictPriority is less or equal 0.55

//Entity Query
from com.braintribe.model.user.Group where conflictPriority <= 0.55

//Select Query
select * from com.braintribe.model.user.Group g where g.conflictPriority <= 0.55

//Instances of User will be returned when the value of name is equal to the value 'firstName'

//Entity Query
from com.braintribe.model.user.User where name like 'firstName'

//Select Query
//Instances of User will be returned when the value of name is equal to the value 'firstName'
from com.braintribe.model.user.User where name ilike 'firstName'
```

## In Operator
The following example makes use of the in operator where the value defined on the left-hand side will be compared against the collection defined on the right-side. You can either use this operator to search a collection property for the value defined, or you search on a specific property for one of several values passed as a set.
```
//This query will return instances of Person that have the value 'nick' in the property (of the type set) nickNames

//Entity Query
from com.braintribe.model.custom.Person where 'nick' in nickNames

//Select Query
select * from com.braintribe.model.custom.Person p where 'nick' in p.nickNames

//This query will return instances of User that have the value "Smith" or "Williams" defined in the property lastName

//Entity Query
from com.braintribe.model.user.User where lastName in ('Smith', 'Williams')

//Select Query
select * from com.braintribe.model.user.User where lastName in ('Smith', 'Williams')
```

## Date Function
The following query uses the date operator to provide the query with a date; instances of `Person` are returned when the `dateOfBirth` when the value of the date is the 24th of September 1989:
```
//Entity Query
from com.braintribe.custom.model.Person where dateOfBirth = date(1989Y,9M,24D,14H, 23m,0s, +0200Z)

//Select Query
from com.braintribe.custom.model.Person where dateOfBirth = date(1989Y,9M,24D,14H, 42m,0s, +0200Z)
```

## ListIndex Function
The following query uses the `listIndex` function; instances of `Owner` are returned where the index of the list `companyList` is equal to or less then 1.
```
//Select Query
select cs.name from com.braintribe.model.model.Owner o join o.companyList cs where listIndex(cs) <=1
```

## Enum References
The following query shows how to create a enum reference:
```
//Entity Query
from com.braintribe.model.processing.query.test.model.Person where enum(com.braintribe.model.processing.query.test.model, red) != null

//Select Query
select * from com.from com.braintribe.model.processing.query.test.model.Person p where enum(com.braintribe.model.processing.query.test.model, red) != null
```

## Reference Function
The following query provides an entity using the reference function. The value false in the second query means that the entity is found using the `PreliminaryEntityReference`.
```
// Entity Query
from com.braintribe.model.custom.Person where reference(com.braintribe.model.custom.Person, 23, false) ! = null

//Select Query
select * from com.braintribe.model.custom.Person p where reference(com.braintribe.model.custom.Person, 23, false) != null
```

## Pagination
The following query provides entities limited using pagination. The entities themselves are not restricted by any conditions. Instead, `offset` defines the start index for the entities returned (in this case the first entity will have the index of `20`) while `limit` defines the amount of results returned, or the page size (in this case `200`).
```
//Entity Query
from com.braintribe.model.user.User limit 20 offset 200

//Select Query
select * from com.braintribe.model.user.User limit 20 offset 200
```

## Keyword Escape
The following query uses quotations around the key word from, a keyword in the query syntax, to escape it so it can be use in the query statement. The query returns `Person` entities where the property from is not null.
```
//Entity Query
from com.braintribe.custom.model.Person where "from" !=null

//Select Query
select p."from" from com.braintribe.custom.model.Person p
```

## Join
The following examples show the different joins that can be done in tribefire.
```
//Inner Join
select * from com.braintribe.model.resource.Resource r join r.resourceSource

//Full Join
select * from com.braintribe.model.resource.Resource r full join r.resourceSource

//Left Join
select * from com.braintribe.model.resource.Resource r left join r.resourceSource

//Right Join
select * from com.braintribe.model.resource.Resource r right join r.resourceSource

//Multiple Joins
select u.name, r.name from com.braintribe.model.user.User u join u.groups g join g.roles r
```

## Simple Property Query
The query returns the `firstName` property of the `User` entity with the ID of `cortex`.
```
property firstName of reference(com.braintribe.model.user.User, 'cortex')
```

## Distinct Property Query
The property `firstName` returns the [distinct](gmql_distinct.md) property of the `User` entity with the ID of `john.smith`.
```
distinct property firstName of reference(com.braintribe.model.user.User, 'john.smith')
```

## Property Query with Alias
The property `firstName` is returned from the `User` entity with the ID of `cortex`; the character `n` is defined as an alias.
```
property firstName n of reference(com.braintribe.model.user.User, 'cortex')
```

## String Property Query Restriction
The entities contained in the set property roles of the `User` entity with ID of `cortex` is only returned only where name contains the value `tf-admin`.
```
property roles n of reference(com.braintribe.model.user.User, 'cortex') where n.name like 'tf-admin'
```

## Ordered Property Query
The entities contained in the set property roles of the `User` entity with `ID` of `john.smith` is only returned ordered by the `Role` entity's name descending.
```
property roles n of reference(com.braintribe.model.user.User, 'john.smith') order by name desc
```

## Pagination Property Query
The entities contained in the set property roles of the `User` entity with the ID of `mary.williams` is returned. The `Role` entities will begin with the index of `10` and limited to `150` entries.
```
property roles n of reference(com.braintribe.model.user.User, 'mary.williams') limit 10 offset 2
```

## Ordered Pagination Query
The entities contained in the set property roles of the `User` entity with the ID of `mary.williams` is returned. The `Role` entities is returned ordered by the `Role`'s property name descending. The starting index of the entities is 20 and limited to 200 entries.
```
property roles n of reference(com.braintribe.model.user.User, 'mary.williams') order by name desc limit 20 offset 200
```

## Ordered Pagination Restriction Query
The entities contained in the set property roles of the `User` entity with the ID of `john.smith` is returned. The `Role` entities is returned only where its name property is `tf-admin` order by the same property descending. The entities starts with the index of `20` and limit of `200`.
```
property roles of reference(com.braintribe.model.user.User, 'john.smith') where name like '*role*' order by name desc limit 20 offset 1
```
