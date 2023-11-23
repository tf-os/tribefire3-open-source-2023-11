---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Whitespace
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "This metadata is used to control how whitespaces in Strings are handled when exporting models to XML."
sidebar: essentials
layout: page
permalink: whitespace.html
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
`WhitespaceFormatting` | `com.braintribe.model.meta.data.display.formatting.WhitespaceFormatting`

## General
This metadata provides no actual functionality in tribefire, but rather is used when handling model to XML exports.

Model to XML exports are used to constrain the 'value space' of strings, and can have the following values:
* `preserve` – no whitespace formatting is undertaken. The complete value remains unchanged.
* `replace` – replaces all occurrences of tab, line feed, and carriage return with space (#x20)
* `collapse` – replace constraint is used, and then leading and trailing spaces are removed, and multiple spaces are reduced to a single space

{%include note.html content="Control Center itself carries out no functionality regarding whitespace handling."%}

## Example
The metadata has only one enum property that is used to define the way whitespace are handled: `policy`

You can select one of the available options from the policy drop-down list.
