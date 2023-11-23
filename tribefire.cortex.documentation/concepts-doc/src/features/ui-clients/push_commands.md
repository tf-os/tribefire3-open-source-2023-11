# UI Push Commands

The Explorer UI may receive push notifications to, for example,  display messages to the users.
Besides messages, the client may receive also the so called `Commands` to be executed. These commands perform well know operations, and those operations, as well as all the available commands, will be described here.

## ApplyManipulation

The ApplyManipulation (`com.braintribe.model.uicommand.ApplyManipulation`) is a Command which contains a Manipulation to be applied in the client.

It has a single configuration property, which is:

**manipulation** - the manipulation to be applied in the client;


## CloseUrl

The CloseUrl (`com.braintribe.model.uicommand.CloseUrl`) is a Command which closes a tab which contains an URL previously openned via an inline GotoUrl.

It has a single configuration property, which is:

**name** - the name of the tab which you want to close. It must have the same name of the one used in the GotoUrl command;


## DownloadResource

The DownloadResource (`com.braintribe.model.uicommand.DownloadResource`) is a Command which contains a List of Resources that will be presented to the users, so they can download those Resources.

It has a single configuration property, which is:

**resources** - the list of Resources to be downloaded;


## GotoModelPath

The GotoModelPath (`com.braintribe.model.uicommand.GotoModelPath`) is a Command which contains a ModelPath to be openned in a view by the client.

Those are the configuration properties it contains:

**path** - the path which will be run/fired;

**openWithActionElements** - Collection of elements for which Action is triggered. May be null/empty;

**selectedElement** - Which element should be selected. If null, latest one is selected;

**addToCurrentView** - if true, opens in the current view as a new Tether element. If false opens in a new tab;

**showFullModelPath** - if true, all elements are showed in the Tether. If false, only the last one is showed;


## GotoUrl

The GotoUrl (`com.braintribe.model.uicommand.GotoUrl`) is a Command which contains an URL to be openned by the client.

Those are the configuration properties it contains:

**url** - the URL which will be openned by the command expert;

**target** - it is configured with some of the well known targets. Those are:

inline - the URL is opened inside GME, in a new tab;

_blank - the URL is opened in a new browser window. This is the default behaviour done when no target is set at all;

Notice that if any other target is set, that target is set to the js window.open command.

## PreviewOpener & PreviewOpenerById

The PreviewOpener (`com.braintribe.model.uicommand.PreviewOpener`) and PreviewOpenerById (`com.braintribe.model.uicommand.PreviewOpenerById`) are commands which contains either a GenericEntity (content) or the documentId for being displayed by a preview UI. Those are the configuration properties it contains:

**content** - the content to be previewed;

**documentId** - the documentId of the content to be previewed;

**title** - the title to be used in the preview UI. If a content is set, then the title is the selective information of the content;

**width and height** - dimensions of the preview window;

**keyConfiguration** - the key configuration used for hiding the preview window;

## Refresh

The Refresh (`com.braintribe.model.uicommand.Refresh`) is a Command which reloads an entity from the session. The configurable propery it contains is:

**referenceToRefresh** - the reference to the entity which is going to be refreshed;

## RefreshPreview ##

The RefreshPreview (`com.braintribe.model.uicommand.RefreshPreview`) is a Command which refreshes a preview representation of a content. Those are the configuration properties it contains:

**typeSignature** - the typeSignature of the content;

**entityId** - the Id of the content;

## ReloadView ##

The ReloadView (`com.braintribe.model.uicommand.ReloadView`) is a Command which reloads (by redoing a query or service request) the current view. Those are the configuration properties it contains:

**reloadAll** - if true, then not only the current view is reloaded, but all reloadable views are reloaded once they are activated;

## RunQuery and RunQueryString ##

The RunQuery (`com.braintribe.model.uicommand.RunQuery`) and RunQueryString (`com.braintribe.model.uicommand.RunQueryString`) are commands which contains a query to be executed, and the result of such query is displayed in a new GME tab. The difference between them is that the first one receives a query instance, and the second one receives a query as string.

## RunWorkbenchAction ##

The RunWorkbenchAction (`com.braintribe.model.uicommand.RunWorkbenchAction`) is a Command which executes a WorkbenchAction which is the content of the configured workbench folder id.

## UpdateWorkbenchFolderDisplay ##

The UpdateWorkbenchFolderDisplay (`com.braintribe.model.uicommand.UpdateWorkbenchFolderDisplay`) is a Command which updates a workbench folder display information. The configuration aspects of it are:

**folderId** - the id of the folder to have its display updated;

**replacement** - the new string of the folder display;

**matchPattern** - the pattern (regex) to be matched in the current display text, and which will be replaced by the replacement;
