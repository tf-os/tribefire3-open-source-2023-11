---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Smart Model Metadata
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "The smart package gathers together all metadata involved with mapping integration entities and properties to their Smart Model equivalents."
sidebar: essentials
layout: page
permalink: smart_model_metadata.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
Although we recommend that you use the Smart Mapping feature of the tribefire Modeler when mapping <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.smart_model}}">smart models</a>, you can find an overview of the most important metadata that handle the process in the background here.

## Available Metadata

<div class="datatable-begin"></div>

Name    | Description  
------- | -----------
[Composite Inverse Key Property Assignment](composite_inverse.html) |  This metadata is used map a complex property using multiple Inverse Key Property Assignment instances. This means the join's direction is inverted and created using multiple references on properties belonging to the integration entity and the complex property type.
[Composite Key Property Assignment](composite_key.html) | This metadata is used map a complex property using multiple Inverse Key Property Assignment instances. This means the join's direction is inverted and created using multiple references on properties belonging to the integration entity and the complex property type.
[Inverse Key Property Assignment](inverse_key.html) | This metadata is used to create an inverted join between complex properties, either single or multiple aggregations.
[Key Property Assignment](key_property.html) | This metadata is used to create a simple join between complex properties, either single or multiple aggregations.
[Link Property Assignment](link_property.html) | This metadata is used to map a complex property, either a single or multiple aggregation, using an additional linking entity.
[Ordered Link Property Assignment](ordered_link.html) | This metadata is used to map a complex property, either a single or multiple aggregation, using an additional linking entity. You can use ordered multiple aggregations, that is, lists, for this metadata.
[Qualified Entity Assignment](qualified_entity.html) | 	This metadata is used to determine smart mapping at the entity level.
[Qualified Property Assignment](qualified_property.html) | This metadata defines qualified mapping between integration- and smart-level properties.


<div class="datatable-end"></div>
