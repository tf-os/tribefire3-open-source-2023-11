---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Access Type Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The access type selector allows you to activate metadata depending on the type of access the model containing the metadata is associated with."
sidebar: essentials
layout: page
permalink: access_type_selector.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

Selector Name  | Type Signature  
------- | -----------
`AccessTypeSelector` | `com.braintribe.model.meta.selector.AccessTypeSelector`

## General
When configured, only <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a> which belongs to a <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">model</a> associated with the specific <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.access}}">access</a> type is resolved. If the access type is different, the metadata is not resolved.

You can assign the default and custom accesses to this selector. The access type selector contains two properties that are used to configure it:

Property | Description
------| ---------
Access Type | Used to specify the access type the metadata is resolved for.
Assignable | Used to specify whether the metadata is resolved for the subtypes of the Access Type.

{%include note.html content="Leaving the **Assignable** checkbox unchecked means that all subtypes of the Access Type are also valid, so that metadata belonging to a model associated with a subtype is also resolved. <br/> <br/> If you check the checkbox, only the type assigned to the Access Type property is considered valid. "%}

## Example
You must first determine the access type that should be matched, and whether subtypes of this type should also be considered when matching.

In this example, a S[Selective Information](selectiveinformation.html) metadata was assigned to an entity called `Customer`, and an access type selector was assigned to the metadata. The Access Type property was assigned the `SmoodAccess` type while the **Assignable** property is left unchecked.

This means that this metadata is only resolved when its model is associated with a SMOOD access or any subtype of SMOOD.

{%include image.html file="metadata/AccessTypeSelector02.png"%}

The `Customer` entity belongs to `SalesModel`, which in turn is associated with Sales Model Access. This access is of the type SMOOD, meaning that the metadata is resolved.

{%include image.html file="metadata/AccessTypeSelector03.png"%}
