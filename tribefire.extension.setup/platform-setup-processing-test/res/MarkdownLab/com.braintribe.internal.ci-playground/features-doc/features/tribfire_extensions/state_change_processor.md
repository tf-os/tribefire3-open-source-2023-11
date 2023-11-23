# State Change Processor
>A state change processor carries out an operation when there is a change in a watched state.

## General
tribefire offers the ability to create custom state change processors, which are used to carry out operations when a watched state is altered, in conjunction with the On Change metadata. 

{% include tip.html content="For more information about the On Change metadata, see [On Change](on_change.html)."%}

You can script such a processor using one of the available scripting languages directly in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a> or create one in a cartridge. 

A state change processor is configured by using one of the subtypes of the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity</a> `StateChangeProcessor` depending on the type of processor that you wish to configure. 

We support the following types of `StateChangeProcessor`:

Type | Description
---- | -----
`ScriptedStateChangeProcessor` | Works based on a script which you can create directly in Control Center.
`VersatileScriptedStateChangeProcessor` | Works based on a script which you can create directly in Control Center and allows you to specifically address the three different phases of the `StateChangeProcessor`: `beforeScript` (script called before a state change is committed), `processScript` (after a state change is committed in an asynchronous way), and `afterScript` (after a state change is committed in a synchronous way).
`CartridgeStateChangeProcessor` | Works based on an implementation provided in a cartridge.


{% include tip.html content="For a tutorial on creating a scripted state change processor, see [Creating a Scripted State Change Processor](creating_a_scripted_state_change_processor.html)"%}

{% include apidoc_url.html className="StateChangeProcessor" link="interfacecom_1_1braintribe_1_1model_1_1extensiondeployment_1_1_state_change_processor.html"%}

## Properties

Property | Description  
------- | -----------
`externalId`  | External ID of the state change processor.
`name` | Name of the state change processor.
`autoDeploy`   | Boolean flag influencing whether to autmatically deploy this state change processor.    
`cartridge` | Configured cartridge which contains the state change processor implementation
`deploymentStatus` | Deployment state of this state change processor.
`script` | Only in `ScriptedStateChange` processor. A `Script` object where the implementation of a state change processor can be inputed directly.
`afterScript` |  Only in `VersatileScriptedStateChange` processor.
`beforeScript` |  Only in `VersatileScriptedStateChange` processor.
`processScript` | Only in `VersatileScriptedStateChange` processor.