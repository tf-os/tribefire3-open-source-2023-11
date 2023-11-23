# State Change Processor

TODO move to correct extension

>A state change processor carries out an operation when there is a change in a watched state.

## General
tribefire offers the ability to create custom state change processors, which are used to carry out operations when a watched state is altered, in conjunction with the On Change metadata. 

>For more information about the On Change metadata, see [On Change](asset://tribefire.cortex.documentation:concepts-doc/metadata/on_change.md).
You can script such a processor using one of the available scripting languages directly in Control Center or create one in a cartridge. 

A state change processor is configured by using one of the subtypes of the entity `StateChangeProcessor` depending on the type of processor that you wish to configure. 

We support the following types of `StateChangeProcessor`:

Type | Description
---- | -----
`ScriptedStateChangeProcessor` | Works based on a script which you can create directly in Control Center.
`VersatileScriptedStateChangeProcessor` | Works based on a script which you can create directly in Control Center and allows you to specifically address the three different phases of the `StateChangeProcessor`: `beforeScript` (script called before a state change is committed), `processScript` (after a state change is committed in an asynchronous way), and `afterScript` (after a state change is committed in a synchronous way).
`CartridgeStateChangeProcessor` | Works based on an implementation provided in a cartridge.


> For a tutorial on creating a scripted state change processor, see [Creating a Scripted State Change Processor](asset://tribefire.cortex.documentation:tutorials-doc/extension-points/creating_a_scripted_state_change_processor.md).
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