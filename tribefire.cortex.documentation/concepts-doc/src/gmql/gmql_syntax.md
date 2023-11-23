# GMQL Syntax
As you already know, GMQL supports three standard query types:
* Select Query
* Entity Query
* Property Query

Each query has its own statement that indicates what query type is being used:
* Select Query uses the statement `select`
* Entity Query uses the statement `from`
* Property Query uses the statement `property`


## Select Query
Select queries require the explicit definition of aliases.

The following select query will return all instances of the `User` entity, which has `u` defined as its alias.
```
select * from com.braintribe.model.user.User u
```

### Select Query Aliases
The alias value is added directly after the entity reference: `select * from com.braintribe.model.user.User u`
The above select query determines the character `u` as the alias for the entity `User`.

This alias is used to refer to properties within the source entity: `select u.firstName, u.lastName from com.braintribe.model.user.User u`

The example above uses the alias to refer to two properties within the `User` entity, which is represented by the alias `u`. It is also used when making value comparisons:
```
select u.firstName, u.lastName from com.braintribe.model.user.User u where u.lastName = 'Smith'
```
This is the same query as the one before, except that only instances of `User` that have the value `Smith` as the property `lastName` are returned.

### Complex Properties
If you select a property that is complex, the entity is returned:
```
select  u.picture  from com.braintribe.model.user.User u
```

In the query shown above, the JSON representation of the entity will be returned, the `picture` property of `User` representing an entity called `Icon`.

It is also possible to return data from the entity represented in the property, if the entity is of a single aggregation, that is, not a of a collection type.

For example, the `User` entity has a single aggregation relationship with `Icon`, they property type of `picture`. The `Icon` entity contains two properties: `name` and `ID`. You can create a query that references these properties:
```
select u.lastName, u.picture.name from com.braintribe.model.user.User u
```

The above query will return results for the value of the property `lastName` along with the corresponding value for the name of the picture entity for each `User` entity.

### Joins
When creating a join on two entities, you must assign an alias for the joined entity as well as for the main entity. A join is created on a complex property from the main entity, and the alias should be defined after the actual join:
```
select * from com.braintribe.model.user.User u join u.groups g
```

The query statement above creates a join on the entity `User` using the complex property `groups` to join on the entity type `Group`. As you can see the first alias `u` is used to refer to the property on which the join should be created `u.groups` and then afterwards the alias for the `Group` entity is defined, in the example above `g`.

## Entity Query
Entity queries don't require the explicit use of aliases. In the example no alias is given, so one will be automatically designated, using the entity's short name – in the example below `com.braintribe.model.user.Group` will have the alias `Group`.

The following entity query will return all instances for the entity Group.
```
from com.braintribe.model.user.Group
```

### Entity Query Aliases
Although optional, aliases can be defined in entity queries in the same manner as in select queries, meaning that they are defined after the entity reference.

The following example shows the most simple entity query possible, where all instances of the entity `User` are returned, since there are no value comparisons or offsets to restrict this query. The alias, as mentioned above, is defined after the entity reference, in this case the type signature, and has the value `u`:
```
from com.braintribe.model.user.User u
```

As in select queries, we can then use this alias when referencing property sources; however, since no individual properties can be selected (entity query can only return whole entity instances), property sources are only used as part of value comparisons, or other conditions added to the end of the query statement, for example `order by`.

The following example shows a value comparison on the property `lastName`. Again, as in a select query, the alias is added to the property with the use of a period (`.`) to define the property source:
```
from com.braintribe.model.user.User u where u.lastName = 'cortex'
```

## Property Query
A property query does not require an alias.

The following property query returns the value of `name` of the `User` type with the ID of `john.smith`:
```
property name of reference(com.braintribe.model.user.User, 'john.smith')
```

### Property Query Aliases
Although optional, property query aliases can be defined by added the alias after the property source reference.

The following example shows the most basic property query. It will return the property value of `lastName` belonging to the entity instance `User` which has the ID of `john.smith`:
```
property name n of reference(com.braintribe.model.user.User, 'john.smith')
```

A property query can only return the value found at a particular entity instance. This means that when using aliases to reference property sources, it only makes sense to do so when the property type is a collection (List, Set, Map). This allows the use of value comparisons, or other conditions added to the end of the query statement, for example `order by`.

The following property query uses a value comparison on the `User` entity's property `roles`, a set containing `Role` entities. The `Role` entity contains various properties, including `name`. We can use the alias to search the elements in the collection and return only these that match the value comparison. In this example, only `Role` entities with the name `tf-admin` are returned:
```
property Roles r of reference(com.braintribe.model.User, 'cortex') where r.name like 'tf-admin'
```

