# plugins (NG suite, or 4.x)

The current suite of plugins for Eclipse consists of the following plugins: 

## Devrock
The [Devrock plugin](asset://com.braintribe.devrock.eclipse:devrock-documentation/devrock.md) is the main plugin. It is always required in order for the other plugins to work. Basically, it contains services for the other plugins and features that aren't related to any specific plugin. Also, all configuration UI is handled by it. 

## Artifact Container
The [Artifact Container Plugin](asset://com.braintribe.devrock.eclipse:artifact-container-ng-documentation/artifact-container.md) is the plugin that implements the dynamic classpath containers for an Eclipse project. 

## Model Builder
The [Model Builder Plugin](asset://com.braintribe.devrock.eclipse:model-builder-ng-documentation/model-builder.md) is the plugin that intercepts changes on model-type artifacts and builds its 'model-declaration.xml' file.

## Debug Module Builder
The [Debug Module Plugin](asset://com.braintribe.devrock.eclipse:debug-module-builder-ng-documentation/debug-module-builder.md) is the plugin that intercepts debug launches on debug modules and injects project dependencies if matches exist in the workspace.

## Mungo Jerry 
The [Mungo Jerry Plugin](asset://com.braintribe.devrock.eclipse:mungo-jerry-ng-documentation/mungo-jerry.md) is a plugin that features analysis functions for GWT projects. 

## Greyface
The [Greyface plugin](asset://com.braintribe.devrock.eclipse:greyface-documentation/greyface.md) is a plugin that can scan external repositories (such as Maven Central) for specified artifacts and import the found and selected artifacts into another repository (ours for instance).

## Debug Launcher
This is the only plugin that hasn't been replaced. It's used to allow for classpaths that are too long to have them declared as a String. 

## artifact reflection builder
 Once the authors of this builder do provide documentation, it will also be accessible from here. For now, no information is avaiable, so I can't tell you more that is a builder that needs to run before JDT's java builder runs. 



# common features

## logging 

Logging has improved, all plugins can either be parameterized with a common or specific logger.properties file. All plugins will look for the properties file at start-up.

Logging is however absolutely standard, no changes there whatsoever. 

The main packages are 
        
        com.braintribe.devrock.api : devrock's exposed features
        com.braintribe.devrock.bridge.eclipse : devrock's mc-core link
        com.braintribe.devrock.commands : devrock's features
        com.braintribe.devrock.plugin : devrock's main plugin
        com.braintribe.devrock.importer : devrock's QI features 
        com.braintribe.devrock.preferences : devrock's preferences dialogs

        com.braintribe.devrock.mc : all parts of mc-core, aka malaclypse 
        com.braintribe.devrock.ac : artifact-container
        com.braintribe.devrock.mnb : model builder
        com.braintribe.devrock.mj : mungo-jerry
        com.braintribe.devrock.dmb : debug-module builder

The location of these properties file is now expected to be in the 'dev-env' root, if nothing is found there, it will default to the location of the workspace.

### using a common file 
The name of the common file is simply :

     logger.properties


### using a file per plugin
If you want to use a specific file for a plugin, you need to prefix the plugin’s id to the file. 

The pattern is 

    <plugin id>.logger.properties

The ids of the plugins are as follows: 

<table>
	<tr>
		<th>plugin</th>
		<th>id</th>
	</tr>
	<tr>
		<td>Devrock</td>
		<td>com.braintribe.devrock.DevrockPlugin</td>
	</tr>
	<tr>
		<td>Artifact Container</td>
		<td>com.braintribe.devrock.ArtifactContainerPlugin</td>
	</tr>
	<tr>
		<td>Debug Module Builder</td>
		<td>com.braintribe.devrock.DebugModuleBuilderPlugin</td>
	</tr>
	<tr>
		<td>Model Builder</td>
		<td>com.braintribe.devrock.ModelBuilderPlugin</td>
	</tr>
	<tr>
		<td>Mungo Jerry</td>
		<td>com.braintribe.devrock.MungoJerryPlugin</td>
	</tr>
</table>

Hint: if you are specifying a log output file by just its name, it will end up in the directory where your eclipse’s executable lies. 
