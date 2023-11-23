@Library('main') _
withUsernamePasswordCredentialsMaskingSecurePasswordsOnly(credentialsId: 'artifactory-library-tests', usernameVariable: 'ARTIFACTORY_USERNAME', passwordVariable: 'ARTIFACTORY_PASSWORD') {
	runCiPipeline(
		jenkinsfile: this,
		ciNotificationChannel: '#pd-cxz-cicd',
		projectBaseName: 'core',
		integrationTests: [
			[
				artifactName: 'library-integration-test',
				componentsSettings: [
					[
						name: 'tribefire-master',
						env: [
							LIBRARY_REPOSITORY_USERNAME: ARTIFACTORY_USERNAME, 
							LIBRARY_REPOSITORY_PASSWORD_ENC: ARTIFACTORY_PASSWORD, 
							LIBRARY_RAVENHURST_URL: globals.ravenhurstBaseUrl(), 
							LIBRARY_REPOSITORY_URL: globals.artifactoryBaseUrl(),
						]
					]
				]
			]
		]
	)
}
