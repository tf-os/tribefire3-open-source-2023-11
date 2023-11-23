# GMQL Functions
GMQL supports the following functions:
* [`avg(.)`](gmql_functions.md#avgsourceproperty) – average value
* [`count(.)`](gmql_functions.md#countsourceproperty-distint) – total number of non-null values.
* `count(., true)` – distinct count, i.e. total number of distinct values
* [`max(.)`](gmql_functions.md#maxsourceproperty) – biggest value
* [`min(.)`](gmql_functions.md#syntax-2) – smallest value
* [`sum(.)`](gmql_functions.md#sumsourceproperty) – sum of all values
> Only select queries can carry out the above aggregate functions.
* [`now()`](gmql_functions.md#now) - returns the current time and date
* [`fullText(., .)`](gmql_functions.md#fulltextalias-string) – performs a full text search
* [`localize(., .)`](gmql_functions.md#localizevalue-string) – returns the localized value, according to country code
* [`concatenation(...)`](gmql_functions.md#concatenationstringtype--stringfunction) – joins two strings together
* [`lower(.)`](gmql_functions.md#lowerstring--stringfunction) – converts string to lower case
* [`upper(.)`](gmql_functions.md#upperstring--stringfunction) – converts string to upper case
* [`toString(.)`](gmql_functions.md#tostringstring--stringfunction--sourceproperty) – converts value to string
* [`username()`](#username) - returns the username of the currently logged in user


## avg(sourceProperty)
The `avg()` function returns the average of all values counted; the values are provided to the average function via the property passed to the function. The type of property that is defined in the function must be a countable, that is, a numeric, type.

>Only select queries can carry out `avg` functions.

### Syntax
```
select avg(ALIAS.PROPERTY_NAME) from TYPE_SIGNATURE ALIAS
```
The following query will produce the average age of all `Person` instances:
```
select avg(p.age) from com.braintribe.model.Person p
```

## count(sourceProperty, distinct)
The `count()` function counts the amount of non null values occurring in the property passed to it. There is a second parameter, `distinct`, a Boolean value that defines whether only unique values should be counted (`false`) or all non null values (`true`). If you do not pass this second parameter, the value of it defaults to false, and returns only the count for distinct values.

If the property passed to the count function is of a collection type, all elements in each collection will be counted and the total returned.

>Only select queries can carry out `count` functions.

### Syntax
Return the total amount of distinct non null values occurring in a property:
```
select count(ALIAS.PROPERTY_NAME) from TYPE_SIGNATURE ALIAS
```
You can return the amount of all non null values by using the `false` parameter.
```
select count(ALIAS.PROPERTY_NAME, true) from TYPE_SIGNATURE ALIAS
```
* distinct == false
  Returns the total amount of non null values for the property `age`.
  ```
  select count(p.age) from com.braintribe.model.Person p
  ```
* distinct == true
  Returns the total amount of distinct non null values for the property `age`.
  ```
  select count(p.age, true) from com.braintribe.model.Person p
  ```

## max(sourceProperty)
The `max()` function returns the largest value of the property passed to it. The property type passed must be a numeric type.

>Only select queries can carry out the `max` function.

### Syntax
Return the largest value contained in the property passed:
```
select max(ALIAS.PROPERTY_NAME) from TYPE_SIGNATURE ALIAS
```
The following returns the value of the oldest age found in the `age` property of the `Person` instances:
```
select max(p.age) from com.braintribe.model.Person p
```

## min(sourceProperty)
The `min()` function returns the smallest value of the property passed to it. The property type passed must be a numeric type.

>Only select queries can carry out the `min` function.

### Syntax
Return the smallest value of the property passed:
```
select min(ALIAS.PROPERTY_NAME) from TYPE_SIGNATURE ALIAS
```
Returns the value of the smallest (in this case, youngest) age found in the `age` property of the `Person` instances:
```
select min(p.age) from com.braintribe.model.Person p
```

## sum(sourceProperty)
The `sum()` function returns the total sum of all values of a numeric property passed to it.

>Only select queries can carry out the `sum` function.

### Syntax
Returns the total sum of all values of the property passed:
```
select sum(ALIAS.PROPERTY_NAME) from TYPE_SIGNATURE ALIAS
```
Returns the total sum of all ages found in the property `ages` of the `Person` instances:
```
select sum(p.age) from com.braintribe.model.Person p
```

## now()
The `now()` function returns the system's current time and date, which can be used either to display the current time or as part of a value comparison.

### Syntax
* Select Query
  ```
  select now()from TYPE_SIGNATURE ALIAS where now() OPERATOR VALUE
  ```
  The following query will use the current date as a comparison. It will compare a property called `lastLogin` from an entity `User` to show all `User`s who have at that moment logged in.
  ```
  select now() from com.braintribe.model.user.User u where u.lastLogin = now()
  ```
* Entity Query

  Since the entity query cannot select which specific properties should be returned (for example, using select in a select query), the `now()` keyword can only be used in a value comparison.
  ```
  from TYPE_SIGNATURE ALIAS where now() OPERATOR VALUE
  ```
  The following query will use the current date as a comparison. It will compare a property called `lastLogin` from an entity `User` to show all `User`s who have at that moment logged in.
  ```
  from com.braintribe.model.user.User u where u.lastLogin = now()
  ```
* Property Query

  The property query can only make value comparisons on properties that are of a collection type (List, Set, Map).
  ```
  property PROPERTY_REFERENCE of reference(TYPE_SIGNATURE, ID) where now() OPERATOR VALUE
  ```
  For example:
  ```
  property favouritePersons of reference(com.braintribe.model.Person,5L) p where now() = lastLogon
  ```

## fullText(alias, string)
The `fullText()` function performs a full text search on a specific entity, and contains two parameters:
* `alias` – defines the source entity where the full text should be executed
* `string` – defines the search string that is used by the full text

The full text search is used as part of a value comparison.

>The search phrase must be a string, therefore it must be surrounded by single quotation marks `' '`, for example: `'search phrase'`

### Syntax
* Select Query
  ```
  select ENTITY_PROPERTIES | * from TYPE_SIGNATURE ALIAS where fullText(ALIAS, SEARCH_PHRASE)
  ```
  The following query returns all entities containing the full text phrase `'Smith'`:
  ```
  select * from com.braintribe.model.user.User u where fullText(u, 'Smith')
  ```

* Entity Query
  ```
  from TYPE_SIGNATURE ALIAS where fullText(ALIAS, SEARCH_PHRASE)
  ```
  The following query returns all entities containing the full text search phrase `'cortex'`:
  ```
  select * from com.braintribe.model.user.User u where fullText(u, 'cortex')
  ```

* Property Query

  Full text searches can only be carried out on properties that are of a collection type, since these are the only property types that can be compared; all other types return a single value or instance.
  ```
  property PROPERTY_REFERENCE ALIAS of reference(TYPE_SIGNATURE, ID) where fullText(ALIAS, SEARCH_PHRASE)
  ```
  For example:
  ```
  property roles r of reference(com.braintribe.model.user.User, 'cortex') where fullText(r, 'admin')
  ```

## localize(value, string)
The localize provides the corresponding localized value according to the language code passed. This function is used only on properties that are of the `LocalizedString` type, which contains a map whose key contains a language code and its value, the corresponding word or phrase in the that language.

Language Code (id property) | Localized Value
---- | ----
en | Word
de | Wort

The function contains two parameters:
* `value` – defines the property where the `LocalizedString` is found
* `string` – the key value of the `LocaizedString` map, defining the language code.

### Syntax
```
select localize(LOCALIZED_STRING_PROPERTY, 'LOCALIZED_CODE') from TYPE_SIGNATURE ALIAS
```
The following will provide the results of the property `jobDescription` for the instances of the entity `Person` in German.
```
select localized(p.jobDescription, 'de')from com.braintribe.model.Person p
```

## concatenation(stringType | stringFunction)
The `concatenation()` function merges two strings together, which can then either be displayed as part of the returned results or as part of a value comparison. For example, two values are provided `StringA` and `StringB`; the result of this concatenation is `StringAStringB`. This function accepts either string types or a string function.

The function can be passed as many string types as is required, with each passed string type being separated by a comma.

### Syntax
* Select Query

  The `concatenation()` function used as part of a value comparison:
  ```
  select * from TYPE_SIGNATURE ALIAS where concatenation(STRING_TYPE OR STRING_FUNCTION) COMPARISON_OPERATOR COMPARISON_OPERAND
  ```
  Displays the concatenation as part of the select query:
  ```
  select concatenation(STRING_TYPE OR STRING_FUNCTION) from TYPE_SIGNATURE
  ```
  This query will concatenate the two properties `p.firstName` and `p.lastName` before comparing it to a specific value:
  ```
  select * from com.braintribe.model.Person p where concatenation(p.firstName, p.lastName) = 'DavidRoberts'
  ```
  It is also possible to use string functions within the concatenation function `u.firstName` is converted to lowercase using lower and concatenated with `u.lastName`; all User instances where the result of the concatenation is `robertTaylor`.
  ```
  select * from com.braintribe.model.user.User u where concatenation(lower(u.firstName), u.lastName) = 'robertTaylor'
  ```

* Entity Query

  ```
  from TYPE_SIGNATURE where concatenation(STRING_TYPE OR STRING_FUNCTION) COMPARISON_OPERATOR COMPARISON_OPERAND
  ```
  This query will concatenate the two properties `firstName` and `lastName` before comparing it to a specific value:
  ```
  from com.braintribe.model.Person where concatenation(firstName, lastName) = 'DavidRoberts'
  ```

* Property Query

  The property query can only carry out value comparisons on properties that are of a collection type (List, Set, Map).
  ```
  property PROPERTY_REFERENCE of reference(TYPE_SIGNATURE, ID) where concatenation(STRING_TYPE OR STRING_FUNCTION) COMPARISON_OPERATOR COMPARISON_OPERAND
  ```
  This query will concatenate two property values and compare it to a specific value.
  ```
  property favouritePersons of reference(com.braintribe.model.Person, 5L) where concatenation(firstName, lastName) like 'DavidRoberts'
  ```

## lower(string | stringFunction)
The `lower()` function is used to convert strings to all lowercase – a value, for example, `Smith`, is converted to `smith` This function can only be carried on properties of the type string or string functions, which can then either be displayed as part of the returned results or as part of a value comparison.

The function can be passed as many string types as is required, with each passed string type being separated by a comma.

### Syntax
* Select Query
  ```
  select * from TYPE_SIGNATURE ALIAS where lower(STRING_TYPE OR STRING_FUNCTION) COMPARISON_OPERATOR COMPARISON_OPERAND
  ```
  Returns all instances of the `Person` entity when the lowercase of `p.firstName` is equal to `robert`.

* Entity Query
  ```
  from TYPE_SIGNATURE ALIAS where lower(STRING_TYPE OR STRING_FUNCTION) COMPARISON_OPERATOR COMPARISON_OPERAND
  ```
  Returns all instances of the `Person` entity when the lowercase of `p.firstName` is equal to `robert`.
  ```
  from com.braintribe.model.Person p where lower(p.firstName) = 'robert'
  ```

* Property Query

  The property query can only carry out value comparisons on properties that are of a collection type (List, Set, Map).
  ```
  property PROPERTY_REFERENCE of reference(TYPE_SIGNATURE, ID) where lower(STRING_TYPE OR STRING_FUNCTION) COMPARISON_OPERATOR COMPARISON_OPERAND
  ```
  Returns all instances of the `User` entity when the lowercase of `l.firstName` is equal to `john`.
  ```
  property users u of reference(com.braintribe.model.user.Group, 'operators') where lower(u.firstName) like 'john'
  ```

## upper(string | stringFunction)
The `upper()` function is used to convert strings to all uppercase – a value, for example, `smith`, is converted to `SMITH`. This function can only be carried on properties of the type string or string functions, which can then either be displayed as part of the returned results or as part of a value comparison.

The function can be passed as many string types as is required, with each passed string type being separated by a comma.

* Select Query
  ```
  select * from TYPE_SIGNATURE ALIAS where upper(STRING_TYPE, OR_STRING_FUNCTION) COMPARISON_OPERATOR COMPARISON_OPERAND
  ```
  Returns all instances of `Person` when the uppercase value of `p.firstName` is equal to `ROBERT`:
  ```
  select * from com.braintribe.model.Person p where upper(p.firstName) = 'ROBERT'
  ```

* Entity Query
  ```
  from TYPE_SIGNATURE ALIAS where upper(STRING_TYPE, OR_STRING_FUNCTION) COMPARISON_OPERATOR COMPARISON_OPERAND
  ```
  Returns all instances of `Person` when the uppercase value of `p.firstName` is equal to `ROBERT`:
  ```
  from com.braintribe.model.Person p where upper(p.firstName) = 'ROBERT'
  ```

* Property Query

  The property query can only carry out value comparisons on properties that are of a collection type (List, Set, Map):
  ```
  property PROPERTY_REFERENCE of reference(TYPE_SIGNATURE, ID)where upper(STRING_TYPE, OR_STRING_FUNCTION) COMPARISON_OPERATOR COMPARISON_OPERAND
  ```
  Return all instances of User contained in users of the `Group` entity with the ID `admins` where the uppercase value of `u.lastName` is equal to `SMITH`.
  ```
  property users u of reference(com.braintribe.model.user.Group, 'admins') where upper(u.lastName) like 'SMITH'
  ```

## toString(string | stringFunction | sourceProperty)
The `toString()` function is used to convert parameters passed to a string type. This function can be carried out on properties of any type or on string functions, which can then either be displayed as part of the returned results or as part of a value comparison.

* Select Query
  ```
  select toString(VALUE_TO_STRING) from TYPE_SIGNATURE ALIAS
  select ENTITY_PROPERTIES|* from TYPE_SIGNATURE ALIAS where toString(STRING_TYPE OR STRING_FUNCTION) OPERATOR OPERAND
  ```
  Returns all instances of `Person`, selecting the property `dateOfBirth`, returned as a string:
  ```
  select toString(p.dateOfBirth) from com.braintribe.model.Person p
  ```
  Returns all instances of `Person`, selecting `firstName` and `lastName` properties when the `toString` value of `dateOfBirth` matches the string comparison:
  ```
  select p.firstName, p.secondName from com.braintribe.custom.model.Person p where toString(p.dateOfBirth) like  'Wed Aug 19 14:23:00 CEST 1987'
  ```

* Property Query
  ```
  property PROPERTY_REFERENCE of reference(TYPE_SIGNATURE, ID) where toString(STRING_TYPE OR STRING_FUNCTION) OPERATOR OPERAND
  ```
  Return all instances of `User` in users of `Group` entity with the ID `operators` when the value of `u.name` is equal to `smith`.
  ```
  property users u of reference(com.braintribe.model.user.Group, 'operators') where toString(u.name) like 'john.smith'
  ```

## username()

The `username()` function returns the name of the user that is currently logged in.

Imagine you have an entity type in your model called `Document` which has a property called `owner`. The `owner` property contains a username. Let's also assume you want to build a query that searches through all documents that have the current user set as `owner`.

You could, of course, build individual queries with the username hardcoded. You could also build a single generic query with the `username()` function:

```
from Document d where d.owner = username()
```

> Note that the function simply returns the current username and it is up to you to use it somewhere in your query.