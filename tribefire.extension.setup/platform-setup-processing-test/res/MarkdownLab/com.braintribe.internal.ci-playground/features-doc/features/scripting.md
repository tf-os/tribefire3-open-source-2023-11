# Scripting
>You can create certain tribefire extension points directly in Control Center by using the scripting functionality.

## General
Normally, you extend tribefire by creating new <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.extension_point}}">extension points</a> in a <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.cartridge}}">cartridge</a>. However, if you don't need a full-blown extension point and just want to quickly create one, you can create smaller, more light-weight solutions by using the scripting functionality.

You can use scripting to implement the following extension points:
* Actions
* State Change Processors
* [Transition Processors](process_definition_components.html#transition-processor)
* [Conditions](process_definition_components.html#condition)
* Service Processors
* Access Request Processors

All scriptable extension points are derived from the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity type</a> `ScriptedProcessor`, with each one having their own scripted type, for example `ScriptedActionProcessor`, `ScriptedStateChangeProcessor`, and so on.

The main property in `ScriptedProcessor` inherited by all instances is called `script`. This is where you provide your actual script.

{% include supported_scripting_languages.md %}

{% include note.html content="All scripting languages have access to the same features, meaning there is no difference between them at a tribefire level, so choose the one you are more comfortable with."%}

{% include tip.html content="For a tutorial on creating a scripted state change processor, see [Creating a Scripted State Change Processor](creating_a_scripted_state_change_processor.html)"%}

## Variables
All scriptable extension points share certain variables, but some extension points have access to more methods than others because of the context they use. See the **Contexts** section for more information.

Variable | Description
---- | ----
`$context` | Represents the current context. Each context is unique and is determined by its type – this means, for example, a state change processor receives a `StateChangeContext`, while an action processor receives a `ActionExecutionContext`. <br/> <br/> `$context` behaves like a normal keyword and represents the context, so for example, in the `StateChangeContext`, you can gain access to the current session by using: `$context.getSession();` because the `getSession()` method is provided by the `StateChangeContext`.
`tools` | Allows you to use the `getTypeReflection()`, `create()`, and `getLogger()` methods.

* `getTypeReflection()` allows you to monitor or modify behavior of all types in tribefire dynamically, allowing the instantiation and invocation of types and methods, for example, without knowing their exact names. It also allows you to investigate information about all types and their structure, rather than actual instances of these types. This method returns `GenericModelTypeReflection` – the base class for all type reflections in tribefire. During scripting, you can use the full features of `GenericModelTypeReflection`.

  {%include apidoc_url.html className="GenericModelTypeReflection" link="interfacecom_1_1braintribe_1_1model_1_1generic_1_1reflection_1_1_generic_model_type_reflection.html" %}

* `create()` provides a simple way of creating new type instances based on the type signature passed: `tools.create("com.braintribe.model.user.User")`

* `getLogger()` allows you to push messages, exceptions, or both to the configured log file and console.

  {%include apidoc_url.html className="Logger" link="classcom_1_1braintribe_1_1logging_1_1_logger.html"%}


## Contexts
The contexts of scriptable extension points are based on their Java API counterparts, which means they have the same functionality.

Scriptable Component | Java API Context
--- | --- 
Scripted State Change Processor | `StateChangeContext` 
Scripted Action Processor | `ActionExecutionContext`
Scripted Transition Processor | `TransitionProcessorContext`
Scripted Condition Processor | `ConditionProcessorContext`
Scripted Service Processor | `ScriptedServiceProcessor`

Each context provides different functionality you can use in your script.

{%include apidoc_url.html className="StateChangeContext" link="interfacecom_1_1braintribe_1_1model_1_1processing_1_1sp_1_1api_1_1_state_change_context.html"%}
{%include apidoc_url.html className="ActionExecutionContext" link="interfacecom_1_1braintribe_1_1model_1_1processing_1_1action_1_1api_1_1_action_execution_context.html"%}
{%include apidoc_url.html className="TransitionProcessorContext" link="interfacecom_1_1braintribe_1_1model_1_1processing_1_1pmp_1_1api_1_1_transition_processor_context.html"%}
{%include apidoc_url.html className="ConditionProcessorContext" link="interfacecom_1_1braintribe_1_1model_1_1processing_1_1condition_1_1api_1_1_condition_processor_context.html"%}
{%include apidoc_url.html className="ScriptedServiceProcessor" link="classcom_1_1braintribe_1_1model_1_1processing_1_1deployment_1_1processor_1_1_scripted_service_processor.html"%}