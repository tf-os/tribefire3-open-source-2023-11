# Static Resources (Files)

### How to attach a file to a module?

In order to attach file resources to your module simply create a folder called `resources` directly inside your module and place your files there, like this:

```
my-module/
    src/
    resources/
        image.jpg
        document.pdf
    asset.man
    pom.xml
```

The build process automatically detects this folder and creates an additional `zip` part with your artifact.

### Private vs Public

Per default, all the resources are private, meaning they are only accessible from the owner module via java API. The lone exception is the content of a folder called `public` which might optionally be places inside the `resources` folder, like this:

```
my-module/
    src/
    resources/
        public/
            public-image.jpg
            public-document.pdf
    asset.man
    pom.xml
```

Public resources are considered as if they were the resources of the platform itself, they are all projected into the same space during setup, and it is not possible to tell which resource comes from which module.

**Warning:** It is the the responsibility of the module developer to ensure the public resources do not conflict with resources of other modules.

This is on purpose, as the alternative would be to separate resources by modules, and this would be reflected in the path / URL used to access the resources. This would make migration of resources between modules problematic in terms of backwards compatibility.

### Accessing the resources

Assuming the following resources structure:

```
resources/
    private-file.txt
    public/
        my-space/
        public-file.txt
```

these are the ways to access these resources:

**Java API:**

```java
public class MyModuleSpace implements TribefireModuleContract {

	@Import
	private ModuleResourcesContract moduleResources;

	@Import
	private PlatformResourcesContract platformResources;


    ...

    private File privateFile() {
        return moduleResources.resource("private-file.txt").asFile();
    }

    private File publicFile() {
        return platformResources.publicResources("my-space/public-file.txt").asFile();
    }
}
```

**HTTP:**
```
${tribefire-services-URL}/res/my-space/public-file.txt
```

### Where are the actual files stored?

Private resources are stored within the module's folder, under:
```
modules/my-module/resources
```

Public resources are stored in the storage, under:
```
storage/public-resources
```
