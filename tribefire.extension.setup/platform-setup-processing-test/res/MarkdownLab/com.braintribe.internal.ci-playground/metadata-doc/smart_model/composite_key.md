---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Composite Key Property Assignment
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 22.01.2018
summary: "This metadata is used to map a complex property using multiple Key Property Assignment instances."
sidebar: essentials
layout: page
permalink: composite_key.html
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
`CompositeKeyPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment`

## General
This metadata functions as a conjunction, since all references must be matched before the data is provided. This means the join is created using multiple Key Property Assignment references on properties belonging to the integration entity and the complex property type.

{%include tip.html content="For more information about Key Property Assignment, see [Key Property Assignment](key_property.html)."%}

Property | Description | Type
------| --------- | -------
Key Property Assignments | A set containing all Key Property Assignments a property should be joined on | `Set<KeyPropertyAssignment>`
