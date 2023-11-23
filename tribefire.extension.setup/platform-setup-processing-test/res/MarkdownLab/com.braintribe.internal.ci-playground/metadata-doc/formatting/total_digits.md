---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Total Digits Formatting
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "This metadata is used to control the exact number of digits of a numeric property (Integer, Double, Long, and so on)."
sidebar: essentials
layout: page
permalink: total_digits.html
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
`TotalDigitsFormatting` | `com.braintribe.model.meta.data.display.formatting.TotalDigitsFormatting`

## General
This metadata provides no actual functionality in Control Center/Explorer, but rather is used to define XML schema settings when exporting a model to XML.
## Example
The metadata has one specific property used to control this restriction, called `digits`. The value must be greater than `0`.
