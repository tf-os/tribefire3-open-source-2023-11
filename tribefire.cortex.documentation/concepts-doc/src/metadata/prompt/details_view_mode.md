# DetailsViewMode

This metadata allows you to configure the details panel visibility.

Metadata Property Name  | Type Signature  
------- | -----------
`DetailsViewMode` | `com.braintribe.model.meta.data.prompt.DetailsViewMode`

## General

If the DetailsViewMode is configured, then we use the configured mode for choosing whether or not to display the details view by default.
There are 3 different modes available:

**visible** - when this is set, then it works as if there was no metadata configured at all (default behaviour). Which means, the details view panel is shown;

**hidden** - when this is set, then the details view panel is hidden, but we still have the possibility to show it by using the show details action;

**forcedHidden** - when this is set, then the details view panel is hidden, and we do not have the possibility to show it again, which means the show details action is also hidden;

You can attach this metadata to entity types or templates.
