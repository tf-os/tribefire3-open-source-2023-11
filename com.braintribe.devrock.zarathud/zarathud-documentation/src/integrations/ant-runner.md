# Running an analysis via ant target

In ant (devrock-ant-tasks to be precise), you can call Zed's analysis via the integrated task.

```
ant zed -Dtarget=<condensed name of terminal> -Dmode=taciturn/terse/verbose/garrulous -DsolutionFile=<name of solutionfile> -DterminalJar=<jar of terminal> -DpomFile=<pom>  -DoutputDirectory=<directory> -Dwrite=<true|false>
```

this of course requires the following target in the build.xml file:

```xml
	<project ...>
		<target name="init">
			<bt:pom id="pom" file="pom.xml">
			<property name="versionedName" value="${pom.artifactId}-${pom.version}"/>
		</target>

		<target name="produce_cp" depends="init">
			<bt:dependencies pomFile="pom.xml" solutionListFile="solutions" addSelf="false">
				<pom refid="pom"/>
			</bt:dependencies>
		</target>

		<target name="analyze-artifact" depends="produce_cp" solutionListFile="solutions" terminalJar="${versionedName}.jar" >
			<pom refid="pom"/>
		</target>
	</project>
```

## parameters

follows a declarations of the parameters you can pass to the task


parameter | type | description | default
------- | ----------- | ------- | ------ 
pomFile | File | the pom to read | 
solutionListFile | File | the full classpath of the terminal as produced by bt:dependencies
terminalJar | File | the jar file of the terminal
outputDirectory | File | where to write the data | . (current directory)
write | boolean | whether to write all files | false
verbosity | ConsoleOutputVerbosity | the [verbosity](../forensics/verbosity.md), i.e. the detail-level of output | verbose


>The  *pomFile* argument can be null if the *pom-entity* is attached as in the example above.

>The solution list file is required and must be named

>The terminal jar is required and must be named. 

