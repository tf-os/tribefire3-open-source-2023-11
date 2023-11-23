# On Action

This metadata allows you to assign an ActionProcessor that takes effect when an action request is executed on an entity. You can use BeanShell or JavaScript to create your processors.

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

> You must always start any command that calls tribefire with `$`.

There are two main methods which can be called from the context element: `getRequest() or getSession()`

There are two main Java classes which can be called with scripting with the Action Proccesor: `ScriptContext` and `ActionScriptContext`.