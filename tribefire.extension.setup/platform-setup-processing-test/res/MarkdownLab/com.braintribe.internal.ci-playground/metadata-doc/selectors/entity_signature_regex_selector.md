---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Entity Signature Regex Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The entity signature regex selector allows you to activate metadata based on the type signature of the entity the metadata is assigned to."
sidebar: essentials
layout: page
permalink: entity_signature_regex_selector.html
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
`EntitySignatureRegexSelector` | `com.braintribe.model.meta.selector.EntitySignatureRegexSelector`

## General
You can define this selector on both the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_instance}}">entity instance</a> and the property level.

In the case of property-level <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a>, it is the entity the property belongs to that is used when matching against the regex. The whole of the entity's type signature must be matched by the regular expression for the metadata to be resolved. If the type signature is not matched, the metadata is not resolved.

## Example
The selector contains only one property that needs configuration - `regex`. The `regex` property is where you must provide the type signature.

The entity signature regex selector is used to match entity type signatures against a regular expression, and only if the expression is matched the metadata is resolved.

In this example, the metadata [Selective Information](selectiveinformation.html) was added to an entity called `Customer`, and the metadata was assigned a entity signature regex selector with the following regular expression: `[a-z]{3}\.braintribe\.[a-zA-Z]*\.[a-zA-Z]*\.[a-zA-Z]*`

This regular expression searches for:
* a three letter word, followed by a period,
* the word `braintribe` followed by a period,
* another word followed by a period,
* another word followed by a period,
* ...and a word.

{%include image.html file="metadata/EntityTypeRegexSelector01.png"%}

We can see from the type signature of the entity that the package name matches the regular expression and the metadata is resolved.

{%include image.html file="metadata/EntityTypeRegexSelector02.png"%}
