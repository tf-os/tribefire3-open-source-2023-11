---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Conjunction Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The conjunction selector allows you to determine two or more cases that must all be true for a metadata to be resolved."
sidebar: essentials
layout: page
permalink: conjunction_selector.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
This selector has only one property which can be configured: `operands`.

Using this property, you build a list of metadata selectors that all must be true before the action of the metadata function. This is the equivalent of an **AND** operator.

{%include note.html content="You can add as many selectors to the `operand` property as you like."%}

## Example
First, define the selector with an instance of the `ConjunctionSelector`.

Double click the newly created instance to open it up for editing. Here you can add as many `MetadataSelectors` as you wish. In this example, two selectors have been chosen: a `UseCaseSelector` and a `RoleSelector`. This means that both these conditions must be met before the metadata is resolved.

{%include image.html file="metadata/ConjunctionSelector07.png"%}
