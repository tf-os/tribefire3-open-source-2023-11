---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Property Type Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The Property Type Selector allows you to resolve metadata depending on the type of property (String, Date, Entity, etc)."
sidebar: essentials
layout: page
permalink: property_type_selector.html
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
`AccessSelector` | `com.braintribe.model.meta.selector.AccessSelector`

## General
This selector can be used at the property level, as well as at the entity level. Once set, <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a> is only activated on a property only when the type defined in the selector matches the property type.

This selector has one property that must be configured - `typeCondition`. This allows you to define which type should activate the metadata, based on an instance of Type Condition, of which there are several. For more information, see the Type Conditions document.

For example, when you configure the metadata [Visible and Hidden](visible.html), at the entity-level property metadata, and set the selector to use an instance of a Simple Type Condition, which has the Simple Type Name string, only properties which are of the type String have their metadata resolved. That is, you can choose to make all strings visible or invisible. Other properties belonging to this entity are not affected.


## Example
You can use this selector on any property metadata, but it makes most sense to use it on metadata defined at the entity level, using the Property Metadata property. This property assigns metadata configured here to all properties belonging to the entity.

In this example, there is an entity `Person`. The metadata Visible or Hidden is added to Property Metadata, which then makes all properties either visible or invisible depending on how the metadata data is configured.

{%include image.html file="metadata/PropertyTypeSelector01.png"%}

A Property Type Selector is instantiated with the type condition Simple Type Condition, which, in turn, is defined as String. This means that only properties of the type String have their metadata resolved, meaning the other properties are displayed regardless of the setting configured. and that you can now decided whether the string type properties should be shown or not.

This then makes all string properties belonging to this entity invisible.
