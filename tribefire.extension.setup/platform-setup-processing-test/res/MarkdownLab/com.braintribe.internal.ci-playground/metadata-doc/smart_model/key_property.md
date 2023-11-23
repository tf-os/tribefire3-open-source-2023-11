---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Key Property Assignment
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 22.01.2018
summary: "This metadata is used to create a simple join between complex properties, either single or multiple aggregations."
sidebar: essentials
layout: page
permalink: key_property.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

Metadata Property Name  | Type Signature  
------- | -----------
`KeyPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment`

## General
The simple join functions by creating a reference between a property belonging to a mapped integration entity and a property in the entity type of the complex property. When these properties are matched, the data is provided. Key Property Assignment contains two mapping-specific properties:

* `keyProperty`
* `property`

Property | Description | Type
------| --------- | -------
`keyProperty` | The key property, against which the Property will be compared. In a simple join this will be the entity type of the complex property. | `GmProperty`
`property` | This is the property that will be compared against the key property. In a simple join this will be a property belonging to the integration entity being mapped | `QualifiedProperty`
