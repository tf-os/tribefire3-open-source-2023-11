# GMQL Group By

The Group By function is used o group the aggregate results by a defined property.

The Group By function is used in conjunction with aggregate functions to group the aggregate results by the property defined in the group by function.

```
select AGGREGATE_FUNCTION from ENTITY_REFERENCE ALIAS group by PROPERTY_REFERENCE
```

The following query counts the amount of roles in each instance, and are grouped by `name` property of `User`. This in effect shows counts the amount of roles of each `User` instance and groups the results by the name of each `User`.
```
select u.name, count(u.roles) from com.braintribe.model.user.User u group by u.name
```
