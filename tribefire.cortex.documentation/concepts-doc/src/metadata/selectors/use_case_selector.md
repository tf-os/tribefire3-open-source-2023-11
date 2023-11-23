# Use Case Selector

The use case selector allows you to define a unique use case string identifier describing when a metadata should be resolved.

## General

You can specify on which component (a specific area of the UI) a metadata should be resolved in or create your own use case string identifier.

## UI Element Selector

There are several options available which affect the functionality of a particular component. You configure the Use Case Selector by entering the name of the component you wish to affect into the Use Case property.

Component | Configuration
------| ---------
[GIMA](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#gima) | Enter `gima` in the Use Case property.
[Selection](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#selection) | Enter `selection` in the Use Case property.
[Thumbnail](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#thumbnail) | Enter `thumbnail ` in the Use Case property.
[Assembly Panel](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#assembly-panel) | Enter `assembly ` in the Use Case property.
[Property Panel](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#property-panel) | Enter `property ` in the Use Case property.
[Quick Access](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#quick-access) | Enter `quickAccess ` in the Use Case property.
[Automatic Queries](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#automatic-queries) | Enter `automaticQueries ` in the Use Case property.
[Global Actions](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#global-actions) | Enter `globalActions ` in the Use Case property.
[Metadata Editor](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#metadata-editor) | Enter `metadataEditor` in the Use Case property.
[Select Result Panel](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#select-result-panel) | Enter `selectResultPanel` in the Use Case property.
[Service Request Panel](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md#service-request-panel) | Enter `serviceRequestPanel` in the Use Case property.

## Custom Use Case

You simply provide a string identifier of your use case. Use case selector comes in handy during garbage collection.

> For more information, see [Using the Garbage Collector](asset://tribefire.cortex.documentation:tutorials-doc/control-center/using_garbage_collector.md).

## Custom Use Case Triggered via URL

You can use the custom use case selector to trigger the resolving of any metadata attached to Control Center or Explorer elements using the URL. You do not have use to predefined use cases, but you can create your own and trigger it by passing the use case ID as a parameter: `http://localhost:8080/tribefire-control-center/?useCase=useCaseID`

Consider the following example: you set the Hidden metadata on the **Connections** entry point in Control Center and assign the use case selector to it.
> For more information, see [Visible and Hidden](../prompt/visible.html).

You specify the use case ID to be, for example, `hideElement`, and commit your changes.

* If you open Control Center using the `http://localhost:8080/tribefire-control-center/` URL, nothing changes. 
* However, if you open Control Center using the `http://localhost:8080/tribefire-control-center/?useCase=hideElement` URL, the use case selector with the `hideElement` ID is triggered, and, as it was assigned to the Hidden metadata on the **Connections** entry point, the said entry point is no longer visible.