---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Fulltext Metadata
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 18.01.2018
summary: "The package Full-Text gathers together all metadata that are concerned with full-text searching."
sidebar: essentials
layout: page
permalink: fulltext_metadata.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
This package has one type, FulltextMetaData, and is used to collect all full-text related metadata. This means that any metadata that is related to full-text searching generally has two super types:
* `FulltextMetadata`
* its corresponding metadata type (either `EntityTypeMetadata` or `PropertyMetadata`).

The metadata in this package only affects entities and properties.

## Available Metadata

<div class="datatable-begin"></div>

Name    | Description  
------- | -----------
FulltextEntity | Indicates whether this entity is part of the full-text store.
AnalyzedProperty | Indicates whether this property is analyzed or not.
FulltextProperty | Indicates whether this property is part of the full-text store.
StorageHint | Defines how this property is persisted in the full-text store. Uses the `StorageOption` enum as possible options.
StorageOption | Specifies available options of persisting a property in a full-text store. Available are the following options: `reference`, `embedded`, `encoded`

<div class="datatable-end"></div>
