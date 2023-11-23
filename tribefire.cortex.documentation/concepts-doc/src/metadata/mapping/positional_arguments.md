# Positional Arguments

This metadata allows you to create a list of arguments whose position determines the order they should be provided in an API call.

Metadata Property Name  | Type Signature  
------- | -----------
[`PositionalArguments`](javadoc:com.braintribe.model.meta.data.mapping.PositionalArguments) | `com.braintribe.model.meta.data.mapping.PositionalArguments`

## General

In Tribefire API calls you can use two kinds of arguments:

* named
* positional

Positional arguments are mapped to a property name and are an alternative way to address that property. If you don't have many arguments in your request, it is better to use positional arguments.

## Example

Assuming you have the following REST call: `api/cortex/getModel?name=someModelName&version=someVersion`, there are two parameters there:

* name
* version

The call above uses named parameters, which means that you must provide the `name=` and `version=` code every time you make this call. Using positional parameters, you could create a list of parameters so that the `getModel` call expects the `name` and `version` parameters to follow: `api/cortex/getModel/someModelName/someVersion`
