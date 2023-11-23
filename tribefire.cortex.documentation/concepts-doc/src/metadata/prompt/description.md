# Description

This metadata allows you to configure the entity's description.

Metadata Property Name  | Type Signature  
------- | -----------
`Description` | `com.braintribe.model.meta.data.prompt.Description`

## General

This metadata allows you to change the entity's description. To assign this metadata programmatically, use the `@Description` annotation.

This parameter is also used in Control Center as a tooltip.  ![](../../images/displayinfo_desc.png)

> This metadata supports localization.

## Localization

To have multiple versions of this metadata you must attach the annotation multiple times, once for each locale. If only a message is passed in the annotation, it is used as a value for the default locale:

```java
@Description("default description")
String getPropertyWithName();
```

If other locale needs to be specified, simply use the `locale` element of the annotation:

```java
@Description("default description")
@Description(locale = "de", value = "Beschreibung")
String getPropertyWithNames();
```

Be careful when specifying the `globalId` element. Since all annotations are turned into a single metadata instance, use only a single `globalId` value. It is enough to specify it once. 

However, you can also specify the `globalId` for each individual annotation, but you must make sure it has the same value each time.

>If different values are used, one of them is picked randomly.

Example:

```java
@Description(locale = "pl", value = "Opis", globalId = "md.descriptions")
@Description(locale = "de", value = "Beschreibung", globalId = "md.descriptions")
String getPropertyWithNames();
```

If no `globalId` is specified, a default one is generated (like for any other meta data annotation) in the form of `annotated:${ownerElement.globalId}/${annotationSimpleName}`, for example `annotated:property:com.braintribe.model.MyEntity/someProperty/Description`.