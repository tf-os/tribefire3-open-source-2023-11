---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Garbage Collection
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 12.02.2018
summary: "You can use this metadata to influence the behavior of the garbage collector."
sidebar: essentials
layout: page
permalink: garbage_collection.html
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
`GarbageCollection` | `com.braintribe.model.meta.data.cleanupGarbageCollection.java`

## General
Using this metadata, you can configure how the garbage collector behaves for the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_instance}}">instances</a> of <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a> this metadata is assigned to. You can attach the `GarbageCollection` metadata on the entity level.

{% include tip.html content="For information about using the garbage collector, see [Using the Garbage Collector](using_garbage_collector.html)"%}

There is little configuration for this metadata - the most important is the `kind` property:

Value |  Description
------| -------
`anchor` | Considered root entities, it marks the point where garbage collector starts its reachability walks. Anchor entities will never be collected by the garbage collection.
`hold` | Defines entities that should never be collected by the garbage collector.
`collect` | Removes all marked entities, unless they are reachable (directly or indirectly) from one of the root entities.
