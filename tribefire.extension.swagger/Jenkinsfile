@Library('main') _
runCiPipeline(
	jenkinsfile: this,
	ciNotificationChannel: '#pd-cxz-cicd',
	projectBaseName: 'core',
	integrationTests : [
		[
			artifactName: 'swagger-model-import-integration-test',
			skip: true
		]
	]
)
