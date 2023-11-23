# Control Center
>Control center functions as an administrative tool for the deployment and configuration of your tribefire platform.

## General
Control Center is an administration client that is used to configure your instance of tribefire. The GUI is the main hub when interacting with the platform and can be used to configure the different components that make up your instance of tribefire, such as installing cartridges, creating and deploying <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.deployable}}">deployables</a>, modeling, setting security configuration, and so on.

{%include tip.html content="For information on how to log in to Control Center see [Accessing Control Center](accessing_control_center.html)"%}

The client is used to view the different system accesses – system configurations including the main **Cortex** access, along with accesses for user-sessions and statistics, audit trails, and the standard authentication accesses.

It functions by displaying the different system accesses which are then used to administer tribefire. There are several different <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.access}}">accesses</a> which can be viewed by Control Center, but the basic display of the client never changes. The default access, that is, the open is first displayed on opening, is  **Cortex**, the main access for tribefire administration.

{% include image.html file="controlCenter.png" max-width=600 %}
<br>
The left-hand side of the client displays the links to the different components that are being configured, while in the main area of the screen it shows the different items for each section in context.

Control Center is fully customizable through the use of <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.workbench}}">workbenches</a>, and can be configured to behave in any way you require.

### Packaging
Control Center is delivered as part of the standard installation of tribefire and deployed as a `.war` to the J2EE container, alongside the core system, tribefire Services. Once deployed, no further configuration is required and you can access Control Center at `https://localhost:8080/tribefire-control-center/`

## Cortex Access in Control Center

The primary access and the default access shown after logging into the Control Center, the Cortex access is the GUI representation of the Cortex database, which contains the system configuration. Via the client, all system and cartridge related items – access, processors, models, actions, and so on – can be created, configured, and deployed.

The Cortex access has its own links, displayed on the left-hand side of the client, for ease of use. Each link relates to a configurable component in the Cortex access.

### Other Accesses in Control Center

Alongside the Cortex access, there are also a set of other system accesses – databases relating to a different area of concern in the tribefire system. They can be accessed by using the **Switch to** function.

Included are accesses that allow:
* control of default user authentication and authorization user registry, where administrators can assign roles and groups for each user,
* displaying information of user sessions, statistics, and an audit trail, showing all manipulations of the Cortex database

## Workbench

Workbench in the context of tribefire is a concept that allows you to design and modify the way the different accesses are displayed in Control Center and Explorer. It enables uses to create their own custom queries and actions to be displayed in the client. The implementation of the actions can be developed in a cartridge and then configured using the workbench.

Workbench is made up of two elements, the workbench model and the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.workbench_access}}">workbench access</a>. The access is assigned to the main access it is associated with, i.e. the access the workbench is used to design. Switching to this workbench access, in either Control Center or Explorer (depending if you are customizing a system or a custom access), allows the customization of these elements.

You can easily add an access to a workbench by using the **Create Workbench Access** action on the appropriate access.

### Workbench Model and Workbench Access

As with every concept in tribefire, the workbench is modeled. This model describes the elements required when customizing the design of an access, such as Folder, Query, Action entities and so on. There is one standard workbench model, which is delivered as part of the core tribefire platform and is considered one of its base models. This model can be used every time a new workbench access is required.

As part of the design pattern of tribefire, every model requires a corresponding access for actual use in the platform. The model provides the structure for the data and the access is the implementation that stores or accesses that data. This is the same for workbench, the model describes the structures required for a workbench and the access stores that actual instances of these structure.

When a new access requires a workbench, a new instance of the workbench access is created. It is a base <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.smood}}">SMOOD</a> access associated with the workbench model. This is done automatically through the **Create Workbench Access** action.

### Workbench Configuration

On 'Switching to' to the workbench access, the client provides a GUI for the configuration of workbench, including links for the main components of any workbench configuration, such as actions, queries and folders. This can be used to design the main access.
