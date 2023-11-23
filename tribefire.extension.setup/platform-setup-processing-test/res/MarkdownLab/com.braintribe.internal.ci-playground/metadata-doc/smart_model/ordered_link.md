---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Ordered Link Property Assignment
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 22.01.2018
summary: "This metadata is used to map a complex property, either a single or multiple aggregation, using an additional linking entity."
sidebar: essentials
layout: page
permalink: ordered_link.html
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
`OrderedLinkPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.OrderedLinkPropertyAssignment`

## General
This metadata functions in exactly the same manner as Link Property Assignment, except that you can map lists using this metadata.

{%include tip.html content="For more information about Link Property Assignment, see [Link Property Assignment](link_property.html)."%}

Inheriting the same five properties from Link Property Assignment, this metadata adds an additional property, called Link Index.

Property | Description | Type
------| --------- | -------
Link Index | Controls the ordering of properties assigned to list. The values for this property must be of the type integer, begin at 0 and be sequential | `Int`
Link Access | The access through which the linking entity can be accessed.  | `IncrementalAccess`
Other Key | The property belonging to the complex property type that is joined to a corresponding property in the linking entity, as defined by Link Other Key | `GmProperty`
Key | The property belonging to the integration entity that is joined to a corresponding property in the linking entity, as defined by Link Key | `GmProperty`
Link Other Key | The property belonging to the linking entity that is joined to a corresponding property in the complex property type, as defined by Other Key | `GmProperty`
Link Key | The property belonging to the linking entity that is joined to a corresponding property in the integration entity, as defined by Key | `GmProperty`
