---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Inverse Key Property Assignment
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 22.01.2018
summary: "This metadata is used to create an inverted join between complex properties, either single or multiple aggregations. "
sidebar: essentials
layout: page
permalink: inverse_key.html
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
`InverseKeyPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment`

## General
The functionality of the Key Property Assignment metadata is inverted, so that the `id` of the `key` property is used to find all entities that use this reference at the right-hand side of the join.

{%include tip.html content="For more information about Key Property Assignment, see [Key Property Assignment](key_property.html)."%}

The metadata contains two mapping-specific properties:
* `keyProperty`
* `property`

Property | Description | Type
------| --------- | -------
`keyProperty` | The key property, against which the Property will be compared. In a simple join this will be the entity type of the complex property. | `GmProperty`
`property` | This is the property that will be compared against the key property. In a simple join this will be a property belonging to the integration entity being mapped. | `QualifiedProperty`

## Example
Currently, several upper and lower boundary configurations are supported:

Lower Boundary | Upper Boundary
------| ---------
month | null, year
day | null, year, month
year | null, year
second | null, year
null, milisecond | null, year

{%include note.html content="If no valid combination is found, the default pattern is used - MM/dd/yyyy HH:mm for the English language."%}

To put things in perspective, let's assume that the lower boundary is a `day` and the upper boundary is a `month`. This means that the available values are all the days in a month. For example, setting the Date Clipping metadata for the `dateOfBirth` property of the `Person` entity, results in the following being displayed:

{%include image.html file="metadata/date_clipping.png"%}
