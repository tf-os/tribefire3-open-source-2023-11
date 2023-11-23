---
##################
published: false
##################
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Constant Property Assignment
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 04.05.2018
summary: "This metadata is used to / does ..."
sidebar: essentials
layout: page
permalink: constant_property.html
# hide_sidebar: true
# toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
# example image: {% include image.html file="wire_diagram_denot_and_experts.png" max-width=600 %}
# example tip: {% include tip.html content="For more information, see [Metadata](general_metadata_properties.html)" %}
# example note: see below
---

Metadata Property Name  | Type Signature  
------- | -----------
`ConstantPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.ConstantPropertyAssignment`

## General

The metadata contains the following properties:
* `prop1`
* `prop2`

Property | Description | Type
------| --------- | -------
`prop1` | Description | `GmProperty`
`prop2` | Description | `QualifiedProperty`

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

