@Library('main') _
runCiPipeline(
	jenkinsfile: this,
	projectBaseName: 'core',
	unitTestsEnvironmentVariables: [
		AZURE_CONTAINER_NAME: 'testrku',
		AZURE_CONNECTION_STRING: getSecret('azure-tests-connection-string')
	]
)
