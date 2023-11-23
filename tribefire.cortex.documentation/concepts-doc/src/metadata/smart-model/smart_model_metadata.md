# Smart Model Metadata

The smart package gathers together all metadata involved with mapping integration entities and properties to their Smart Model equivalents.

## General

Although we recommend that you use the Smart Mapping feature of the tribefire Modeler when mapping smart models, you can find an overview of the most important metadata that handle the process in the background here.

## Available Metadata

Name    | Description  
------- | -----------
[Composite Inverse Key Property Assignment](composite_inverse.md) |  This metadata is used map a complex property using multiple Inverse Key Property Assignment instances. This means the join's direction is inverted and created using multiple references on properties belonging to the integration entity and the complex property type.
[Composite Key Property Assignment](composite_key.md) | This metadata is used map a complex property using multiple Inverse Key Property Assignment instances. This means the join's direction is inverted and created using multiple references on properties belonging to the integration entity and the complex property type.
[Inverse Key Property Assignment](inverse_key.md) | This metadata is used to create an inverted join between complex properties, either single or multiple aggregations.
[Key Property Assignment](key_property.md) | This metadata is used to create a simple join between complex properties, either single or multiple aggregations.
[Link Property Assignment](link_property.md) | This metadata is used to map a complex property, either a single or multiple aggregation, using an additional linking entity.
[Ordered Link Property Assignment](ordered_link.md) | This metadata is used to map a complex property, either a single or multiple aggregation, using an additional linking entity. You can use ordered multiple aggregations, that is, lists, for this metadata.
[Qualified Entity Assignment](qualified_entity.md) | This metadata is used to determine smart mapping at the entity level.
[Qualified Property Assignment](qualified_property.md) | This metadata defines qualified mapping between integration- and smart-level properties.
