@Library('main') _
runCiPipeline(
	jenkinsfile: this,
	ciNotificationChannel: '#pd-cxz-cicd',
	projectBaseName: 'core',
	integrationTests : [
                [
                        artifactName: 'ws-processing-integration-test',
                        skip: true
                ]
        ]
)
