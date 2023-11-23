# tribefire.extension.schemed-xml:schemed-xsd-xml-marshaller-roundtrip-test

This a test setup that :

- analyzes an XSD and creates the skeleton and mapping model from it
- reads an XML file associated with the XSD into an assembly of the types from the skeletion model using the mapping model
- writes the assembly out as an XML file
- validates the written XML with the original XSD


## specifics
As the skeleton model needs to be on the classpath while unmarshalling the XML file, it cannot work in the same JVM as the one that creates the skeleton model. 
Hence, a second JVM is started for the marshalling test, using a specific runner artifact. The classpath for the runner is packaged with this artifact,  see

```
	/res/setup/schemed-xsd-xml-roundtrip-runner-2.0.3-pc-cmd.zip
```

This artifact will always unpack the zip into the classpath directory, always starting from a clean slate. 


## requirements

The zip file that contains the runner can be obtained while building (standard build) of 

```
	tribefire.extension.schemed-xml:schemed-xsd-xml-roundtrip-runner
```

The build process of the runner artifact will leave the zip file at its

```
/dist/schemed-xsd-xml-roundtrip-runner-2.0.2-pc-cmd.zip
```

