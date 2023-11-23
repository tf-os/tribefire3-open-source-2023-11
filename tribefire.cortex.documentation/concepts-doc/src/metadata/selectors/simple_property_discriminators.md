# Simple Property Discriminators

The simple property discriminators allow you to resolve metadata depending on the value of a property.

## General

Using a discriminator means that the metadata is resolved only if the property discriminator's value is matched.

`SimplePropertyDiscriminator` is the base abstract type which all other simple property discriminators inherit from. There are five discriminators which are concerned with matching a different simple type:

* Boolean
* Date
* Integer
* Long
* String

In addition, there are two other property discriminators that offer further functionality on simple type pattern matching:

* Null Property Discriminator
* String Regex Property Discriminator

Although each discriminator handles a different simple type, the functionality of those discriminators is the same.

The `SimplePropertyDiscriminator` abstract type defines one property - `discriminatorProperty`. This property is inherited by all simple property discriminators and is where you must specify the property to be matched against. The five main simple properties have an extra property - `discriminatorValue`. This is where you specify the value that should be matched against.

If and when tribefire matches the property provided in the `discriminatorProperty` property against the value found in the `discriminatorValue`, the metadata is resolved.

String Regex Property Discriminator and Null Property Discriminator provides different options for configuration. In the case of Null Property Discriminator there is an option to invert the functionality (instead of resolving the metadata when a null value is found, it is resolved when a non-null value is found); otherwise, you only need to enter the `discriminatorProperty` for this discriminator to function.

In the case of String Regex Property Discriminator, a `discriminatorRegex` property allows you to enter a regular expression that, when matched with the value found in `discriminatorProperty`, activates the metadata resolution.

> After you have configured a simple property discriminator, you must restart you tribefire host before any changes take effect.

## Boolean Property Discriminator

This discriminator has two properties that must be configured:

* `discriminatorProperty`
* `discriminatorValue`

`discriminatorProperty` defines the property where tribefire should search for the Boolean value, while `discriminatorValue` can be set to true (box checked) or false (box unchecked). The Boolean Property Discriminator resolves metadata when the `discriminatorProperty` given, which must be a Boolean value, matches the discriminator value defined.

This example uses an entity called `SalesDocument`, which has a Boolean property called `active`. The metadata Visible was added to `SalesDocument`, meaning that no entities are shown without any additional Selector being defined.

![](../../images/BooleanPropertyDiscriminator02.png)

We add the Visible metadata to `SalesDocument` and define a Boolean Property Discriminator as its selector.

We define the properties. We set `discriminatorProperty` to the value active and `discriminatorValue` is left unchecked. This means that any `SalesDocument` whose property active is set to `false` is invisible, meaning that the metadata is only resolved on these entities.

![](../../images/BooleanPropertyDiscriminator06.png)

## Date Property Discriminator

This discriminator has two properties that must be configured:

* `discriminatorProperty`
* `discriminatorValue`.

`discriminatorProperty` defines the property where tribefire should search for the Date value, while `discriminatorValue` is defined with a date. The Date Property Discriminator resolves metadata when the `discriminatorProperty` given matches the defined `discriminatorValue`.

In this example we configure an entity called `Invoice` with an instance of the metadata Hidden, meaning that all entities are invisible without any Selector configured; the Date Property Discriminator is defined as that selector. Its two properties – Discriminator Property and Discriminator Value – are then defined. Discriminator Property searches the property `paymentDate` for the date value to match, and `discriminatorValue` is the date `8/7/2014 12:00`. This means that the metadata is resolved on any entities whose property `paymentDate` matches this value.

![](../../images/DatePropertyDiscriminator02.png)

We add the Hidden metadata to the entity `Invoice` and define the Date Property Discriminator as its selector. Then, we define its properties. We set `discriminatorProperty` to the value `paymentDate` and `discriminatorValue` to `8/7/2014 12:00`.

This means that any entities whose `paymentDate` matches the provided value are invisible, meaning that the metadata is now active on these entities:

