---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Hibernate Mapping Metadata
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "Hibernate mapping in the context of tribefire refers to the mapping of entities and their properties to tables in a JDBC database. tribefire can carry out this process automatically, but it is also possible to control the mapping process using metadata."
sidebar: essentials
layout: page
permalink: hibernate_mapping_metadata.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
Hibernate-related metadata offer the opportunity to configure entities and properties using a Hibernate `.xml` file (`\*.hbm.xml`). This can be done either by entering a code snippet in the XML property or by to a linking to specific `.hbm.xml` by entering its URI in the `xmlFileUri` property. This allows you more control in the configuration of your model and its equivalents in a database.
Depending on your approach, there are several ways to map tribefire model elements and database tables. You can either:

* create and design the model yourself, and when finished add the entity and property mapping metadata, or
* use the tribefire connection to generate a database scheme from a database connection, and generate a model from the schema, along with the relevant metadata.

Remember, however, that changes made to the model and its mapping to the tables (such as the names of foreign keys or collection tables) do not take effect in the database automatically. Therefore the approach taken when dealing with Hibernate mapping, and connections to databases in general, alters according to your requirements.

## Available Metadata

<div class="datatable-begin"></div>

Name    | Description  
------- | -----------
[Composite ID](composite_id.html) | This metadata allows you to create a composite key.
[Embedded and Embeddable](embedded.html) | This metadata allows you to create embedded entities.
[Entity Level Hibernate Mapping](entity_level.html) |  This metadata is used to map at an entity level.
[Property Level Hibernate Mapping](property_level.html) | This metadata is used to map at a property level.

<div class="datatable-end"></div>
