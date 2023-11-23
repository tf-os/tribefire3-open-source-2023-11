# DEVROCK plugin

The Devrock plugin is the main plugin for the [suite](asset://com.braintribe.devrock.eclipse:ng-plugin-documentation/ng-plugins.md). All other plugins require the Devrock plugin to be available in Eclipse. 

Apart from all [configuration](./configuration.md) tasks for the other plugins, it also acts as a bridge to 'malaclypse'. Currently, it sports a 'mc ng'-core, i.e. it contains malaclypse itself. In the future, that might be changed so that it connects to a server (GIN) rather than handling the requests from the other plugins on its own.




## natures
There are a number of well-known natures used throughout the plugins. Natures are Eclipse's way to tag a project so that it can be linked to builders, i.e. experts that can handle the specific build of such projects. THe natures are not exclusive per se, so one project can have multiple such tags.


nature | description 
------- | ----------- 
org.eclipse.jdt.core.javanature | standard JAVA project
net.sf.eclipse.tomcat.tomcatnature | a Tomcat project that is linked to the Tomcat plugin
com.sysdeo.eclipse.tomcat.tomcatnature | a Tomcat project that is linked to the older Sysdeo plugin (equivalent to the other Tomcat nature)
com.braintribe.devrock.artifactcontainer.natures.TribefireServicesNature | a debug module project linked to the [Debug Module Builder](asset://com.braintribe.devrock.eclipse:debug-module-builder-ng-documentation/debug-module-builder.md)
com.braintribe.eclipse.model.nature.ModelNature | a GM model project linked to the [Model Builder](asset://com.braintribe.devrock.eclipse:model-builder-ng-documentation/model-builder.md)
com.braintribe.devrock.mj.natures.GwtTerminalNature | a GWT terminal (use still unclear), linked to [Mungo Jerry](asset://com.braintribe.devrock.eclipse:mungo-jerry-ng-documentation/mungo-jerry.md)
com.braintribe.devrock.mj.natures.GwtLibraryNature | a GWT library (use still unclear), linked to [Mungo Jerry](asset://com.braintribe.devrock.eclipse:mungo-jerry-ng-documentation/mungo-jerry.md)


The associated builders can add and remove natures. Natures are shown by decorators - if applicable - as little icons attached to the projects in the package-explorer. 




