---
##################
# published: false
##################
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Embedded and Embeddable
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# provide the keywords to be picked by the search engine and SEO, separated by a comma
keywords: 
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 21.02.2018
summary: "This metadata allows you to create embedded properties."
sidebar: essentials
layout: page
permalink: embedded.html
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
`JpaEmbeddable` | `com.braintribe.model.accessdeployment.jpa.meta.JpaEmbeddable`
`JpaEmbedded`   | `com.braintribe.model.accessdeployment.jpa.meta.JpaEmbedded`

{%include apidoc_url.html className="JpaEmbeddable" link="interfacecom_1_1braintribe_1_1model_1_1accessdeployment_1_1jpa_1_1meta_1_1_jpa_embeddable.html"%}
{%include apidoc_url.html className="JpaEmbedded" link="interfacecom_1_1braintribe_1_1model_1_1accessdeployment_1_1jpa_1_1meta_1_1_jpa_embedded.html"%}

## General
An embedded property is a property where its type in the model is an entity, but in the database both the owner and its embedded entity are stored in a single table. Only read operations are supported on embedded properties.

## Configuration
To configure, the embedded entity type has to be marked as embeddable with the `JpaEmbeddable` metadata. The owner entity's property must then be annotated with a `JpaEmbedded` metadata, with a map where keys are property names of the embedded entity type and values are any property mappings. 
Currently, only `PropertyMapping` is supported there, with `columnName` and `type` properties being considered mandatory. 