![](../../images/DatePropertyDiscriminator06.png)

## Integer Property Discriminator

This discriminator has two properties that must be configured:

* `discriminatorProperty`
* `discriminatorValue`.

`discriminatorProperty` defines the property where tribefire should search for the Integer value, while `discriminatorValue` is defined with an integer. The metadata is resolved when the `discriminatorProperty` integer  matches the `discriminatorValue` integer value.

This example uses an entity called `Keyfact`, which has the integer property `rating`. The metadata Hidden is then added to the `Keyfact` entity, which without any associated selector makes all entities invisible. A new Integer Property Discriminator is created for this metadata's selector, and defined with the `discriminatorProperty` `rating` and `discriminatorValue` of `59`. This means that the metadata is only be resolved, and hence make invisible, if the entity's rating property matches the value of `59`.

![](../../images/IntegerPropertyDiscriminator02.png)

We add the Hidden metadata to `Keyfact` and define the Integer Property Discriminator as its selector. Then, we define its properties. We set `discriminatorProperty` to `rating` and `discriminatorValue` to `59`.

This means that the metadata is only activated on entities whose rating property has the value of `59`.

![](../../images/IntegerPropertyDiscriminator06.png)

## Long Discriminator Value

This discriminator has two properties that must be configured:

* `discriminatorProperty`
* `discriminatorValue`.

`discriminatorProperty` defines the property where tribefire should search for the long value, while `discriminatorValue` is defined with a `long`. The metadata is resolved when the `discriminatorProperty` long matches the `discriminatorValue` long value.

This example uses an entity called `Person`, which has a property, `refNumber`, of the type `long`. The metadata Hidden is added to this entity, and without any selector defined it makes all entities invisible. A new Long Property Discriminator is created for this selector, and its properties are defined: `discriminatorProperty` with `refNumber` and `discriminatorValue` of `2330`. This means that the metadata is only activated for `Person` entities whose property `refNumber` has the value `2330`.

![](../../images/LongPropertyDiscriminator02.png)

We add the Hidden metadata to the `Person` entity and define the Long Discriminator Property as its selector Then, we define its properties. We set `discriminatorProperty` to `refNumber` and `discriminatorValue` to `2230`. This means that the metadata is only resolved on any entity whose property `refNumber` has the value `2230`, thus making them invisible.

![](../../images/LongPropertyDiscriminator06.png)

## String Property Discriminator

This discriminator has two properties that must be configured:

* `discriminatorProperty`
* `discriminatorValue`.

`discriminatorProperty` defines the property where tribefire should search for the String value, while `discriminatorValue` is defined with a String. The metadata is resolved when the `discriminatorProperty` String matches the `discriminatorValue` String value.

This example uses an entity called `Person`, which has the property `position`, of the type String. The metadata Hidden is added to this entity (`Person`), and without any selector defined it makes all entities invisible. A new String Property Discriminator is created for this selector, and its properties  defined: `discriminatorProperty` with `position` and `discriminatorValue` with `Technical Writer`. This means that the metadata is only activated on `Person` entities whose property `position` has the value `Technical Writer`.

![](../../images/StringPropertyDiscriminator02.png)

We add the Hidden metadata to the `Person` entity and define String Property Discriminator as its selector. Then, we define its properties. We set `discriminatorProperty` to `position` and `discriminatorValue` to `Technical Writer`.

This means that the metadata is only activated on any entity whose property `position` has the value `Technical Writer`, thus making them invisible.

![](../../images/StringPropertyDiscriminator06.png)

## Null Property Discriminator

The Null Property Discriminator resolves metadata when the provided `discriminatorProperty`'s value is `null`. You only have to define one main property - `discriminatorProperty`. It is there that you define where tribefire should look for a null value. When the value of the `discriminatorProperty`'s value is null, the metadata is resolved.

You can also use another property - `inverse`. This property negates the discriminator, so that it only activates the metadata if the value of `discriminatorProperty` is not null.

## String Regex Property Discriminator

This discriminator has two properties that must be configured:

