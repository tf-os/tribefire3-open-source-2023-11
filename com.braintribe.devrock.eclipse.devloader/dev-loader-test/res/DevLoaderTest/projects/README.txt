The sub folders contain example projects for the various combinations of build tools and classpath generators.

The first word specifies the dependency management tool:
  ac - DevRock's Artifact Container
  gradle - Gradle
  maven - Maven

The second word specifies the tool that writes the .#webclasspath file:
  ac - DevRock's Artifact Container
  gradle - Gradle (i.e. classpath file generated during Gradle build)
  maven - Maven (i.e. classpath file generated during Maven build)
  etp - Eclipse Tomcat plugin
