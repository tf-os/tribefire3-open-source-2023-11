---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Visible and Hidden
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "These metadata allow you to configure the visibility of the entity type they are assigned to."
sidebar: essentials
layout: page
permalink: visible.html
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
`Visible` | `com.braintribe.model.meta.data.prompt.Visible`
`Hidden` | `com.braintribe.model.meta.data.prompt.Hidden`

## General
{%include note.html content="This use of this metadata property does not preclude the option of configuring queries in Workbench for this entity type. Only the entity type is hidden in Explorer, not any queries based upon it."%}

If you assign the Visible metadata to an element, the element is visible in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.explorer}}">Explorer</a>. The Hidden metatada, does the opposite. If you assign the Hidden metadata to an element, the element is not displayed in tribefire Explorer.

You can attach this metadata to:
* <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">models</a>
* properties
* <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
* enum types
* enum constants

## Example
If the Visible metadata is added, the element is visible in Explorer. In this example an entity type called `Address` has been configured with this metadata property. As shown in the screenshot below, `Address` is visible.
{%include image.html file="metadata/EntityVisibilityCheckedExample.png"%}
If the Hidden metadata is added, the element is not visible in Explorer. In this example an entity type called `Address` has been configured with this metadata property. As shown in the screenshot below, `Address` is not visible.
{%include image.html file="metadata/EntityVisibilityUncheckedExample.png"%}
