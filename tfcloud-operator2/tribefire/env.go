package tribefire

import (
	"fmt"
	"github.com/dchest/uniuri"
	corev1 "k8s.io/api/core/v1"
	"strconv"
	"strings"
	"time"
	tribefirev1 "tribefire-operator/api/v1"

	. "tribefire-operator/common"
)

const LocalModePort = 30080

const RuntimeLogLevel = "TRIBEFIRE_RUNTIME_LOGLEVEL"

//TODO: make pool size configurable

// template for ActiveMQ based TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED
const SharedJsonActiveMq = `[
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "tribefire-mq",
		"denotation": {
			"_type": "com.braintribe.model.messaging.jms.JmsActiveMqConnection",
			"name": "MQ Denotation",
			"hostAddress": "tcp://@@ACTIVEMQ_SERVICE_URL@@"
		}
	},
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "tribefire-leadership-db",
		"denotation": {
			"_type": "com.braintribe.model.deployment.database.pool.HikariCpConnectionPool",
			"externalId": "leadership-db",
			"name": "Leadership DB",
			"minPoolSize": 0,
			"maxPoolSize": 3,
			"connectionDescriptor": {
				"_type": "com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor",
				"user": "$(TF_SYS_DB_USER)",
				"password": "$(TF_SYS_DB_PASSWORD)",
				"url": "@@DB_URL@@",
				"driver": "org.postgresql.Driver"
			}
		}
	},
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "tribefire-locking-db",
		"denotation": {
			"_type": "com.braintribe.model.deployment.database.pool.HikariCpConnectionPool",
			"externalId": "locking-db",
			"name": "Locking DB",
			"minPoolSize": 0,
			"maxPoolSize": 3,
			"connectionDescriptor": {
				"_type": "com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor",
				"user": "$(TF_SYS_DB_USER)",
				"password": "$(TF_SYS_DB_PASSWORD)",
				"url": "@@DB_URL@@",
				"driver": "org.postgresql.Driver"
			}
		}
	},
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "user-sessions-db",
		"denotation": {
			"_type": "com.braintribe.model.deployment.database.pool.HikariCpConnectionPool",
			"externalId": "user-sessions-db",
			"name": "User Sessions DB",
			"minPoolSize": 0,
			"maxPoolSize": 3,
			"connectionDescriptor": {
				"_type": "com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor",
				"user": "$(TF_SYS_DB_USER)",
				"password": "$(TF_SYS_DB_PASSWORD)",
				"url": "@@DB_URL@@",
				"driver": "org.postgresql.Driver"
			}
		}
	}
]`

// template for Etcd based TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED
const SharedJsonEtcd = `
[
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "tribefire-mq",
		"denotation": {
			"_type": "com.braintribe.model.messaging.etcd.EtcdMessaging",
			"project": "@@INITIATIVE_FQ@@",
			"endpointUrls": [
				"@@ETCD_URL@@"
			]
		}
	},
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "tribefire-leadership-manager",
		"denotation": {
			"_type": "com.braintribe.model.plugin.etcd.EtcdPlugableLeadershipManager",
			"project": "@@INITIATIVE_FQ@@",
			"endpointUrls": [
				"@@ETCD_URL@@"
			]
		}
	},
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "tribefire-lock-manager",
		"denotation": {
			"_type": "com.braintribe.model.plugin.etcd.EtcdPlugableLockManager",
			"project": "@@INITIATIVE_FQ@@",
			"endpointUrls": [
				"@@ETCD_URL@@"
			]
		}
	},
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "user-sessions-db",
		"denotation": {
			"_type": "com.braintribe.model.deployment.database.pool.HikariCpConnectionPool",
			"externalId": "user-sessions-db",
			"name": "User Sessions DB",
			"minPoolSize": 0,
			"maxPoolSize": 3,
			"connectionDescriptor": {
				"_type": "com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor",
				"user": "$(TF_SYS_DB_USER)",
				"password": "$(TF_SYS_DB_PASSWORD)",
				"url": "@@DB_URL@@",
				"driver": "org.postgresql.Driver"
			}
		}
	},
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "tribefire-transient-messaging-data-db",
		"denotation": {
			"_type": "com.braintribe.model.deployment.database.pool.HikariCpConnectionPool",
			"externalId": "tribefire-transient-messaging-data-db",
			"name": "TransientMessagingDataDB",
			"minPoolSize": 1,
			"maxPoolSize": 3,
			"connectionDescriptor": {
				"_type": "com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor",
				"user": "$(TF_SYS_DB_USER)",
				"password": "$(TF_SYS_DB_PASSWORD)",
				"url": "@@DB_URL@@",
				"driver": "org.postgresql.Driver"
			}
		}
	},
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "tribefire-locking",
		"denotation": {
			"_type": "com.braintribe.model.deployment.database.pool.HikariCpConnectionPool",
			"externalId": "tribefire-locking",
			"name": "Locking Connection Pool",
			"minPoolSize": 1,
			"maxPoolSize": 3,
			"connectionDescriptor": {
				"_type": "com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor",
				"user": "$(TF_SYS_DB_USER)",
				"password": "$(TF_SYS_DB_PASSWORD)",
				"url": "@@DB_URL@@",
				"driver": "org.postgresql.Driver"
			}
		}
	}
	@@DCSA_CONFIG_JSON@@
]`

