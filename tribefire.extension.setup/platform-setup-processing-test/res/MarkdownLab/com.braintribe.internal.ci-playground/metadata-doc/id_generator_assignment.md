---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: ID Generator Assignment
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 19.01.2018
summary: "This metadata allows you to assign an UID generator to an entity, so when a new instance is created, a UID is assigned to its index property."
sidebar: essentials
layout: page
permalink: id_generator_assignment.html
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
`IdGeneratorAssignment` | `com.braintribe.model.idgendeployment.IdGeneratorAssignment`

## General
For the UID generation to work, you must first configure the generator property of this metadata. Currently available generators include:
* `numericUidGenerator`
* `uuidGenerator`

Once properly configured, the ID is assigned to a new entity instance when it is first committed.
{%include note.html content="You must configure and deploy the generator before using it."%}

## Example
Add the metadata to the entity you wish to assign a generated ID. Select the Metadata property of the entity and click **Add**.

Select the IdGeneratorAssignment metadata and click **Add** and **Finish**.

Click **Assign** at the **Generator** property and select the generator you wish to use. You are then asked to configure the generator by giving it a name and external ID. The configuration is the same regardless of the generator that you choose.
{%include note.html content="After configuring the generator, you must deploy it."%}
Once deployed, it is ready for use. Every time you create and commit a new instance of the entity which the generator is assigned to, a new UID is generated.
