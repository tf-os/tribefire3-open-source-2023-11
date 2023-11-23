package common

import "os"

const (
	OperatorNamespace                      = "OPERATOR_WATCH_NAMESPACE"
	OperatorNamePrefix                     = "OPERATOR_NAME_PREFIX"
	OperatorExtendedLogging                = "OPERATOR_LOGGING_EXTENDED"
	OperatorJsonLogging                    = "OPERATOR_LOGGING_JSON"
	OperatorDevelopmentLogging             = "OPERATOR_LOGGING_DEVELOPMENT"
	OperatorLogLevel                       = "OPERATOR_LOG_LEVEL"
	OperatorDisableNameParsingForIngress   = "OPERATOR_DISABLE_NAME_PARSING_FOR_INGRESS"
	OperatorEnableShortDomainNames         = "OPERATOR_ENABLE_SHORT_DOMAIN_NAMES"
	OperatorEnableSameGeneration           = "OPERATOR_ENABLE_SAME_GENERATION_RECONCILE"
	OperatorWebhooksDisabled               = "OPERATOR_WEBHOOKS_DISABLED"
	UsePostgresCheckerInitContainer        = "TRIBEFIRE_USE_POSTGRES_CHECKER_INIT_CONTAINER"
	UseCloudSqlProxy                       = "TRIBEFIRE_USE_CLOUDSQL_PROXY"
	TribefireSystemDbHostPort              = "TRIBEFIRE_SYSTEM_DB_HOST_PORT"
	TribefireSystemDbHostOpts              = "TRIBEFIRE_SYSTEM_DB_OPTS"
	CustomCartridgeHealthCheckUri          = "CUSTOM_CARTRIDGE_HEALTH_CHECK_URI"
	CustomCartridgeReadinessCheckUri       = "CUSTOM_CARTRIDGE_READINESS_CHECK_URI"
	UseDefaultComponentResourceConstraints = "USE_DEFAULT_COMPONENT_RESOURCE_CONSTRAINTS"
	UsePodPriorityClasses                  = "USE_POD_PRIORITY_CLASSES"
	CustomHealthCheckUri                   = "CUSTOM_HEALTH_CHECK_URI"
)

func DisableWebhooks() bool {
	return os.Getenv(OperatorWebhooksDisabled) == "true"
}

func EnableSameGenerationReconcile() bool {
	return os.Getenv(OperatorEnableSameGeneration) == "true"
}

func DevelopmentLogging() bool {
	return os.Getenv(OperatorDevelopmentLogging) == "true"
}

func LogLevel() string {
	return os.Getenv(OperatorLogLevel)
}

func WatchNamespace() string {
	return os.Getenv(OperatorNamespace)
}

func NamePrefix() string {
	return os.Getenv(OperatorNamePrefix)
}

func JsonLoggingEnabled() bool {
	return os.Getenv(OperatorJsonLogging) == "true"
}

func ExtendedLoggingEnabled() bool {
	return os.Getenv(OperatorExtendedLogging) == "true"
}

func PostgresCheckerEnabled() bool {
	return os.Getenv(UsePostgresCheckerInitContainer) == "true"
}

func CloudSqlProxyEnabled() bool {
	return os.Getenv(UseCloudSqlProxy) == "true"
}

func SystemDbHostPort() string {
	return os.Getenv(TribefireSystemDbHostPort)
}

func SystemDbOpts() string {
	return os.Getenv(TribefireSystemDbHostOpts)
}

func DisableNameParsingForIngress() bool {
	return os.Getenv(OperatorDisableNameParsingForIngress) == "true"
}

func EnableShortDomainNames() bool {
	return os.Getenv(OperatorEnableShortDomainNames) == "true"
}

func CustomCartridgeHealthCheckPath() string {
	return os.Getenv(CustomCartridgeHealthCheckUri)
}
func CustomCartridgeReadinessCheckPath() string {
	return os.Getenv(CustomCartridgeReadinessCheckUri)
}

func DefaultComponentResourceConstraintsEnabled() bool {
	return os.Getenv(UseDefaultComponentResourceConstraints) == "true"
}

func PodPriorityClassesEnabled() bool {
	return os.Getenv(UsePodPriorityClasses) == "true"
}

func CustomHealthCheckPath() string {
	return os.Getenv(CustomHealthCheckUri)
}
