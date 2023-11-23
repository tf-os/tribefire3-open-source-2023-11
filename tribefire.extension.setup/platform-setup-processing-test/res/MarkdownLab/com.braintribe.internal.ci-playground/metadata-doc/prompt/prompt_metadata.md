---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Prompt Metadata
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 18.01.2018
summary: "The package Prompt controls how UI elements are displayed in Control Center or Explorer."
sidebar: essentials
layout: page
permalink: prompt_metadata.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
In contrast to the [Display](display_metadata.html) package, which concentrates on how the models components are displayed, this package concentrates on the way each instance of a component is presented.

One example of a metadata belonging to Prompt is Confidential, which handles a property like a password, meaning that its values are hidden and replaced by stars.

There are two base entities which are used in Prompting: `EntityPrompting` and `PropertyPrompting`. Each extends (are subtypes of) their respective component metadata, `EntityMetadata` and `PropertyMetadata` respectively, and are the super type for all Prompt metadata in this package.
{%include note.html content="The metadata in this package only affect entities and properties."%}

## Available Metadata

<div class="datatable-begin"></div>

Name    | Description  
------- | -----------
[Condensed](condensed.html) |  This metadata allows you to configure how a property within an entity behaves when collapsed.
[Confidential and NonConfidential](confidential.html) | These metadata allow you to configure whether the property value is hidden.
[Default Navigation](default_navigation.html) | This metadata allows you to configure which property is displayed by default when opening an entity in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.explorer}}">Explorer</a>.
[Description](description.html) | This metadata allows you to configure the entity's description.
[Entity Compound Viewing](entity_compound.html) | Using this metadata, you can display properties belonging to a complex type.
[Name](name.html) | This metadata allows you to configure how the entity is labeled.
[Inline](inline.html) | This metadata property allows you to configure whether the complex properties of an entity type are displayed inline.
[Priority](priority.html) | This metadata property allows you to configure the priority of the element it has been assigned to.
[Show as List](show_as_list.html) | This metadata property allows you to configure how maps should be handled, either as a traditional map or as a list.
[Singleton](singleton.html) | This metadata allows you to configure whether the Query Panel is displayed in tribefire Explorer.
[Visible and Hidden](visible.html) | These metadata allow you to configure the visibility of the entity type they are assigned to.


<div class="datatable-end"></div>
