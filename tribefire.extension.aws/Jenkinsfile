@Library('main') _

// S3 credentials are read by unit tests and client-side integration tests (i.e., not by the TF services).
withUsernamePasswordCredentialsMaskingSecurePasswordsOnly(credentialsId: 'aws-s3-tests', usernameVariable: 'S3_ACCESS_KEY', passwordVariable: 'S3_SECRET_ACCESS_KEY') {
	runCiPipeline([
		jenkinsfile: this,
		projectBaseName: 'core',
		includeTestOutputInPipelineLogs: true,
		integrationTests : [
			[
				artifactName: 'aws-integration-test',
				skip: false
			]
		]
	])
}