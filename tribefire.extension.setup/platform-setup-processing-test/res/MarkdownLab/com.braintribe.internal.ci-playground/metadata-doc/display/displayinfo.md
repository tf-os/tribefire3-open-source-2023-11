# DisplayInfo

You can use this metadata to configure the name, description, and icon of an element at once.

Metadata Property Name  | Type Signature  
------- | -----------
`DisplayInfo` | `com.braintribe.model.meta.data.display.DisplayInfo`

## General

Using this metadata allows you to specify several values at once:

Parameter | Description
------| ---------
`Name` | This metadata allows you to configure how the entity is labeled. For more information, see [Name](../PROMPT/name.md).
`Description` | This metadata allows you to change the entity's description. For more information, see [Description](../PROMPT/description.md).
`Icon` | This metadata allows you to change the entity's icon. For more information, see [Icon](icon.md).

If you only want to change one of the values in a subtype of your entity, it is better to use the name, description, or icon metadata individually. If you want to set all the values at the same time, it is better to use `DisplayInfo`.
