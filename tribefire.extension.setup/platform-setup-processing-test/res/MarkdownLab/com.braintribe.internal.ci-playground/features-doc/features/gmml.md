# Generic Model Manipulation Language
>Generic Model Manipulation Language (GMML) is a character-based serialization form of manipulating instances from the ManipulationModel.

## General
GMML is primarily used to feed `CollaborativeSmoodAccesses` such as the **cortex** access incrementally with data to make it a event-source database. 
{%include tip.html content="For more information on `CollaborativeSmoodAccess`, see [Smart Memory Object-oriented Database](smood.html)."%}

For fast pattern matching, the following example shows GMML in action without requiring you to read the following detailed explanation.

```
User = foo.bar.User
Group = foo.bar.Group
Status = foo.bar.Status

user1 = User()
.name = 'John'
.birthday = date(1967Y,1M,14D)
.status = Status::active

user2 = User()
.name = 'Johanna'
.birthday = date(1970Y,5M,23D)
.status = Status::active

user3 = User()
.name = 'Paul'
.birthday = date(1972Y,12M,22D)
.status = Status::active

user1.friends = (user2, user3)
user2.friends = (user1, user3)
user3.friends = (user1, user2)

group = Group('group:the-extra-ordinaries')
.members + (user1, user2, user3)
```

The pattern matching example shows how three new `User` entities are being created and equipped with simple values but also wired to each other via the `friends` property. Finally an existing `Group` entity is being looked up by its `globalId` and the three new users are added to `members` property.

## Supported Manipulations

Implementation of GMML can be found in the `com.braintribe.gm:ManipulationModel#2.0` artifact. With GMML, you can have a short notation of the certain manipulations that can happen:

* lifecycle manipulations
  * [InstantiationManipulation](javadoc:com.braintribe.model.generic.manipulation.InstantiationManipulation)
  * [DeleteManipulation](javadoc:com.braintribe.model.generic.manipulation.DeleteManipulation)
* property manipulations
  * [ChangeValueManipulation](javadoc:com.braintribe.model.generic.manipulation.ChangeValueManipulation)
  * collection manipulations
    * [AddManipulation](javadoc:com.braintribe.model.generic.manipulation.AddManipulation)
    * [RemoveManipulation](javadoc:com.braintribe.model.generic.manipulation.RemoveManipulation)
    * [ClearCollectionManipulation](javadoc:com.braintribe.model.generic.manipulation.ClearCollectionManipulation)

The format can be parsed into instances of the `ManipulationModel` for maintenance operations and later execution or can be directly executed without creating those instances. The format can be generated in the following ways:

* programmatically
  * using the [ManipulationStringifier](javadoc:com.braintribe.model.processing.manipulation.marshaller.ManipulationStringifier) on a [Manipulation](javadoc:com.braintribe.model.generic.manipulation.Manipulation) instance
  * using the [ManMarshaller](javadoc:com.braintribe.model.processing.manipulation.marshaller.ManMarshaller) on any value from any model which will result
* manually

## Statements

GMML grammar supports the following statements:

