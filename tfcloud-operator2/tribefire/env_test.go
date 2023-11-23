package tribefire

import (
	"github.com/stretchr/testify/assert"
	"k8s.io/api/core/v1"
	"os"
	"strings"
	"testing"
	tribefirev1 "tribefire-operator/api/v1"
	. "tribefire-operator/common"
)

var backendParams = []tribefirev1.BackendParam{
	{
		Name:  "url",
		Value: "http://void.me",
	},
	{
		Name:  "username",
		Value: "tribefire",
	},
	{
		Name:  "password",
		Value: "cortex",
	},
}

var env = []v1.EnvVar{
	{
		Name:  "TRIBEFIRE_LOGLEVEL",
		Value: "FINE",
	},
	{
		Name:  "TRIBEFIRE_NODE_ID",
		Value: "123",
	},
}

func TestGetBackendParam(t *testing.T) {
	url := GetBackendParam(backendParams, "url")
	username := GetBackendParam(backendParams, "username")
	password := GetBackendParam(backendParams, "password")

	assert.Equal(t, "http://void.me", url)
	assert.Equal(t, "tribefire", username)
	assert.Equal(t, "cortex", password)
}

func TestAddBackendParam(t *testing.T) {
	params := SetBackendParam(backendParams, "log-level", "DEBUG")
	logLevel := GetBackendParam(params, "log-level")

	assert.Equal(t, "DEBUG", logLevel)
	assert.Equal(t, 4, len(params))
}

func TestSetBackendParam(t *testing.T) {
	params := SetBackendParam(backendParams, "url", "http://no.de")
	url := GetBackendParam(params, "url")

	assert.Equal(t, "http://no.de", url)
	assert.Equal(t, 3, len(params))
}

func TestFindEnvVar(t *testing.T) {
	nodeId := FindEnvVar(env, "TRIBEFIRE_NODE_ID")
	logLevel := FindEnvVar(env, "TRIBEFIRE_LOGLEVEL")

	assert.NotNil(t, nodeId)
	assert.NotNil(t, logLevel)

	assert.Equal(t, "123", nodeId.Value)
	assert.Equal(t, "FINE", logLevel.Value)
}

func TestAddNewEnvVar(t *testing.T) {
	newEnv := UpdateEnvVar(env, "TRIBEFIRE_TENANT_ID", "xyz")
	tenantId := FindEnvVar(newEnv, "TRIBEFIRE_TENANT_ID")

	assert.NotNil(t, newEnv)
	assert.NotNil(t, tenantId)

	assert.Equal(t, "xyz", tenantId.Value)
	assert.Equal(t, 3, len(newEnv))
}

func TestUpdateEnvVar(t *testing.T) {
	newEnv := UpdateEnvVar(env, "TRIBEFIRE_LOGLEVEL", "DEBUG")
	logLevel := FindEnvVar(newEnv, "TRIBEFIRE_LOGLEVEL")

	assert.NotNil(t, newEnv)
	assert.NotNil(t, logLevel)

	assert.Equal(t, "DEBUG", logLevel.Value)

}

func TestCreateDatabaseUrlCloudSqlProxy(t *testing.T) {
	os.Clearenv()
	err := os.Setenv(UseCloudSqlProxy, "true")
	assert.Nil(t, err, "Setting env %s failed: %v", UseCloudSqlProxy, err)

	tf := &tribefirev1.TribefireRuntime{}
	tf.Spec.DatabaseType = tribefirev1.CloudSqlDatabase
	url := buildDatabaseUrl(tf, "demo")
	assert.Equal(t, "jdbc:postgresql://localhost:5432/demo", url)
}

func TestCreateDatabaseUrlLocalPostgres(t *testing.T) {
	os.Clearenv()
	tf := &tribefirev1.TribefireRuntime{}
	tf.Name = "demo"
	tf.Spec.DatabaseType = tribefirev1.LocalPostgresql
	url := buildDatabaseUrl(tf, "demo")
	assert.Equal(t, "jdbc:postgresql://demo-postgres:5432/demo", url)
}

