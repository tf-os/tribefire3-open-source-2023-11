# Whitespace

This metadata is used to control how whitespaces in Strings are handled when exporting models to XML.

Metadata Property Name  | Type Signature  
------- | -----------
`WhitespaceFormatting` | `com.braintribe.model.meta.data.display.formatting.WhitespaceFormatting`

## General

This metadata provides no actual functionality in tribefire, but rather is used when handling model to XML exports.

Model to XML exports are used to constrain the 'value space' of strings, and can have the following values:

* `preserve` – no whitespace formatting is undertaken. The complete value remains unchanged.
* `replace` – replaces all occurrences of tab, line feed, and carriage return with space (#x20)
* `collapse` – replace constraint is used, and then leading and trailing spaces are removed, and multiple spaces are reduced to a single space

> Control Center itself carries out no functionality regarding whitespace handling.

## Example

The metadata has only one enum property that is used to define the way whitespace are handled: `policy`

You can select one of the available options from the policy drop-down list.
