---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Access Type Signature Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The access type selector allows you to activate metadata depending on the type of the type signature of the access the model containing the metadata is associated with."
sidebar: essentials
layout: page
permalink: access_type_signature_selector.html
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
`AccessTypeSignatureSelector` | `com.braintribe.model.meta.selector.AccessTypeSignatureSelector`

## General
When configured, only <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a> which belongs to a <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">model</a> associated with a specific <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.access}}">access</a> type signature is resolved. If the access type signature is different, the metadata is not resolved.

You can assign the default and custom accesses to this selector. The access type selector contains two properties that are used to configure it:

Property | Description
------| ---------
Denotation Type Signature | Used to specify the access type signature the metadata is resolved for.
Assignable | Used to specify whether the metadata is resolved for the subtypes of the Access Type.

{%include note.html content="Leaving the **Assignable** checkbox unchecked means that all subtypes of the Access Type are also valid, so that metadata belonging to a model associated with a subtype is also resolved. <br/> <br/> If you check the checkbox, only the type assigned to the Access Type property is considered valid. "%}

## Example
In this example, a [Selective Information](selectiveinformation.html) metadata was added to an entity `Customer`, and the metadata was assigned an access type signature selector. The Denotation Type Signature for the selector was `com.braintribe.model.accessdeployment.IncrementalAccess`.

The **Assignable** property was unchecked. This means that the metadata is resolved only when its corresponding access is of the type Incremental Access or a subtype of it.

{%include image.html file="metadata/AccessTypeSignatureSelector02.png"%}


This entity belongs to a model called `SalesModel`, which was assigned to a <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.smood}}">SMOOD</a> access.

{%include image.html file="metadata/AccessTypeSignatureSelector03.png"%}


Because SMOOD is a derivative of `IncrementalAccess`, the metadata is resolved.
