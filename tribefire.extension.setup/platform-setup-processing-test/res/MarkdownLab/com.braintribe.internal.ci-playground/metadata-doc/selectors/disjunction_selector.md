---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Disjunction Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The disjunction selector allows you to define two or more metadata selectors, one of which must be matched for the metadata to be resolved."
sidebar: essentials
layout: page
permalink: disjunction_selector.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
The `DisjunctionSelector` functions as an **OR** operator, unlike the `ConjunctionSelector`, where all selector conditions must be met.

This means that if any one of the conditions are met, the metadata functions according to the behavior defined by that selector.

{%include note.html content="You can add as many selectors to the `operand` property as you like."%}

## Example
In this example, we define the operand property with an instance of the `RoleSelector`.

If any of the conditions are met, that is, if one of the selectors are true, that metadata is resolved; if more than one metadata matches the criteria, [Conflict Priority](general_metadata_properties.html) determines which should be resolved. In this example, we define two `RoleSelectors` for
* `john.smith.role`
* `robert.taylor.role`

This means, that if the user has either the role `john.smith.role.a` OR `robert.taylor.role.a` then the metadata is resolved.

The user John Smith has the role `john.smith.role.a` defined, so the metadata is resolved and displayed.

{%include image.html file="metadata/Disjunction06.png"%}

The user Robert Taylor has the role `robert.taylor.role.a` defined, so the metadata is resolved and displayed.

{%include image.html file="metadata/Disjunction07.png"%}

However, the user Cortex has neither of the roles assigned, and therefore, the metadata is not resolved.

{%include image.html file="metadata/Disjunction08.png"%}
