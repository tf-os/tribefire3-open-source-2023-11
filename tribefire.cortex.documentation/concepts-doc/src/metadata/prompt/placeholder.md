# Placeholder

This metadata property allows you to configure a piece of text as a placeholder in a text field.

Metadata Property Name  | Type Signature  
------------------------| -----------
 `Placeholder`| `com.braintribe.model.meta.data.prompt.Placeholder`

## General
Use the `Placeholder` metadata to add more information or special conditions for using a certain text feild. The placeholder text is then displayed sirectly inside the text field.

## Example
You can set the `Placeholder` metadata in the control center (see [Adding Metadata](../adding_metadata.md)) or in your code (see the example below)


```java
@Description("default description")
@Placeholder("Enter the name for this property. Make sure it is more than 8 characters long.")
String getPropertyWithName();
```
The following image shows how the placeholder text is displayed.

![](../../images/placeholder.jpg)