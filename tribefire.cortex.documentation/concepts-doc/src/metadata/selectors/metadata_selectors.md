# Metadata Selectors

Selectors allow you to define conditions on the functionality of metadata. This means that after configuring a selector, the metadata is only resolved (used) when that condition is met.

## General

Defining a selector means that a metadata is only checked and its functionality implemented if the condition assigned in the selector is met.

The base class for all selectors is the `MetaDataSelector`. The selector property found in all metadata is also of the type `MetadataSelector`. You configure your metadata by adding new or existing instances of `MetadataSelector` to the `selector` property.

## Access Selectors

A model containing entity types with metadata is normally associated with an access. Access selectors are used to determine whether that metadata should be resolved based on the access.

* [Access Selector](access_selector.md)
* [Access Type Selector](access_type_selector.md)
* [Access Type Signature Selector](access_type_signature_selector.md)

## Entity Selectors

You can assign metadata to entities or to properties of those entities. Entity selectors are used to determine whether metadata should be resolved depending on the entity the metadata is assigned to.

* [Entity Signature Regex Selector](entity_signature_regex_selector.md)
* [Gm Entity Type Selector](gm_type_selector.md)
* [Entity Type Selector](entity_type_selector.md)

## Simple Property Discriminators

The simple property discriminators allow you to resolve metadata depending on the value of a property. There are several different types of property discriminators, each one related to its associated simple type. See [Simple Property Discriminators](simple_property_discriminators.md).

## Property Selectors

The property selectors are to determine whether to activate metadata based on the information relative to the property itself, rather than the value of the property. For example, the metadata might be resolved depending on the name of a property.

* [Property Type Selector](property_type_selector.md)
* [Property Name Selector](property_name_selector.md)
* [Property RegEx Selector](property_regex_selector.md)

## Use Case Selector

The use case selector is used to determine which component (the area of the graphical user interface) the condition is assigned to. See [Use Case Selector](use_case_selector.md).

## Role Selector

The role selector is used to define metadata behavior depending on roles defined in the **Authentication and Authorization** access. Each user can be assigned roles or assigned to a group, which consists of different roles. Using the role selector means that the metadata is only resolved if the current session user has a particular role. See [Role Selector](role_selector.md).

## Logical Selectors

The logical selectors do not place constraints on the metadata themselves. Rather, they change the behavior of the other metadata selectors.

* [Negation Selector](negation_selector.md)
* [Disjunction Selector](disjunction_selector.md)
* [Conjunction Selector](conjunction_selector.md)
