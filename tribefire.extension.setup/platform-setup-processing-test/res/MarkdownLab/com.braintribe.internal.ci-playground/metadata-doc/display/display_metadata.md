# Display Metadata

The Display package gathers together all metadata that in some way alters the way the model components are displayed in Control Center or Explorer.

## General

This package is similar to another metadata package [Prompt Metadata](../PROMPT/prompt_metadata.md). However, the Display package is concerned with how the components are displayed in tribefire – for example, the DisplayInfo can be used to change programing-centric names into a more readable variation, such as `firstName` to First Name – whereas the Prompt package is concerned with how the values of the components are handled in tribefire. Priority, for example, alters the order of properties.

The metadata based on DisplayInfo are used to determine how the components' names should be displayed in tribefire – as shown above, DisplayInfo can be used to display a property name as First Name, rather than firstName.

## Available Metadata

Name    | Description  
------- | -----------
SortDirection | Enum. Available options are ascending and descending
[Bulleting](bulleting.md) |  This metadata property allows you to configure whether properties of a list type are displayed in a numbered list.
[DefaultSort](defaultsort.md) | You can use this metadata to configure which column is sorted by default.
[DisplayInfo](displayinfo.md) | You can use this metadata to configure the name, description, and icon of an element at once.
[Emphasized](emphasized.md) | This metadata property allows you to configure the emphasis of each instance when displayed in Explorer.
[GroupAssignment](groupassignment.md) | This metadata allows you to assign different properties to a group.
[GroupPriority](grouppriority.md) | This metadata allows you to assign different properties to a group.
[Icon](icon.md) | This metadata allows you to change the model's icon.
[ModelDisplayInfo](modeldisplayinfo.md) | This metadata allows you to configure how the model should be labeled. 
[SelectiveInformation](selectiveinformation.md) | This metadata allows you to configure what is displayed in the first column of each entity type instance in tribefire Explorer.
