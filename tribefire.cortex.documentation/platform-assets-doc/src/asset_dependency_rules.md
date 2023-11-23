## Asset Dependency Rules

Platform assets use maven POM files to manage asset dependencies. POM files are normally used to manage build dependencies for Java archives (`.jar`, `.war`, `.ear`, etc. ). Each dependency may address either one (manage/build) or both demands.
Assets with certain natures use the Maven POM to manage asset as well as build dependencies, for example:
* `Plugin`
* `PluginPriming`
* `ModelPriming`
* `CustomCartridge`

In the cases of the dependencies above, special care must be taken to address the different dependency purposes by qualifying the dependency declaration with:
* tag processing instructions
* Maven type
* classifier elements

Below is the description of the three possible cases.

### Build Only Dependencies

Such dependencies are **not** taken into account while resolving the platform setup but while resolving build dependencies.

```xml
<dependency>
    <groupId>tribefire.cortex</groupId>
    <artifactId>platform-model</artifactId>
    <version>${V.tribefire.cortex}</version>
</dependency>
```

### Asset Only Dependencies

Such dependencies are taken into account while resolving the platform setup but **not** while resolving build dependencies.

```xml
<dependency>
    <groupId>tribefire.cortex</groupId>
    <artifactId>platform-model</artifactId>
    <version>${V.tribefire.cortex}</version>
    <classifier>asset</classifier>
    <type>man</type>
    <?tag asset?>    
</dependency>
```

### Mixed Dependencies

Such dependencies are taken into account while resolving the platform setup **and** while resolving build dependencies.

```xml
<dependency>
    <groupId>tribefire.cortex</groupId>
    <artifactId>platform-model</artifactId>
    <version>${V.tribefire.cortex}</version>
    <?tag asset?>    
</dependency>
```