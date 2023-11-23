# Scripting 

The scripting module offers facilities to create and register new script types and corresponding script engines. 
Thus, scripts can be used in other algorithms, processors and services. 
Script input and output is transferred in the interface as java objects. 

By default, support for Groovy scripting is provided, see description 
of [Groovy scripting](asset://tribefire.extension.scripting:groovy-scripting-doc/groovy-scripting.md). 
Additional scripting engines can be added by users and deployed in tribefire as needed. 

For example, to read a Groovy script from disk:

```java
  Resource scriptResource = session.resources().create().store(new FileInputStream("script.groovy"));
  GroovyScript groovyScript = session.create(GroovyScript.T);
  groovyScript.setSource(scriptResource);
```

A typical use-case for scripts is in tribefire `ServiceProcessor`s, of which all the needed `ScriptedServiceProcessor`s are
provided, too, making it very convenient to process `ServiceRequest`s with scripts.  

This can be done according to this example:

```java
  ScriptedServiceProcessor processor = session.create(ScriptedServiceProcessor.T);
  // ...
  processor.setScript(groovyScript);
```
and has to be assigned to a `ProcessWith` 

```java	
  ProcessWith processWith = session.create(ProcessWith.T);
  processWith.setProcessor(processor);
```

Finally the script has to be provided with a suited input data (here a `ServiceRequest` named `MyRequest`)

```java
  modelEditor.onEntityType(MyRequest.T).addMetaData(processWith);
```

where modelEditor is a `BasicModelMetaDataEditor` obtained from the session. 

You can then process a request using the script

```java
  MyRequest request = MyRequest.T.create();
  request.setText("test"); // this assumes the script needs a String/text input. 
  // evt. set domainId 
  Maybe<String> reversedTextMaybe = request.eval(evaluator).getReasoned(); // assuming a String output.
```

## Scripted service processors

The following service processors are provided:
 - `ScriptedSericeProcessor`
 - `ScriptedServicePreProcessor`
 - `ScriptedServicePostProcessor`
 - `ScriptedSerciceAroundProcessor`

Scripts run as a service processors obtain their input parameters via bindings of this type
- ScriptedServiceProcessor
  - `$context` of type `ServiceRequestContext`
  - `$request` of type `ServiceRequest`
  - `$tools` of type `ScriptTools`
  - returns `Object` (as defined in request)
- ScriptedServicePreProcessor`
  - `$context` of type `ServiceRequestContext`
  - `$request` of type `ServiceRequest`
  - `$tools` of type `ScriptTools`
  - returns `ServiceRequest`
- ScriptedServicePostProcessor
  - `$context` of type `ServiceRequestContext`
  - `$response` of type `Object`
  - `$tools` of type `ScriptTools`
  - returns `Object` (as defined in request)
- ScriptedServiceAroundProcessor
  - `$context` of type `ServiceRequestContext`
  - `$request` of type `ServiceRequest`
  - `$tools` of type `ScriptTools`
  - `$proceedContext` of type `ProceedContext` <br>
     whereas the proceed context can be used to execute the internal processing chain, i.e. using the  `proceed` methods.
  - returns `Object` (as defined in request)

The `$tools` offer access to many tribefire facilities:
- Logging: `$tools.getLogger().info("Hello Scripting");`
- Script: `$tools.getScript()`
- Deployable: `$tools.getDeployable()`
- Runtime properties: `$tools.getRuntimeProperty("TRIBEFIRE_SERVICES_URL")`
- System session factory: `$tools.getSystemSessionFactory()`
- Authentification: `$tools.isAuthenticated()`
  - Request session factory: `$tools.getSessionFactory()`
- Create new entities: `$tools.create("com.braintribe.gm.model.reason.essential.InvalidArgument")`
- Type reflection: `$tools.getTypeReflection()`
- AttirbuteContext: `$tools.getAttributeContext()`

## Example script

A very simple `ScriptedServiceAroundProcessor` script with a request providing `String text` input and `String` output could look like this:
```groovy
text = $request.text;
$request.text = text.replaceAll("rr", "r-r");
return $proceedContext.proceed($request);
```

## Scripting failures, error handling

The scripting module uses `Reason`s and returns `Maybe` objects according to tribefire standard.

There are two specific reasons why script processing may fail:
 - `ScriptCompileError` if the compilation of a script fails. The syntax error is reported.  
 - `ScriptRuntimeError` if the execution of a script fails. The error message is reported. 

 In addition to the errors related to script handling, script can also produce internal erros and also 
 expose those as `Reason` and `Maybe` towards tribefire. For example in the groovy script you can do

```groovy
   // exit with Maybe
   import com.braintribe.gm.model.reason.essential.InvalidArgument;
   $tools.abortWithMaybe(Reasons.build(InvalidArgument.T).text("script needs argument").toMaybe());

   // exit with Reason
   import com.braintribe.gm.model.security.reason.SessionNotFound;
   $tools.abortWithReason(Reasons.build(SessionNotFound.T).text("request should be authorized").toReason());
```


## Details on scripting in tribefire

To achieve this, the module provides the new denotation type `Script` to specify a script language and its script `Resource`. 
This is combined with the denotation type `ScriptingEngine` offering `Deployable` functionality within tribefire. 
The actual script data is hold by `Script` via an internal `Resource` field. 
For linking of scripts (and their resources) to other code (i.e. `ScriptedSerciceProcessor`) there is also a `HasScript` denotation type, which offers an internal field `script` containing a concrete `Script`. 
In order to allow full flexibility the scripting module in addition offers a `HardwiredScriptingEngine` inheriting 
from `ScriptingEngine` but providing a `HardwiredDeployable` functionality. 
Finally, the `EvaluateScriptWith` denotation type provides entity-level meta data holding an internal field *engine* 
returning a `ScriptingEngine`. This is needed to wire specific `Script` denotation types to specific `ScriptEngine` 
experts. 

On API level, `ScriptingEngine<Script>` provides an interface for expert code to compile and evaluate script data, 
where for the latter parameters are passed via a `Map<String,Object>`. 
The `CompiledScript` interface is for compiled version of scripts and offers a direct way for evaluation, too. 
In addition, there is a `ScriptingEngineResolver` that can resolve the engine expert for a given script type, and also offers direct compile and evaluation methods. 

The scripting module offers two contracts: the `ScriptingContract` and the `ScriptingBindersContract`. 
The latter is used by modules providing concrete script support to bind a `ScriptingEngine` API to a `ScriptingEngine` expert type (via a `PlainComponentBinder`). 
The former yields access to a `ScriptingEngineResolver` (concretely, to the `MetaDataMappedScriptingEngineResolver`). 

## 