const DcsaConfigJson = `
,{
	"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
	"_id": "8",
	"bindId": "tribefire-dcsa-shared-storage",
	"denotation": {
		"_type": "com.braintribe.model.plugin.jdbc.JdbcPlugableDcsaSharedStorage",
		"_id": "9",
		"driver": "org.postgresql.Driver",
		"password": "${decrypt('@@DB_PASSWORD@@')}",
		"project": "tribefire",
		"url": "@@DB_URL@@",
		"username": "@@DB_USERNAME@@"
	}
}
`

// template for ActiveMQ based TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED - for cartridges
const SharedJsonActiveMqCartridge = `[
	{
		"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
		"bindId": "tribefire-mq",
		"denotation": {
			"_type": "com.braintribe.model.messaging.jms.JmsActiveMqConnection",
			"name": "MQ Denotation",
			"hostAddress": "tcp://@@ACTIVEMQ_SERVICE_URL@@"
		}
	}
]`

// template for Etcd based TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED - for cartridges
const SharedJsonEtcdCartridge = `
[
		{
				"_type": "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
				"bindId": "tribefire-mq",
				"denotation": {
						"_type": "com.braintribe.model.messaging.etcd.EtcdMessaging",
						"project": "@@INITIATIVE_FQ@@",
						"endpointUrls": [
								"@@ETCD_URL@@"
						]
				}
		}
]`

// build the full list of environment vars that are required for running TF master/control-center etc.
func buildEnvVars(tf *tribefirev1.TribefireRuntime, component *tribefirev1.TribefireComponent) *[]corev1.EnvVar {
	baseDomain := buildBaseDomain(tf)
	if component.PublicUrl != "" {
		baseDomain = extractHostNamePort(component.PublicUrl)
	}

	protocol := "https"
	if component.Protocol != "" {
		protocol = component.Protocol
	}

	baseUrl := protocol + "://" + baseDomain
	if tf.IsLocalDomain() {
		baseUrl = "http://" + baseDomain + ":" + strconv.Itoa(LocalModePort)
	}

	tfServicesUrl := "http://" + tf.Name + "-" + MasterAppName + ":" + strconv.Itoa(HttpPort)

	var envVars []corev1.EnvVar

	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_SERVICES_URL", Value: tfServicesUrl})
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_PUBLIC_SERVICES_URL", Value: baseUrl + IngressPathMaster})

	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_EXPLORER_URL", Value: getComponentUrl(baseUrl, tf, tribefirev1.Explorer)})
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_MODELER_URL", Value: getComponentUrl(baseUrl, tf, tribefirev1.Modeler)})
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_WEBREADER_URL", Value: getComponentUrl(baseUrl, tf, tribefirev1.WebReader)})
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_CONTROL_CENTER_URL", Value: getComponentUrl(baseUrl, tf, tribefirev1.ControlCenter)})

	envVars = append(envVars, corev1.EnvVar{Name: "INITIATIVE_NAME", Value: tf.Name})
	envVars = append(envVars, corev1.EnvVar{Name: "WORKSPACE_NAME", Value: tf.Namespace})

	//tf-operator related variables
	//a random number that can used whenever needed
	envVars = append(envVars, corev1.EnvVar{Name: "TF_OPERATOR_RANDOM", Value: randomValue(10)})
	//POD ID in custom variable
	envVars = append(envVars, corev1.EnvVar{Name: "TF_OPERATOR_POD_ID", ValueFrom: &corev1.EnvVarSource{FieldRef: &corev1.ObjectFieldSelector{FieldPath: "metadata.name"}}})

	stage := getLabel(tf, "stage")
	if stage != "" {
		envVars = append(envVars, corev1.EnvVar{Name: "INITIATIVE_STAGE", Value: stage})
	}

	envVars = append(envVars, corev1.EnvVar{Name: RuntimeLogLevel, Value: component.LogLevel})

	if component.Type == tribefirev1.Services || component.Type == tribefirev1.Cartridge {
		envVars = addProcessingEnv(tf, component, envVars)
	}

	if component.Type == tribefirev1.Services {
		envVars = addTribefireMasterEnv(tf, component, envVars)
	}

	if component.Type == tribefirev1.Cartridge {
		envVars = addTribefireCartridgeEnv(tf, component, envVars)
	}

	envVars = append(envVars, corev1.EnvVar{Name: "JSON_LOGGING_ENABLED", Value: component.LogJson})

	// add JPDA variables if component hast JPDA enabled
	if component.EnableJpda == "true" {
		envVars = addJPDAVars(envVars)
	}

	// finally add any custom env vars provided in the TribefireRuntime CR
	envVars = append(envVars, component.Env...)

	return &envVars
}

