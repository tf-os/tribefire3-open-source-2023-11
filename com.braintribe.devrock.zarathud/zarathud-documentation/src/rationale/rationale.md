# Rationale (mgmt redux)

There are several reasons why Zed is quite extraordinarily useful for us and our business case.

## CI vs CE
Currently, we are doing something we use the term continuous integration  (CI) for. Actually, this is a misnomer, because in order to continuously integrate something, we need a thing to integrate into. While this is definitely possible with some of our artifacts (those we use in the platform themselves) and some of the terminal projects (ADX), it's obviously not possible to integrate things into a terminal as we do not have access to this terminal. And we hope that these cases grow..

So, the correct term would rather be continuous exposure (CE) - we provide access to a repository of checked artifacts. When we do a release nowadays, we do not actually create a distribution, but the scope of a distribution : we actually provide a checked repository, i.e. a sum of checked artifacts that can be combined and used as the user (the developer) sees fit.

Of course, as we cannot simply compile all possible terminals when we changed an artifact (even if we could: always keep in mind that squandering processing-power isn't such a great idea in the first place), there must be other means to ensure that a published artifact's hotfixes are compatible. Internally we of course still compile the (somewhat) related artifacts, but that only tells us that the functionality of the artifact that we currently access in the terminal is still compatible. We do not know if a feature that is in the artifact, yet not used in our terminal, is still compatible.

Lucky for us, potential compile errors introduced by a hotfix revision can easily be detected by comparing two versions of an artifact - no need to compile anything (To use the current hyped term, it's using a *golden master* approach by constantly comparing the next installment of the artifact with the last one)

Zed's comparison feature aims to do that.

Obviously, Zed cannot know whether the functionality is still working just by means of its analysis, but neither does any build process - that is why we do run integration tests, and any external developer should do it. But with Zed, we can predict whether a switch to a new version does break the compilation, and if so, it can also provide hints what *release notes* should contain.

## Java modules
Currently, we do not use Java modules yet. As a consequence, this means that any public class in our codebase can be accessed from an external side, and therefore any changes on these classes may break external code accessing such a class - even if such an access is not in our intention. In other words, not we decide what the API exposed to the outside is, but the visibility of the classes within Java does it. For Java 9 on, Java modules were introduced to remedy this.

Java modules allow us to specify the visibility of packages independent of the visibility of the classes within (with some restrictions of course) and therefore to control the access. However, this requires a clear understanding of what a specific class requires (and others provide) and vice-versa.  

Zed's analysis feature aims to deliver this information.


## Dependencies
In the Maven world, the coordinates of the first encounter of a certain dependency is crucial for clash resolving (basically, in short : the first encountered dependency wins). While you can mitigate clash resolving in Maven, easier is to trick with the by adding 'fake' dependencies to the terminal.

Even if we as a team agree to prefer the optimistic approach (higher version wins) that doesn't require such tricks, we are confronted to the Maven world, and we do have such cases in our codebase.

A clean dependency tree however is absolutely necessary when it comes to branching, i.e. when a new version increment is required, and one needs to determine what needs to be incremented as well.

Zed's dependency analysis feature can tell us how the dependency actually needs to look like, and hence gives us the possibility to automatically *correct* dependency declarations.  


## Shadowing
Shadowing happens when two classes with the same name appear in the classpath. As the Java classloader will only load a class once per name, and as there are always multiple ways that lead to the loading of a class, situations when the wrong class is loaded can occur. Unfortunately, this is not something a compile process detects - integration tests may detect it, but most of the time, it's the customer that is confronted with the issue. 
  
Zed's classpath analysis feature can tell us whether duplicate classes exist in the classpath.


## Model validation
With the introduction of modules in tribefire, models are always transported into tribefire as binary jars. Tribefire itself cannot know whether this is a valid model before it starts to incorporate it.

Zed's model analysis feature not only tells is whether it is valid, but also whether it is canonic (i.e. can be transported, projected, without loosing information during the transformation), can be expressed in containment based formats (expressive XML).

## Conclusion
The community seems to wake up in that matters. Gradle 6 is sporting similar features for the dependency tree and the classpath. Of course, models and further GM specifica are unique to our platform and no external tool can supply that. Furthermore, Zed aims to be useful without even access to the code - it can act on any artifact.
