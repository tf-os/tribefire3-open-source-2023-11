---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Access Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The access selector allows you to activate metadata based on the external ID of the access associated with the model the metadata is assigned to."
sidebar: essentials
layout: page
permalink: access_selector.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

Selector Name  | Type Signature  
------- | -----------
`AccessSelector` | `com.braintribe.model.meta.selector.AccessSelector`

## General
When configured, this selector only resolves <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a> on any <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_instance}}">entity instance</a> or property belonging to a <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">model</a> whose <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.access}}">access'</a> external ID matches the one defined in the selector. If the external IDs do not match the metadata is not resolved.

## Example
The access selector contains only one property that requires configuration - `externalid`.

The `externalId` property is where you must provide the value of an access' external ID. Only metadata belonging to models associated with that access are resolved.

In this example, we have the [Selective Information](selectiveinformation.html) metadata belonging to an entity called `Customer`. We assigned it an access selector, whose external ID is `salesModelAccess`.

{%include image.html file="metadata/AccessSelector02.png"%}

The entity, `Customer`, belongs to a model called `SalesModel`. We can see that this metadata is resolved since the external ID of the access associated with this model is `salesModelAccess`, the same as defined in the `externalId` property of the access selector.

{%include image.html file="metadata/AccessSelector03.png"%}
