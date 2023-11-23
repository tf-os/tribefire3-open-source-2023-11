# Transient Resources

### How to create a `Resource` instance easily and efficiently.

Let's just use a simple example of creating a transient resource, for example as a result of a `ServiceRequest` evaluation:

```java
public class MyModuleSpace implements TribefireModuleContract {

	@Import
	private ResourceProcessingContract resourceProcessing;

    ...

    private Resource createResource1(List<String> lines) {
		return resourceProcessing.transientResourceBuilder() //
            .newResource() //
			.lines(lines);
    }

    private Resource createResource2(List<String> lines) {
		return resourceProcessing.transientResourceBuilder() //
            .withMimeType("text/html") //
            .newResource() //
			.usingWriter(this::writeData);
    }

	private void writeData(Writer writer) {
        writer.append("<html>\n");
        ...
    }
}
```

For more information about the fluent API see [ResourceWriterBuilder](javadoc:com.braintribe.model.resource.api.ResourceWriterBuilder).

Also note the actual imported implementation is backed by a [FileBackedPipe](javadoc:com.braintribe.utils.stream.file.FileBackedPipe) configured for the platform, which brings additional advantages:

* For small amounts of data only memory is used.
* For bigger amounts of data temporary files are used, and these files are being recycled, meaning rather than deleting a file and creating a new one, an unused file will be used for a different purpose (which improves performance).
* Temporary files are garbage collected automatically.