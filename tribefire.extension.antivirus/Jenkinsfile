@Library('main') _
runCiPipeline([
	jenkinsfile: this,
	projectBaseName: 'core',
	includeTestOutputInPipelineLogs: true,
	integrationTests : [
		[
			artifactName: 'antivirus-integration-test',
			skip: false
		]
	]	
])
