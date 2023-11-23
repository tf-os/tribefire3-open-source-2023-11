com.braintribe.devrock.zarathud:zarathud
========================================

# What is it?
Zed is the current iteration of the Zarathud project. It's quite old - at least, the idea is. It started during the early days of the GmCore, made its move into the TF1.x world, but had to be completely redone for TF2.x world and for the newer Java versions.


# How does it work?
Zed is using *ASM* to analyze the **compiled** java code. Therefore it can work with any artifact, being one of ours or a third-party artifact. It analyzes the 'real' dependencies of an artifact, i.e. tries to figure out what classes are actually needed from the various dependencies. 

# What does Zed aim for?
Zed aka Zarathud is a tool to analyze existing binary java code. It can extract data from such an artifact and store it as an assembly. Using that data, several features can be implemented, like comparing the extracted data of two versions of the same artifact, analyzing dependencies et al. [More high-level rationale can be found here](./rationale/rationale.md)

## extraction
Zed can read **binary** java classes (either in jar or in a bin-folder), and extract data about classes, methods, fields etc.
>So one can imagine that Zed's data could be collected by running over our repositories and build an index from that. Next, AC could for instance sport something similar like CTRL+SHIFT+T, but not just querying the JDT indices (all classes somehow referenced by the project in your workspace), but actually across all classes in our repositories, with the option to get a respective dependency entry.

## modules
Moving to modules is a lot of work that most of us don't even dare to start. Zed aims to help in that area, and produces import requirements for the terminal, and in reverse, export requirements for the dependencies. 
>In a further step, zed should read the terminal's 'module-info.java' and at least show discrepanciues between what is declared there and what is actually need. Of course, zed is only savvy about the terminal, its view on the dependencies is very limited (terminal-centered). But still, the information about the export requirements of the terminal attributes to a analysis of the dependencies 'module-info.java' in turn.


## analysis
Using the extracted data, Zed can run *forensics*, in order to find problems in type references, implicit dependencies without declarations etc.
[Forensics](./forensics/forensics.md) help to find issues with an artifact - missing dependency declarations, shadowing classes in the classpath et al. Model forensics allow to validate a model *before* it is presented to Tribefire.

- dependencies : missing, superfluous and 'forwarded' depedencies
- classpath: duplicate class-resource definitions in multiple jars in the classpath
- model: generic-entity declaration, string literals, enum declarations, conform/non-conform methods 
- module : all imported packages with their origin, required exported packages of all dependencies


## comparison (projected)
Using two sets of extracted data, Zed can compare them and analyze them for compatibility.
The comparison feature allows us to either control publishing of hotfix revisions, or at the least, create awareness of breaking changes so one can react to them (change notes)


# How can I run an analysis with Zed?

There are currently 4 different ways to get Zed to run an analysis

* directly via [Java](./integrations/java-runner.md)
* via [Jinni](./integrations/jinni-runner.md)
* via [ant](./integrations/ant-runner.md)
* via [eclipse](./integrations/eclipse-runner.md)

## how good is the analysis?
Well, to be frank, zed is no way finished and hence impossible to be perfect (if ever that would be possible). I do think that all its weaknesses are in the realm of understanding ASM lingo, and I still harbor the hope that eventually, zed will always work fine.

Until then, zed will tell you what it didn't understand in its output - it will show that in its output.
