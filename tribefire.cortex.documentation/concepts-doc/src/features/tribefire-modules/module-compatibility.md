# Tribefire module classpath and components compatibility

**DEFINITION:** We call two modules "**conflicting modules**" if they depend on different versions of the same artifacts, and we call these dependencies "**conflicting dependencies**".

As Tribefire modules might declare arbitrary dependencies, a natural question to ask is **what happens if we include two conflicting modules into our setup**?

**This scenario is allowed**, as **every module** might have its **own classpath** with its own dependencies independent of other modules. **There are, however, a few limitations** and some consequences for the developer running his application from an IDE. There is a special support provided by our Eclipse plugin called "DevRock Artifact Container" (usually referred to as just "AC"), so this is relevant if you don't use Eclipse.


## Module classpaths
**DEFINITION:** "**Promoting an artifact/library/dependency**" means moving the artifact (from a module or a platform library) to the main classpath (i.e. treating it as if it was part of the platform itself).

**NOTE:** An artifact can only be promoted if all it's dependencies can be promoted. ([More details here.](troubleshooting/cannot-prepare-setup.md#extra-details))

**NOTE:** The setup process tries to promote as many artifacts as possible for optimization - the promoted jars can be loaded just once, not once per module where they are used.

In a **simple case**, our module consists of a few jars and the **module and all it's deps can be promoted to the main classpath**, i.e. treated as if they were part of the platform from the beginning. In this case the module **doesn't have it's own classpath**, and `jinni` denotes such module as `(native)`.

However, a **module might contain dependencies which cannot be promoted**, as they conflict with jars already on the main classpath, or due to [module's configuration](development/module-classpath.md). The `module classpath` consists of all these jars and when loading a module which has one, a class-loader dedicated to this module is created. This class-loader knows about all the module-specific jars and has the main-classpath class-loader as its parent.

> Note that this class-loader always tries to load each class from the module's classpath first, and falls back to the main classpath class-loader only if the class was not found.\
\
This order ensures that module specific dependencies are always picked before platform dependencies, or dependencies from other modules which were placed on the main classpath (for optimization reasons - we want to load as much with the main classpath class-loader, thus sharing as much as possible among all modules).\
\
BTW, this is exactly the same principle as individual `wars` within one servlet container (`Tomcat`) having their own jars, own class-loader, which examines the jars first, and parent class-loader second. With all the possible problems.

## When are these classpaths determined?

**When building a Tribefire application** based on a given [project artifact](application-structure.md#project) (locally or for server deployment <!--- (TODO discuss this server deployment term with CWI/MLA))-->, **all the modules are analyzed** for their dependencies. If **no conflicting dependencies** are found, **all the artifacts are placed on the main classpath** of the application, so the result is equivalent to a monolithic application with all its dependencies declared in one place, and all classes being loaded by the main class-loader.

On the other hand, **if a pair of conflicting modules is detected**, **at most one can be placed** with all its dependencies **on the main classpath**, while the other module will have at least some dependencies on its own classpath (only a non-conflicting artifact whose all dependencies are non-conflicting might be placed on the main classpath).

> The module's specific classpath information is placed in each `module`'s  folder, in a file simply called "`classpath`". The content of the file depends on the type of packaging <!-- (TODO term CWI/MLA)-->we have chosen.\
\
In case of a NON-DEBUG projection, it contains a list of jar files which are placed under `modules/libs` folder, which contains jars for all the module-specific dependencies of all modules.\
\
In case of a DEBUG projection (local Tomcat with a `--debugProject`), this file contains links to jars located in your local maven repository. For this case [see Consequences for IDEs](#consequences-for-ides) at the bottom.

## Platform and Tribefire APIs
It should be obvious, that **separating the classpaths is not possible in all cases**. While having **two different versions of an xml parser is no big deal**, having two **modules using two different versions of Tribefire APIs is**. In fact, this case would already be problematic with a single module, if the module itself is not be compatible with the chosen Tribefire platform.

Generally speaking, **one can only use modules compatible with the chosen platform, which means modules developed against the same Tribefire version**. No surprise here.

Note that when we say **Tribefire APIs** we mean [GM API artifacts](application-structure.md#gm-apis).

# Models

In addition to this, we also **don't allow conflicting models, because they too need to be placed on the main classpath**.

Models are treated in a very special way, with bytecode being generated and there being a central register of all the custom types (entities and enums) accessible via our [GenericModelTypeReflection](javadoc:com.braintribe.model.generic.reflection.GenericModelTypeReflection).

In other words, there is some black magic done with models and that only works if they are all on the main classpath.


## Platform Libraries

[Platform libraries](application-structure.md#platform-libraries) are a special type of assets, as **they (if jar part is available) and all their dependencies have to be put on the main classpath**. Hence, they might also be incompatible with other modules and platform libraries.


## Consequences for IDEs (other than Eclipse)

Typically, when you run an application from an IDE, you want that application to correspond to the code you have written there. If your application consists of individual parts, with separate compilation outputs (e.g. different Eclipse projects / Intellij moduls), the IDE has to manage the application's classpath to include all these outputs.

But even if your IDE supports this, if **a module has its own "`classpath`" file**, you'd need something that manages this file and puts the compilation output entries in it, as per default it references your local repository.

As mentioned above, this is not a problem when using `AC` plugin in Eclipse (AKA `DevRock Artifact Container`), as it can manage all the module's classpaths of a Tribefire application.

With other IDEs / Eclipse without DevRock, you'd need to install the artifacts locally first to have the latest changes when running the application.

Also note that it seems hot code replacement seems to only works for classes on the main classpath. Don't quote me on that though.