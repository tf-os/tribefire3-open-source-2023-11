@Library('main') _
runCiPipeline(
	jenkinsfile: this,
	projectBaseName: 'core',
	timeout: '2 hours',
	artifactsWhichNeedToBeRepublishedWhenSolutionListChanges: ['tribefire-js', 'tf-js'],
	mailNotificationRecipients: [
		"michel.docouto@braintribe.com",
		"stefan.prieler@braintribe.com"
	]
)
