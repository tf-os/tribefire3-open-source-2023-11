---
##################
# published: false
##################
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Composite ID
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# provide the keywords to be picked by the search engine and SEO, separated by a comma
keywords: 
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 21.02.2018
summary: "This metadata allows you to create a composite key."
sidebar: essentials
layout: page
permalink: composite_id.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
# example image: {% include image.html file="wire_diagram_denot_and_experts.png" max-width=600 %}
# example tip: {% include tip.html content="For more information, see [Metadata](general_metadata_properties.html)" %}
# example note: see below
---

Metadata Property Name  | Type Signature  
------- | -----------
`JpaCompositeId` | `com.braintribe.model.accessdeployment.jpa.meta.JpaCompositeId`

{%include apidoc_url.html className="JpaCompositeId" link="interfacecom_1_1braintribe_1_1model_1_1accessdeployment_1_1jpa_1_1meta_1_1_jpa_composite_id.html"%}

## General
You can only attach this metadata to an ID property. Only a composite ID consisting of up to 30 columns is supported.

## Configuration
To configure, simply add the `JpaCompositeId` metadata to the ID property of your entity. Read, update, and delete operations in the database are fully supported.

Creating new instances in a database is supported only if the ID is given explicitly, so when a properly formatted String value is set to the `id` property by the user. The proper format is a comma-separated concatenation of the individual values. The format for different types is a `GM string`. 
{%include tip.html content="For more information, see the `instanceToGmString()` method of the `ScalarType` class. "%}
{%include apidoc_url.html className="ScalarType" link="interfacecom_1_1braintribe_1_1model_1_1generic_1_1reflection_1_1_scalar_type.html"%}