# Finger prints

Finger prints in Z identify the place of a problem and the type of the problem. They can be expressed in an expressive form, but are of course modelled as well.

in the expressive form, they look like this:

```
group:com.braintribe.devrock/artifact:analysis-artifact-model/type:com.braintribe.model.artifact.analysis.AnalysisArtifact/property:Dependers/issue:PropertyNameLiteralMissing=info
```

Basically, the finger print can contain as many slots as are required. For the time being these exist:


key | description 
------- | -----------
group | the group id of the containing artifact 
artifact | the artifact id of the containing artifact 
package | the JAVA package associated (if any)
type | the JAVA type or GenericEntity (if any)
property | the property (of a model - if any)
method | the method (if any)
issue | the issue code 


Finger prints (as returned by the forensic modules) are associated with their rating.

a typical output looks like this

```
group:com.braintribe.gm/artifact:exchange-model/issue:ForwardDeclarations=ok
group:com.braintribe.gm/artifact:exchange-model/package:com.braintribe.model.exchange/type:ExchangePackage/property:Payloads/issue:PropertyNameLiteralMissing=info
group:com.braintribe.gm/artifact:exchange-model/package:com.braintribe.model.exchange/type:ExchangePayload/property:ExternalReferences/issue:PropertyNameLiteralMissing=info
group:com.braintribe.gm/artifact:exchange-model/package:com.braintribe.model.exchange/type:ExchangePackage/property:Exported/issue:PropertyNameLiteralMissing=info
group:com.braintribe.gm/artifact:exchange-model/package:com.braintribe.model.exchange/type:GenericExchangePayload/property:Assembly/issue:PropertyNameLiteralMissing=info
group:com.braintribe.gm/artifact:exchange-model/package:com.braintribe.model.exchange/type:ExchangePackage/property:ExportedBy/issue:PropertyNameLiteralMissing=info
```

the expressive parser will output the slots of a finger print from left to right with increasing detail until it specifies the exact location of the issue.
