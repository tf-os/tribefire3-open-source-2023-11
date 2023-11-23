# VirtualEnum

This metadata allows you to configure a property as if it was an enum property.

Metadata Property Name  | Type Signature  
------- | -----------
`VirtualEnum` | `com.braintribe.model.meta.data.prompt.VirtualEnum`

## General

If the VirtualEnum is set to a property, then that property, in edit mode, will behave as if it was an enum property, which is, it will display a combobox so the users can selected a value from a possible value list.

The configurable aspects are:

**forceSelection** - if true, the the users must not edit directly the field, but they are forced to choose between one of the possible values displayed;

**contants** - a list of `VirtualEnumConstant` containing the list of possible values;

The `VirtualEnumConstant` can be configured with the following properties:

**value** - the actual value of a `VirtualEnumConstant` that will be assigned to the property with the VirtualEnum metadata after the edition;

**displayValue** - the `LocalizedString` display value to be shown to the users for each constant;

**icon** - the icon to be displayed alongside the display value;
