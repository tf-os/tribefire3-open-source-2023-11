# Configuring Workbench

Configuring a workbench access allows you to customize how your access looks like in Explorer.

## General

When you create a workbench access for your access, you can customize what is displayed in Explorer.

> For information on how to create a workbench access, see [Creating a Workbench Access](creating_workbench.md)

## Basic Workbench Configuration

<iframe width="560" height="315" src="https://www.youtube.com/embed/h65myyxxLyY" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>


## View Action Bar Configuration

You can configure the behavior of the view action bar by creating a new perspective for it in the workbench.

> For information on the View Action Bar, see [UI Elements](asset://tribefire.cortex.documentation:concepts-doc/features/ui-clients/ui_elements.md).

To configure the view action bar:

1. Switch to the Workbench access and open the **Perspectives** entry point.
2. Create a new perspective and provide all necessary parameters.
3. In the perspective, define a new folder.
4. In your new folder, define another 3 new folders:

Folder | Description
------  | ----------
View   | name - `$exchangeContentView` </br> displayName - `View` </br> tags - `$condensation`, `$option` </br> The `$condensation` and `$option` tags enable two additional display change options.
Maximize | name - `$maximize` </br> displayName - `Maximize` </br> The Maximize action has two states: `maximize` and `restore`. You can define different icons and display names for both. You can influence the `restore` action by defining it as a subfolder of the `maximize` folder with the name `$restore`.
Show Details | name - `$showDetailsPanel` </br> displayName - `Show Details` </br> The Show Details action has two states: `show details` and `hide details`. You can define different icons and display names for both. You can influence the `hide details` action by defining it as a subfolder of the `show details` folder with the name `$hideDetailsPanel`.

5. Commit your changes and reload the UI client. Your changes should now be visible.