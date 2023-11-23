graph TB
	subgraph views

		GROUP_ADX[tribefire.adx.phoenix:adx-view] -->ADX_VIEW_25_1(2.5.1)
		ADX_VIEW_25_1 --> ADX_VIEW_25_1_LOCKS>"tribefire.adx.phoenix:adx-aws-deploymentmodel#2.5.1<br/>tribefire.adx.phoenix:adx-aws-initializer-module#2.5.1<br/>tribefire.adx.phoenix:adx-aws-module#2.5.1"]

		GROUP_CONVERSION[tribefire.extension.conversion:conversion-view] -->CONVERSION_VIEW_23_1(2.3.1) & CONVERSION_VIEW_22_1(2.2.1)
		CONVERSION_VIEW_23_1 --> CONVERSION_VIEW_23_1_LOCKS>"tribefire.extension.conversion:conversion-aspose-license#2.3.3-pc<br/>tribefire.extension.conversion:conversion-commons#2.3.9-pc<br/>tribefire.extension.conversion:conversion-deployment-model#2.3.3-pc"]

		CONVERSION_VIEW_22_1 --> CONVERSION_VIEW_22_1_LOCKS>"tribefire.extension.conversion:conversion-aspose-license#2.2.1-pc<br/>tribefire.extension.conversion:conversion-commons#2.2.1-pc"]

		GROUP_CORTEX[tribefire.cortex.assets:tribefire-standard-view] -->TRIBEFIRE_STANDARD_VIEW_20_1(2.0.2)
		TRIBEFIRE_STANDARD_VIEW_20_1 --> TRIBEFIRE_STANDARD_VIEW_20_1_LOCKS>"tribefire.cortex:cortex-api-model#2.0.17-pc<br/>tribefire.cortex:cortex-cors-handler#2.0.10-pc<br/>tribefire.cortex:cortex-deployment-model#2.0.26-pc"]

		USE_CASE>"Setup repository configuration: tribefire.adx.phoenix:adx-view#2.5<br/><br/>We examine how setup-repository-configuration works with and without a repository configuration. First we setup repository configuration without pointing to a preexisting repository configuration. This is visualized with green nodes.<br/>Then we point to an existing repository configuration and run the setup-repository-configuration request. In this case we use QualifiedArtifactFilter and we restrict tribefire.extension.conversion:conversion-view to version [2.2,2.3). This is visualized with pink nodes."]
	end

ADX_VIEW_25_1 -. "depends on conversion-view [2.3,2.4) without repository configuration" .-> CONVERSION_VIEW_23_1
ADX_VIEW_25_1 -. "depends on conversion-view [2.3,2.4) with repository configuration" .-> CONVERSION_VIEW_22_1
CONVERSION_VIEW_23_1 -. "depends on tribefire-standard-view [2.0,2.1)" .-> TRIBEFIRE_STANDARD_VIEW_20_1
ADX_VIEW_25_1 -. "depends on tribefire-standard-view [2.0,2.1)" .-> TRIBEFIRE_STANDARD_VIEW_20_1

style ADX_VIEW_25_1 fill:#FF1493
style CONVERSION_VIEW_23_1 fill:#008000
style TRIBEFIRE_STANDARD_VIEW_20_1 fill:#008000

style CONVERSION_VIEW_22_1 fill:#FFC0CB
