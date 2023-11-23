@Library('main') _
runCiPipeline(
	jenkinsfile: this,
	ciNotificationChannel: '#pd-cxz-cicd',
	projectBaseName: 'core',
	enableAdvancedSettings: true,
	integrationTests : [
		[
			artifactName: 'simple-integration-test',
			skip: true,
		]
	], 
	customFinalStageSteps: { context ->
		echo 'This is an example custom step. We can also access configuration settings from here:'
		echo 'timeout: ' + context.settings.timeout
	}
)
