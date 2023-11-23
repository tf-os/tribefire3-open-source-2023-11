---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Show as List
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "This metadata property allows you to configure how maps should be handled, either as a traditional map or as a list."
sidebar: essentials
layout: page
permalink: show_as_list.html
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
`ShowAsList` | `com.braintribe.model.meta.data.prompt.ShowAsList`

## General
When the Show As List metadata is assigned to a map property, then the property is displayed as a list, rather than as a map. It is important that your map adhere to the following structure:
```
map<Integer, Some other object>
```
The key of the map must be of the type `integer`; the value can be any valid object accepted by tribefire, either a simple type (String, Date, and so on) or a complex type. It is even possible for the value to be a collection itself.

Secondly, because the metadata uses the keys as an index when constructing the list, the keys in the map must follow the order `0,1,2,3...` Otherwise, the metadata is not able to function correctly.
## Example
If you assign the Show As List metadata to a map property, the property is displayed like a list.

{%include image.html file="metadata/MapAsListTrue02.png"%}
