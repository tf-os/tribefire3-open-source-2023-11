# Creating Template-based Actions
Template-based actions are a powerful feature of Tribefire, improving (or even defining) the functionality of your accesses by allowing you to build custom operations. These operations can be then easily managed via their template.

Template evaluation occurs whenever a template-based action is executed. This means that any Value Descriptors assigned to the template are resolved, bringing actual data to the template. 

## Prerequisites
A properly configured template is the foundation of any template-based action, and as such it needs to be configured first.

1. Switch to the workbench of the access where you intend to add the template.
2. In **Quick Access...**, find the `Template` query. All current templates (if any) are returned.
3. Click **New**. The new template form opens.
4. Assign the properties in accordance with your requirements. For explanation of the individual properties, see [Template](asset://tribefire.cortex.documentation:concepts-doc/features/Templates/template.md).
5. Commit your changes and proceed to creating the template-based action.

## Template-base Action Properties
All Template-based actions (TBAs) have the following set of properties:

Property | Description
--- | ---
displayName | The name under which your TBA will be available in Tribefire Explorer.
forceFormular | This option forces tribefire to display a form  which allows an administrator to enter specific values. This form should be implemented when the template action is using variables.
icon | If you wish to have an icon associated with this TBA, you can assign one here.
inplaceContextCriterion | By defining this property the TBA will be displayed as a button on the buttons panel. You can use different criteria to define exactly when or where this button should be displayed.
multiSelectionSupport | Enable this option to support multiselection.
template | Template hosts the entity type to be processed. This is the property that sets apart different action types - they need to be assigned with the appropriate template, having the correct prototype.


## Creating a TemplateQueryAction
When configured, Template Query Action is available as a regular query in your access, returning the entity type assigned in `entityTypeSignature` of the prototype, which in this case must be set to `EntityQuery`.

1. In the workbench, find the `TemplateQueryAction` in **Quick Access...**. All current actions (if any) are returned.
2. Click **New**. The form for adding a new action opens.
3. Assign the properties in accordance with your requirements. Remember that in this case you need a template with `EntityQuery` as the prototype.
4. Commit your changes. The query should now be available in the access itself. Try it out!

When you have completed the above procedures, it's time to try out your query. Switch to the target access, and execute it from **Quick Access...** menu - it's that simple!

## Creating a TemplateInstantiationAction
When configured and added as a workbench entry, this action will instatiate its prototype.

1. In the workbench, find the `TemplateInstantiationAction` in **Quick Access...**. All current actions (if any) are returned.
2. Click **New**. The form for adding a new action opens.
3. Assign the properties in accordance with your requirements. In this case, any template with an instantiable prototype is allowed.
4. Assign the action to a folder in workbench and commit your changes. You should now find the action in your access. Try it out!

## Creating a TemplateServiceRequestAction
When configured and added as a workbench entry, this action will execute the `ServiceRequest` assigned as its prototype.

1. In the workbench, find the `TemplateServiceRequestAction` in **Quick Access...**. All current actions (if any) are returned.
2. Click **New**. The form for adding a new action opens.
3. Assign the properties in accordance with your requirements. You will need a template with a properly configured `ServiceRequest` as its prototype.
4. Assign the action to a folder in workbench and commit your changes. You should now find the action in your access. Try it out!

## Creating a TemplateInstantiationServiceRequestAction
When configured and added as a workbench entry, this action will execute the `ServiceRequest` assigned as its prototype. In this case, the service request is supposed to instantiate an entity - this is the difference between this action and the `TemplateServiceRequestAction`.

1. In the workbench, find the `TemplateInstantiationServiceRequestAction` in **Quick Access...**. All current actions (if any) are returned.
2. Click **New**. The form for adding a new action opens.
3. Assign the properties in accordance with your requirements. You will need a template with a properly configured `ServiceRequest` as its prototype. The service request is expected to **instantiate** an entity in this case.
4. Assign the action to a folder in workbench and commit your changes. You should now find the action in your access, available from the **New** button. Try it out!