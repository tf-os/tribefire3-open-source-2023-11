@Library('main') _
runCiPipeline(
	jenkinsfile: this,
	projectBaseName: 'core',
	integrationTests : [
		[
			artifactName: 'elasticsearch-integration-test',
			skip: true, // integration tests won't work in the cloud
			componentsSettings: [
				[
					name: 'tribefire-master',
					env: [
						ELASTIC_HOST: 'localhost',
						ELASTIC_RUN_SERVICE: 'true',
						ELASTIC_ACCESS_INDEX: 'users',
						ELASTIC_CREATE_DEMO_ACCESS: 'true'
					]
				]
			]
		]
	]
)
