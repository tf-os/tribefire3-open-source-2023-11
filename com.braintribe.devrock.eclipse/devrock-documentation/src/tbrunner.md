# tb runner
The 'tb runner' is a launcher for the build process. Actually, it currently simply issues a call to 'ant', so 'tb runner' is a misnomer. 

![picture of tb runner](tbrunner.main.png "tb runner's single artifact mode")

The dialog has several modes: 
- if you have one or more projects selected in the workspace, they will automatically added to the target list, i.e. one tab per project is preconfigured. 

- if nothing is selected, you can enter the artifact you want to build, and add (or again remove) tabs for additional build targets.


## tb runner modes
There are choices that you have when it comes of how to build the artifacts. 

- transitive : this means that not only the artifact selected is built, but the required build sequence of the artifact is followed, so all pre-requisites of the artifact are built as well.

```
transitive : ant -Drange=<artifact> install 
non-transitive : ant -Drange=[<artifact>] install
```

- group-wide : this means that the build process will run within the group, i.e. all artifacts outside the group are not built, only referenced. The build file of the group is accessed in that case. 

- codebase-wide: this means that the build process will run *outside* the group, i.e. it will access the build file that resides in the root directory of the groups. You need to make sure that you have such a file in that directory, otherwise the process will fail. 

- process settings
Here you can switch the behavior that the project - if not already existing in your workspace - is imported into the workspace right after the build has run. 

Each artifact you want to build uses one tab, and you can add as many as you want. If you have projects selected in the workspace when you call the command, it will open a tab for each of the selected projects, and if prompted, builds them.

![picture of tb runner](tbrunner.multiple.png "tb runner's multiple artifact mode")

The feature fully supports the 'virtual environment', i.e. any environment variable or system property you override there is injected to the ant process.

>Note that the feature just issues the correct commands sequences for our ant-based tooling:
It can differentiate between Windows and *nix operating system to use the correct call to ant, but it doesn't handle any path, so you need to setup a search path to ant. 


The call issued is : 
```
ant[.bat] -Drange=<expression> [skip=<expression>]
```

Running the build has three ways to show what it is doing

- a process that you can see in Eclipse standard place to show processes. Every step of the build process is reflected in the process (job) visual output
- a console is automatically generated that shows the STDIN/STDOUT output of the build process
- the resulting status is shown in Eclipse log view. 