func TestCreateDatabaseUrlCloudSqlNoProxy(t *testing.T) {
	os.Clearenv()
	err := os.Setenv(TribefireSystemDbHostPort, "systemdb.tribefire.cloud:5432")
	assert.Nil(t, err, "Setting env %s failed: %v", UseCloudSqlProxy, err)

	tf := &tribefirev1.TribefireRuntime{}
	tf.Name = "demo"
	tf.Spec.DatabaseType = tribefirev1.CloudSqlDatabase
	url := buildDatabaseUrl(tf, "cloud-demo")
	assert.Equal(t, "jdbc:postgresql://systemdb.tribefire.cloud:5432/cloud-demo", url)
}

func TestCreateDatabaseUrlCloudSqlNoProxyWithOpts(t *testing.T) {
	os.Clearenv()
	err := os.Setenv(TribefireSystemDbHostPort, "systemdb.tribefire.cloud:5432")
	assert.Nil(t, err, "Setting env %s failed: %v", TribefireSystemDbHostPort, err)

	err = os.Setenv(TribefireSystemDbHostOpts, "?ssl=required")
	assert.Nil(t, err, "Setting env %s failed: %v", TribefireSystemDbHostOpts, err)

	tf := &tribefirev1.TribefireRuntime{}
	tf.Name = "demo"
	tf.Spec.DatabaseType = tribefirev1.CloudSqlDatabase
	url := buildDatabaseUrl(tf, "cloud-ssl-demo")
	assert.Equal(t, "jdbc:postgresql://systemdb.tribefire.cloud:5432/cloud-ssl-demo?ssl=required", url)
}

func TestHandleAdditionalDbs(t *testing.T) {
	tf := &tribefirev1.TribefireRuntime{}
	tf.Name = "demo"
	tf.Spec.AdditionalDatabases = []tribefirev1.DatabaseSpec{
		{
			Name:                 "demo",
			Type:                 "local",
			InstanceDescriptor:   "jdbc://1.2.3.4:5432/demo",
			EnvPrefixes:          []string{"PRE1", "PRE2"},
			DatabaseName:         "demo",
			CredentialsSecretRef: v1.SecretReference{Name: "dbCredentials", Namespace: "demo"},
		},
	}

	envVars := handleAdditionalDb(tf)
	expectedEnvVarNames := []string{
		"PRE1_DB_URL", "PRE1_DB_USER", "PRE1_DB_PASS", "PRE1_DB_PASSWORD",
		"PRE2_DB_URL", "PRE2_DB_USER", "PRE2_DB_PASS", "PRE2_DB_PASSWORD",
	}

	var actualEnvVarNames []string
	for _, envVar := range envVars {
		actualEnvVarNames = append(actualEnvVarNames, envVar.Name)
	}

	assert.ElementsMatch(t, actualEnvVarNames, expectedEnvVarNames)
}

func TestCreateCustomSharedJson(t *testing.T) {
	tf := &tribefirev1.TribefireRuntime{}
	tf.Name = "demo"

	sharedJson := createCustomSharedJson(tf, SharedJsonEtcd, "demo")
	assert.False(t, strings.Contains(sharedJson, "tribefire-dcsa-shared-storage"))

	tf.Spec.Dcsa.InstanceDescriptor = "jdbc:postgresql://1.2.3.4:5432/demo-dcsa"
	tf.Spec.Dcsa.CredentialsSecretRef.Name = "demo-dcsa"
	sharedJson = createCustomSharedJson(tf, SharedJsonEtcd, "demo")
	assert.True(t, strings.Contains(sharedJson, "tribefire-dcsa-shared-storage"))
	assert.True(t, strings.Contains(sharedJson, "jdbc:postgresql://1.2.3.4:5432/demo-dcsa"))
	assert.True(t, strings.Contains(sharedJson, "tribefire-transient-messaging-data-db"))
}
