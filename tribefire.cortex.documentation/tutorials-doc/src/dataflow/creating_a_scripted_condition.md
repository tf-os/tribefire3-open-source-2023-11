# Creating a Scripted Condition
A condition is used to determine the progression of the process when a node has several conditional edges leading from it. You can attach a condition to the following components:
* conditional edge:
  * `condition` property

>For more information about dataflow engine, see [Dataflow Engine](asset://tribefire.cortex.documentation:concepts-doc/features/dataflow-engine/dataflow_engine.md).

## Creating a Scripted Condition in Control Center
1. In Control Center, navigate to **Conditions** entry point.
2. On the Action Bar, click **New** and select **ScriptedCondition**. A new modal window opens.
3. In the modal window, provide an **externalId**, **name**, and click the **Assign** link next to the **script** label. A new modal window opens.
4. Select the script type which you want to code in and click **Finish**. The script properties window opens.
5. In the script properties window, there is only one property: `source`. Paste or write your custom script there, for example:
   ```javascript
   $.getProcess().getProcessEntity().getProcessEntityIntProperty() != 1;
   ```
   >For more information on the available functionality and syntax of a script, see [Scripting](asset://tribefire.cortex.documentation:concepts-doc/features/scripting.md).

6. Apply your changes. You can now assign your new scripted condition to a conditional node in the process designer.

   >For more information on the process designer and the different components of a process, see [Process Definition Components](asset://tribefire.cortex.documentation:concepts-doc/features/dataflow-engine/process_definition_components.md).


>For information on the deployment of various process definition components, see [Dataflow Engine Components](asset://tribefire.cortex.documentation:concepts-doc/features/dataflow-engine/dataflow_engine_components.md#general).