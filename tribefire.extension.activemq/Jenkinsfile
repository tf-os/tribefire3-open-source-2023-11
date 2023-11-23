@Library('main') _
runCiPipeline([
	jenkinsfile: this,
	projectBaseName: 'core',
	integrationTests : [
		[
			artifactName: 'active-mq-server-integration-test',
			skip: true
		]
	]
])
