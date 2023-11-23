---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Type Conditions
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The type condition criteria belong to the TypeConditionModel, and are used to match against a variety of types."
sidebar: essentials
layout: page
permalink: type_conditions.html
# hide_sidebar: true
# toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---


## General
There are two <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a> selectors, [Entity Type Selector](entity_type_selector.html) and [Property Type Selector](property_type_selector.html), that depend on the matching of a type against a type condition to decide whether a metadata should be resolved. These metadata selectors are assigned to metadata, while a type condition is added to the metadata selector itself.

The type associated with the metadata is matched against the type condition, and the corresponding metadata is resolved, according to whether the type condition has been matched. Type conditions allow you to match against entities, properties, collections, and elements within a collection. There are also other type conditions that are not used to match types; by contrast, they can be considered helper conditions that are used to change the scope of type condition matching. For example, you can use the conjunction and disjunction conditions to define more than one condition that should be matched by the metadata selector.

## IsAnyType
This condition is used as a catch-all criterion, meaning that it matches against all types. It can be used at the entity or property level and requires no configuration – you need only assign it to the metadata selector for it to work. Once assigned, the metadata selector always resolves the metadata associated with it.

## IsAssignableTo
This condition is valid when the type this condition is assigned to is or is a subtype of the type provided in the `typeSignature` property. This condition has only the `typeSignature` property where you specify the type signature of the type to be matched against.

## IsTypeKind
This condition is valid when the type this condition is assigned to is of the same kind as the type specified in the `kind` property. This condition has only the `kind` property where you select one of the available properties from a drop-down list. The following options are available:
* enumType
* entityType
* simpleType
* scalarType
* collectionType
* linearCollectionType
* mapType
* listType
* setType
* stringType
* numberType
* booleanType
* integerType
* longType
* floatType
* doubleType
* decimalType
* dateType

## IsType
This condition is valid when when the type this condition is assigned exactly matches the type condition provided in the `typeSignature` property. This condition has only the `typeSignature` property where you select one of the available type conditions.

## CollectionElementCondition

This condition is valid when the type this condition is assigned to is a linear collection (list/set) and the collection element matches the type condition provided in the `condition` property. This condition has only the `condition` property where you select one of the available type conditions.

## TypeConditionConjunction and TypeConditionDisjunction

The Type Condition Conjunction and the Type Condition Disjunction conditions are not used to match against a specific type; instead, they are used to extend the amount of conditions that the metadata selectors can resolve against.

You can assign any number of other type conditions to the conjunction or disjunction. Both conditions function in the same manner, only their logical behavior is different:
* conjunctions function as an AND operator, meaning that all conditions must be matched for the metadata to be resolved
* disjunctions function as an OR operator, meaning that only one conditions must be matched for the metadata to be resolved

Both the Type Condition Conjunction and Type Condition Disjunction are derived from the `TypeConditionJunction` entity, which contains the property `operands`. You add your type conditions to this property.

## TypeConditionNegation
The Type Condition Negation condition is not used to match against a specific type; instead, this condition is used to reverse the functionality of type conditions.

Normally, only when a type condition is matched is the metadata resolved by the selectors. Using the negation condition, only metadata that are not matched are resolved. This means that you can use this to target metadata that should not be matched.

The condition contains only one property that requires configuration - `operand`, where you can assign a type condition. If this condition is then matched, the metadata is not resolved.

It is only possible to add one type condition to the Type Condition Negation condition; if you wish to add multiple negations criteria, you should use the disjunction or conjunction conditions.

## MapKeyCondition

This condition is used for maps only. When the key this condition is assigned to matches the type condition provided in the `condition` property, the metadata is resolved. This condition has only the `condition` property where you select one of the available type conditions.

## MapValueCondition

This condition is used for maps only. When the value this condition is assigned to matches the type condition provided in the `condition` property, the metadata is resolved. This condition has only the `condition` property where you select one of the available type conditions.
