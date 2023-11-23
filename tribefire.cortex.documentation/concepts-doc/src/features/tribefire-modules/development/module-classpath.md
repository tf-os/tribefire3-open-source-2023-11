# Controlling Module Classpath

As mentioned in the [module classpaths](../module-compatibility.md#module-classpaths) documentation, the setup process tries to promote jars (from a module to the main classpath) as much as possible for optimization. There are, however, some reasons to prevent that, and the modules offer configuration options to do so.

This configuration is done in the `asset.man` file, whose content describes a [TribefireModule denotation](javadoc:com.braintribe.model.asset.natures.TribefireModule) (click to see the documentation).

A complex real-life example taken from the `elastic-search-module`:

```
$nature = !com.braintribe.model.asset.natures.TribefireModule()

// elasticsearch not only uses log4j-api (which is fine), but actually also uses classes from log4j-core.
// this is not good (not at all!), because this prevents us from bridging to slf4j (via log4j-to-slf4j).
// unfortunately elasticsearch uses a lot of log4j classes, thus there doesn't seem to be an easy fix.
// however, by marking log4j dependencies as private here we can at least make sure that other modules
// (which also use log4j) can be bridged as usual.
.privateDeps=['org\\.elasticsearch(\\.|:).+','io\\.netty:.+','org\\.codehaus\\.groovy:.+','commons-logging\\:commons-logging#.+','org\\.apache\\.logging\\.log4j:.+']

.forbiddenDeps=['org\\.glassfish\\.main\\.javaee-api:.+']
```
