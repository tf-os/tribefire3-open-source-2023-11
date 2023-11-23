---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: On Action
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 17.01.2018
summary: "This metadata allows you to assign an ActionProcessor that takes effect when an action request is executed on an entity. You can use BeanShell or JavaScript to create your processors."
sidebar: essentials
layout: page
permalink: on_action.html
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
`OnAction` | `com.braintribe.model.extensiondeployment.meta.OnAction`

## General
You have to attach an Action or a Scripted Action processor to the processor property of this metadata. There are two entities you can attach:
* Cartridge Action Processor
* Scripted Action Processor

Both these entities have the same properties that can be defined, apart from one crucial difference. Where the Cartridge Action Processor has a property `Cartridge`, the Scripted Action Processor has a property `Script`. Both these properties should then be defined by a Cartridge instance or a Script instance (either BeanShell or JavaScript) respectively.

* Cartridge Action Processor
The Cartridge Action Processor then requires the appropriate cartridge instance.

* Scripted Action Processor
Alternatively, you can use the Scripted Action Processor to add either a BeanShell or JavaScript directly in tribefire. Both these objects are the same, only differing in the language they accept. You can enter the script into the source property.

{% include note.html content="You must always start any command that calls tribefire with `$`"%}

There are two main methods which can be called from the context element: `getRequest() or getSession()`

There are two main Java classes which can be called with scripting with the Action Proccesor: `ScriptContext` and `ActionScriptContext`.

{%include apidoc.html%}
