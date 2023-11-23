# Alias

This metadata allows you to configure an alias for an element. An alias provides an alternative way to call a certain element in the code.

Metadata Property Name  | Type Signature  
------- | -----------
[`Alias`](javadoc:com.braintribe.model.meta.data.mapping.Alias) | `com.braintribe.model.meta.data.mapping.Alias`

## General

As opposed to a [Name](asset://tribefire.cortex.documentation:concepts-doc/metadata/prompt/name.md), you cad add multiple aliases to a single element. You can assign aliases to:

* enum constants
* enum types
* property names
* entity types

## Example

Assuming you have a property called `documentationInputPath` which is used by a command-line application, if you wanted to use this property in a REST or Java API call, you would need to type the full property name every time: `cli generate --documentationInputPath="path"`.

If you assign an alias, let's say `@Alias("d")` which stands for the full name of the property, you can provide the value of this property in the following way `cli generate -d="path"`.