* `discriminatorProperty`
* `discriminatorRegex`

`discriminatorProperty` defines the property where tribefire should search for the String value, while `discriminatorValue` is defined with a regular expression. The metadata is resolved when the `discriminatorProperty` String matches the `discriminatorValue` regular expression.

This example uses an entity called `Person`, which has a property `emailAddress`, of the type String. The metadata Hidden is added to this entity (Person), and without any selector defined it makes all entities invisible. A new String Regex Property Discriminator is created for this selector, and its properties are defined: `discriminatorProperty` with `emailAddress` and `discriminatorRegex` with a regular expression that matches any emails that have the ending `@braintribe.com`: `[a-zA-Z]*.[a-zA-Z]*@braintribe.com`

This means that metadata is activated on any entity whose property `emailAddress` has `@braintribe.com` as part of its value.

![](../../images/StringRegExPropertyDiscriminator02.png)

We add the Hidden metadata to the `Person` entity and define String Regex Property Discriminator as its selector. Then, we define its properties. We set `discriminatorProperty` to `emailAddress` and `discriminatorRegex` to  `[a-zA-Z]*.[a-zA-Z]*@braintribe.com`.

This means that the metadata is only activated on any entity whose property emailAddress has the pattern firstName.secondName@braintribe.com, thus making them invisible

![](../../images/StringRegExPropertyDiscriminator06.png)

## Property Value Comparator

This comparator is a powerful alternative for all the type discriminators described above, since it can compare any property with a specific value - saving you the time necessary to find a specific discriminator. In addition to exact matches, you can also set any kind of comparison using a range of standard operators.

This comparator has three properties that must be configured:

* `operator` - defines the comparison type between the set value and the actual value. You have a number of standard operators to choose from.
* `value` - the value to which the actual property value is getting compared.
* `propertyPath` - the property where Tribefire should search for the `value`. For collection properties, their size is used to evaluate the comparison.

### Property Value Comparator Collection Property Comparisons

The following comparisons are supported for collection properties using the `PropertyValueComparator`:

* `contains` comparison for collection values
    If the `propertyPath` (explicit or implicit) addresses a collection property only the `contains` operator is supported to compare whether the given compare value is contained in the collection.

* `element` comparison for list and map values:
    If the property path specifies a collection property followed by `.<index>` the `<index>` value will either taken as an index (`int` for list, `key` for map) to address a certain element in the collection. The element value will be compared against the compared value.

* `size` comparison of list, set, and map values:
    If the property path specifies a collection property followed by `.size` the size of the collection will be compared against the compared value. 

### Property Value Comparator Operators

The following operators are available:

Operator | Description
---|---
equal |  Checks if the values are equal.
notEqual | Checks if the values are not equal.
like | Used when comparing string (text) values. Your `value` is the pattern getting checked against the property value. Note that this pattern, when entered without special characters (`%` for character sequence or `_` for any single character) basically amounts to `equal`.
ilike | Similar to like, but without case sensitivity.
greater | Used to compare numbers and dates. Checks if the property value is greater than your `value`.
greaterOrEqual | As above, but including equal values.
less | Used to compare numbers and dates. Checks if the property value is less than your `value`.
lessOrEqual | As above, but including equal values.
in | Dedicated to working with collections. This operator allows you to check whether an element (your `value`) is in a collection (property value). If it is, metadata is resolved.
contains | This is the opposite of `in`. Checks if your collection (your `value`) contains an element (the property value). If it does, metadata is resolved.

In practice, you can use this comparator instead of any of the above discriminators.

Let's try to mimic the behavior of the integer discriminator described previously, where we compare our value to the `rating` value to decide whether or not to resolve the metadata:

![](../../images/IntegerPropertyDiscriminator02.png)

1. First, we can set the operator to `equal` to find a specific rating value.
2. In this case, we want to enter `rating` in the `propertyPath`, as this is where we expect our value to be.
3. Finally, we can set the value to 59. Now the comparator will behave as the integer discriminator - metadata will only be resolved if the value of `rating` is 59.