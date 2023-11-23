# UI Elements
>There are several components that make up the construction of Control Center/Explorer.

## General
<a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a> and <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.explorer}}">Explorer</a> are constructed of the following components:
* GIMA
* Selection
* Thumbnail
* Assembly
* Property
* Quick Access
* Automatic Queries
* Global Actions
* Metadata Editor
* Select Result Panel
* Service Request Panel

## GIMA
The GIMA component, which stands for Guided Instantiation and Manipulation Assistant, is the UI which is displayed when you create a new <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_instance}}">entity instance</a>, or also when you edit that instance. It allows you to define or change the properties of the entity.

{% include image.html file="Gima.png" max-width=600 %}

## Selection
The selection component, also known as the Selection Constellation, is the UI you use to add new or existing elements to properties. It allows you to select a existing instance or create a new one based on an entity type. The selection constellation also allows you different views that can be used.

You can choose between the different views by clicking on one of the buttons displayed at the top of the Selection Constellation UI.

{% include image.html file="Selection04.png" max-width=600 %}

* Clicking {% include inline_image.html file="HomeIcon.png" max-width=600 %} displays the home screen in Selection Constellation.
* Clicking {% include inline_image.html file="QuickAccessIcon.png" max-width=600 %} displays the Quick Access component in Selection Constellation, allowing you to choose between an existing instance of an entity or creation of a new one based on an entity type.
* Clicking {% include inline_image.html file="ChangesIcon.png" max-width=600 %} displays the changes screen in the Selection Constellation. This shows all changes that have been made to tribefire which have yet to be persisted.
* Clicking {% include inline_image.html file="ClipboardIcon.png" max-width=600 %} displays the Clipboard screen in the Selection Constellation.
* Clicking {% include inline_image.html file="WorkBenchIcon.png" max-width=600 %} displays the Workbench in the Selection Constellation.
* Clicking {% include inline_image.html file="QueryIcon.png" max-width=600 %} displays a query based on the type required. That is, it displays all entities of a particular type required for selection.

## Thumbnail
The thumbnail UI is the section which displays icons as opposed to details after selecting thumbnail view.

## Assembly Panel
The assembly panel refers to the main section of the tribefire. This is where the results of any query executed are displayed.

{% include image.html file="assembly01.png" max-width=600 %}

## Property Panel
The property panel refers to the panel displayed at the right-hand side of Control Center/Explorer which shows details of the currently selected entity.

You can display or hide this panel by clicking the Hide/Show Details button found at the bottom of tribefire. The property panel displays different information depending on the entity type. You can also define groups and assign properties to them to order how the properties are displayed in the property panel.

{% include image.html file="PropertyPanel01.png" max-width=600 %}

## Quick Access
The QuickAccess component refers to the panel which is displayed at the left-hand side of the screen. It displays the individual instances of entity and the entity types of the model.

{% include image.html file="quickAccessPanel.png" max-width=600 %}

## Automatic Queries
The Automatic Queries is a <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.workbench}}">Workbench</a> component. It is used to control how the Workbench component should function if it has not yet been configured. That is, if no workbench has been deployed, or a deployed Workbench has no folders defined. If this is the case, tribefire automatically creates folders, which are then displayed in the left-hand panel, and their associated queries.

{% include image.html file="automaticQueryExample.png" max-width=600 %}

## Global Actions
The global actions panel (also known as Action Bar) allows you to create and upload resources, redo and undo, commit your changes, create a workbench access, and many more.

{% include image.html file="image2017-8-8 9-41-14.png" max-width=600 %}

## Metadata Editor
The metadata editor allows you to perform actions on <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a>. You can open the editor by double-clicking an entity type.

{% include image.html file="image2017-8-8 9-51-16.png" max-width=600 %}

## Select Result Panel
The select result panel is displayed when you perform select queries in Explorer.

{% include image.html file="image2017-8-8 10-32-40.png" max-width=600 %}


## Service Request Panel
The service request panel is a panel displayed when you double-click a Service Request in the Quick Access search box.

{% include image.html file="image2017-8-8 11-1-11.png" max-width=600 %}
