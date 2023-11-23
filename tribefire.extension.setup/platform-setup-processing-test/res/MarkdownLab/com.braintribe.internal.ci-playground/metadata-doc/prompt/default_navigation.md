---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Default Navigation
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "This metadata allows you to configure which property is displayed by default when opening an entity in Explorer."
sidebar: essentials
layout: page
permalink: default_navigation.html
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
`DefaultNavigation` | `com.braintribe.model.meta.data.prompt.DefaultNavigation`

## General
This metadata defines a default property to be used when navigating an entity. Normally, when using the **Open** action or double clicking an entity, you navigate to an entity, showing all its properties. By defining this metadata, you can specify a particular property which you navigate to.
{%include note.html content="The property you want to navigate to must be a complex property."%}

## Example
You assign this metadata on the entity level. When you create the metadata, in the `DefaultNavigation()` window, you specify the property you want to navigate to by assigning it to the property `field`.
