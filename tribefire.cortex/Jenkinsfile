@Library('main') _
runCiPipeline([
	jenkinsfile: this,
	projectBaseName: 'core',
	timeout: '2 hours',
	ciNotificationChannel: '#pd-cxz-cicd',
	integrationTests : [
		[
			artifactName: 'gm-websocket-server-integration-test',
			skip: true
		],
		[
			artifactName: 'tribefire-jdbc-driver-integration-test',
			skip: true
		]
	],
	unitTestsEnvironmentVariables: [
		DEVROCK_TESTS_REPOSITORY_BASE_URL: globals.artifactoryBaseUrl(),
	]

])
