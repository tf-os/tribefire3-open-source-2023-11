@Library('main') _

runCiPipeline([
	jenkinsfile: this,
	projectBaseName: 'core',
	integrationTests : [
		[
			artifactName: 'cache-integration-test',
			skip: true
		]
	]
])
