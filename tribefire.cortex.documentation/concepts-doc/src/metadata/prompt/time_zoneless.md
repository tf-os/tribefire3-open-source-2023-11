# TimeZoneless

This metadata allows you to configure a date property to be formatted and inputted at UTC timezone.

Metadata Property Name  | Type Signature  
------- | -----------
`TimeZoneless` | `com.braintribe.model.meta.data.prompt.TimeZoneless`

## General

If the TimeZoneless is set to a date property, then we display that property always at the UTC timezone, which means, the displayed value will always be the same no matter on which time zone the users are in.

## Example

When entering birthday *14.01.1976* in Europe, it should always view *14.01.1976* in Brazil as well.
