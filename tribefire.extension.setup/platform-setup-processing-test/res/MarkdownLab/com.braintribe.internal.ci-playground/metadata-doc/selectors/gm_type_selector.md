---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Gm Entity Type Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The Gm entity type selector allows you to activate metadata based on the entity type."
sidebar: essentials
layout: page
permalink: gm_type_selector.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
When configured, the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity type</a> defined in the selector is compared to one of the following:
* the entity the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a> is assigned to
* the entity containing the property the metadata is assigned to

The Gm entity type selector contains only one property - `Gm Entity Type`. This property is where you provide the type signature of the entity to be compared against.

The metadata is resolved only when the entity assigned to this property matches the type signature of the Gm Entity Type property.


## Example
Any metadata's entity or entity belonging to the metadata's property is matched against the entity in Gm Entity Type property before the metadata is resolved.

In this example, a [Display Info](displayinfo.html) metadata was added to the property invoices, and a GM entity type selector was assigned to the metadata with the `Customer` entity type.

{%include image.html file="metadata/GmEntityTypeSelector02.png"%}

We can see that the property `invoices` belongs to the entity `Customer`, meaning that the metadata is resolved.

{%include image.html file="metadata/GmEntityTypeSelector03.png"%}
