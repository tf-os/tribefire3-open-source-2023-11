# Exposed commands and toolbar items

## features 

### Copy & paste

The copy and paste features are used to copy dependencies from an artifact in a container (actually a jar reference) into the clipboard and - respectively - copying it from the clipboard in the pom of the selected artifact. 

- copy

   You can select an entry in the container, and press the appropriate key. In that moment, a XML snippet of the dependency (as it would appear in the pom) is copied to clipboard, but also - thanks to an Eclipse feature - a modeled instance of the dependency is added. How the expression of the version looks like can be set by the configuration switch explained above.
  
>The selection works as follows: for each element in the selection (you can of course select several items in the package-explorer), it is checked whether it is an entry in the container. If so, it is taken. If it's not a container-entry, the project is determined, and the dependency derived from that. Thus, you can even select an entry within a container *and* the owner project of the container at the same time.


- paste

  You can of course always open the pom manually and simply paste the content of the clipboard into file with CTRL+V. 
  
  But you can also simply select the artifact whose pom you want to modify in the package explorer and use the 'paste'-command. The command will automatically open your POM and inject the dependency into it (if the dependency already exists, it will be updated). Again, how the expression of the version looks like in the pom can be influenced by the pertinent configuration switch explained above.


>Note that the configuration switches are independent. You can have the copy-switch differing from the paste-switch. Of course, if you only use the text-editor approach for pasting, the switch that formatted the text representation of the dependency is the 'copy'-switch. If you however use the 'paste'-command, the relevant switch is the 'paste'-switch.


You can configure that using switches.

### Jar import 

The jar import feature can translate entries in a container into projects and import them. Of course, there are several restrictions to what it can do .

First of all, the artifact that provided the jar reference in the container may not be avaiable to be imported as it doesn't exist in the current source repositories, or simply perhaps we don't have any sources for it (third party artifact for instance may no have a source in our git).

Secondly, you might not have a 'matching' artifact in the source repositories, matching in the sense that the version of the jar reference has no match to the source projects. Of course, the obvious problem with the 'publishing candidate' has been solved.

>Note : In some cases the version of the jar reference and the associated code artifact don't match. For instance if you are using a published artifact (say 1.0.1) and your current source's version is of course the pc-level (1.0.2-pc). A simple match won't work here. 
Therefore, an auto-range transformation takes place, so 1.0.1 is turned into [1.0, 1.1), and this is used to scan for matches amongst the sources. Furthermore, only artifacts that match the range AND have at least the same (or higher) version as the jar reference had are considered matching. 


## Repository configuration 
One of the most critical configuration data lies within the 'repository configuration' as it defines what artifacts are available to the developer, so it's important to be able to analyze what's declared.


### compiling the repository configuration
This feature actually compiles the 'repository configuration', enriches it with the different dynamic parts, and then shows the result in a dialog.

>It is important to see that the current 'repository configuration' (or malaclypse's configuration) is not static, but compiled when an access to a malaclypse feature requiring the configuration is is made. The page simply requests malaclypse to compile the configuration and to show it. 

### loading a persisted repository configuration 
This feature loads a repository configuration persisted as a YAML or XML file and displays it. 

>This is useful if devrock-ant-tasks has found an issue and dumped the repository-configuration (depending on the task also the resolution) which was active when the issue occurred. 

### viewing
The dialog shows what repositories are in place and in want order they will be processed. 

![picture of the active repository configuration](preferences.repository.jpg "repository")

>Contrary to old malaclypse (and maven), the order of how they appear is not defined directly by their order in the configuration file (as in the settings.xml), but is (at least) influenced by the capabilities of a repository and how it is configured (dominance and lock filters). 

Furthermore, the dialog shows the 'origination' of the configuration - reasoned data that showed what env-variables and what files were used to compile the active configuration. 

As a repository can have filters attached (and showing them in the dialog would've meant to squeeze too much data into it), you can view the repository's data in YAML format by clicking on the 'details' button.

More about malaclypse's configuration can be found in its documentation artifact (com.braintribe.devrock:mc-core-documentation).
  
## workspace export / import
This feature can export all projects and working sets of your workspace into a YAML file. The projects are not stored by their location, but by their identification, so the format is portable as long as the project can be found again in your configured source repositories. 


### export
The export function simply enumerates all projects in your workspace, and, if they can be identified as artifacts (they must have a pom.xml and their identification must be fully contained within), adds them to the list. Additionally, it also records working-sets and their composition. All that is marshalled into a YAML file. 
 
 
### export selected
The export selected feature will only process the projects and/or working-sets selected. 

### import
>Note that the import features need a live QI database, i.e. scan-data of the source repositories need to be available when running an import.

The import function loads a previously persisted YAML file. Any working set referenced is acquired from the current workspace (if not present, it is generated), and its project references parsed. For each project - no matter if in a working set or not - the versioned artifact identification is taken and run as query against the QI's data. If a project can be found amongst the projects within the source repositories, it is imported, and - if referenced by a working set - attached to the working set. Existing working sets and projects are not impacted by the import, the feature skips them.
 
### import selected
This feature also loads a previously persisted YAML file. Differing to the command above, it also shows the content of the YAML file, and you can select what you want to be imported. 

![picture of a stored workspace](dialog.ws.import.jpg "selective import")

There are 3 switches that configure what you can see :

- show intrinsic working sets : shows the two working-sets that are always present (even if no working-sets are configured), Java Main Sources and Java Test Sources. They are of no consequence.
- show working sets : shows existing working-sets and their content.
- show duplicates : all projects referenced in working-sets are also repeated as 'unboxed projects' in the workspace. If working-sets are shown, you might not want to see them. Of course, if working-sets are hidden, only the 'unboxed projects' of the workspace are selectable. 

> Note that you can selected duplicates, i.e. the project will only be inserted once into the workspace, but may be referenced by multiple working-sets. So just pick what you want, the plugin will sort-out the rest. 

If you have selected either a working set, then itself and its projects are imported. If you select projects (and not a working-set) then the project is imported.

Of course, you also simply import all the content using the respective button. 


> Note that your choices (the file you selected, the state of the three check-boxes) are stored and will be used to prime the dialog next time you open it. 



