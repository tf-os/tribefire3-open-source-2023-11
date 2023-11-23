---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Formatting Metadata
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "The Formatting package gathers together all metadata that is used to format properties when exported to XML."
sidebar: essentials
layout: page
permalink: formatting_metadata.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
This properties have no actual functionality within <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a> or <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.explorer}}">Explorer</a>, but are used to define specific XML schema restrictions on numeric and String properties.
## Available Metadata

<div class="datatable-begin"></div>

Name    | Description  
------- | -----------
[Fraction Digits Formatting](fraction_digits.html) |  This metadata influences the amount of decimal places a decimal property should have.
[Total Digits Formatting](total_digits.html) | This metadata influences the exact amount of digits a numeric property should have.
[Whitespace Formatting](whitespace.html) | This metadata influences the whitespace formatting policy that should be used.


<div class="datatable-end"></div>
