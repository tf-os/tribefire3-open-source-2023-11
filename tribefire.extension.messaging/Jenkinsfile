@Library('main') _
runCiPipeline([
	jenkinsfile: this,
	projectBaseName: 'core',
	includeTestOutputInPipelineLogs: true,
	cloudAccount: 'd1',
	integrationTests : [
		[
			artifactName: 'messaging-integration-test',
			skip: false,
						componentsSettings: [
            				[
            					name: 'tribefire-master',
            					env: [
            						DEMO_CONNECTOR: 'KAFKA',
            						DEMO_MODE_ACTIVE: 'false',
            						DEMO_CONNECTOR_URL: 'kafka-dev.kafka.svc.cluster.local:9092',
            						DEMO_CONNECTOR_TOPIC: 'demoKafkaTopic'
            					]
				]
			]
		]
	]
])
