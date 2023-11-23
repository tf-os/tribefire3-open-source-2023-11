# Legacy REST Query Examples

You can find example REST GMQL queries on this page.

[](asset://tribefire.cortex.documentation:includes-doc/rest_old_tip.md?INCLUDE)

## Available Query Examples

* [Assembly Query](rest_query_examples.md#assembly-query)
* [Property Query](rest_query_examples.md#property-query)
* [Entity Query](rest_query_examples.md#entity-query)
* [Select Query](rest_query_examples.md#select-query)


## General

The query REST calls allow you to build queries using the parameter `statement`.  

There are three types of query available in tribefire, using the Generic Model Query Language:

* `PropertyQuery` - returns a value for a property belonging to a specific entity instance.
* `EntityQuery` - returns a list of entities, including all properties, for a specific entity type. You can also use conditions and orderings to refine the results returned.
* `SelectQuery` - returns only specific properties for entity, uses more than one entity as a data source, as well as using conditions and orderings standard to the `EntityQuery`.

### Using GMQL

For more information on GMQL syntax, see [GMQL Query Types](asset://tribefire.cortex.documentation:concepts-doc/gmql/gmql_types.md).

## Assembly Query 

As well as being able to build the query using GMQL that is given as the `statement` property, the assembly query request has the property `body` where a serialized JSON object is given. This can be either as a full JSON object or a `URLEncoded` body.

You can use the assembly query request to carry out Entity, Select, and Property Queries; the REST statement remains the same, but, of course, the JSON object is different for each particular query.

The basic URL of the assembly query request is:
`http://localhost:8080/tribefire-services/rest/assembly-query?sessionId=<sessionId>&accessId=<accessId>&body=<body>`

### Entity Query

The following query is a simple one that returns only instances of the entity `User` where the first name is `Mary`. It also includes a traversing criteria pattern that includes the groups of this `User` instance.

```json
http://localhost:8080/tribefire-services/rest/assembly-query?sessionId=yourSessionId&accessId=auth&statement=from com.braintribe.model.user.User&body=
{
  "_id" : "0",
  "_type" : "com.braintribe.model.query.EntityQuery",
  "distinct" : false,
  "entityId" : null,
  "entityTypeSignature" : "com.braintribe.model.user.User",
  "evaluationExcludes" : null,
  "froms" : null,
  "ignorePriviledgedRoles" : null,
  "ordering" : null,
  "queryContext" : null,
  "restriction" : {
    "_id" : "1",
    "_type" : "com.braintribe.model.query.Restriction",
    "condition" : {
      "_id" : "2",
      "_type" : "com.braintribe.model.query.conditions.ValueComparison",
      "entityId" : null,
      "leftOperand" : {
        "_id" : "3",
        "_type" : "com.braintribe.model.query.PropertyOperand",
        "entityId" : null,
        "propertyName" : "firstName",
        "source" : null
      },
      "operator" : {
        "_type" : "com.braintribe.model.query.Operator",
        "value" : "equal"
      },
      "rightOperand" : "Mary"
    },
    "entityId" : null,
    "paging" : null
  },
  "traversingCriterion" : {
    "_id" : "4",
    "_type" : "com.braintribe.model.generic.pr.criteria.PatternCriterion",
    "criteria" : [ {
      "_id" : "5",
      "_type" : "com.braintribe.model.generic.pr.criteria.EntityCriterion",
      "id" : null,
      "strategy" : null,
      "typeSignature" : "com.braintribe.model.user.User"
    }, {
      "_id" : "6",
      "_type" : "com.braintribe.model.generic.pr.criteria.PropertyCriterion",
      "id" : null,
      "propertyName" : "groups",
      "typeSignature" : null
    } ],
    "id" : null
  }
}
```

This query returns:

```
[
  {
    "_type": "com.braintribe.model.user.User",
    "_id": "0",
    "email": "mary.williams@braintribe.com",
    "firstName": "Mary",
    "globalId": "a7f7abc7-52e9-4339-8dea-b11ebf6496c9",
    "id": "mary.williams",
    "lastName": "Williams",
    "name": "mary.williams",
    "partition": "auth",
    "password": "*****"
  }
]
```

### Select Query

The following select query creates a join on the `User` entities and the `Roles` entities. It also selects the `name` property for both the `User` and `Roles` entities.

```json
http://localhost:8080/tribefire-services/rest/assembly-query?sessionId=yourSessionId&accessId=auth&statement=from com.braintribe.model.user.User&body=
{
  "_id" : "0",
  "_type" : "com.braintribe.model.query.SelectQuery",
  "distinct" : false,
  "entityId" : null,
  "evaluationExcludes" : null,
  "froms" : [ {
    "_id" : "1",
    "_type" : "com.braintribe.model.query.From",
    "entityId" : null,
    "entityTypeSignature" : "com.braintribe.model.user.User",
    "joins" : {
      "_type" : "set",
      "value" : [ {
        "_id" : "2",
        "_type" : "com.braintribe.model.query.Join",
        "entityId" : null,
        "joinType" : {
          "_type" : "com.braintribe.model.query.JoinType",
          "value" : "inner"
        },
        "joins" : null,
        "property" : "groups",
        "source" : {
          "_ref" : "1"
        }
      } ]
    }
  } ],
  "groupBy" : null,
  "ignorePriviledgedRoles" : null,
  "ordering" : null,
  "queryContext" : null,
  "restriction" : null,
  "selections" : [ {
    "_id" : "3",
    "_type" : "com.braintribe.model.query.PropertyOperand",
    "entityId" : null,
    "propertyName" : "name",
    "source" : {
      "_ref" : "1"
    }
  }, {
    "_id" : "4",
    "_type" : "com.braintribe.model.query.PropertyOperand",
    "entityId" : null,
    "propertyName" : "name",
    "source" : {
      "_ref" : "2"
    }
  } ],
  "traversingCriterion" : null
}
```

## Property Query

The property query is used only to return a specific value, or values if the property type is a collection for a specific instance of a given entity.

The basic syntax of the property query is: `property PROPERTY_NAME of entity(TYPE_SIGNATURE, ID)`.

### Simple Properties

The simplest property query requires you to define the entity type, the property `name` belonging to the entity, and the `id` of the specific instance whose value should be returned. For example, to return the value for the property `lastName` that belongs to the entity `Person` whose `id` is `5`:

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=property lastName of reference(tribefire.demo.model.data.Person, 51L)
```

This returns the value of the property of `lastName`, belonging to the instance of the entity `Person` whose `id` is `5`.

### Complex Properties

If you query a property whose type is complex, the query returns the complex type instance.

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=property children of reference(tribefire.demo.model.data.Person, 1L)
```

This returns the complex entity found at the property, in this case `Person`:

```json
[
  {
    "_type": "tribefire.demo.model.data.Person",
    "_id": "0",
    "firstName": "J.J.",
    "gender": {
      "value": "male",
      "_type": "tribefire.demo.model.data.Gender"
    },
    "id": {
      "value": "21",
      "_type": "long"
    },
    "lastName": "Doe",
    "partition": "access.demo",
    "ssn": "555"
  },
  {
    "_type": "tribefire.demo.model.data.Person",
    "_id": "1",
    "firstName": "Mary",
    "gender": {
      "value": "female",
      "_type": "tribefire.demo.model.data.Gender"
    },
    "id": {
      "value": "26",
      "_type": "long"
    },
    "lastName": "Doe",
    "partition": "access.demo",
    "ssn": "666"
  }
]
```

## Entity Query

An entity query is used to return instances of a particular entity. Not as powerful as the select query, entity query can, however, still perform ordering and restrictions to return a specific set of instances. The basic entity query uses the `from` keyword to select the source entity, using the type signature to define the particular entity.

The most simple entity query syntax is: `from TYPE_SIGNATURE`.

An example of the most simple entity query is:

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=from tribefire.demo.model.data.Person
```

This returns **all** instances from the `Person` type, as defined by its type signature.

### Restriction

It is possible, however, through the use of restrictions, to place a condition on the entity query, so that it returns only the set of instances that match that particular condition. This is done through the use of the `where` keyword.

#### Operators

When creating a restriction, one of the most important characters is the operator. This allows you to create a value comparison on specific property values. Below is a list of operator values allowed using REST:

Operator    | Description
------- | -----------
`contains` | Used when investigating collections, this operator is used to check whether a certain value is contained within a collection.
`in`| Left operand element is in right operand collection. Right must be a collection that contains the element like the value defined by the left operand.
`like` | Used in string comparisons. For example, `firstName like "Rob"` returns any instance where the string value matches exactly the value given. In the case of the example only `Rob` is returned, not `Robert`. If you wish to match partial strings, you must use the `*` wildcard. For example, `firstName like "Rob*"` returns both `Rob` and `Robert`.
`>=` | greater than or equal to
`>`	| greater than
`!=` | not equal to
`<	` | less than
`<=` | less than or equal to
`=	` | Whether the left-hand value equals the right-hand value

#### Property Restriction

The property restriction matches a specific property against a value and only returns instances that match this condition. The syntax for property restriction is: `from TYPE_SIGNATURE where PROPERTY_NAME OPERATOR PROPERTY_VALUE`

This is similar to an equation, where the left-hand side must match the right-hand side according to an operator.

The following shows a simple restriction example, using the `where` keyword.
```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=from tribefire.demo.model.data.Person where lastName like 'Doe'
```

This returns only instances of `Person` that have the value `Doe` in the property `lastName`.

#### Complex Property Restriction

If the property that you wish to place a restriction on is of a complex type, you must also refer to a specific property contained within the complex property itself. For example, to refer to the `id` property of the entity instance (of the type of the complex property) you use the property name plus the property contained in the complex entity, separated by a point `.`

The syntax for a complex property value comparison is: `from TYPE_SIGNATURE where COMPLEX_PROPERTY.PROPERTY OPERATOR VALUE`

For example, in the demo model there is an entity `Person` which has the property `father`. This property is of the type `Person` (referring to the entity of the same name). To return all instances of `Person` which have a certain father, you can refer to its (the related father's) `id`.

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=from tribefire.demo.model.data.Person where father.id = 1L
```

This returns all instances of `Person` which have the `Person` instance with the `id` of `1` assigned to its `father` property.
> Because the `depth` property is not used, the call only returns information about simple types.

A second method of comparing complex properties is to use the `reference(typeSignature, id)` parameter. This is used to compare an entity with the entity assigned to a complex property.

The syntax is as follows: `from TYPE_SIGNATURE where PROPERTY = reference(TYPE_SIGNATURE, ID)`

Using the same example above:

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionOD&accessId=access.demo&statement=from tribefire.demo.model.data.Person where father = reference(tribefire.demo.model.data.Person, 1L)
```

This call returns the same information as the first call.

#### Collection Property Restriction

##### In

The operator `in` is used to compare one value against a set of values contained in a collection. If the value provided is contained within this collection the entity instance is returned as part of the results set. The value is provided on the left-half side of the operator while the set is provided on the right-hand side.

There are two specific ways that this operator can be used. You can either use this operator to search  a collection property for the value defined, or you search on a specific property for one of several values passed as a set.

In the following example, the `Person` entity has a property called `nicknames` which is a set. This query returns all instances of the `Person` where the value passed (`Smith`) is contained in the `nicknames`. This means that any entity whose `nicknames` property contains the value `Smith` is returned.

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=from tribefire.demo.model.data.Person where 'Doe' in children
```

In the next example, the `Person` entity is used and has a property called `lastName`. The query returns all instances of the `Person` where the `lastName` property is matched against the values passed. If one of the values matches, the instance is returned. This means any entities that have the value `Doe` or `Ponton` defined in `lastName` are returned.

```
http://localhost:8080/tribefire-services/rest/query?sessionId=820c7633-59c0-45ea-92be-98056cf932e9&accessId=access.demo&statement=from tribefire.demo.model.data.Person where lastName in ["Doe", "Ponton"]
```

##### Contains

If you wish to search on entities which have a property of the type collection against a specific entity belonging to that collection, you can use the keyword `contains`. This is used to return instances whose collection property contains a specific entity.

The syntax for this property is as follows: `from TYPE_SIGNATURE where COLLECTION_PROPERTY contains entity(TYPE_SIGNATURE, ID)`

For example, the entity `Person` has a collection property called `children`. Using the `contains` keyword you can return all instances of `Person` where `children` contains the `Person` instance whose `id` is `21`.

```
http://localhost:8080/tribefire-services/rest/query?sessionId=170613140325956d2a31451abc4989a1&accessId=access.demo&statement=from tribefire.demo.model.data.Person where children contains reference(tribefire.demo.model.data.Person, 21L, false)
```

This call returns:

```json
[ {
  "_id" : "0",
  "_type" : "com.braintribe.model.sales.Opportunity",
  "bookedDate" : {
    "_type" : "date",
    "value" : "2012-08-15T14:46:00.000+0200"
  },
  "closeDate" : {
    "_type" : "date",
    "value" : "2012-06-15T14:46:00.000+0200"
  },
  "description" : "TF - Any Madder",
  "documents" : null,
  "externalInvolvedPersons" : null,
  "id" : {
    "_type" : "long",
    "value" : 3
  },
  "importance" : 1,
  "internalInvolvedPersons" : null,
  "name" : "TF - Any Madder",
  "opportunityOfferType" : {
    "_type" : "com.braintribe.model.sales.OpportunityOfferType",
    "value" : "LICENSE"
  },
  "opportunityOffers" : null,
  "predictedAmount" : null,
  "probability" : {
    "_type" : "double",
    "value" : 100.0
  },
  "salesDocuments" : null,
  "salesStage" : {
    "_type" : "com.braintribe.model.sales.SalesStage",
    "value" : "QUOTE_ACCEPTED"
  }
} ]
```

#### Junctions

You can also use a junction (either a conjunction or a disjunction) to create more than one value comparison. A conjunction will function logically as an `AND`, meaning that all statements must be matched for a result to be returned, while a disjunction functions logically as an `OR`, meaning that one of the statements must be matched for a result to be returned.

The value comparisons are the same as shown above, and are separated either by the keyword `and` or `or`.

```
//Condition
from ENTITY_TYPE_SIGNATURE where CONDITION and SECOND_CONDITION

//Disjunction
from ENTITY_TYPE_SIGNATURE where CONDITION or CONDITION
```

##### Conjunction

```
http://localhost:8080/tribefire-services/rest/query?sessionId=170613140325956d2a31451abc4989a1&accessId=access.demo&statement=from tribefire.demo.model.data.Person where firstName like 'John' and lastName like 'Doe'
```

This returns only the instances of `Person` where the property `firstName` has the value `John` and the property `lastName` has the value `Doe`:

```json
[
  {
    "_type": "tribefire.demo.model.data.Person",
    "_id": "0",
    "anything": 1,
    "firstName": "John",
    "gender": {
      "value": "male",
      "_type": "tribefire.demo.model.data.Gender"
    },
    "id": {
      "value": "1",
      "_type": "long"
    },
    "lastName": "Doe",
    "partition": "access.demo",
    "ssn": "111"
  }
]
```

##### Disjunction

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=from tribefire.demo.model.data.Person where ssn like '333'  or ssn like '444'
```

This returns all instances of `Person` where the property `ssn` has the value of either `333` or `444`:

```json
[
  {
    "_type": "tribefire.demo.model.data.Person",
    "_id": "0",
    "firstName": "Sue",
    "gender": {
      "value": "female",
      "_type": "tribefire.demo.model.data.Gender"
    },
    "id": {
      "value": "11",
      "_type": "long"
    },
    "lastName": "Doe",
    "partition": "access.demo",
    "ssn": "333"
  },
  {
    "_type": "tribefire.demo.model.data.Person",
    "_id": "1",
    "firstName": "James",
    "gender": {
      "value": "male",
      "_type": "tribefire.demo.model.data.Gender"
    },
    "id": {
      "value": "16",
      "_type": "long"
    },
    "lastName": "Doe",
    "partition": "access.demo",
    "ssn": "444"
  }
]
```

##### Negation

Negation is used to negate the usual functionality of a value comparison. Instead of instances being returned when the comparison is matched, instance are only returned if the value is not matched.

The negate a query statement use the term `not`: `from ENTITY_TYPE_SIGNATURE where not CONDITION`.

For example, use a `where` condition to find all instances of `Person` entity where the value of `ssn` is `333`, will now be negated, so that only instances that have another value (anything other than `333`) is returned:

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=from tribefire.demo.model.data.Person where not ssn like '333'
```

##### Ordering

In addition to adding restrictions to your REST query calls, you can also add an ordering. To add an ordering you use the keyword `order by` followed by the property name. In addition you can also add an ordering by using either `asc` (for ascending – lowest to highest) or `desc` (for descending – highest to lowest).

The syntax for ordering is:

```
//Ascending
from TYPE_SIGNATURE order by PROPERTY asc

http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=from tribefire.demo.model.data.Person where not ssn like '333' order by ssn asc

//Descending
from TYPE_SIGNATURE order by PROPERTY desc

http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=from tribefire.demo.model.data.Person where not ssn like '333' order by ssn desc
```

###### Cascade Ordering

You can also order on multiple property, through the technique known as cascade ordering. This means that a list of properties are given and the results are ordered on the first, and then on the second, and so on. This is similar to a dictionary, where are A words are grouped together, then all AA words and so on.

You simply enter a list of properties which should be ordered, separated by a comma. You can also use `asc` or `desc` for each property to set that individual ordering's direction.

The syntax for Cascade Ordering is as follows: `from TYPE_SIGNATURE order by PROPERTY_ONE DIRECTION, PROPERTY_TWO DIRECTION, PROPERTY_N DIRECTION`.

This orders all instances of `Person` by `lastName` and then `firstName`, ascending and descending respectively:

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=from tribefire.demo.model.data.Person order by lastName asc, firstName desc
```

##### Paging

You can also using the parameter `limit` to restrict the amount of results that are returned. This function has two modes of operation: `limit` and `paging`. `limit` determines only the amount of results to return, beginning with the first result in the query, while `paging` requests two values, the first describing the amount of results returned and the second defining from which index the results should start from.

```
LIMIT
from TYPE_SIGNATURE limit AMOUNT_OF_RESULTS_VALUE

PAGING
from TYPE_SIGNATURE limit AMOUNT_OF_RESULTS_VALUE,START_INDEX
```

The following returns only two results:

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=from tribefire.demo.model.data.Person limit 2
```

## Select Query

The select query is the most powerful query available in tribefire. As well as containing the functionality of entity queries, it can also be used to return only specific properties.

A simple select query uses three keywords: `select`, `from`, `as`.

* `select` – used to define the properties that should be returned from the query.
* `from` – used to define the data source (that is, the main entity selected) for the query.
* `as` – used to define an alias for the data source (or in more complex queries)

### Select Types

#### One Property

The syntax for a simple select query is: `select ALIAS.PROPERTY from TYPE_SIGNATURE as ALIAS`.

This following example returns a list of `firstName` property values for the `Person` entity.

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=select p.firstName from tribefire.demo.model.data.Person p
```

#### Multiple Properties

You can use as many properties as you wish. The syntax is: `select ALIAS.PROPERTY, ALIAS.PROPERTY_2, ALIAS.PROPERTY_N from TYPE_SIGNATURE as ALIAS`.

The following returns a collection of list instances, each representing one result of `Person`, and containing the values of the two properties requested `firstName` and `secondName`:

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=accesss.demo&statement=select person.firstName, person.lastName from tribefire.demo.model.data.Person person&depth=reachable
```

This return the following, which is only a selection of the complete result:

```json
{
  "_id" : "0",
  "_type" : "com.braintribe.model.record.ListRecord",
  "id" : null,
  "values" : [ "John", "White" ]
}, {
  "_id" : "1",
  "_type" : "com.braintribe.model.record.ListRecord",
  "id" : null,
  "values" : [ "Anthony ", "Stark" ]
}
```

> If you select more than one property, each returned result is a list, because the `depth` parameter defaults to `shallow`, meaning that only simple properties are shown.

#### All Properties

You can refer to the alias to return all properties: `select ALIAS from TYPE_SIGNATURE as ALIAS`.

The following example returns full objects for each instance of `Person`:
```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=select person from com.braintribe.model.sales.Person as person
```

### Restriction

It is possible, however, through the use of restrictions, to place a condition on the select query, so that it returns only the set of instances that match that particular condition. This is done through the use of the `where` keyword.

#### Operators

When creating a restriction, one of the most important characters is the operator. This allows you to create a value comparison on specific property values. The allowed operators are the same as in entity query:

Operator    | Description
------- | -----------
`contains` | Used when investigating collections, this operator is used to check whether a certain value is contained within a collection.
`in`| Left operand element is in right operand collection. Right must be a collection that contains the element like the value defined by the left operand.
`like` | Used in string comparisons. For example, `firstName like "Rob"` returns any instance where the string value matches exactly the value given. In the case of the example only `Rob` is returned, not `Robert`. If you wish to match partial strings, you must use the `*` wildcard. For example, `firstName like "Rob*"` returns both `Rob` and `Robert`.
`>=` | greater than or equal to
`>`	| greater than
`!=` | not equal to
`<	` | less than
`<=` | less than or equal to
`=	` | Whether the left-hand value equals the right-hand value

#### Property Restriction

The property restriction matches a specific property against a value and only returns instances that match this condition. The syntax for property restriction is `select ALIAS.PROPERTY_N from TYPE_SIGNATURE as ALIAS where ALIAS.PROPERTY_NAME OPERATOR PROPERTY_VALUE`

This is similar to an equation, where the left-hand side must match the right-hand side according to an operator.

The following shows a simple restriction example, using the `where` keyword:

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=select person  from tribefire.demo.model.data.Person as person where person.lastName like Doe
```

This only returns instances of `Person` that have the value `Doe` in the property `lastName`.

#### Complex Property Restriction

If the property that you wish to place a restriction on is of a complex type, you must also refer to a specific property contained within the complex property itself. For example, to refer to the `id` property of the entity instance (of the type of the complex property) you use the property name plus the property contained in the complex entity, separated by a point `.`

The syntax for a complex property value comparison is: `select ALIAS|ALIAS.PROPERTY_N from TYPE_SIGNATURE as ALIAS where ALIAS.COMPLEX_PROPERTY.PROPERTY OPERATOR VALUE`

For example, in the sales model there is an entity `Company` which has the property `salesDocument`. This property is of the type `SalesDocument` (referring to the entity of the same name). To return all instances of `Company` which contain a certain document, you can refer to its (the related `SalesDocument`) `id`.

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionId&accessId=access.demo&statement=select salesDocument from tribefire.demo.model.data.Company as company
```

> Because the `depth` property is not used, the call only returns information for simple types. Use `depth=reachable` or `depth=n` (where `n` represents the value of the amount of levels to be traversed) to return information for complex entities.

#### Collection Property Restriction

##### In

The operator `in` is used to compare one value against a set of values contained in a collection. If the value provided is contained within this collection the entity instance is returned as part of the results set. The value is provided on the left-half side of the operator while the set is provided on the right-hand side.

There are two specific ways that this operator can be used. You can either use this operator to search  a collection property for the value defined, or you search on a specific property for one of several values passed as a set.

In the following example, the `Person` entity has a property called `nicknames` which is a set. This query returns all instances of the `Person` where the value passed (`Smith`) is contained in `nicknames`. This means that any entity whose `nicknames` property contains the value `Smith` is returned.

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionID&accessId=peopleModelAccess&statement= select per from com.braintribe.custom.model.Person per where "Smith" in per.nicknames
```

In the next example, the `User` entity is used and has a property called `lastName`. The query returns all instances of `User` where the `lastName` property is matched against the values passed. If one of the values is matched the instance is returned. This means any entities that have the value `Smith` or `Williams` defined in `lastName` are returned.

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionID&accessId=auth&statement=select per from com.braintribe.custom.model.Person as per where per.lastName in ["Smith", "Williams"]
```

##### Contains

If you wish to search on entities which have a property of the type collection against a specific entity belonging to that collection, you can use the keyword contains. This is used to return instances whose collection property contains a specific entity.

The syntax for this property is: `select ALIAS|ALIAS.PROPERTY_N from TYPE_SIGNATURE as ALIAS where ALIAS.COLLECTION_PROPERTY contains entity(TYPE_SIGNATURE, ID)`.
For example, the entity `Opportunity` has a collection property called `opportunityOffers`. Using the contains keyword you can return all instances of `Opportunity` where `opportunityOffers` contains the `OpportunityOffer` instance whose `id` is `6`:

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionID&accessId=SalesModel&statement=select opp.name from com.braintribe.model.sales.Opportunity where opportunityOffers contains entity (com.braintribe.model.sales.OpportunityOffer, 6L)
```

#### Junctions

You can also use a junction (either a `conjunction` or a `disjunction`) to create more than one value comparison. A conjunction will function logically as an `AND`, meaning that all statements must be matched for a result to be returned, while a disjunction functions logically as an `OR`, meaning that one of the statements must be matched for a result to be returned.

The value comparisons are the same as shown above, and are separated either by the keyword `and` or `or`.

```
The syntax for ordering is
//Ascending
from TYPE_SIGNATURE order by PROPERTY asc
//Descending
from TYPE_SIGNATURE order by PROPERTY desc
```

##### Conjunction

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionID&accessId=SalesModel&statement=select person.firstName, person.lastName from com.braintribe.model.sales.Person person where person.firstName like  Craig  and person.lastName like  Orman &depth=reachable
```

This only returns instances of `Person` where the property `firstName` has the value `Craig` and the property `secondName` has the value `Orman`.

```json
[ {
  "_id" : "0",
  "_type" : "com.braintribe.model.record.ListRecord",
  "id" : null,
  "values" : [ "Craig", "Orman" ]
} ]
```

##### Disjunction

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionID&accessId=SalesModel&statement=select person.position from com.braintribe.model.sales.Person person where person.position like CEO or person.position like CFO
```

This returns all instances of `Person` where the property `position` has the value of either `CEO` or `CFO`.

##### Negation

Negation is used to negate the usual functionality of a value comparison. Instead of instances being returned when the comparison is matched, instance are only returned if the value is not matched.

To negate a select query use the value `not`: `select ALIAS|ALIAS.PROPERTY_NAME`.
For example, use a `where` condition to find all instances of `Person` entity where the value of `position` is `SalesManager`, will now be negated, so that only instances that have another value (anything other than `SalesManager`) is returned.

```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionID&accessId=SalesModel&statement=select person.position from com.braintribe.model.sales.Person person where not person.position like SalesManager
```

##### Ordering

In addition to adding value comparisons to your REST query calls, you can also add an ordering. To add an ordering you use the keyword `order by` followed by the property name. Additionally, you can also add an ordering by using either `asc` (for ascending – lowest to highest) or `desc` (for descending – highest to lowest).

The syntax for ordering is:

```
//Ascending
select ALIAS|ALIAS.PROPERTY_N from TYPE_SIGNATURE as ALIAS order by ALIAS.PROPERTY asc

http://localhost:8080/tribefire-services/rest/query?sessionId=2da32ce8-c60e-4a03-827c-8f31dad0be67&accessId=SalesModel&statement=select person from com.braintribe.model.sales.Person as person  order by person.lastName asc

//Descending
select ALIAS|ALIAS.PROPERTY_N from TYPE_SIGNATURE as ALIAS order by ALIAS.PROPERTY desc

http://localhost:8080/tribefire-services/rest/query?sessionId=2da32ce8-c60e-4a03-827c-8f31dad0be67&accessId=SalesModel&statement=select person from com.braintribe.model.sales.Person as person order by person.lastName desc
```

###### Cascade Ordering

You can also order on multiple properties through the technique known as cascade ordering. This means that a list of properties are given and the results are ordered on the first, and then on the second, and so on. This is similar to a dictionary, where are A words are grouped together, then all AA words and so on.

You simply enter a list of properties which should be ordered separated by comma. You can also use `asc` or `desc` for each property to set that individual ordering's direction.

The syntax for Cascade Ordering is: `select ALIAS|ALIAS.PROPERTY_N from TYPE_SIGNATURE as ALIAS order by ALIAS.PROPERTY_ONE DIRECTION, ALIAS.PROPERTY_TWO DIRECTION, ALIAS.PROPERTY_N DIRECTION`.

This orders all instances of `Person` by `lastName` and then `firstName`, ascending and descending respectively.
```
http://localhost:8080/tribefire-services/rest/query?sessionId=yourSessionID&accessId=SalesModel&statement=select person.firstName, person.lastName, person.position from com.braintribe.model.sales.Person person order by person.lastName asc, person.firstName desc&depth=reachable
```

##### Paging

You can also using the parameter `limit` to restrict the amount of results that are returned. This function has two modes of operation: `limit` and `paging`. `limit` returns only a specific amount of results, beginning with the first result in the query, while `paging` requests two values, the first describing the amount of results returned and the second defining from which index the results should start with.

```
LIMIT
from TYPE_SIGNATURE limit AMOUNT_OF_RESULTS_VALUE

PAGING
from TYPE_SIGNATURE limit AMOUNT_OF_RESULTS_VALUE,START_INDEX
```

The following examples both return only two results, with the second REST call returning two values beginning at index number 2.
```
http://localhost:8080/tribefire-services/rest/query?sessionId=2da32ce8-c60e-4a03-827c-8f31dad0be67&accessId=SalesModel&statement=from com.braintribe.model.sales.Person limit 2

http://localhost:8080/tribefire-services/rest/query?sessionId=2da32ce8-c60e-4a03-827c-8f31dad0be67&accessId=SalesModel&statement=from com.braintribe.model.sales.Person limit 2,02
```
