# Groovy Scripting

The groovy scripting module provides Groovy scripting to tribefire using the `javax.script.ScriptEngineManager` "Groovy" engine [(see Oracle documentation)](https://docs.oracle.com/en/java/javase/17/docs/api/java.scripting/javax/script/ScriptEngineManager.html). 

Groovy scripting is a default part of the [scripting extension](asset://tribefire.extension.scripting:scripting-doc/scripting.md). 

## Details

The module provides the `GroovyScript` denotation type extending `Script`, as well as the `GroovyScriptingEngine` extending `ScriptingEngine`.

The `GroovyInitializer` couples the two by adding an `EvaluateScriptWith` to the meta data of `GroovyScript`. The `EvaluteScriptWith` is wired to `GroovyScriptingEngine`, and the `GroovyEngine` is deployed as (default) expert for the `GroovyScriptingEngine`.    