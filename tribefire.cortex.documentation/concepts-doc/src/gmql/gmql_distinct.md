# GMQL Distinct
Distinct can be used on any of the query types and should be placed before the entity and property statements and after the select statement.

## Select Query
```
select distinct SOURCE_PROPERTIES|* from ENTITY_REFERENCE ALIAS
```
For example:
```
select distinct p.name from com.braintribe.model.Person p
```

## Entity Query
```
distinct from ENTITY_REFERENCE
```
For example:
```
distinct from com.braintribe.model.user.User
```

## Property Query
The functionality of the property query (returns the instance of the property requested) means that a distinct statement only makes sense on a collection property, since only a property that is of a collection type can contain multiple elements that are the same.
```
distinct property ENTITY_REFERENCE
```
For example:
```
distinct property roles of reference(com.braintribe.model.user.User, 'john.smith')
```
