# Entity Level Hibernate Mapping

This metadata is used to configure Hibernate mapping at the entity level.

Metadata Property Name  | Type Signature  
------- | -----------
`EntityMapping` | `com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping`

## General

tribefire handles the entity type mapping without any manual configuration, but you can override the automatic mapping and configure it yourself with this metadata.

tribefire handles entity type mapping without any configuration, and does so as soon as the access is deployed. This automatically creates the corresponding tables within the JDBC database defined by your connection. However, by using the aforementioned metadata, you can override the automatic mapping and configure it yourself.

There are several properties used at the entity level to create and define corresponding tables in your database.
> The properties of the EntityMapping metadata represent a series of mapping options and functionality. You do not have to use all of them - use only those relevant to your entity mapping.

## Properties

Name | Description
------| ---------
`catalog` | This property defines a direct mapping to a Hibernate `catalog` property.
`discriminatorColumnName` | This property is used when dealing with Table Per Type (TPT) inheritance. Under this mapping strategy, there exists only one database table which contains all references to all classes in the type hierarchy. This metadata property is used to distinguish the column where the reference to the particular type is stored. This strategy functions in contrast to the Table Per Class (TPC), where each class in the hierarchy is mapped to an individual table in the database. <br/> <br/> If you have an hierarchy and the top of the supertype is not referenced anywhere else (meaning no other entity has an aggregation property of the supertype) then TPC is used. However, if there is a entity which does have an aggregation with the supertype then TPT is used. <br/> <br/> The strategy used when creating a database from a model (that is, the database is populated automatically with the various tables corresponding to the model) depends on the modeling of supertypes and its subtypes. You can, however, override the automatic mapping by using the XML Hibernate files, either by providing a code snippet in the `xml` property or linking to an existing file using the `xmlFileUrl` property.
`discriminatorType` | This property defines a direct mapping to a Hibernate `discriminatorType` property.
`discriminatorValue` | This property defines a direct mapping to a Hibernate `discriminatorValue` property.
`forceMapping` | This Boolean property specifies whether or not to map a type containing subtypes. <br/> <br/> By default, only instantiable types with no subtypes and types referenced by other types are mapped as hibernate classes.
`mapToDb` |This Boolean property instructs tribefire whether this entity should be mapped to the database or not. <br/> <br/> If the box is unmarked, this means no corresponding table is created in the database, and any attempts to create data for this corresponding entity in Explorer result in an error, since there is no connection between the entity and its table.
`schema` | This property defines a direct mapping to a Hibernate schema property.
`tableName` |This property defines the name of the table in the database this entity is mapped to. <br/> <br/> This means either a new table is created with a name of the value given here, or attempt to find an existing table in the database, to complete the mapping is made.
`xml` | This property allows you to enter an XML Hibernate mapping code snippet.
`xmlFileUrl` | This property allows you to enter a link to the location of an XML Hibernate file that contains mapping information. The file is then used during the mapping process.

> For a description of general metadata that could also be helpful in a Hibernate context, see [General Metadata Properties](asset://tribefire.cortex.documentation:concepts-doc/metadata/general_metadata_properties.md).
