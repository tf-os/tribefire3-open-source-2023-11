@Library('main') _
runCiPipeline([
	jenkinsfile: this,
	projectBaseName: 'core',
	includeTestOutputInPipelineLogs: true,
	integrationTests : [
		[
			artifactName: 'tracing-integration-test',
			skip: false
		]
	]	
])
