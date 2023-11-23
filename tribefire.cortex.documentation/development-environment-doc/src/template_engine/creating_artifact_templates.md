# Creating Artifact Templates

You can introduce your own artifact templates to the template engine.

## General

If you find yourself continuously working with a specific set of assets designed in a particular way, it might be worth to consider creating an artifact template. By creating your own artifact templates, you can speed up the the development of a project as you can put both dynamic and static files inside an artifact template and reuse them later.

## Creating Artifact Templates

The regular artifact template creation flow is as follows:

1. [Create a service request](#creating-a-service-request)
2. [Create the actual template](#creating-a-template)

### Creating a Service Request

As the template engine is accessible via Jinni, you must create a new artifact template generation service request and introduce it to Jinni. Created service requests reflect the artifact template dependency hierarchy so if a template has a dependency to another template, its request will also extend the request of the other template.

> For now this can only be done by Core developers as introducing a new artifact template to Jinni requires changing Jinni's service model.

You can use Apache Freemarker in the service request to populate String properties with initial values, for example:

```java
@Description("The group id of the projected artifact.")
@Initializer("'${support.getRealFileName(request.installationPath)}'")
@Alias("gid")
String getGroupId();
void setGroupId(String groupId);
``` 

Inside the Freemarker expression, you can use the `$support` and `$request` objects.

### Creating a Template

Creating the insides of a template mostly consists in providing static and dynamic resources. Static resources are files which your template should use as is, while dynamic resources are `.ftl` files with Apache FreeMarker code.

#### Aggregator Templates

You use the aggregator template to run several other requests in a sequence.

> For more information, see [Aggregator Template](artifact_template_types.md#aggregator-template) 

It is the `requests.groovy` file that allows you to create, configure and return the requests you need to create a bundle of artifacts. Normally, you add the content to the `requests.groovy` file in the following order:

1. import necessary requests 
2. define shared variables
3. define and instantiate actual requests
4. return a list of previously defined requests

Inside the Groovy script, you can use the `{$request}` object.  

#### Available Objects

There are several main objects you can use in your FreeMarker template:

* `${static}`
* `${template}`
* `${support}`
* `${request}`
* `${data}`

> For more information on FreeMarker and its usage, see [FreeMarker Template Author's Guide](https://freemarker.apache.org/docs/dgui.html).

Even though you could create the template manually, it is best to create an empty template using Jinni by calling `jinni create-template-artifact`. For information about the parameters this call takes, call `jinni help create-template-artifact`.

#### `${static}`

`${static}` is an object that manages static resources. For information on what you can use this object for, see its [JavaDoc](javadoc:com.braintribe.template.processing.projection.support.StaticHandler).

> You normally use the `${static}` object in the `static.handler.ftl` file as a part of FreeMarker directives `#for` `#if`, `#assign`, etc.

#### `${template}`

`${template}` is an object that provides the reflection capabilities to the template - this means the template can influence its own structure. For information on what you can use this object for, see its [JavaDoc](javadoc:com.braintribe.template.processing.projection.support.TemplateHandler).

You can extend XMLs from the dynamic and static parts of the template. To extend an XML, use the following syntax:

```xml
$template{"xpathexpression", param2, "REPLACE or INSERT"}
```

Example `build.xml.ftl`:

```xml
<#assign attachments>
<attach file="asset.man" type="asset:man" />
</#assign>

${template.extendXml('/project/target/*[local-name() = "install"]', attachments, 'INSERT')}
```

The code above adds an `attach` entry to the `build.xml` file.

#### `${support}`

`${support}` is an object that provides utility methods. For information on what you can use this object for, see its [JavaDoc](javadoc:com.braintribe.template.processing.projection.support.TemplateSupport).

#### `${request}`

`${request}` is an object that allows for reading the parameters that were sent as a part of the Jinni request, for example `artifactID` or `groupID`.

```xml
ContainerProjection = com.braintribe.model.asset.natures.ContainerProjection

$nature = ContainerProjection()

$nature
.containerName = '${request.artifactId}'
.important = ${request.important?c}
.inherited = ${request.inherited?c}
```

#### `${data}`

`${data}` is an object that allows you to access the data from the `data.yml` file. For information on that file, see [`data.yml`](artifact_template_types.md#datayml).