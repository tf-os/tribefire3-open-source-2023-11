# Creating a Dataflow Model and Access

<iframe width="560" height="315" src="https://www.youtube.com/embed/hn6GNQhSAaE" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

Dataflow engine works based on a model, acces, and the dataflow engine components. Apart from the configuration of those components, you must make sure to configure your model and access correctly.

>For more information, see [Dataflow Engine Components](asset://tribefire.cortex.documentation:concepts-doc/features/dataflow-engine/dataflow_engine_components.md).

## Creating a Model for Dataflow Engine
This model is where you must create your entity which derives from the `Process` entity. Before you are able to do this, you must first merge your business model with the Process Model, since this model contains the `Process` entity required for its creation.

To make your model depend on Process Model:

1. In Control Center, go to **Custom Models** and click **New**.
2. In the window that appeared, in the **Dependencies** section, click **Add**.
3. Search for `ProcessModel`, click **Add and Finish** and then click **Execute**.
4. Proceed to create your actual model. Make sure that your process entity derives from the `Process` entity contained in the `ProcessModel`.

## Creating and Access for Dataflow Engine
The access you use for the dataflow engine must have a `StateProcessingAspect` configured. The state processing aspect contains the specific instance of the processing engine required for your process. This aspect has a property called `Processor` that should be assigned the instance of the processing engine that will be used. The state processing aspect is always assigned to an access.

To assign a `StateProcessingAspect` to an access:

1. Click on your access and, on the **Action Bar**, click **More -> Setup Aspects**.  
2. In the window that appeared, click **Execute**. 
3. Right-click the **aspects** menu and click **Add**.
4. Search for `StateProcessingAspect`, and click **Add and Finish**. 
5. In the new window that appeared, provide the `externalId`, `name` and assign a new instance of a processor in the **processors** section by clicking **Add**.
6. In the new window that appeared, search for `ProcessingEngine` and click **Add**.
7. Provide the `externalId`, `name` and click **Add and Finish**.
8. Commit your changes.