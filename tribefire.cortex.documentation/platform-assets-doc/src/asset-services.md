# Platform Asset Services

There exist services providing further information, convenience tooling and features around Platform Assets.

## GetAssets

**Request**: `GetAssets` (_com.braintribe.model.platformsetup.api.request_)

**Description**: Returns a `AssetCollection` based on the provided filter criteria of this request.

**Filter Criteria**:

Filter | Type | Description
-----| ----------- | -----------
`natures` | Set\<AssetNature\> | Set of natures, see [Asset Nature Values](#asset-nature-values)
`setupAssets` | boolean | Includes setup asset(s). Queries for type `PlatformSetup`.
`effective` | Set\<String\> | Effective assets vs. build dependencies (currently, as long as clash resolving is not supported, all assets are effective, so default is set to `true`)
`repoOrigin` | Set\<String\> | Set of repository origins (wildcard support)
`groupId` | Set\<String\> | Set of asset group ids (wildcard support)
`name` | Set\<String\> | Set of asset names (wildcard support)

### Asset Nature Values

Following natures are supported:

*	AssetAggregator
*	ContainerProjection
*	MarkdownDocumentation
*	MarkdownDocumentationConfig
*	ModelPriming
*	PlatformLibrary
*	RuntimeProperties
*	LicensePriming
*	TribefireModule
*	PrimingModule
*	ManipulationPriming
*	ScriptPriming
*	ResourcePriming
*	TribefireWebPlatform

### Sample Requests

#### Get assets of natures TribefireModule and ModelPriming
```
http://localhost:8080/tribefire-services/api/v1/setup/com.braintribe.model.platformsetup.api.request.GetAssets?nature=TribefireModule&nature=ModelPriming&sessionId=...
```

#### Get assets having group id tribefire.cortex.assets
```
http://localhost:8080/tribefire-services/api/v1/setup/com.braintribe.model.platformsetup.api.request.GetAssets?groupId=tribefire.cortex.assets&sessionId=...
```

#### Get assets having term explor in its name
```
http://localhost:8080/tribefire-services/api/v1/setup/com.braintribe.model.platformsetup.api.request.GetAssets?name=*explor*&sessionId=...
```
