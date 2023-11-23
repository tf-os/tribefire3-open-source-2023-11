# Prompt Metadata

The package Prompt controls how UI elements are displayed in Control Center or Explorer.

## General

In contrast to the [Display](../display/display_metadata.md) package, which concentrates on how the models components are displayed, this package concentrates on the way each instance of a component is presented.

One example of a metadata belonging to Prompt is Confidential, which handles a property like a password, meaning that its values are hidden and replaced by stars.

There are two base entities which are used in Prompting: `EntityPrompting` and `PropertyPrompting`. Each extends (are subtypes of) their respective component metadata, `EntityMetadata` and `PropertyMetadata` respectively, and are the super type for all Prompt metadata in this package.
>The metadata in this package only affect entities and properties.

## Available Metadata

Name    | Description  
------- | -----------
[Auto Commit and ManualCommit](auto_commit.md) | These metadata allow you to configure whether a commit is automatically performed after manipulations are performed.
[Auto Expand](auto_expand.md) | This metadata allows you to configure whether the nodes within the list view will be automatically expanded or not.
[Auto Paging Size](auto_paging.md) | This metadata allows you to configure the auto paging size when fetching queries from the services side.
[Column Display](column_display.md) | This metadata allows you to configure the columns definition of an entity while displaying it with a list view.
[Condensed](condensed.md) |  This metadata allows you to configure how a property within an entity behaves when collapsed.
[Confidential and NonConfidential](confidential.md) | These metadata allow you to configure whether the property value is hidden.
[Default Navigation](default_navigation.md) | This metadata allows you to configure which property is displayed by default when opening an entity in Explorer.
[Default View](default_view.md) | This metadata allows you to configure which view is the preferred one to be used for opening a given type in Explorer.
[Description](description.md) | This metadata allows you to configure the entity's description.
[Details View Mode](details_view_mode.md) | This metadata allows you to configure the details panel visibility.
[Embedded](embedded.md) | This metadata allows you to embed properties into their parent entity type.
[Entity Compound Viewing](entity_compound.md) | Using this metadata, you can display properties belonging to a complex type.
[Name](name.md) | This metadata allows you to configure how the entity is labeled.
[Inline](inline.md) | This metadata property allows you to configure whether the complex properties of an entity type are displayed inline.
[Priority](priority.md) | This metadata property allows you to configure the priority of the element it has been assigned to.
[Show as List](show_as_list.md) | This metadata property allows you to configure how maps should be handled, either as a traditional map or as a list.
[Singleton](singleton.md) | This metadata allows you to configure whether the Query Panel is displayed in tribefire Explorer.
[Time Zoneless](time_zoneless.md) | This metadata allows you to configure a date property to be formatted and inputted at UTC timezone.
[Virtual Enum](virtual_enum.md) | This metadata allows you to configure a property as if it was an enum property.
[Visible and Hidden](visible.md) | These metadata allow you to configure the visibility of the entity type they are assigned to.
