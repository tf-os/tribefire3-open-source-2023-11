---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Qualified Entity Assignment
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 22.01.2018
summary: "This metadata is used to determine smart mapping at the entity level. "
sidebar: essentials
layout: page
permalink: qualified_entity.html
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
`QualifiedEntityAssignment` | `com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment`

## General
This metadata contains two mapping-specific properties:

* access
* entity type

Property | Description | Type
------| --------- | -------
Access | The access through which the integration entity can be accessed | `IncrementalAccess`
Entity | The integration entity which is mapped to the smart entity this metadata is assigned to | `EntityType`
