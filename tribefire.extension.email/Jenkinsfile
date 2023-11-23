@Library('main') _
// provide GMail credentials as ENV (read by client side of tests, i.e. by JUnit tests, hence ENV works)
withUsernamePasswordCredentialsMaskingSecurePasswordsOnly(credentialsId: 'gmail-email-tests', usernameVariable: 'GMAIL_EMAIL', passwordVariable: 'GMAIL_PASSWORD_ENCRYPTED') {
    runCiPipeline(
    	jenkinsfile: this,
    	projectBaseName: 'core'
    )
}
