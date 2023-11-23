
You might want to validate the values for certain metadata that introduce constraints before the service processor is triggered. Current metadata where validation is supported include:

* [Min](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/min.md)
* [Max](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/min.md)
* [MinLength](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/minlength.md)
* [MaxLength](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/minlength.md)
* [Mandatory](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/mandatory.md)
* [Pattern](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/pattern.md)

To enable basic validation against those metadata you must attach the `com.braintribe.model.cortex.preprocessor.RequestValidatorPreProcessor` service preprocessor to the main entity in your service request (the one that derives from `ServiceRequest`) using a `PreProcessWith` metadata.

You can use the following information to find the `RequestValidatorPreProcessor`:

Property | Value
-----    | ------
globalId | `default:preprocessor/requestValidator`
externalId |  `preProcessor.requestValidator.default`
name | `Default Request Validator`
type | `com.braintribe.model.cortex.preprocessor.RequestValidatorPreProcessor`
declaredModel | `cortex-deployment-model`

> For more information about the `PreProcessWith` metadata, see [ProcessWith](asset://tribefire.cortex.documentation:concepts-doc/metadata/ProcessWith.md) as both metadata work in a similar fashion.

This metadata (and the validation it introduces) is automatically propagated to requests derived from that entity type so you can use an abstract supertype for all your requests that you want to have validated.

> The validation might increase the total time of evaluating a service request.