// adds the environment vars required for JPDA based debugging
func addJPDAVars(envVars []corev1.EnvVar) []corev1.EnvVar {
	//envVars = append(envVars, corev1.EnvVar{Name: "JPDA_ADDRESS", Value: strconv.Itoa(JpdaPort)})
	//return append(envVars, corev1.EnvVar{Name: "JPDA_TRANSPORT", Value: "dt_socket"})
	return append(envVars, corev1.EnvVar{Name: "DEBUG_PORTS_ENABLED", Value: "true"})
}

// get the actual component url - necessary because they can be custom specified and the context is not available; return empty string if not found
func getComponentUrl(baseUrl string, tf *tribefirev1.TribefireRuntime, componentType tribefirev1.ComponentType) string {
	actualApiPath := ""
	for _, component := range tf.Spec.Components {
		if component.Type == componentType {
			actualApiPath = component.ApiPath
			return baseUrl + actualApiPath
		}
	}

	L().Debugf("No component for componentType: '%s' to set environment variable", componentType)
	return actualApiPath
}

// add environment variables related to tribefire master and cartridges
func addProcessingEnv(tf *tribefirev1.TribefireRuntime, c *tribefirev1.TribefireComponent, envVars []corev1.EnvVar) []corev1.EnvVar {
	tenantId := buildRuntimeFqName(tf)

	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_TENANT_ID", Value: tenantId})
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_IS_CLUSTERED", Value: "true"})
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_NODE_ID", Value: "$(TF_OPERATOR_POD_ID)--$(TF_OPERATOR_RANDOM)"})

	return envVars
}

func addTribefireCartridgeEnv(tf *tribefirev1.TribefireRuntime, c *tribefirev1.TribefireComponent, envVars []corev1.EnvVar) []corev1.EnvVar {

	// e.g. http://demo-fire-demo-cartridge:8080
	tribefireLocalBaseUrl := "http://" + tf.Name + "-" + c.Name + ":" + strconv.Itoa(HttpPort)

	var customSharedJson string
	if tf.Spec.Backend.Type == tribefirev1.EtcdBackend {
		customSharedJson = createCustomSharedJsonCartridge(tf, SharedJsonEtcdCartridge)
	} else if tf.Spec.Backend.Type == tribefirev1.ActiveMqBackend {
		customSharedJson = createCustomSharedJsonCartridge(tf, SharedJsonActiveMqCartridge)
	} else {
		customSharedJson = createCustomSharedJsonCartridge(tf, SharedJsonEtcdCartridge)
	}

	//enrich standard environment variables with cartridge specific ones
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_IS_EXTENSION_HOST", Value: "true"})
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_LOCAL_BASE_URL", Value: tribefireLocalBaseUrl})
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED", Value: customSharedJson})

	return envVars
}

