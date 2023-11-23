# Debug Module Builder
The Debug Module Builder is a builder that is attached to Jinni's debug projects. While the part about the Tomcat devloader is handled by the [Artifact Container](asset://com.braintribe.devrock.eclipse:artifact-container-ng-documentation/artifact-container.md), handling the modules within is done by this plugin. 

It reacts on a specific nature attached to the project, and will update some files within the debug project's file system to reflect what jar references among the dependencies can be replaced references to the projects backing the jars.

## nature
In order for the debug module builder to act on a project, the project must have a specific nature. 

```
    com.braintribe.devrock.artifactcontainer.natures.TribefireServicesNature
```
No worries, you don't have to type that in :-)

Both the natures and the linked builders are declared in the .project file of the project.

```xml
	<?xml version="1.0" encoding="UTF-8"?>
	<projectDescription>
		<name>my-demo-project - tribefire.extension.demo</name>
		<comment></comment>
		<projects>
		</projects>
		<buildSpec>
			<buildCommand>
				<name>org.eclipse.jdt.core.javabuilder</name>
				<arguments/>
			</buildCommand>
			<buildCommand>
				<name>com.braintribe.devrock.dmb.builder.DebugModuleBuilder</name>
				<arguments>
				</arguments>
			</buildCommand>
		</buildSpec>
		<natures>
			<nature>com.braintribe.devrock.artifactcontainer.natures.TribefireServicesNature</nature>
			<nature>net.sf.eclipse.tomcat.tomcatnature</nature>
			<nature>org.eclipse.jdt.core.javanature</nature>
		</natures>
	</projectDescription>
```

Of course, a debug module has two other natures - the Java nature (which makes it a JDT project), the Tomcat nature (see the pertinent section in the documentation about the [artifact container](asset://com.braintribe.devrock.eclipse:artifact-container-ng-documentation/artifact-container.md)). However, this nature is only relevant for the container and the respective Tomcat plugin and is not managed by Devrock (only reacted on).

## manipulating the nature 
The debug module builder exposes two commands to manipulate the nature:

#### attaching the debug module nature 
You'll find the command to attach the nature to a project in the 'devrock context menu'. It is available if the currently selected project doesn't have the nature yet. 

#### removing the debug module nature 
The command to detach the nature from a project in the same place, the 'devrock context menu'. It is only available if the currently selected project does have the nature. 

## calling the builder
As mentioned above, the builder should be triggered by JDT. In detail: JDT discovers whether anything has changed on the project. Depending on what changes it will start its build process and then (even if a Java build was unnecessary) subsequently call the attached builder. The debug-module-builder will *always* rewrite the file, so no matter whether it's a *partial* build, the builder-plugin will regard it as a full build.

However - well at least it seems to be so in some weird cases - the builder doesn't seem to be called. Hence, two commands have been implemented:

### build selected models of the workspace
This command will look at the current selection in the package-explorer, extract all debug-module artifacts (i.e. projects showing the debug-module-nature), and then build the declaration file for them.

### build all models of the workspace 
Same as above, but rather looking at the current selection, it will identify all debug-module projects contained in the workspace, and run the build of these projects.


## Configuration 
The plugin has no configuration as it uses the [Devrock Plugin](asset://com.braintribe.devrock.eclipse:devrock-documentation/devrock.md) to access malaclypse in order to determine the dependencies of the model (which has its proper configuration). 


## details

*Note : not sure whether this is required? What to write here? The place and naming of the files?*

### involved folders and files
Jinni creates these debug modules and they come in a special flavour. 

#### terminal artifact
By itself the terminal (the debug module) is a standard artifact with the tomcat nature, as it will be run within Tomcat.

- pom : The pom is special in that way that it contains only 'flat' dependencies, i.e. all transitive dependencies are directly written to the pom, and each dependency excludes all its transitive dependencies. In that way, you see all the dependencies directly in the pom.

##### module artifacts
Each of the modules only have two files:
- solution : The solution file is a 'new line'-delimited file that lists all dependencies of the module.


>in 'Malaclypse'-lingo, they are 'CompiledDependencyIdentification', so they contain the artifact's identification, the part (jar typed), and a classifier. Part and classifier both can be omitted and will regarded as having default values.


- classpath : The classpath file is again a 'new line'-delimited file. If the translation process cannot match none of the dependencies contained in the *soution* file, the *classpath* file will be identical. Otherwise, it will contain references to the diverse output-locations of the matched projects. See below. 

### translation process

The builder reads the *solution file* of the debug module and writes the *classpath file*. 
For each entry, the following happens:

- if the solution cannot be mapped to an artifact in the workspace, the JAR reference (as declared in the *solution* file) is written.
- if the solution can be mapped to an artifact in the workspace, the following happens: the output location of the project is determined and written to the *classpath* file, all 'exported' folders of the project are determined and added to the *classpath* file.







