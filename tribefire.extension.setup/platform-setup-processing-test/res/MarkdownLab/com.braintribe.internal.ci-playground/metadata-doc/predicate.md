---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Predicate Metadata
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "While regular metadata can represent any information (for example color, font-size, description), predicate metadata describes, using a Boolean value, if a given element has a particular property."
sidebar: essentials
layout: page
permalink: predicate.html
# hide_sidebar: true
# toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

Typical examples of predicate metadata are: Visible, which defines if an element should be displayed or Mandatory, which defines if a given property must have a value.

tribefire's JAVA API has a special support for working with predicate metadata. In the example below, we use the Visible metadata in a non-predicate and predicate scenarios to illustrate the support the JAVA API's metadata resolver has for predicate metadata.

Currently, we support the following predicate metadata:

* [Deletable and NonDeletable](deletable.html)
* [Visible and Hidden](visible.html)
* [Instantiable and NonInstantiable](instantiable.html)
* [Queryable and NonQueryable](queryable.html)
* [Confidential and NonConfidential](confidential.html)
* [Mandatory and Optional](mandatory.html)
* [MinLength and MaxLength](minlenght.html)
* [Modifiable and Unmodifiable](modifiable.html)
* [Referenceable and NonReferenceable](referenceable.html)
* [Unique and NonUnique](unique.html)


## Declaration
In the code, you can declare the above metadata in different ways:
* Visible - Not Declared as Predicate
    Definition:
    ```java
    interface Visible extends UniversalMetaData {
        boolean getIsVisible();
        void setIsVisible(boolean isVisible);
    }
    ```
    Resolution:
    ```java
    boolean isVisible(EntityType<?> entityType) {
        Visible visible = mdResolver.getMetaData()
                    .entity(MyEntity.T).exclusive();
        return visible == null || visible.getIsVisible();
    }
    ```
* Visible - Declared as Predicate
    Definition:
    ```java
    interface Visible extends UniversalMetaData, Predicate { };
    ```
    Resolution:
    ```java
    boolean isVisible(EntityType<?> entityType) {
        Return mdResolver.getMetaData()
                .entity(MyEntity.T).is(Visible.T);
    }
    ```

The result of both examples is the same even though the Visible metadata is not configured. In first example, the resolution returns `null` and in the second, the `is(Visible.T)` method returns `true` if no metadata is configured. That is the default behavior.

## Default Predicate Behavior
Normally, if no predicate metadata is configured, metadata resolver returns `true` during the evaluation.

In some cases, however, you need the default value to be `false`, for example when a property is unique. You do this by having your interface derive from `ExplicitPredicate` and not just `Predicate` directly, for example:
```java
Interface Unique extends PropertyMetaData, ExplicitPredicate {}
```
If you declare your interface like so, calling the `mdResolver.getMetaData().entity(MyEntity.T).is(Unique.T)` method returns `false`.
## PredicateErasure
In some cases you need to make sure that certain properties are not visible. To support this functionality, every predicate metadata can extend the `PredicateErasure` interface. For example, we can have metadata which acts in an opposite manner to our Visible metadata:
```java
interface Visible extends UniversalMetaData, Predicate { }

interface Hidden extends Visible, PredicateErasure { }
```
If you wanted to set only a few properties as hidden, you would then assign the Hidden metadata to `GmEntityType.propertyMetaData` of your entity and assign the Visible metadata to all the entries you want to have displayed.
