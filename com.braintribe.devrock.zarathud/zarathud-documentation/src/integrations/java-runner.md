# Running an analysis via java

In order to run an analysis with Zed from Java code, all you need is to import Zed's wiring artifact and then get the runner from Wire:

the wiring artifact is
```
com.braintribe.devrock.zarathud:zarathud-wrirings#[1.0,1.1)
```

There are multiple ways to run an analysis.

## using a terminal 

The example is using the 'resolving runner' feature - starting from the terminal  as a String in the 'condensed qualified artifact name' and using the current configuration (based on the current location). The feature will internally run a classpath analysis starting with the terminal as a dependency (i.e. it can have a range as version).

```java
public Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>> test(String terminal) {
	try (	
		WireContext<ZedRunnerContract> wireContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE);
	) {

		ResolvingRunnerContext rrc = ResolvingRunnerContext.T.create();
		rrc.setTerminal( terminal);
		rrc.setConsoleOutputVerbosity( com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity.verbose);

		ZedWireRunner zedWireRunner = wireContext.contract().resolvingRunner( rrc);
		return zedWireRunner.run();

	}

}
```

## using a preconfigured classpath resolver

```java
public Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>> test(String terminal) {

	 
    WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, EnvironmentSensitiveConfigurationWireModule.INSTANCE).build();
             
    ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
            
	WireContext<ZedRunnerContract> zedContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE);

	ResolvingRunnerContext rrc = ResolvingRunnerContext.T.create();
	rrc.setTerminal( terminal);
	rrc.setConsoleOutputVerbosity( com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity.verbose);

	ZedWireRunner zedWireRunner = zedContext.contract().preconfiguredResolvingRunner( rrc, classpathResolver);

	Pair<ForensicsRating, Map<FingerPrint, ForensicsRating>> result = zedWireRunner.run();

	resolverContext.close();
	zedContext.close();

	return result;
}
```

The pair returned contains the overall rating (the worst rating of all [forensics](../forensics/forensics.md)) as first value and a Map of the fingerprint (see [forensics](../forensics/forensics.md)) with its associated rating.

> Of course, these examples are incomplete when it comes of handling the various Wire contexts. Some examples here would better use a combined Wire context for both actions, and closing the context should also happen.
