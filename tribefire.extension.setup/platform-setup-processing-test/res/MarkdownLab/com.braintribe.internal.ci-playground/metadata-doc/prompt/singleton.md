---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Singleton
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "This metadata allows you to configure whether the Query Panel is displayed in Explorer."
sidebar: essentials
layout: page
permalink: singleton.html
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
`Singleton` | `com.braintribe.model.meta.data.prompt.Singleton`

## General
You assign this metadata on the entity level. If the Singleton metadata is assigned to an <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity type</a>, the Query Panel is not displayed in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.explorer}}">Explorer</a> for that particular entity type.

## Example
 The following screenshot shows how tribefire Explorer looks when the Singleton metadata is set for the `Company` entity type.
{%include image.html file="metadata/image2017-7-26 15-59-18.png" max-width=600%}
