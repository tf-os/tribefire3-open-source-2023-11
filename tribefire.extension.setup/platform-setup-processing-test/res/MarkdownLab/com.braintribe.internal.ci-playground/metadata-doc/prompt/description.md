---
##################
# published: false
##################
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Description
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# provide the keywords to be picked by the search engine and SEO, separated by a comma
keywords: 
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 21.02.2018
summary: "This metadata allows you to configure the entity's description."
sidebar: essentials
layout: page
permalink: description.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
# example image: {% include image.html file="wire_diagram_denot_and_experts.png" max-width=600 %}
# example tip: {% include tip.html content="For more information, see [Metadata](general_metadata_properties.html)" %}
# example note: see below
---

Metadata Property Name  | Type Signature  
------- | -----------
`Description` | `com.braintribe.model.meta.data.prompt.Description`

## General
This metadata allows you to change the entity's description. To assign this metadata programmatically, use the `@Description` annotation. 

This parameter is also used in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a> as a tooltip.  {% include image.html file="displayinfo_desc.png"%}

{%include note.html content="This metadata supports localization."%}


## Localization

To have multiple versions of this metadata you must attach the annotation multiple times, once for each locale. If only a message is passed in the annotation, it is used as a value for the default locale:
```java
@Description("default description")
String getPropertyWithName();
```
If other locale needs to be specified, simply use the `locale` element of the annotation:

```java
@Description("default description")
@Description(locale = "de", value = "Beschreibung")
String getPropertyWithNames();
```

Be careful when specifying the `globalId` element. Since all annotations are turned into a single metadata instance, use only a single `globalId` value. It is enough to specify it once. 

However, you can also specify the `globalId` for each individual annotation, but you must make sure it has the same value each time. 

{%include note.html content="If different values are used, one of them is picked randomly."%}

Example:
```java
@Description(locale = "pl", value = "Opis", globalId = "md.descriptions")
@Description(locale = "de", value = "Beschreibung", globalId = "md.descriptions")
String getPropertyWithNames();
```

If no `globalId` is specified, a default one is generated (like for any other meta data annotation) in the form of `annotated:${ownerElement.globalId}/${annotationSimpleName}`, for example `annotated:property:com.braintribe.model.MyEntity/someProperty/Description`.