// add tribefire master specific environment variables
func addTribefireMasterEnv(
	tf *tribefirev1.TribefireRuntime, c *tribefirev1.TribefireComponent, envVars []corev1.EnvVar) []corev1.EnvVar {

	tfDbName := createDatabaseName(tf)
	tfDbSecret := buildDefaultDbSecretName(tf)
	baseDomain := buildBaseDomain(tf)
	cookieDomain := baseDomain

	if c.PublicUrl != "" {
		cookieDomain = extractHostNamePort(c.PublicUrl)
	}

	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_BINDING_USER_SESSIONS_DS", Value: "user-sessions-db"})

	envVars = append(envVars, corev1.EnvVar{Name: "DEPLOYMENT_TIMESTAMP", Value: time.Now().UTC().Format(time.RFC3339)})
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_COOKIE_PATH", Value: "/"})
	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_COOKIE_DOMAIN", Value: cookieDomain})
	envVars = append(envVars, corev1.EnvVar{
		Name: "TF_SYS_DB_USER", ValueFrom: &corev1.EnvVarSource{
			SecretKeyRef: &corev1.SecretKeySelector{
				LocalObjectReference: corev1.LocalObjectReference{
					Name: tfDbSecret,
				},
				Key: "username",
			},
		},
	})

	envVars = append(envVars, corev1.EnvVar{
		Name: "TF_SYS_DB_PASSWORD", ValueFrom: &corev1.EnvVarSource{
			SecretKeyRef: &corev1.SecretKeySelector{
				LocalObjectReference: corev1.LocalObjectReference{
					Name: tfDbSecret,
				},
				Key: "password",
			},
		},
	})

	if tf.IsDcsaEnabled() {
		L().Debugf("Enabling DCSA env vars for %s", tf.Name)
		envVars = append(envVars, corev1.EnvVar{
			Name: "TF_DCSA_DB_USER", ValueFrom: &corev1.EnvVarSource{
				SecretKeyRef: &corev1.SecretKeySelector{
					LocalObjectReference: corev1.LocalObjectReference{
						Name: tf.Spec.Dcsa.CredentialsSecretRef.Name,
					},
					Key: "username",
				},
			},
		})

		envVars = append(envVars, corev1.EnvVar{
			Name: "TF_DCSA_DB_PASSWORD", ValueFrom: &corev1.EnvVarSource{
				SecretKeyRef: &corev1.SecretKeySelector{
					LocalObjectReference: corev1.LocalObjectReference{
						Name: tf.Spec.Dcsa.CredentialsSecretRef.Name,
					},
					Key: "password",
				},
			},
		})

	}

	if tf.IsLocalDatabase() {
		tfDbName = "postgres"
	}

	var customSharedJson string
	if tf.Spec.Backend.Type == tribefirev1.EtcdBackend {
		customSharedJson = createCustomSharedJson(tf, SharedJsonEtcd, tfDbName)
	} else if tf.Spec.Backend.Type == tribefirev1.ActiveMqBackend {
		customSharedJson = createCustomSharedJson(tf, SharedJsonActiveMq, tfDbName)
	} else {
		customSharedJson = createCustomSharedJson(tf, SharedJsonEtcd, tfDbName)
	}

	envVars = append(envVars, corev1.EnvVar{Name: "TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED", Value: customSharedJson})
	if tf.HasAdditionalDatabases() {
		L().Debugf("Handling additional databases")
		envVars = append(envVars, handleAdditionalDb(tf)...)
	}

	return envVars
}

// prepare the value for TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED by replacing placeholders accordingly, i.e.
// depending on the configured backend type - for master
func createCustomSharedJson(tf *tribefirev1.TribefireRuntime, template string, tfDbName string) string {
	customSharedJson := strings.Replace(template, "@@DB_URL@@", buildDatabaseUrl(tf, tfDbName), -1)
	customSharedJson = prepareCustomSharedJson(tf, customSharedJson)
	if tf.IsDcsaEnabled() {
		dcsaConfig := createDcsaConfig(tf)
		oneLinerDcsaConfig := polishJsonToOneLiner(dcsaConfig)
		customSharedJson = strings.Replace(customSharedJson, "@@DCSA_CONFIG_JSON@@", oneLinerDcsaConfig, -1)
	} else {
		customSharedJson = strings.Replace(customSharedJson, "@@DCSA_CONFIG_JSON@@", "", -1)
	}

	return customSharedJson
}

func createDcsaConfig(tf *tribefirev1.TribefireRuntime) string {
	dcsaConfig := strings.Replace(DcsaConfigJson, "@@DB_URL@@", tf.Spec.Dcsa.InstanceDescriptor, -1)
	dcsaConfig = strings.Replace(dcsaConfig, "@@DB_USERNAME@@", "$(TF_DCSA_DB_USER)", -1)
	return strings.Replace(dcsaConfig, "@@DB_PASSWORD@@", "$(TF_DCSA_DB_PASSWORD)", -1)
}

