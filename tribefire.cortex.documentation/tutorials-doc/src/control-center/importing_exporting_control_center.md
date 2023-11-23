# Importing/Exporting Assemblies in Control Center

You can use Control Center to import and export assemblies.

> For more information on each of the following actions, see [Importing/Exporting Assemblies](asset://tribefire.cortex.documentation:tutorials-doc/resources/importing_exporting_assemblies.md).

## Write to Resource

<iframe width="560" height="315" src="https://www.youtube.com/embed/IR9hJA4mQhw?rel=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

Property | Description
----- | ------
Resource Base Name | Name of your resource file. If you do not provide a name, the default name `assembly` is used.
Encoding Type | File type of your resource.
Pretty Output | Boolean flag influencing whether the output is formatted with XML tags in new lines, indentation, etc.
Stabilize Order | Boolean flag influencing whether the order of collections is sorted. This comes in handy when you compare different resource files.
Write Empty Properties | Boolean flag influencing whether `null` values are exported.

## Read from Resource

<iframe width="560" height="315" src="https://www.youtube.com/embed/JvZAm3zZZeA?rel=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

Property | Description
----- | ------
Lenient | Boolean flag influencing whether reading an assembly fails when it contains unknown properties or entities of unknown types. If checked, unknown instances/values are ignored.

## Exchange Package - Export

<iframe width="560" height="315" src="https://www.youtube.com/embed/GyLAdgMVt-U?rel=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

Property | Description
----- | ------
Name | Name of your exchange package. If you do not provide a name, the default name `exchange-package-accessid-date` is used.
Description | Description of your exchange package.

## Exchange Package - Export and Write

<iframe width="560" height="315" src="https://www.youtube.com/embed/-A6OfUxYR0Q?rel=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

Property | Description
----- | ------
Name | Name of your exchange package. If you do not provide a name, the default name `exchange-package-accessid-date` is used.
Description | Description of your exchange package.
Pretty Output | Boolean flag influencing whether the output is formatted with XML tags in new lines, indentation, etc.
Stabilize Order | Boolean flag influencing whether the order of collections is sorted. This comes in handy when you compare different resource files.
Write Empty Properties | Boolean flag influencing whether `null` values are exported.

## Exchange Package - Read

<iframe width="560" height="315" src="https://www.youtube.com/embed/75fHumIhv7U?rel=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

Property | Description
----- | ------
Lenient | Boolean flag influencing whether reading an assembly fails when it contains unknown properties or entities of unknown types. If checked, unknown instances/values are ignored.

## Exchange Package - Import

<iframe width="560" height="315" src="https://www.youtube.com/embed/hmWB94-7YMQ?rel=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

Property | Description
----- | ------
Create Shallow Instance For Missing References | Boolean flag influencing whether to create shallow entities for missing references(with a globalId only) or ignore them.
Include Envelope | Boolean flag influencing whether to import the `ExchangePayload` instance, or just the content. <br/> When you import resources into tribefire, they are packaged in entity type `ExchangePayload`. This is what we call an envelope.
Requires Global ID | Boolean flag influencing whether to fail when you want to import entities which have no `globalId` property. <br/> Prior to the actual import, the system checks if an entity is already in your system based on the `globalId` property. If it is, it is not added again, but updated. If an entity you are importing has no `globalId`, it might be a problem if you want to import it multiple times.
Use System Session | Boolean flag influencing whether to use the system session for the import.


## Exchange Package - Read and Import

<iframe width="560" height="315" src="https://www.youtube.com/embed/hwtpjeqUAM4?rel=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

Property | Description
----- | ------
Lenient | Boolean flag influencing whether reading an assembly fails when it contains unknown properties or entities of unknown types. If checked, unknown instances/values are ignored.
Create Shallow Instance For Missing References | Boolean flag influencing whether to create shallow entities for missing references(with a globalId only) or ignore them.
Include Envelope | Boolean flag influencing whether to import the `ExchangePayload` instance, or just the content. <br/> When you import resources into tribefire, they are packaged in entity type `ExchangePayload`. This is what we call an envelope.
Requires Global ID | Boolean flag influencing whether to fail when you want to import entities which have no `globalId` property. <br/> Prior to the actual import, the system checks if an entity is already in your system based on the `globalId` property. If it is, it is not added again, but updated. If an entity you are importing has no `globalId`, it might be a problem if you want to import it multiple times.
