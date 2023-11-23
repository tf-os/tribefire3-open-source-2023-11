---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Resource Streamer Metadata
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata, resource]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "You can use these metadata to influence the streaming of resources."
sidebar: essentials
layout: page
permalink: resource_streamer_metadata.html
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
`StreamWith` | `com.braintribe.model.extensiondeployment.meta.StreamWith`
`UploadWith` | `com.braintribe.model.extensiondeployment.meta.UploadWith`
`BinaryProcessWith` | `com.braintribe.model.extensiondeployment.meta.BinaryProcessWith`

{%include tip.html content="For information about implementing a resource streamer, see [Implementing a Resource Streamer](resource_streamer.html)"%}

## StreamWith
The `com.braintribe.model.extensiondeployment.meta.StreamWith` metadata binds a `ResourceSource` <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity type</a> with a `BinaryRetrieval` <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.denotation_type}}">denotation instance</a> which references an `BinaryRetrieval` processor capable of retrieving the binary data based on <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_instance}}">instances</a> of `ResourceSource`.

## UploadWith
The `com.braintribe.model.extensiondeployment.meta.UploadWith ` metadata binds a `ResourceSource` entity type with a `BinaryPersistence` denotation instance which references an `BinaryPersistence` processor capable of persisting the binary data associated with instances of `ResourceSource`.

Besides the mandatory `BinaryPersistence` reference, the `UploadWith` metadata can hold a list of pre- and post- `ResourceEnricher` denotation instances, which are used to select `ResourceEnricher` processors to be invoked before and after the `BinaryPersistence` processor in order to enrich the `Resource` instance being created.

## BinaryProcessWith
The `com.braintribe.model.extensiondeployment.meta.BinaryProcessWith` metadata is a convenience type which simply extends from both `StreamWith` and `UploadWith`.
