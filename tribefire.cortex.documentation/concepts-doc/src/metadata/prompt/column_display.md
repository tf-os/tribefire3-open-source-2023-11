# ColumnDisplay

This metadata allows you to configure the columns definition of an entity while displaying it with a list view.

Metadata Property Name  | Type Signature  
------- | -----------
`ColumnDisplay` | `com.braintribe.model.meta.data.prompt.ColumnDisplay`

## General

If the ColumnDisplay is set to an entity, then we will use the configured aspects to address how the property columns of that entity are displayed within a list view.

The configuration aspects are:

**displayNode** - if true, then the node element within the list view is displayed. If false, it is hidden;

**nodeWidth** - the width of the node column;

**nodeTitle** - the `LocalizedString` display text to be used as the header of the node column;

**preventSingleEntryExpand** - when we have only one entry available in a list view, that entry is automatically expanded. This setting, when using false, prevents that default behaviour. A single entry will not be automatically expanded;

**disableExpansion** - if this is set, then when displaying the results in a list view, the user will not have the option to expand any entries. Of course, if this is true, then the *preventSingleEntryExpand* will have no effect;

**displayPaths** - a list of columns which should be visible for the parent entity. If this is empty, then all columns are shown. Despite the visibility of columns, other things can be configured for each column. Those are the configuration aspects for each column:

*path* - the path (property name) of the column to be visible;

*declaringTypeSignature* - the type signature of the entity where the property is declared;

*width* - the width of the column in the list view;

*title* - the `LocalizedString` display text to be used as the header of the column;
