---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Confidential and NonConfidential
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "These metadata allow you to configure whether the property value is hidden. "
sidebar: essentials
layout: page
permalink: confidential.html
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
`Confidential` | `com.braintribe.model.meta.data.prompt.Confidential`
`NonConfidential` | `com.braintribe.model.meta.data.prompt.NonConfidential`

## General
If you assign the Confidential metadata to an element, then the element behaves like a password field and hides property's value. If the NonConfidential (or no) property is assigned, the property behaves like a normal property and its value is visible.

You can attach this metadata to:

* <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">models</a>
* properties
* <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
* enum types
* enum constants

## Example
In this example, the Confidential metadata was assigned to the `AttendeePassword` property:

{%include image.html file="metadata/PasswordPropertyExample.png"%}
