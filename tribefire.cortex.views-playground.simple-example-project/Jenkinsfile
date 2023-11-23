@Library('cicd@dev')
@Library('main@dev') _
runCiPipeline(
	jenkinsfile: this,
	projectBaseName: 'core',
	useUnreleasedPipelineDockerImage: true,
	integrationTests : [
		[
			artifactName: 'simple-integration-test',
			skip: true
		]
	]
)
