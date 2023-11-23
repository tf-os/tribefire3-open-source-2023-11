# Creating a Scripted Transition Processor
A transition processor is used in the dataflow engine to manipulate the process entity or objects belonging to it when it reaches a configured node or edge. You can attach such a processor to the following components:
* node:
  * `onEntered` list property
  * `onLeft` list property
* edge:
  * `onTransit` list property

>For more information about dataflow engine, see [Dataflow Engine](asset://tribefire.cortex.documentation:concepts-doc/features/dataflow-engine/dataflow_engine.md).

## Creating a Scripted Transition Processor in Control Center
1. In Control Center, navigate to **Transition Processors** entry point.
2. On the Action Bar, click **New** and select **ScriptedTransitionProcessor**. A new modal window opens.
3. In the modal window, provide an **externalId**, **name**, and click the **Assign** link next to the **script** label. A new modal window opens.
4. Select the script type which you want to code in and click **Finish**. The script properties window opens.
5. In the script properties window, there is only one property: `source`. Paste or write your custom script there, for example:
   ```javascript
   if($.getProcess().getProcessEntity().getProcessStepNumber()=="3"){
     $.getProcess().getProcessEntity().setProcessStepNumber("4");
     $.continueWithState("nextStep");
   } else {
    $.continueWithState("error");
   }
   ```
   >For more information on the available functionality and syntax of a script, see [Scripting](asset://tribefire.cortex.documentation:concepts-doc/features/scripting.md).

6. Apply your changes. You can now assign your new scripted transition processor to a node or an edge in the process designer.

   >For more information on the process designer and the different components of a process, see [Process Definition Components](asset://tribefire.cortex.documentation:concepts-doc/features/dataflow-engine/process_definition_components.md).

