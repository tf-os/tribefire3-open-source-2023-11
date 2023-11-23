@Library('main') _

runCiPipeline([
	jenkinsfile: this,
	projectBaseName: 'core',
	integrationTests : [
		[
			artifactName: 'ldap-integration-test',
			skip: false,
			componentsSettings: [
				[
					name: 'tribefire-master',
					env: [
						LDAP_BASE_USERS: "OU=Users,dc=mydomain,dc=org",
						LDAP_USER_OBJECT_CLASSES: "person",
						LDAP_GROUP_OBJECT_CLASSES: "organizationalUnit",
						LDAP_BASE_GROUPS: "OU=Groups,dc=mydomain,dc=org",
						LDAP_CONN_PASSWORD_ENCRYPTED: "Mhyjr4d1jUsD/1NyCqgZtblV9LEGeLdgEGWPFVW/iUwKmYk7WzPMf6VIk0Xl/OlKS+KWeA==",
						LDAP_CONN_USERNAME: "uid=admin,ou=system",
						LDAP_USER_ID: "uid",
						LDAP_CONN_URL: "ldap://__CARTRIDGE_URL_PREFIX__ldap-cartridge:10389",
						LDAP_USER_FILTER: "(uid=%s)",
						LDAP_USER_MAIL: "postalAddress",
						LDAP_USER_NAME: "uid",
						LDAP_USE_EMPTY_ASPECTS: "true"
					]
				]
			]
		]
	]
])
