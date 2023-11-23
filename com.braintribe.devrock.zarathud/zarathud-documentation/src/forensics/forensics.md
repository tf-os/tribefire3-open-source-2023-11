# Forensics

Forensics in Zed are modules that analyze what the extraction has delivered.

All forensic modules use the extracted data as input and - while also providing modeled data - retrieve all problems found. These problems are expressed as [finger prints](./fingerprint.md) that identify the issue and address of where the issue was detected.

Common to all forensics is that they rate issues. There are 4 different ratings

code | description 
------- | -----------
OK | nothing to report 
INFO | some issue to report, but nothing to worry about
WARN | while not a direct problem, you should know about it
ERROR | a big, fat fail - you definitively check it out


You can influence the ratings of course, but there are standard (default) ratings that Zed applies if not overridden. See about [rating](./ratings.md).

## classpath forensics
Checks for duplicate classes in the classpath, i.e. finds classes with an identical name (package and type name) in separate artifacts in the classpath. Finds not only the presence, but also where they are referenced from.

The following issues may be raised by the classpath forensics:

code | description | default rating
------- | ----------- | ----------- 
ShadowingClassesinClasspath | duplicate classes in the classpath | WARN


>Note : duplicate classes in classpath can be a problem if they are just named the same but contain different code, for instance, it could be of different version. As a class is loaded only once into the JVM, the wrong class may be loaded and thus lead to a runtime problem when accessed.

## dependency forensics
Checks the dependencies of an artifact, i.e. finds all missing (dependencies transitively inherited from the dependency tree, yet not declared as first-level dependencies) and excess (dependencies declared as first-level dependencies, yet not referenced by the terminal) dependencies. While determining the references of classes in the terminal, it also respects 'forward declarations'.

code | description | default rating
------- | ----------- | ----------- 
MissingDependencyDeclarations | some transitively acquired dependencies are used without declaration | WARN
ExcessDependencyDeclarations | some dependencies are declared, but never used directly | INFO
ForwardDeclarations | forward declarations were detected | OK

You can influence what Zed considers to be a missing or excessive dependency by adding markers into the pom of some artifacts.


- Missing Dependencies: while not a problem on the compile or runtime side, this poses a problem if an automatic process accesses the dependency list as the terminal was touched - for what ever reason and with what action whatsoever - and it actual direct dependencies are to be touched as well. If dependencies are not properly declared as direct dependencies, they are not detected as such and will lead to untouched artifacts. Also, automated branching requires that any required dependencies are correctly declared.


- Excess Dependencies : again, not a problem on the compile or runtime side, this also poses a problem if an automatic process accesses the dependency list as the terminal was touched - for what ever reason and with what action whatsoever - and it actual direct dependencies are to be touched as well. Dependencies listed as direct dependencies would be touched (and perhaps their dependencies as well) which leads to unnecessarily touched artifacts.


- ForwardDeclarations : this is not an issue per se, and therefore it's only an INFO. Basically, forward declarations actually 'mask' a dependency. Zed needs to understand forward declarations and hence can and does show them.

### Aggregators
Zed needs to understand the functionality of aggregators. 

> An aggregator is an artifact that in itself doesn't contain any code and hence doesn't contribute to a classpath, yet it's dependencies. It can be compared to a parent that passes down its declared dependencies (not the managed dependencies) to all other artifacts referencing it as parent. An artifact can however have one single artifact, and a fine-grained codebase will require a more flexible structure to declare a set of dependencies to be used for specific purposes. Hence the aggregator. 

As an aggregator doesn't contribute to the classpath, any dependency to one would be always considered as an 'excess' dependency. So, Zed needs to detect and understand the use of aggregators.

Aggregators are **supposed** to be marked accordingly. The owner of a dependency should declare a dependency to an aggregator and the aggregator to its proper dependencies.

processing instruction | description 
------- | -----------
aggregator | this dependency leads to an aggregator
aggregate | this dependency leads to an aggregate (which is a dependency of an aggregator)


>As this requires discipline that not everbody is either willing or even aware of it, Zed also regards a dependency to a 'pom'-typed artifact as a dependency to an aggregator and its dependencies in turn as aggregates. 



## module forensics
At its current state, the module forensics is just a listing of what the terminal requires to be declared as input, and what the dependencies in turn need to expose as exports - both on the package level.

While zed tries to understand any type reference in the terminal, there are some types it doesn't see at all - thinking of transient types (putting resulting types into a another function, as it's not  really clear how ASM handles such type references. If ASM (or rather the Java compiler) does list them in the respective node-structure, they will be found.

At a later state, zed should also analyze the 'module-info' class, extract the declared data and then compare this data with the data collected - just as it does with the dependencies of the terminal.


## model forensics
Checks the validity of a model.

code | description | default rating 
------- | ----------- | -------------
MissingGetter | property without a matching get.. function | ERROR
MissingSetter | property without a set.. function | ERROR
TypeMismatch | setter / getter types do not match | ERROR
InvalidTypes | invalid type for a property | ERROR
NonConformMethods | unallowed methods found | ERROR
ConformMethods | allowed methods found | INFO
CollectionInCollection | collection type has collection element type | ERROR
PropertyNameLiteralMissing | property has no corresponding tag field | WARN
PropertyNameLiteralTypeMismatch | property tag has wrong type | ERROR
PropertyNameLiteralMismatch | property tag's value doesn't match property | ERROR
UnexpectedField | field found in model not related to property | INFO
ContainsNoGenericEntities | no generic entities found in a model | INFO
InvalidEntitytypeDeclaration | entity type T literal is invalid | ERROR
MissingEntitytypeDeclaration | entity type T literal is missing | ERROR
MissingEntitytypeDeclaration | no model-declaration.xml found | ERROR
EnumTypeNoEnumbaseDerivation | enum does not derive from EnumBase | ERROR
EnumTypeNoTypeFunction | enum type doesn't contain required type() function | ERROR
EnumTypeNoTField | enum type T literal is missing | ERROR
MissingTypeDeclarations | model-declaration.xml does not declare all types of the model | ERROR 
ExcessTypeDeclarations | model-declaration.xml declares types that do not exist | ERROR
DeclarationFileInvalid | invalid format (xml error) | ERROR
NonCanonic | the model is not isomorph, i.e. it will lose information if transformed | INFO


>Conform / Non-Conform methods : Conform methods are methods that you can add to a model in its Java form (as Zed gets it to see) and that do not interfere with the getter/setter. For instance *default* and *static* methods are valid extensions, aka *conform* methods, as long as their names to not use the prefixes for getter/setter. Non-conform methods are all other methods that are not getter/setter.

>Non canonic : a model is canonic if it is isomorph, i.e. when it can be transposed across different valid formats, or simply said: if it survives a roundtrip via the modeler. Basically, it means that it must valid in the first place and may not contain conform methods.


>Unexpected field: **Why only info? add...**