func buildDatabaseUrl(tf *tribefirev1.TribefireRuntime, dbName string) string {
	if CloudSqlProxyEnabled() {
		return fmt.Sprintf("jdbc:postgresql://localhost:5432/%s", dbName)
	}

	if tf.IsLocalDatabase() {
		postgresLocalModeServiceUrl := tf.Name + "-postgres:5432"
		return fmt.Sprintf("jdbc:postgresql://%s/%s", postgresLocalModeServiceUrl, dbName)
	}

	dbOpts := SystemDbOpts()
	return fmt.Sprintf("jdbc:postgresql://%s/%s%s", SystemDbHostPort(), dbName, dbOpts)
}

// prepare the value for TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED by replacing placeholders accordingly, i.e.
// depending on the configured backend type - for cartridges
func createCustomSharedJsonCartridge(tf *tribefirev1.TribefireRuntime, template string) string {
	customSharedJson := prepareCustomSharedJson(tf, template)
	return customSharedJson
}

func prepareCustomSharedJson(tf *tribefirev1.TribefireRuntime, template string) string {
	customSharedJson := template

	if tf.Spec.Backend.Type == tribefirev1.ActiveMqBackend {
		var activeMqServiceUrl string
		urlFromParam := GetBackendParam(tf.Spec.Backend.Params, "url")
		if urlFromParam != "" {
			activeMqServiceUrl = urlFromParam
		} else {
			activeMqServiceUrl = DefaultResourceName(tf, "activemq") + ":61616"
			if len(tf.Spec.Backend.Params) == 0 {
				tf.Spec.Backend.Params = []tribefirev1.BackendParam{}
			}

			SetBackendParam(tf.Spec.Backend.Params, "url", activeMqServiceUrl) // todo sdk.Update()!
		}

		customSharedJson = strings.Replace(customSharedJson, "@@ACTIVEMQ_SERVICE_URL@@", activeMqServiceUrl, -1)
	} else if tf.Spec.Backend.Type == tribefirev1.EtcdBackend {
		urlFromParam := GetBackendParam(tf.Spec.Backend.Params, "url")
		username := GetBackendParam(tf.Spec.Backend.Params, "username")
		password := GetBackendParam(tf.Spec.Backend.Params, "password")
		customSharedJson = strings.Replace(customSharedJson, "@@ETCD_URL@@", urlFromParam, -1)
		customSharedJson = strings.Replace(customSharedJson, "@@INITIATIVE_FQ@@", createEtcdTribefireRuntimeDefinition(tf), -1)
		customSharedJson = strings.Replace(customSharedJson, "@@ETCD_USERNAME@@", username, -1)
		customSharedJson = strings.Replace(customSharedJson, "@@ETCD_PASSWORD@@", password, -1)
	}

	customSharedJson = polishJsonToOneLiner(customSharedJson)

	return customSharedJson
}

//--- Helper Methods ----

// generate a random value
func randomValue(length int) string {
	random := "RND-" + uniuri.NewLen(length)
	return random
}

func SetBackendParam(params []tribefirev1.BackendParam, name string, value string) []tribefirev1.BackendParam {
	backendParam := tribefirev1.BackendParam{Name: name, Value: value}
	for idx, param := range params {
		if param.Name == name {
			params[idx] = backendParam
			return params
		}
	}

	params = append(params, backendParam)
	return params
}

func GetBackendParam(params []tribefirev1.BackendParam, name string) string {
	for _, param := range params {
		if param.Name == name {
			return param.Value
		}
	}

	return ""
}

// deletes '\n' and ' ' from the json - necessary to be able to read from environment variable
func polishJsonToOneLiner(str string) string {
	str = strings.Replace(str, "\n", "", -1)
	str = strings.Replace(str, " ", "", -1)
	return str
}

// create TribefireRuntime definition for 'project' setting for the etcd configuration - used namespace and tribefire runtime name
func createEtcdTribefireRuntimeDefinition(tf *tribefirev1.TribefireRuntime) (tribefireRuntimeDefinition string) {
	tribefireRuntimeDefinition = tf.Namespace + "-" + tf.Name
	return tribefireRuntimeDefinition
}

