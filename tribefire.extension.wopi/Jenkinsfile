@Library('main')
@Library('adx') _
runCiPipeline(
	jenkinsfile: this,
	projectBaseName: 'adx',
	tribefireRepositoryName: adxHelpers.getCoreRepositoryForAdxVersion('2.10'),
	includeTestOutputInPipelineLogs: true,
	stopAfterFirstTestFailure: false,
	integrationTests : [
		[
			artifactName: 'wopi-integration-test',
			skip: false,
			componentsSettings: [
				[
					name: 'tribefire-master',
					env: [
						WOPI_CREATE_DEFAULT_ACCESS: 'true',
						WOPI_CREATE_DEFAULT: 'true'
					]
				]
			]
		]
	]
)
