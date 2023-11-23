# Templates

Templates are entities configured in an access workbench that define the basic structure of a Tribefire object. You could think of template as an empty document, where you don't have any actual data, but you know what **kind** of data it's going to have (for example the first name, the last name, and the date of birth on a birth certificate). The only difference is that we're templating entities and collections instead of documents, typically using [Value Descriptors](../value_descriptors.md) in the process.

> For more information about workbench configuration, see [Creating Workbench](asset://tribefire.cortex.documentation:tutorials-doc/workbench/creating_workbench.md).

Templates are essentially revolving around the following properties:
* a `prototype`. In practice, it's either an entity or a collection.
* a `script` that applies manipulations to the above prototype on runtime (it's not mandatory as such, but necessary for most use-cases). 

Templates gain meaning when assigned to a [template-based action](asset://tribefire.cortex.documentation:tutorials-doc/workbench/template_actions.md) in the workbench. Template-based action, upon execution, runs the scripted manipulations (including the evaluation of Value Descriptors). The result is normally either the creation of a new instance of an entity, the execution of a service request, or the execution of an entity query.

All of the above means that templates are a powerful and convenient way to provide extra functionality to Tribefire, which can be easily managed from a single source - the template.

## Template Properties

All templates have the following set of properties:

Property | Description
--- | ---
description | Description for your convenience - note down what the template is for.
name | Template name.
prototype | A key property defining the template type. What will happen to this type (for example creating a new instance of the type, querying for the type, etc.) depends on the **template-based action** configured later. When you have assigned a prototype, you need to assign an `entityTypeSignature` to it.
prototypeTypeSignature | Enter the `typeSignature` of the prototype.
script | The script is used to define manipulations you want to be performed in your prototype.
technicalName | Technical name for the template, used as a label in Tribefire.
metaData | You can assign metadata to your template here.

### Populating Templates with Data via Value Descriptors

You can use [Value Descriptors](../value_descriptors.md) to feed data to templates. See [Assigning Value Descriptors to Templates](asset://tribefire.cortex.documentation:tutorials-doc/template/assigning_value_descriptors.md) for details.

## What's Next?

For information on how to create template-based actions, see [Creating Template-based Actions](asset://tribefire.cortex.documentation:tutorials-doc/workbench/template_actions.md).
