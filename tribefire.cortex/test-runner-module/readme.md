# test-runner-module

## General

**What is this module doing?**

This module offers a way to execute unit tests within a running `Tribefire` instance.

Typically we would want to run all `JUnit` tests on the classpath of some module. Our module must:
* depend on `this module` and `tribefire.cortex:junit-runner-support`.
* have its main space extend `JUnitTestRunnerModuleSpace`
* declare all it's dependencies private

Declaring dependencies private
```
$nature = (TribefireModule=com.braintribe.model.asset.natures.TribefireModule)()
.privateDeps=['.*']
```

**How does this work?**
* There is code in `JUnitTestRunnerModuleSpace` that automatically registers a special `JUnit` test runner in the `TestRunnerRegistry` (accessible via `TestRunningContact.registry()`).
* This `JUnit` runner scans the module's classspath for `JUnit` tests and executes them.
* That's why all the module's dependencies must be private, so this module gets its own (URL) ClassLoader and the classpath can be analyzed.

**How to run the tests?**

This module binds a service processor for `RunTests` request on the `cortex` domain which executes all test runner registered.

The request itself comes from `tribefire.cortex:test-runner-service-model`.

**How do I write tests which run inside a Tribefire server**

Ideally use `Imp` to access sessions/evaluators. `Imp` can actually detect whether it is running within a `Tribefire` server or as a standalone application, and based on that uses a local or remote session automatically.

Not that `Imp` uses `UserRelatedTestApi` (from `tribefire.cortex:user-related-test-api`) to detect if it's running inside a `Tribefire` server. There are values set by this module 

## bindWireContracts()

`TestRunningContract`: allows access to `TestRunnerRegistry`, which allows a depender to register a `ModuleTestRunner` (from `tribefire.cortex:test-runner-api`). 

## bindHardwired()

Binds a service processor:

- | -
-|-
Name | `Test Running Processor`
ExternalId | `test.running.processor`
Domain |  `cortex` 
Request Type | `RunTests`
Service API Model | `tribefire.cortex:test-runner-service-model`