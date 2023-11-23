---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Inline
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "This metadata property allows you to configure whether the complex properties of an entity type are displayed inline."
sidebar: essentials
layout: page
permalink: inline.html
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
`Inline` | `com.braintribe.model.meta.data.prompt.Inline`

## General
The metadata property refers specifically to complex types, that is, properties which are themselves entity types. If you assign this metadata on a complex property, this property is displayed in a simplified way.

## Example
hen this metadata is added to a complex type property, this property is displayed inline:
{%include image.html file="metadata/image2017-7-26 10-24-38.png" max-width=600%}
