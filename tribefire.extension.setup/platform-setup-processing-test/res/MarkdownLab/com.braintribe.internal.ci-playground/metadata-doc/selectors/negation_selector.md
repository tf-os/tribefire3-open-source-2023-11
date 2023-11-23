---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Negation Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The negation selector allows you to negate the action of metadata."
sidebar: essentials
layout: page
permalink: negation_selector.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
The `operand` property accepts a further metadata selector, either a discriminator, a [use case selector](use_case_selector.html), or [role selector](role_selector.html). If the selected sector is matched, the metadata is not resolved.

## Example
This selector has only one definable property: `Operand`.

{%include image.html file="metadata/NegationSelector02.png"%}

Double click the `Operand` property to chose a metadata selector. You can choose one of the following options:
* discriminator
* use case selector
* role selector

{%include note.html content="You can also choose a further `NegationSelector` instance. However, this negates a negation, meaning it has no effect at all."%}
