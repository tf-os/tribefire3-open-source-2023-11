---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Metadata Selectors
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 22.01.2018
summary: "Selectors allow you to define conditions on the functionality of metadata. This means that after configuring a selector, the metadata is only resolved (used) when that condition is met."
sidebar: essentials
layout: page
permalink: metadata_selectors.html
# hide_sidebar: true
# toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
# example image: {% include image.html file="wire_diagram_denot_and_experts.png" max-width=600 %}
# example tip: {% include tip.html content="For more information, see [Metadata](general_metadata_properties.html)" %}
# example note: see below
---

## General
Defining a selector means that a metadata is only checked and its functionality implemented if the condition assigned in the selector is met.

The base class for all selectors is the `MetaDataSelector`. The selector property found in all metadata is also of the type `MetadataSelector`. You configure your metadata by adding new or existing instances of `MetadataSelector` to the `selector` property.

## Access Selectors
A  <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">model</a> containing  <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a> with  <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a> is normally associated with an <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.access}}">access</a>. Access selectors are used to determine whether that metadata should be resolved based on the access.
* [Access Selector](access_selector.html)
* [Access Type Selector](access_type_selector.html)
* [Access Type Signature Selector](access_type_signature_selector.html)

## Entity Selectors
You can assign metadata to entities or to properties of those entities. Entity selectors are used to determine whether metadata should be resolved depending on the entity the metadata is assigned to.
* [Entity Signature Regex Selector](entity_signature_regex_selector.html)
* [Gm Entity Type Selector](gm_type_selector.html)
* [Entity Type Selector](entity_type_selector.html)

## Simple Property Discriminators
The simple property discriminators allow you to resolve metadata depending on the value of a property. There are several different types of property discriminators, each one related to its associated simple type. See [Simple Property Discriminators](simple_property_discriminators.html).

## Property Selectors
The property selectors are to determine whether to activate metadata based on the information relative to the property itself, rather than the value of the property. For example, the metadata might be resolved depending on the name of a property.
* [Property Type Selector](property_type_selector.html)
* [Property Name Selector](property_name_selector.html)
* [Property RegEx Selector](property_regex_selector.html)

## Use Case Selector
The use case selector is used to determine which component (the area of the graphical user interface) the condition is assigned to. See [Use Case Selector](use_case_selector.html).

## Role Selector
The role selector is used to define metadata behavior depending on roles defined in the **Authentication and Authorization** access. Each user can be assigned roles or assigned to a group, which consists of different roles. Using the role selector means that the metadata is only resolved if the current session user has a particular role. See [Role Selector](role_selector.html).

## Logical Selectors
The logical selectors do not place constraints on the metadata themselves. Rather, they change the behavior of the other metadata selectors.
* [Negation Selector](negation_selector.html)
* [Disjunction Selector](disjunction_selector.html)
* [Conjunction Selector](conjunction_selector.html)