* [Variable Assignments](#variable-assignment-statements)
* [Property Manipulation](#property-manipulation-statements)
* [Delete Entity](#deleteentity)

### Variable Assignment Statements
These statements do not directly represent a manipulation but help to store values for reuse. Values can be assigned to variables with the following form:

`variable = 'value'`

Examples:

```
someVariable = 'Hello World!'
anotherVar = 1
$0 = true
_my_var1 = (1,2,3,4)
MyType = foo.bar.MyType
```

### Property Manipulation Statements

These statements represent instances of the [PropertyManipulation](javadoc:com.braintribe.model.generic.manipulation.PropertyManipulation)

Property manipulations can come in the following 2 forms:

* `variable.property operator 'value'`
* `.property operator value`

The variable must contain a value of the type `entity`, for example:

```
user2.name = 'john'
.friends + user1
```

If the variable name is left out the last targeted variable is used as property owner.

#### Property Operators

|Operator|Description|Value|Represented Manipulation|
|:---:|---|---|---|
|`=`| Assigns a value to the property| any value|[ChangeValueManipulation](javadoc:com.braintribe.model.generic.manipulation.ChangeValueManipulation)|
|`+`|Inserts one or more elements to a collection|A single value or a collection of values|[AddManipulation](javadoc:com.braintribe.model.generic.manipulation.AddManipulation)|
|`-`|Removes one or more elements to a collection|A single value or a collection of values|[RemoveManipulation](javadoc:com.braintribe.model.generic.manipulation.RemoveManipulation)|
|`--`|Clears a collection|No operand|[ClearCollectionManipulation](javadoc:com.braintribe.model.generic.manipulation.ClearCollectionManipulation)|

### Delete Entity Statements

This statements represent the [DeleteManipulation](javadoc:com.braintribe.model.generic.manipulation.DeleteManipulation)

Entities can be deleted with following form:

`- value`

The value must be of the type `entity`. Here a concrete example with a variable as value:

```
- user
```

## Properties

Properties are defined by `EntityTypes` and have a name and a type.

## Variables

GMML uses variables to assign all kinds of values for reuse in the manipulations as operands. Variables can hold the following values:

* sub types of [CustomType](com.braintribe.model.generic.reflection.CustomType)
* instances from the type system

Variable names can use one the following characters:

* `a-z`
* `A-Z`
* `$`
* `_`
* `0-9` except for the first character

## Type System

* scalar types
  * simple types
    * numeric types
      * integer types
        * `integer` - signed 32bit
        * `long` - signed 64bit
      * floating point types
        * `float` - 32bit
        * `double` - 64bit
        * `decimal` - unlimited
    * `boolean`
    * `string` - unicode
    * `date`
  * `enum`
* custom types
  * `entity`
  * `enum`
* complex types
  * `entity`
  * parameterized collection types
    * `list`
    * `set`
    * `map`

## Values

Values are instances from the types of the type system.

### Simple Values

|Type|Example Literals|
|---|---|
|`Boolean`|`true`<br>`false`|
|`string`|`'Hello World'`|
|`integer`|`16`|
|`long`|`1235678987654321L`|
|`float`|`3.14F` |
|`double`|`2.81D`|
|`decimal`|`4.669201609102990671853203821578B`|
|`date`|`date(2018Y,10M,10D,10H,54m,43S,853s,+0200Z)`|

#### String Literal Rules

String literals are opened and closed with the single quote `'`. The cannot span over more than one line currently and behave very much like JavaScript string literals. Escape characters can be used to insert certain non printable characters and exotic codepoints:  

|Escape|Meaning|Codepoint|Character|
|---|---|---|---|
|`\\`|Escape for the escape character|92|`\`|
|`\uxxxx`|Unicode codepoint|generic||
|`\b`|backspace|8||
|`\t`|horizontal tab|9||
|`\n`|line feed|10||
|`\f`|form feed|12||
|`\r`|carriage return|13||
|`\'`|single quote|39|`'`|

#### Date Literal Rules

Date literals are given in the following form:

`date(dateFragment, ...)`

The number of given date fragments is free and missing fragments are assumed to have a base value. The fragments are added up to make up the whole date. The following `dateFragment` literals are supported:

|Date Fragment|Format| Example|
|---|---|---|
|Year|integer with suffix `Y`|`1976Y`|
|Month|integer with suffix `M`|`1M`|
|Day|integer with suffix `D`|`14D`|
|Hour|integer with suffix `H`|`13H`|
|Minute|integer with suffix `m`|`42m`|
|Second|integer with suffix `S`|`30S`|
|Millisecond|integer with suffix `s`|`835s`|
|Time Zone|`+` or `-` hhmm `Z`|`+0100Z`|

Examples
```
date(1992Y)
date(1992Y,10M,10D)
```
#### Enum Constants

Enum values come in the following form:

`type::constant`

Example with a prepared variable for the type:
```
Status = foo.bar.Status
user.status = Status::active
```

Example with variable assignment for the type:
```
user.status = (Status = foo.bar.Status)::active
```

#### Entity Values

##### Entity Instantiations

Such values represent an [InstantiationManipulation](javadoc:com.braintribe.model.generic.manipulation.InstantiationManipulation). They are formed in the following way:

`type()`

Example with a prepared variable for the type:
```
User = foo.bar.User
user = User()
```

Example with variable assignment for the type:
```
user = (User = foo.bar.User)()
```

##### Entity Lookups

Such values do not represent any manipulation and only lookup existing entities to continue with incremental operations on them. Such values are formed in the following way:

`type('globalId')`

Example with a prepared variable for the type:
```
User = foo.bar.User
user = User('globalId')
```

Example with variable assignment for the type:
```
user = (User = foo.bar.User)('globalId')
```

##### Entity Acquires

Such values represent an [AcquireManipulation](javadoc:com.braintribe.model.generic.manipulation.AcquireManipulation) and are formed in the following way:

`type['globalId']`

Example with prepared variable for the type:
```
User = foo.bar.User
user = User['globalId']
```

Example with variable assignment for the type value:
```
user = (User = foo.bar.User)['globalId']
```

#### Collection Values

Collections may only contain non collection values as elements which are of the following type:

* simple values
* entities
* enum constants

##### List Values

List values are formed in the following way:

`[value1, value2, ....]`

Examples:

```
[1,2,3]
['Hello','World']
[1,true,var,]
```

##### Set Values
Set values are formed in the following way:

`(value1, value2, ....)`

Examples:

```
(1,2,3)
('Hello','World')
(1,true,var,)
```
##### Map Values
Map values are formed in the following way:

`{key1: value1, key2: value2, ....}`

Examples:

```
{1: 'Hallo',2: 'World'}
{var1: 1L, var2: date(1976Y)}
```

##### Collection Delta Values

|Value Form|Valid for Collection Target Type|Description|
|---|---|---|
|`value`|list, set|appends/inserts or removes a single value.|
|`(value1, value2, ...)`|list, set|appends/inserts or removes a single value|
|`{pos1: value1, pos2: value2}`|list, map|inserts/puts values|