@Library('main') _
runCiPipeline([
	jenkinsfile: this,
	projectBaseName: 'core',
	includeTestOutputInPipelineLogs: true,
	integrationTests : [
		[
			artifactName: 'metrics-integration-test',
			skip: false
		]
	]	
])
