@Library('main') _
runCiPipeline(
	jenkinsfile: this,
	projectBaseName: 'core',
	integrationTests : [
		[
			artifactName: 'gcp-integration-test',
			skip: true // skipped because currently there are no GCP credentials configured
		]
	]
)