As you can see the alias refers to each element in the collection, meaning that this query will parse through each each string value and return only those that match the search phrase `tf-admin`; however, to complete this comparison we need a left-hand operator, and this is where we can use the alias.

Taking the example further, we can make use of the `*` wildcard. The following query will return any `Roles` belonging to the User `john.smith` where the role name ends with `*.a`:
```
property roles r of reference(com.braintribe.model.user.User, 'john.smith') where r.name like  '*.a'
```

## Special Characters
There are some characters in GMQL that a very specific meaning:

Character | Description
----------| -----------
`''` | Single quotation marks are used for quoting Strings. For example: `'userName'`
`()` | Parentheses are used for: <br/> - query functions, `now()`, `sum()`, `avg()`, etc. <br/> - literal functions <br/> - sets, using the operand `in()`
`""` | Double quotation marks are used to escape keywords. For example: `com.braintribe.model."select".Entity` - this type signature requires double quotation marks, since `select` is also a keyword associated with the select query.
`[]` | Square brackets are used to specify and index in a list or a key in a map
`:` | A colon is used to pass a variable: `:variableName`

## Literal Functions
There are several functions that are used to reference enums and entity types in tribefire.
* `enum (typeSignature, constant)`
  This creates an enum reference using the two parameters that are passed. The `typeSignature` refers to the actual enum, while the `constant` refers to the constant in that particular enum.
  ```
  select * from com.braintribe.model.Person p where enum(com.braintribe.model.Color, red) != null
  ```
* `reference(typeSignature, value, true|false)`
  This function creates an entity reference using three parameters. The `typeSignature` defines the actual entity to be referenced; `value` can be any valid value object, and finally the boolean parameter determines the type of reference to be created:
  * `true` creates a Persistent Entity Reference
  * `false` creates a Preliminary Entity Reference
  > If this boolean value is not defined it will default to `true`. 
  One use of the reference function is when creating a property query, since it needs a reference to a specific entity instance.
  ```
  select * from com.braintribe.model.Person p where reference(com.braintribe.model.Person, 23) !=null
  ```
  Returns all instances of `User` where the `picture` property contains the instance of `AdaptiveIcon` with the ID `2` (note: L indicates that 2 is of the type long.)
  ```
  select * from com.braintribe.model.user.User u where u.picture = reference(com.braintribe.model.resource.AdaptiveIcon, 2L)
  ```
* `typeSignature(value)`
  This function creates a entity signature reference and represents the type signature of the given value. You can use as part of a selection or as part of a comparison and it will return the type signature for the entity or enum of the value defined.
  ```
  select typeSignature(r) from com.braintribe.model.user.User u join u.roles r
  ```
  Here the query will join two entity types on the property `roles`. This is `User` entity and the `Role` entity. The query will print out the type signature for the role entity.

## Supported Base Types

Type | Description
----------| -----------
Boolean |
String | - must be surrounded by single quotes <br/> - example: `a string` <br/> - you can include escaped Unicode characters
integer | - hex values are allowed <br/> - signed values (for example `+30` and `-30`) are allowed <br/> - if no sign is provided positive values are assumed <br/> - signed values can be either decimal or hexadecimal.
long | - number must be followed by a `L` or `l` character. Example: `123L`, `0x01L` <br/> same format as integer
double | - hexadecimal is not supported <br/> - must be followed by a `d` or `D` unless <br/> - `E` or `e` is used to express an exponent <br/> - a decimal point is used <br/> - for example: `1D`, `1.02`, `1.234E`
decimal | - number must be followed by a `B` or `b` character. Example: `1.023B`, `.23b`, `1.23E4B`, `10.43e+56B` <br/> - corresponds to Java's `BigDecimal` <br/> - has the same format as double
float | - must be followed with a `F` or `f`. for example: `1.203f`, `1.234E4F` <br> - same format as double
date | - `date(1999Y)` – defines the date 1999/01/01 at midnight <br/> - `date(1999Y, 2M)` – defines the date 1999/02/01 at midnight <br/> - `date(1999Y, 2M, 2D)` – defines the date 1999Y, 2M, 2D at midnight <br/> - `date(1999Y, 2M, 2D, 14H, 15M, 50S)` <br/> - date(1999Y, 2M, 2D, 14H, 15M, 50S, 324s, -0100Z)
null | - represents a null object
