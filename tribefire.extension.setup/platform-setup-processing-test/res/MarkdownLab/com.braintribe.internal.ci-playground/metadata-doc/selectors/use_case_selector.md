---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: Use Case Selector
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata, ui]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 23.01.2018
summary: "The use case selector allows you to define a unique use case string identifier describing when a metadata should be resolved. "
sidebar: essentials
layout: page
permalink: use_case_selector.html
# hide_sidebar: true
toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## General
You can specify on which component (a specific area of the UI) a metadata should be resolved in or create your own use case string identifier. 

## UI Element Selector
There are several options available which affect the functionality of a particular component. You configure the Use Case Selector by entering the name of the component you wish to affect into the Use Case property.

Component | Configuration
------| ---------
[GIMA](ui_elements.html#gima) | Enter `gima` in the Use Case property.
[Selection](ui_elements.html#selection) | Enter `selection` in the Use Case property.
[Thumbnail](ui_elements.html#thumbnail) | Enter `thumbnail ` in the Use Case property.
[Assembly Panel](ui_elements.html#assembly-panel) | Enter `assembly ` in the Use Case property.
[Property Panel](ui_elements.html#property-panel) | Enter `property ` in the Use Case property.
[Quick Access](ui_elements.html#quick-access) | Enter `quickAccess ` in the Use Case property.
[Automatic Queries](ui_elements.html#automatic-queries) | Enter `automaticQueries ` in the Use Case property.
[Global Actions](ui_elements.html#global-actions) | Enter `globalActions ` in the Use Case property.
[Metadata Editor](ui_elements.html#metadata-editor) | Enter `metadataEditor` in the Use Case property.
[Select Result Panel](ui_elements.html#select-result-panel) | Enter `selectResultPanel` in the Use Case property.
[Service Request Panel](ui_elements.html#service-request-panel) | Enter `serviceRequestPanel` in the Use Case property.

## Custom Use Case
You simply provide a string identifier of your use case. Use case selector comes in handy during garbage collection.

{%include tip.html content="For more information, see [Using the Garbage Collector](using_garbage_collector.html)."%}

## Custom Use Case Triggered via URL

You can use the custom use case selector to trigger the resolving of any metadata attached to <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a> or <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.explorer}}">Explorer</a> elements using the URL. You do not have use to predefined use cases, but you can create your own and trigger it by passing the use case ID as a parameter: `http://localhost:8080/tribefire-control-center/?useCase=useCaseID`

Consider the following example: you set the Hidden metadata on the **Connections** entry point in Control Center and assign the use case selector to it.
{%include tip.html content="For more information, see [Visible and Hidden](visible.html)."%}

You specify the use case ID to be, for example, `hideElement`, and commit your changes.

* If you open Control Center using the `http://localhost:8080/tribefire-control-center/` URL, nothing changes. 
* However, if you open Control Center using the `http://localhost:8080/tribefire-control-center/?useCase=hideElement` URL, the use case selector with the `hideElement` ID is triggered, and, as it was assigned to the Hidden metadata on the **Connections** entry point, the said entry point is no longer visible.