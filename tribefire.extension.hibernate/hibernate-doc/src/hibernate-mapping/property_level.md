# Property Level Hibernate Mapping

This metadata is used to configure Hibernate mapping at the property level.

Metadata Property Name  | Type Signature  
------- | -----------
`PropertyMapping` | `com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping`

## General

tribefire can handle property mapping without any manual configuration, and does so as soon as the access is deployed. This automatically creates the corresponding tables within the JDBC database defined by your connection. However, by using the aforementioned metadata, you can override the automatic mapping and configure it yourself.

There are several properties used at the property level to create and define corresponding tables in your database.
>The properties of the EntityMapping metadata represent a series of mapping options and functionality. You do not have to use all of them - use only those relevant to your entity mapping.

## Properties

Name | Description
------| ---------
`autoAssignable` | If set to `true`, the given value can not be configured by the user, but is set automatically by Hibernate/DB. A good use case is the auto-increment ID property.
`collectionElementColumn` |Defines the column from the element (meaning the entity type contained in the collection) which should be used in the collection table as a reference.
`collectionElementFetch` | This defines the value of the fetching strategy for collections (many-to-many relationships).
`collectionElementForeignKey` |This property defines the value of the foreign-key attribute of a many-to-many element of a collection property.
`collectionKeyColumn` | The name of the key that is used as a reference in the collections table.
`collectionKeyPropertyRef` | A collections table is created (in a many-to-many relationship) and a reference key also created. This defines which property in the main entity (that is, the entity which the collection belongs to) is used as a reference to said key.
`collectionTableName` | In a many-to-many relationship a table is required that contains references to the tables representing the elements on each side of the relationship. This property defines the name of the table that tribefire maps to.
`columnName` |Defines the name of the column in the database table that this property should be mapped to.
`fetch` | Defines the fetching strategy.
`foreignKey` | This property defines the value of the foreign-key attribute in property elements.
`fetch` | Defines the fetching strategy.
`idGeneration` | This determines the `id` generation strategy that should be used. The value of the property should be the name of the algorithm that is used. For example: `identity` or `hilo`.
`index` | The given value here is used to create an index on the column in the database.
`lazy` | Defines the value of the 'lazy' attribute in hibernate mapping (that is, defines the lazy strategy) as a string value. For example, `true`, `false`, `extra`.
`length` | Determines the length of the property. In the case of a string this is the amount of characters, while for a numeric value it is the number of bytes that are used to store the number.
`listIndexColumn` | Defines the name of the id column that is required when mapping a list property.
`mapKeyColumn` | A specific map table is built in the database to handle the map. This determines the name of the primary key of this table.
`mapKeyForeignKey` | This property defines the value of the foreign-key attribute of a many-to-many element of a map property. <br/> <br> This property is only used when the key of the map is an entity.
`mapKeyLenght` | Determines the length of the primary key that is created in the map table.
`mapKeySimpleType` | Determines the type of the simple type if one is mapped. <br/> <br/> If you are mapping two complex types you don't have to use this property.
`mapToDb` | This instructs tribefire whether this property is mapped to the corresponding database column or not. If the columns and tables are being created and this box is checked, this particular property is not created in the database.
`notNull` | Determines whether the corresponding column allows null values or not.
`oneToMany` | Used to determine the mapping of the property as having a one-to-many relationship with the complex type it has a single aggregation with. For example a Single Company can have one logo, but this logo can be used by multiple different companies. When this mapping option is selected, the mapping process uses a reference to the related entity in the complex type's table by means of a foreign key. <br/> <br/> If you uncheck this box, the relationship, if exists, is determined as many-to-many, for example with a multiple aggregation (`Set`, `Map`, or `List`). <br/> <br/> This is configured in the database as using a lookup table containing two columns which refer to the two tables representing the elements on each side of the relationship. If this is the case, you can use the various collection properties in PropertyMapping to define these column names.
`precision` | Defines the amount of digits in a number.
`readOnly` | If set to `true` then this property only reads from the database but is never updated. This can be useful if you have a calculated property that you definitely don't want to update.
`scale` | Defines the number of digits to the right of a decimal point in a number.
`type` | Determines the type this property should be configured in the database table as, and should generally be the same type as the property itself. This refers only to primitive types, defining a complex type (that is, entity type) here results in an error during the mapping process.
`unique` | Defines whether this corresponding column should have the unique constraint attached to it.
`uniqueKey` | Determines the name of the unique key that the property to be mapped will make. <br/> <br/> This can be configured on several different properties, each using the same Unique Key name, so that the key is a composite of values. <br/> <br/> For example, if you have a `Person` entity and wish to make a unique key from `firstName` and `lastName`. You would add the name of this unique key (`firstLastUniqueKey`, for example) to both the Unique Key property of both `firstName` and `lastName` properties.
`xml` | This property allows you to enter an XML Hibernate mapping code snippet.
`xmlFileUrl` | This property allows you to enter a link to the location of an XML Hibernate file that contains mapping information. The file is then used during the mapping process.

>For a description of general metadata that could also be helpful in a Hibernate context, see [General Metadata Properties](asset://tribefire.cortex.documentation:concepts-doc/metadata/general_metadata_properties.md).
