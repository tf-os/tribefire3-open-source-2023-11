---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Queryable and NonQueryable
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 22.01.2018
summary: "This metadata property allows you to configure whether or not an entity type can be directly queried in Explorer. "
sidebar: essentials
layout: page
permalink: queryable.html
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
`Queryable` | `com.braintribe.model.meta.data.query.Queryable`
`NonQueryable` | `com.braintribe.model.meta.data.query.NonQueryable`

## General
If the Queryable (or no) metadata is configured, then a property is displayed when you directly query for it in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.explorer}}">Explorer</a>. If the NonQueryable metadata is assigned, then a property is not displayed when you directly query for it in Explorer. It is still displayed if it is a part of another entity though.

## Example
If you assign the NonQueryable metadata to an entity, you cannot query for this entity using the Quick Access search box in Explorer.

In the example below, the entity `Person` has been configured so that it cannot be queried. You can search for the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity type</a>, however, when clicking on the entity `Person`, no query is executed.

{%include image.html file="metadata/QueryingAllowedExample.png"%}
