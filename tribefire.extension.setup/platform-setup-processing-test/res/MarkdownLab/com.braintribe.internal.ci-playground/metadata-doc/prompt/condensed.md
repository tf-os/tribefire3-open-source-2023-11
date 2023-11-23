---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Condensed
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "This metadata allows you to configure how a property within an entity behaves when collapsed."
sidebar: essentials
layout: page
permalink: condensed.html
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
`Condensed` | `com.braintribe.model.meta.data.prompt.Condensed`

## General
This metadata refers only to aggregation properties, as these are the only properties that can have further properties. Configuring this metadata allows you to only display the top level of the property. There are three modes of functionality available:

Function | Description
------| ---------
`Optional` |In <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.explorer}}">Explorer</a>, this allows you to choose between modes, done through buttons displayed on the Action Bar.
`Auto` | The condensation is done automatically.
`Forced` | Forces the metadata to condense the entity to show only the top layer.

You can configure this metadata by choosing one method of functionality from a drop-down list and defining on which property this metadata should act.
{%include note.html content="You must refresh your browser after saving changes before they take effect."%}
## Example
This metadata contains two options that must be configured for correct functionality. You must choose the mode of operation from the drop-down list and also select which property this metadata should function on.
### Optional
When this metadata functions in `optional` mode, two buttons appear on the Action Bar in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a> or Explorer, allowing you to choose between different modes of functionality.

In the example below, `optional` mode has been selected and functions on a property called `delegates`, which belongs to an entity called `Person`.
{%include image.html file="metadata/EntityCondensationButtons.png"%}
* Details Hidden
  {%include image.html file="metadata/EntityCondensationOptional02.png"%}
* Details Shown
  {%include image.html file="metadata/EntityCondensationOptional01.png"%}

### Forced
After selecting this mode and the property that it should function on, Explorer only displays the top layer of this hierarchy.

In the example below, the mode forced was selected and functions on the property `delegates`, which belongs to an entity `Person`. Only the top layer of the hierarchy is displayed in Explorer:
{%include image.html file="metadata/EntityCondensationForce01.png"%}
