# Process Definition Components
>There are several different components that make up the process definition.

## General
To create a valid process definition, you must understand the different component that make up a process:

* [nodes](process_definition_components.html#nodes)
* [edges](process_definition_components.html#edges)
* [processors](process_definition_components.html#processors)
* [workers](process_definition_components.html#workers)
* [decoupled interactions](process_definition_components.html#decoupled-interactions)

You can create the process definition using the process designer. Process designer is a GUI that allows you to design process definitions that are executed by the dataflow engine. It provides all the components required to visually display the process, and to design the whole process. The actual logic, as defined by transition processors and condition processors is programmed either in a cartridge or through a script, and these components can then be added to the different nodes or conditional edges in process designer.

You can open process designer by double clicking a newly created process definition in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a>.

## Nodes
A node is used to represent a particular state within the process flow. The dataflow engine drives the process entity from state to state, connected by edges, until an end node is reached and the process is completed. There are main types of node:

Type | Description
--- | ----
[Standard Node](process_definition_components.html#standard-node) | Define the different steps in the process itself, and are joined by either a standard or a conditional edge.
[Restart Node ](process_definition_components.html#restart-node) | Used to restart the process. You can determine how many times the process is allowed to restart using the property `Maximum Number of Restarts Allowed`, and by default this value is `3`. Additionally, you can determine at what stage in the process it should be restarted, by created a restart edge at the position that you wish.


These are the only two types of nodes that can be created, but you can designate them with different functions:

Type | Description
--- | ----
[Process Definition Node](process_definition_components.html#process-definition-node) | Represents the process definition as a whole.
[Initial Node](process_definition_components.html#initial-node) | Determines the entry point of the process.
[End Node](process_definition_components.html#end-node) | Determines the exit point of the process.
[Overdue Node](process_definition_components.html#overdue-node) | Determines where a process is routed to while waiting for the completion of an action, for example, a decoupled interaction or another process. This is an instance of a Restart Node.
[Error Node](process_definition_components.html#error-node) | Determines where a process is routed to when an exception, or an error, it thrown in the process.


### Standard Node
The standard node is the basic building block in the process designer. Used to represent a states in a process, they are connected by edges, and together form the process flow of a process, meaning the path of a process from beginning to end.

{%include note.html content="If a standard node is created and no edge is drawn leading away from it, it is considered automatically to be and end node."%}

The standard node contains two properties that allows for the configuration of Transition Processors:
* `On Entered`
* `On Left`

As their names suggest, these properties determine the timing of the execution of the processors. `On Entered` being executed when the state is first reached and `On Left` when leaving the state. A transition processor is the object that contains the programmed logic of what should happen to the Process-derived entity, and any of its related objects, and can be developed in a cartridge or scripted in Control Center.

Property | Description
--- | ---
`description` | Describes the node.
`name` | Determines the name of the node.
`decoupledInteraction` | Determines whether any decoupled interactions are assigned. For more information, see [Decoupled Interactions](process_definition_components.html#decoupled-interactions).
`errorNode` | Defines the error node that the process should be diverted to if an error occurs at this state. <br/> <br/> You don't actually have to configure the error node in the standard node's properties. Instead, you can draw a connection from the standard node to the error node and selecting **Set Error Node**. <br/> <br/> You can also set a global node by drawing a connection from the process definition node to another node (either a standard or restart node) and selecting **Set Error Node**.
`gracePeriod` | Determines how long the process is allowed to wait at the current state before being moved to the next state (or, depending on the process, to the overdue or error node). <br/> <br/> The entity type `TimeSpan` has two properties: `Unit` and `Value`. The first determines the time unit (seconds, minutes, days, and so on) and the second the value of the time.
`isRestingNode` | Determines if this standard node is a resting node or not. <br/> <br/> A resting node is one that has no further processing steps attached. All processes stopped in such a node are therefore ignored. <br/> <br/> Any end node is automatically set to be a resting node.
`overdueNode` | Determines if this standard node is an overdue node or not. For more information, see [Overdue Node](process_definition_components.html#overdue-node). <br/> <br/> You should not define the overdue node in the properties panel. Instead, you can draw a connection from the standard node to the overdue node and select **Set Overdue Node**.
`state` | Determines the name of the state for the standard node. <br/> <br/> Dataflow engine drives the process by changing the value of the trigger property, as defined in your process entity, until it reaches an end node, or, depending on the process flow, an error node. The state can only be changed to another value if there exists an edge between the two nodes, with one exception: if a global overdue node or error node have been defined, the process can be automatically redirected here without an edge defined between these two nodes. <br/> <br/> This value is automatically defined from the value given when first creating the standard node.
`conditionalEdges` | Determines a list of conditional edges that connect to this standard node. <br/> <br/> You do not have to configure nodes here, since they are added automatically when drawing conditional edges to and from the node.
`onEntered` | Instructs the state engine to carry out that functionality when the process enters the state. This property is defined with an instance of a transition processor, created either in a cartridge or scripted in Control Center. <br/> <br/> It is important to note the difference between the `On Entered` and `On Left` properties, because of decoupled interactions, or other workers. This means that between the process entering the current state and leaving it, it is possible that the information contained in the process object may have changed during this period.
`onLeft` | Instructs the state engine to carry out that functionality when the process leaves the current state. This property is defined with an instance of a transition processor, created either in a cartridge or scripted in Control Center.


### Restart Node
A restart node, like standard nodes, is also used to represent states within the process designer and is connected to other nodes through the use of edges. Unlike the standard node, this node type is able to restart the process according to your requirements.

A restart edge is used to define where the process should be restarted at, and is created by drawing a connection from the restart node and the edge where the process restarts.

The standard node contains a property that allows for the configuration of transition processors, called `On Entered`. Any transition processors that are assigned to this property will then be executed as the process enters this state. A transition processor is the object that contains the programmed logic of what should happen to the Process-derived entity, or any of its related objects, and is either developed in a cartridge or scripted in Control Center.

This node can be designated different functionality, as opposed to a specific state within the process flow, such as an error node or an overdue node.

A restart node is indicated by two arrows the form a circle in the node.

Property | Description
--- | ---
`description` | Describes the node.
`name` | Determines the name of the node.
`errorNode` | Defines the error node that the process should be diverted to if an error occurs at this state. <br/> <br/> You don't actually have to configure the error node in the restart node's properties. Instead, you can draw a connection from the restart node to the error node and selecting **Set Error Node**. <br/> <br/> You can also set a global node by drawing a connection from the process definition node to another node (either a standard or restart node) and selecting **Set Error Node**.
`maximumNumberOfRestarts` | Determines the amount of times the process can be restarted from this node before it is diverted to an error node.
`overdueNode` | Determines if this standard node is an overdue node or not. For more information, see [Overdue Node](process_definition_components.html#overdue-node). <br/> <br/> You should not define the overdue node in the properties panel. Instead, you can draw a connection from the standard node to the overdue node and select **Set Overdue Node**.
`restartEdge` | Determines where the process should be restarted from, determined by a connection from the restart node to an edge somewhere in the process. <br/> <br/> You do not have to define the restart edge in the properties panel. Instead, you can draw a connection from the restart node to an edge in the process and selecting **Set Restart Edge**.
`state` | Determines the name of the state for the standard node. <br/> <br/> Dataflow engine drives the process by changing the value of the trigger property, as defined in your process entity, until it reaches an end node, or, depending on the process flow, an error node. The state can only be changed to another value if there exists an edge between the two nodes, with one exception: if a global overdue node or error node have been defined, the process can be automatically redirected here without an edge defined between these two nodes. <br/> <br/> This value is automatically defined from the value given when first creating the standard node.
`onEntered` | Instructs the state engine to carry out that functionality when the process enters the state. This property is defined with an instance of a transition processor, created either in a cartridge or scripted in Control Center. <br/> <br/> It is important to note the difference between the `On Entered` and `On Left` properties, because of decoupled interactions, or other workers. This means that between the process entering the current state and leaving it, it is possible that the information contained in the process object may have changed during this period.

### Process Definition Node
The process definition node is, as the name suggests, the node that represents the process definition as a whole.

The node provides an overview of the various elements (nodes and edges) that make up the process flow, as well as the ability to define global settings for the process.

This node is displayed in every process definition, and is automatically shown when you first open a new instance. Selecting this node in the process designer causes its properties to be displayed on the properties panel. They contain a mix of properties that affect the process as a whole, provide the structure of the process, and the most important property, called `trigger`.

The trigger properties defines which property in the Process-derived entity should be monitored by the state engine. When the value of this property is changed, the process is started and driven through the process flow, as defined in the process definition. This property is also used to record the current state of the process according to the property flow defined in the property definition.

Property | Description
--- | ---
`description` | Describes the node.
`name` | Determines the name of the node.
`errorNode` | This defines a global error node that the process should be diverted to if an error occurs during any state without a local error node defined. Because it is defined as global, you do not have to draw any edges from the nodes in your process to the error node. The only connection required is an edge between the process definition node and the error node. <br/> <br/> You should not configure the error node in the process definition node's properties. Instead, you draw a connection from the process definition node to the error node and select **Set Error Node**.
`gracePeriod` | Determines how long the process is allowed to wait at the current state before being moved to the next state (or, depending on the process, to the overdue or error node). <br/> <br/> The entity type `TimeSpan` has two properties: `Unit` and `Value`. The first determines the time unit (seconds, minutes, days, and so on) and the second the value of the time. <br/> <br/> Setting the grace period at the process definition node defines it as a global value, meaning that if not overridden by a grace period on the node itself, this period is used by the state engine for all states within the process definition.
`maximumNumberOfRestarts` | Determines the amount of times the process can be restarted from this node before it is diverted to an error node. <br/> <br/> Setting `maximumNumberOfRestarts` on the process definition node defines it as a global value, meaning that if a local value is not set on a restart node itself, this value is used instead.
`overdueNode` | The overdue node is a restart node where a process is diverted to while it waits for the resumption of the aforementioned process, after the execution of a decoupled interaction, when the grace period elapses. For more information, see [Overdue Node](process_definition_components.html#overdue-node). <br/> <br/> You should not define the overdue node in the properties panel. Instead, you can draw a connection from the process definition node to the overdue node and select **Set Overdue Node**. <br/> <br/> Defining an overdue node at the process definition node means it is defined as a global overdue node, allowing any node not already defined with a local overdue node to be redirected here.
`trigger` | Defines the property (this can be a property of any type) in your process-derived entity – this is the entity that contains the information required by the process and is derived from the process-derived entity – that the state engine should monitor. <br/> <br/> When a change occurs in this property, the dataflow engine takes the appropriate action, and through constant changes to the trigger property, drives the process through the process definition.
`userInteraction` | Defines the user interaction switch. <br/> <br/> For more information, see [Overdue Node](process_definition_components.html#decoupled-interactions)
`elements` | Contains all the elements that are contained in the process as a whole. <br/> <br/> You do not have to configure this Set property yourself, since any elements (nodes or edges) are automatically added to this set as you draw them.
`metaData` | Contains all <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a> assigned to the process definition. <br/> <br/> For more information on metadata, see [Metadata](general_metadata_properties.html)
`onTransit` | Instructs the dataflow engine to carry out that functionality when the process is on transit from one node to another, that is, traveling along a edge. This property is defined with an instance of a transition processor, created either in a cartridge or scripted in Control Center. <br/> <br/> Setting the `onTransit` property in the process definition node means that the transition processor defined is executed on every edge traversed.
`workers` | Defines the workers assigned to the process definition. <br/> <br/> For more information, see [Overdue Node](process_definition_components.html#workers).

### Initial Node
The initial node appears is the entry point of the process.

Ot appears after the first time that you create a node, which can be either a standard or restart node. The first time you create a new node in an empty process, the initial node appears with a standard edge connecting it to the newly created node. This means that the first node you create is considered the first state in the process.

It is possible to delete the connection between the initial node and your node by selecting the standard edge and clicking **Remove**. However, be aware that for a process to be considered valid, there must be a connection leading from the initial node to first node in the process.

The initial node is itself a standard node, meaning that you can click it to display its properties in the Property Panel. But because this node is only there to provide a lead-in to the process, and not part of the process itself, there are no property values designed.

### End Node
An end node in a process definition is a terminating point for the process.

It means that process is considered finished and the value of the activity property of the process-derived entity is changed from `Processing` to `Ended`. Depending on the amount of conditional edges, and the design of the process itself, there can be several end nodes, including nodes that are set as error nodes.

{%include note.html content="Generally, a  node is considered an end node if the node has no edges leaving away from it."%}

An end node is set automatically, meaning that you do not have to configure anything so long as there are no edges leaving the node. The end node is represented in the process designer with a circle with a horizontal line inside.

### Overdue Node
The overdue node is a restart node where a process is routed to while waiting for the completion of an action, for example, a decoupled interaction or another process.

The process is redirected to the overdue node as the grace period set either locally or globally has elapsed, and the value of the activity property in the process entity is changed from `Processing` to `Waiting`. The process will not leave the overdue state until this property value has been changed back to `Processing`.

The overdue node is created by drawing an edge from a node to a restart node and selecting **Set overdue node**. An overdue edge will be created, signified by a blue dotted line connecting the two nodes.

It can be set either globally or locally, meaning that you can have specific overdue nodes attached to main nodes in the process, as well as a global overdue node attached to the process definition node. The difference between the two is the scope of the overdue node. While a global node receives overdue processes regardless of their previous state, a local overdue node only receives overdue processes from the node which it is connected to via an overdue edge.

As the overdue node is a restart node, refer to the [Restart Node](process_definition_components.html#restart-node) section for details of its properties.

### Error Node
The error node is a node where a process is routed to when an exception, or an error, it thrown in the process.

There are two types of error that can occur during the execution of a process:
* programming error
* business logic error.

The error node is designed to handle programming errors, in the case that an exception is thrown due to some unforeseen event, for example null pointers in processor code, processor not reachable, error in RPC calls, and so on.

{%include note.html content="Any error in business logic should be handled in the process flow, through the redesign of the process to handle these situations."%}

The error node itself can be either a standard node or restart node. Depending on the type and design of your process flow, the error node will either:
* have the ability to restart the process if defined using the restart node, or
* will be considered an end node if defined using a standard node.

If the error node is a restart node the process property activity will be changed from `Processing` to `Waiting`; however, if the error node is a standard node, and has no further edges, that is, is an end node, the process property activity is changed from `Processing` to `Ended`.

The error node is created by drawing an edge from a node to it and selecting **Set error node**. An error edge is created, signified by a red dotted line connecting the two nodes.

It can be set either globally or locally, meaning that you can have specific error nodes attached to main nodes in the process, as well as a global overdue node attached to the process definition node. The difference between the two is the scope of the error node. While a global node receives overdue processes regardless of their previous state, a local error node will only receive error processes from the node which it is connected to via an error edge.

As the error node can be a restart or a standard node, refer to the [Standard Node](process_definition_components.html#standard-node) and [Restart Node](process_definition_components.html#restart-node) sections for details of its properties.


## Edges
An edge is used to connect one node to another in the process designer.

Edges define the process flow of a process, allowing the dataflow engine to drive the process from one state to another. If the dataflow engine attempts to move the process from one state to another that is not connected by an edge, an exception is thrown. Thus edges are important in allowing the process to be carried out correctly from beginning to end.

An edge also contains a property, `On Transit`, that can be assigned a transition property, allowing you to develop an implementation that will be carried out as the process is traversing the edge. Process designer contains several different types of edges:
* [standard edge](process_definition_components.html#standard-edge)
* [conditional edge](process_definition_components.html#conditional-edge)
* [overdue edge](process_definition_components.html#overdue-edge)
* [error edge](process_definition_components.html#error-edge)

{%include note.html content="An edge is drawn by clicking the **Connect** button so that it is highlighted in orange, clicking and holding down the mouse button on the first node before dragging the resulting line over the second node. The second node will then display four sections, each representing a type of node or edge. Letting go of the mouse button creates the corresponding edge type."%}

### Standard Edge
A standard edge connects two nodes together in a one-to-one relationship. A standard node can connect only one single node to another single node.

The standard edge has a property called `On Transit` that allows you to assign a transition processor, either scripted or developed in a cartridge, to it. It is possible through the use of the processor, therefore, to develop an implementation that is executed when the process traverses along the standard edge. If no processor is provided, the process continues from one state to the next without stopping.

Property | Description
--- | ---
`name` | Determines the name of the edge.
`description` | Describes the edge.
`errorNode` | Defines the error node that the process should be diverted to if an error occurs during this edge's transition. <br/> <br/> This is akin to a local node, since only an error produced on this edge will be rerouted there. You can also set a global node by drawing a connection from the process definition node to another node (either a standard or restart node) and selecting **Set error node**. <br/> <br/> Unlike on nodes, where you can draw the edge to the error node, you must assign the standard edge with the error node using the Properties Panel.
`from` | Defines the node from where the standard edge originates. <br/> <br/> This does not have to be defined using the property panel, as it is automatically defined when the standard edge is drawn from one node to another.
`overdueNode` | The overdue node is a restart node where a process is diverted to while it waits for the resumption of the aforementioned process, after the execution of a decoupled interaction, when the grace period elapses. For more information, see [Overdue Node](process_definition_components.html#overdue-node). <br/> <br/> Set on an standard edge,  this is akin to a local node since it is only on the transition of this edge that the process is rerouted to the defined overdue node. You can define a global overdue node by assigning an overdue node to the process definition node. <br/> <br/> Unlike in nodes, where you can draw the edge to the error node, you must assign the standard edge with the overdue node using the Properties Panel.
`to` | Defines the node to which the standard edge travels. <br/> <br/> This does not have to be defined using the property panel, as it is automatically defined when the standard edge is drawn from one node to another.
`onTransit` | Instructs the dataflow engine to carry out some functionality when the process is on transit to another node using this edge. This property is defined with an instance of a transition processor, created either in a cartridge or scripted in Control Center. <br/> <br/> Setting the `onTransit` property in the process definition node means that the transition processor defined is executed on every edge traversed.

### Conditional Edge
The conditional node allows you to create one-to-many relationships in the process flow.

This means you can create multiple pathways leading from one node so that you can have different branches for the process. The conditional edge has a property `condition` that allows you to assign a condition processor to it. The processor returns a `true` or `false` value, according to the logic programmed there, letting the state engine decide whether it should traverse the edge or not.

When first creating a conditional edge, it will be labeled `default`. This label is changed to `else` on both conditional edges when a second edge is created, with both coming from the same node. You can then add a condition processor to a conditional edge by using its `condition` property. For every set of conditional edges, it is possible to leave one undefined, remembering that you must have at least two edges to correctly define a conditional transition, so that if all other condition processors return `false`, it will automatically follow this undefined conditional edge, which is always labeled `else`.

The conditional edge also has a property called `onTransit` that allows you to assign a transition processor, either scripted or developed in a cartridge, to it. It is possible through the use of the processor, therefore, to develop an implementation that will be executed when the process traverses along the conditional edge. The transition processor is only executed after the corresponding condition processor returns `true` to the dataflow engine, thus the edge will be traversed.

You can create as many conditional edges leading from a common node as you like.

{%include note.html content="If you decided that all conditional edges created should be defined with a condition processor and all return `false`, the state engine will transition to the overdue node, since there are no valid pathways available for your process."%}

With the **Select** button clicked, clicking on a conditional edge displays its properties in the Properties Panel – you must click on the arrow, not on the label. There are various properties that can be configured for the conditional edge, most of them configurable in the process designer itself, without having to set them manually. However, there are also some that must be configured in the Properties Panel.

Property | Description
--- | ---
`name` | Determines the name of the edge.
`description` | Describes the edge.
`condition` | Defines a condition processor used for this conditional edge. <br/> <br/> The processor can be developed in a cartridge or scripted in Control Center, and its logic returns either `true` or `false`. Depending on this returned value, the edge is used for the transition; that is, the dataflow engine decides which state should follow in the process flow according to the returned value. If the returned value is `false`, it evaluates the next condition in the set.
`errorNode` | Defines the error node that the process should be diverted to if an error occurs during this edge's transition. <br/> <br/> This is akin to a local node, since only an error produced on this edge will be rerouted there. You can also set a global node by drawing a connection from the process definition node to another node (either a standard or restart node) and selecting **Set error node**. <br/> <br/> Unlike on nodes, where you can draw the edge to the error node, you must assign the standard edge with the error node using the Properties Panel.
`from` | Defines the node from where this edge originates. <br/> <br/> This does not have to be defined using the property panel, as it is automatically defined when the edge is drawn from one node to another.
`overdueNode` | The overdue node is a restart node where a process is diverted to while it waits for the resumption of the aforementioned process, after the execution of a decoupled interaction, when the grace period elapses. For more information, see [Overdue Node](process_definition_components.html#overdue-node). <br/> <br/> Set on an conditional edge, this is akin to a local node since it is only on the transition of this edge that the process is rerouted to the defined overdue node. You can define a global overdue node by assigning an overdue node to the process definition node. <br/> <br/> Unlike in nodes, where you can draw the edge to the error node, you must assign the conditional edge with the overdue node using the Properties Panel.
`to` | Defines the node to which the edge travels. <br/> <br/> This does not have to be defined using the property panel, as it is automatically defined when the edge is drawn from one node to another.
`onTransit` | Instructs the dataflow engine to carry out some functionality when the process is on transit to another node using this edge. This property is defined with an instance of a transition processor, created either in a cartridge or scripted in Control Center. <br/> <br/> Setting the `onTransit` property in the process definition node means that the transition processor defined is executed on every edge traversed.

### Overdue Edge
An overdue edge is used to represent the relationship between two nodes, one of which has been designated an overdue node.

The overdue node is a node where a process can be directed to when the `activity` property in the process entity is changed to `Waiting`. There are two types of overdue node:
* global
* local

{%include tip.html content="For more information, see [Overdue Node](process_definition_components.html#overdue-node)."%}

The overdue node is created by drawing an edge from a node to a restart node and selecting set overdue node. The creation of a local node is achieved through the drawing of this connection to a node in the process flow to another, while a global node is created by drawing this connection from the process definition node to the designated overdue node.

The relationship is defined by a blue dotted line joining the two nodes. There are no properties contained on this edge, and it is used merely to signify the relationship between the two nodes.

### Error Edge
An error edge is used to represent the relationship between two nodes, one of which has been designated an error node.

The error node is a node where a process can be directed when an error is thrown in the state engine. This can be an exception thrown because of the business logic, for example when a name is spelled incorrectly, or because a process has a bug, for example. There are two types of error node:
* global
* local.

{%include tip.html content="For more information, see [Error Node](process_definition_components.html#error-node)."%}

The error node is created by drawing an edge from a node to a second node and selecting set error node. The creation of a local node is achieved through the drawing of this connection to a node in the process flow to another, while a global node is created by drawing this connection from the process definition node to the designated error node.

The relationship is defined by a red dotted line joining the two nodes. There are no properties contained on this edge, and it is used merely to signify the relationship between the two nodes.

## Processors
There are two types of processors that the state engine can execute during a process:
* [Transition Processor](process_definition_components.html#transition-processor)
* [Condition](process_definition_components.html#condition)

They can be developed in a cartridge or scripted in Control Center, before being assigned to the process flow using the process designer. The transition processor is used on nodes and edges to carry out some functionality on the process entity, and its related objects, while condition is used to check the current process against a conditional statement and return a Boolean value. It allows the dataflow engine to decide which edge should be traversed in a set of conditional edges.

Both the transition and condition processors are of the type `Deployable`, meaning that after creating a new instance, you must deploy it before they can be executed.

### Transition Processor
The transition processor is used to manipulate the process entity or objects belonging to it when it reaches a configured node or edge.

You can develop a transition processor in a cartridge or by scripting one directly in Control Center. All implementations of this processor are based on the entity type `TransitionProcessor`.

The processor has an associated context that provides information on the process entity, and is the same regardless of whether you have a cartridge- or script-developed processor, called `TransitionProcessorContext`. The only difference is in how it is used. The cartridge provides the context object as part of its `process()` method, while in the script (either in JavaScript or BeanShell) the context is accessed by using the dollar sign `$`, for example: `process = $.getProcess();`.

One of the transition processors responsibilities is to indicate to the state engine to drive the current process to the next state. This is achieved through the use of the `continueWithState(Object value)` method in the processor, for example: `continueWithState("validate");`

The method accepts either string or enum types as a value, and instructs the state engine of the next state of the process. Note, however, that the next state should be valid state transition – only nodes (that represent states in the process definition) that are connected by edges are considered valid. If you try to move from one state to another that is not valid (that is, not connected) the state engine throws an exception and the process is moved to an error node if one is defined. If not, the process remains in the current state and the process ends.

{% include tip.html content="For more information on creating transition processors, see [Creating a Scripted Transition Processor](creating_a_scripted_transition_processor.html)."%}

You can assign a transition processor to the following components:
* node:
  * `onEntered` list property
  * `onLeft` list property
* edge:
  * `onTransit` list property

{% include note.html content="You can assign more than one item to list properties, which means you can have more than one processors assigned to a node or an edge."%}


### Condition
A condition is used to determine the progression of the process when a node has several conditional edges leading from it.

You can develop a condition in a cartridge or by scripting one in Control Center. All implementations of this processor are based on the entity type `ConditionProcessor`.

The condition has an associated context that provides the process object, and is the same regardless of whether you have a cartridge- or script-developed one, called `ConditionProcessorContext`. The only difference is how it is used. The cartridge provides the context object as part of its `matches()` method. This method is used to compare a condition statement before returning a Boolean value according to the result of the comparison. This determines whether the conditional edge should be traversed or not. The scripted processor shares the same context, and is used in the same way, except that it is accessed using the dollar sign `$`, for example: `subject = $.getSubject();`.

Conditions are executed after transition processors and only if the process is still in the same state. That means, if a transition processor changes the state, the conditions of the previous are skipped.

{% include tip.html content="For more information on creating conditions, see [Creating a Scripted Condition](creating_a_scripted_condition.html)."%}

You can assign a transition processor to the following components:
* conditional edge:
  * `condition` property

{%include note.html content="You don't need to define a return statement in a condition. You only need to define the conditional statement."%}

## Workers
{% include worker_include.md %}

## Decoupled Interactions
A decoupled interaction is an action that is executed outside of the scope of the dataflow engine itself. Until the decoupled interaction is completed, the process is stopped.

There are several types of decoupled interactions:
* user interaction
* [worker](process_definition_components.html#workers)
* [process definition](dataflow_engine_components.html#process-definiton)

You can assign a decoupled interaction to the following components:
* conditional edge:
  * `condition` property

You can add one of two types of objects to this property:

Type | Description
--- | ---
Decoupled interaction | Contains properties for configuring a worker or a user interaction. <br/> <br/> - A user interaction stops the process and waits until a user has manually restarted the process. Once a user interaction has been defined, a user icon appears next to the node which it has been assigned to. This means that when the process enters a state with a user interaction defined, the property `activity` in the process-derived entity is changed to `waiting`. It remains in this state until the property is manually switched back to `processing`. <br/> <br/> - A worker can be any action that is executed outside the process, for example, a folder watcher that imports new documents for new process instances. Once assigned to a standard node, an icon appears next to the node to indicate that the worker had been set. 
Process definition | Allows you to execute a second process while the first waits.
