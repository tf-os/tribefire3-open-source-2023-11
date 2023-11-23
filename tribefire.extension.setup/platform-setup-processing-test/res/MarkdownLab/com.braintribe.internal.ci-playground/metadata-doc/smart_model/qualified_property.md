---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Qualified Property Assignment
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 22.01.2018
summary: "This metadata defines qualified mapping between integration- and smart-level properties."
sidebar: essentials
layout: page
permalink: qualified_property.html
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
`QualifiedPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment`

## General
Using this metadata, the property belonging to an integration entity is mapped exactly to the smart entity. Data described by the integration property is displayed as is in the smart property.

This metadata contains two mapping-specific properties:
* conversion
* property

Property | Description | Type
------| --------- | -------
`conversion` | Property which converts a primitive type (Boolean, Date, decimal, etc.) to a String | `SmartConversion`
`property` | Integration-level property that should be mapped to the smart-level property that this metadata is assigned to	 | `GmProperty`
