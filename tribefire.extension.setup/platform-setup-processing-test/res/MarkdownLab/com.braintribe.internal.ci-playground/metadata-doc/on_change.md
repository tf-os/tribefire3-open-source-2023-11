---
# page title - looks like <h1> so other sections should not have <h1> or single-hash headings
title: On Change
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
# see the list of available tags in _data/tags.yml
tags: [metadata]
# use datatable: true for tables with lots of data
# see the example below
datatable: false
last_updated: 19.01.2018
summary: "This metadata allows you to assign a StateChangeProcessor that affects another part of tribefire when a property is changed."
sidebar: essentials
layout: page
permalink: on_change.html
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
`OnChange` | `com.braintribe.model.extensiondeployment.meta.OnChange`

## General
This metadata allows you to assign a `StateChangeProcessor` which is used to carry out operations when a particular state is altered. These processors can be scripted directly in tribefire, using one of the available scripting languages.

{% include tip.html content="For more information, see [State Change Processor](state_change_processor.html)."%}

Property | Description
------- | -----------
`callOnAfter` | Boolean property causing the `StateChangeProcessor` to be called after the actual change happens, i.e. after it is committed to persistence.
`callOnBefore` | Boolean property causing the `StateChangeProcessor` to be called before the actual change happens, i.e. before it is committed to persistence.
`processor` | The `StateChangeProcessor` to be used. Note that the  main `processStateChange()` method is always called asynchronously.

As described above, the Cartridge State Change Processor is used when you wish to define the change through a customized cartridge and the Scripted State Change Processor is used when you want to define the change by either a BeanShell or a JavaScript. If you are using a Cartridge you must first install it on the system. However, when using the Scripted State Change Processor, you can enter the script directly into <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a>.

## Example
You must first add the correct object that this processor will use. There are two possible entities that can be attached:
* Cartridge State Change Processor
* Scripted State Change Processor

Both these entities have the same properties that can be defined, apart from one crucial difference. Where the Cartridge State Change Processor has a property entitled Cartridge, the Scripted State Change Processor has a property Script. Both these properties should then be defined by a Cartridge instance or a Script instance (either BeanShell or JavaScript) respectively.

* Cartridge Action Processor
The Cartridge Action Processor then requires the appropriate cartridge instance.

* Scripted Action Processor

Alternatively, you can use the Scripted Action Processor to add either a BeanShell or JavaScript directly in tribefire. Both these objects are the same, only differing in the language they accept. You can enter the script into the source property.

{% include note.html content="You must always start any command that calls tribefire with `$`"%}

There are two main methods which can be called from the context element: `getRequest() or getSession()`

The main class used when scripting is `ScriptContext`.

{%include apidoc.html%}
