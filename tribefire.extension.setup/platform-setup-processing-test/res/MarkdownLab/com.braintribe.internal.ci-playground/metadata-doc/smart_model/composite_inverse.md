---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Composite Inverse Key Property Assignment
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 22.01.2018
summary: "This metadata is used map a complex property using multiple Inverse Key Property Assignment instances. This means the join's direction is inverted and created using multiple references on properties belonging to the integration entity and the complex property type. "
sidebar: essentials
layout: page
permalink: composite_inverse.html
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
`CompositeInverseKeyPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment`

## General
This metadata functions as a conjunction. All references must be matched before the data is provided. The Composite Inverse Key Property Assignment metadata is similar to Inverse Key Property Assignment, but more properties are being compared. The valid type of a property on which this is configured is an entity or a set of entities.

{%include tip.html content="For more information about Inverse Key Property Assignment, see [Inverse Key Property Assignment](inverse_key.html)."%}

Property | Description | Type
------| --------- | ------
Inverse Key Property Assignments | Set containing all Inverse Key Property Assignments the property should be joined on	 | `Set<InverseKeyPropertyAssignment>`
