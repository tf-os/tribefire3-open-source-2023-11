# ID Generator Assignment

This metadata allows you to assign an UID generator to an entity, so when a new instance is created, a UID is assigned to its index property.

Metadata Property Name  | Type Signature  
------- | -----------
`IdGeneratorAssignment` | `com.braintribe.model.idgendeployment.IdGeneratorAssignment`

## General

For the UID generation to work, you must first configure the generator property of this metadata. Currently available generators include:

* `numericUidGenerator`
* `uuidGenerator`

Once properly configured, the ID is assigned to a new entity instance when it is first committed.
> You must configure and deploy the generator before using it.

## Example

Add the metadata to the entity you wish to assign a generated ID. Select the Metadata property of the entity and click **Add**.

Select the IdGeneratorAssignment metadata and click **Add** and **Finish**.

Click **Assign** at the **Generator** property and select the generator you wish to use. You are then asked to configure the generator by giving it a name and external ID. The configuration is the same regardless of the generator that you choose.
> After configuring the generator, you must deploy it.

Once deployed, it is ready for use. Every time you create and commit a new instance of the entity which the generator is assigned to, a new UID is generated.