// get environment variables as a string for output
func getEnvironmentVariablesString(envVars []corev1.EnvVar) string {

	var env string

	for _, envVar := range envVars {
		env = env + envVar.Name + ":" + envVar.Value + ", "
	}

	//remove attached '; ' at the end
	if len(env) > 2 {
		env = env[0 : len(env)-2]
	}

	return env
}

//
// EnvVar manipulation helpers
//

// find the env var identified by name in given env var array
func FindEnvVar(env []corev1.EnvVar, name string) *corev1.EnvVar {
	for _, envVar := range env {
		if envVar.Name == name {
			return &envVar
		}
	}

	return nil
}

func UpdateEnvVar(env []corev1.EnvVar, name string, value string) []corev1.EnvVar {
	for i, _envVar := range env {
		if name == _envVar.Name {
			env[i].Value = value
			return env
		}
	}

	env = append(env, corev1.EnvVar{Name: name, Value: value})
	return env
}

// handle env vars for additional databases
// instance strings look like this: braintribe-databases:europe-west3:general-purpose=tcp:5555
func handleAdditionalDb(tf *tribefirev1.TribefireRuntime) []corev1.EnvVar {
	var dbEnvVars []corev1.EnvVar

	for _, db := range tf.Spec.AdditionalDatabases {

		for _, keyPrefix := range db.EnvPrefixes {
			L().Debugf("Handling prefix %s for additional database: %s", keyPrefix, db.String())

			var dbUrlEnv corev1.EnvVar

			if strings.HasPrefix(db.InstanceDescriptor, "jdbc:") {
				dbUrlEnv = corev1.EnvVar{Name: keyPrefix + "_DB_URL", Value: db.InstanceDescriptor}
			} else {
				dbUrlEnv = *getJdbcUrlEnvFromDatabaseDescriptor(db, keyPrefix)
			}

			dbUserKey := keyPrefix + "_DB_USER"
			dbUserEnv := corev1.EnvVar{
				Name: dbUserKey,
				ValueFrom: &corev1.EnvVarSource{
					SecretKeyRef: &corev1.SecretKeySelector{
						LocalObjectReference: corev1.LocalObjectReference{
							Name: db.CredentialsSecretRef.Name,
						},
						Key: "username",
					},
				},
			}

			dbPass := &corev1.EnvVarSource{
				SecretKeyRef: &corev1.SecretKeySelector{
					LocalObjectReference: corev1.LocalObjectReference{
						Name: db.CredentialsSecretRef.Name,
					},
					Key: "password",
				},
			}

			dbPassKey := keyPrefix + "_DB_PASS"
			dbPassEnv := corev1.EnvVar{
				Name:      dbPassKey,
				ValueFrom: dbPass,
			}

			dbPasswordKey := keyPrefix + "_DB_PASSWORD"
			dbPasswordEnv := corev1.EnvVar{
				Name:      dbPasswordKey,
				ValueFrom: dbPass,
			}

			dbEnvVars = append(dbEnvVars, dbUrlEnv)
			dbEnvVars = append(dbEnvVars, dbUserEnv)
			dbEnvVars = append(dbEnvVars, dbPassEnv)
			dbEnvVars = append(dbEnvVars, dbPasswordEnv)
		}
	}

	return dbEnvVars
}

func getJdbcUrlEnvFromDatabaseDescriptor(db tribefirev1.DatabaseSpec, keyPrefix string) *corev1.EnvVar {
	// braintribe-databases:europe-west3:general-purpose=tcp:5555
	//                    ^^^^                             ^^^^
	//                   part1                             part2
	instanceDescriptorParts := strings.Split(db.InstanceDescriptor, "=")
	if len(instanceDescriptorParts) != 2 {
		L().Errorf("Cannot setup additional database: Invalid spec.additionalDb.instanceDescriptor: %s",
			db.InstanceDescriptor)

		return nil
	}

	// part2 = "tcp:5555"
	protoPort := strings.Split(instanceDescriptorParts[1], ":")
	if len(protoPort) != 2 {
		L().Errorf("Cannot setup additional database: Invalid proto/port in spec.additionalDb.instanceDescriptor: %s",
			db.InstanceDescriptor)

	}

	dbPort := protoPort[1]
	dbUrlValue := fmt.Sprintf("jdbc:postgresql://localhost:%s/%s", dbPort, db.DatabaseName)

	dbUrlKey := keyPrefix + "_DB_URL"
	return &corev1.EnvVar{
		Name:  dbUrlKey,
		Value: dbUrlValue,
	}
}
