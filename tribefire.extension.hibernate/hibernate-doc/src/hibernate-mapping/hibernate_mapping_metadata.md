# Hibernate Mapping Metadata

Hibernate mapping in the context of tribefire refers to the mapping of entities and their properties to tables in a JDBC database. tribefire can carry out this process automatically, but it is also possible to control the mapping process using metadata.

## General

Hibernate-related metadata offer the opportunity to configure entities and properties using a Hibernate `.xml` file (`\*.hbm.xml`). This can be done either by entering a code snippet in the XML property or by to a linking to specific `.hbm.xml` by entering its URI in the `xmlFileUri` property. This allows you more control in the configuration of your model and its equivalents in a database.
Depending on your approach, there are several ways to map tribefire model elements and database tables. You can either:

* create and design the model yourself, and when finished add the entity and property mapping metadata, or
* use the tribefire connection to generate a database scheme from a database connection, and generate a model from the schema, along with the relevant metadata.

Remember, however, that changes made to the model and its mapping to the tables (such as the names of foreign keys or collection tables) do not take effect in the database automatically. Therefore the approach taken when dealing with Hibernate mapping, and connections to databases in general, alters according to your requirements.

## Available Metadata

Name    | Description  
------- | -----------
[Composite ID](composite_id.md) | This metadata allows you to create a composite key.
[Embedded and Embeddable](embedded.md) | This metadata allows you to create embedded entities.
[Entity Level Hibernate Mapping](entity_level.md) |  This metadata is used to map at an entity level.
[Property Level Hibernate Mapping](property_level.md) | This metadata is used to map at a property level.
