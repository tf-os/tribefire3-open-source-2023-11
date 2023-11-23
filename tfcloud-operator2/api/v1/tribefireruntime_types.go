package v1

import (
	"fmt"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"strings"
)

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object
type TribefireRuntimeList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata"`
	Items           []TribefireRuntime `json:"items"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object
// +k8s:openapi-gen=true
// +kubebuilder:resource:shortName=tf
// +kubebuilder:subresource:status
// +kubebuilder:printcolumn:name="Status",type=string,JSONPath=`.status.status`
// +kubebuilder:printcolumn:name="Age",type=date,JSONPath=`.metadata.creationTimestamp`
// +kubebuilder:printcolumn:name="Domain",type=boolean,JSONPath=`.spec.domain`,description="The domain used for the public URL (ingress)"
// +kubebuilder:printcolumn:name="Database",type="string",JSONPath=`.spec.databaseType`,description="Either local (Postgres) or cloudSQL (Google)"
// +kubebuilder:printcolumn:name="Backend",type="string",JSONPath=`.spec.backendType`,description="The messaging backend used for this runtime. Currently etcd or activemq are supported"
// +kubebuilder:printcolumn:name="Unavailable",type="string",JSONPath=`.status.components[?(@.status=="unavailable")].name`,description="List of unavailable components"

type TribefireRuntime struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata"`
	Spec              TribefireSpec   `json:"spec"`
	Status            TribefireStatus `json:"status,omitempty"`
}

// <h5>Description</h5>
// This identified the type of the component, that could be for instance `control-center` or `modeler`
// <h5>Possible Values</h5>
// `services`, `control-center`, `modeler`, `explorer`, `web-reader`, `cartridge`
type ComponentType string

// <h5>Description</h5>
// The overall status of the related component, can be used as an indicator on health/availability of the component.
// `degraded` means "partially available", i.e. when not all replicas of a component are available.
// <h5>Possible Values</h5>
// `available`, `degraded`, `unavailable`
type ComponentStatus string

type ComponentStatusChangeReason string

// <h5>Description</h5>
// The current phase of the TribefireRuntime deployment. This can be used to determine how
// far the deployment has progressed, and can also be used to understand what went wrong. For instance,
// if a TribefireRuntime deployment does not progress any more and is in phase `DatabaseBootstrap`, it might
// indicate that something went wrong during the provisioning of the database.
// <h5>Possible Values</h5>
// `TribefireValidation`, `DatabaseBootstrap`, `SecretBootstrap`, `BackendBootstrap`, `ComponentDeployment`
// `TribefireRunning`
type DeploymentPhase string

// <h5>Description</h5>
// Every `TribefireRuntimeComponent` can have a custom environment that is structured the same as the standard `env`
// section on a `container`. Use it to control the environment variables that a `TribefireRuntimeComponent` container
// sees.
// <h5>Example</h5>
// ```
// - name: "PRODUCTION_MODE"
// value: "true"
// ```
type CustomEnvironment map[string]string

// <h5>Description</h5>
// The backend that should be used for messaging, locking and leadership. Please note that in case of `etcd`, you must
// take care of the provisioning of the etcd cluster yourself. ActiveMQ will be deployed by the operator, every
// TribefireRuntime gets its own ActiveMQ instance.
// <h5>Possible Values</h5>
// `activemq`, `etcd`
// <h5>Default</h5>
// `etcd`
type MessagingBackend string

// <h5>Description</h5>
// The type of the referenced database. If type is `cloudsql`, a Google CloudSQL database will be provisioned for this
// TribefireRuntime. If type is `local`, a local postgresql container will be bootstrapped.
// <h5>Possible Values</h5>
// `cloudsql`, `local`
// <h5>Example</h5>
// `cloudsql`
type DatabaseType string

const (
	Services      ComponentType = "services"
	ControlCenter ComponentType = "control-center"
	Modeler       ComponentType = "modeler"
	Explorer      ComponentType = "explorer"
	WebReader     ComponentType = "web-reader"
	Cartridge     ComponentType = "cartridge"
)

const (
	Available   ComponentStatus = "available"
	Degraded    ComponentStatus = "degraded"
	Unavailable ComponentStatus = "unavailable"
)

const (
	ComponentAvailable   ComponentStatusChangeReason = "ComponentAvailable"
	ComponentUnavailable ComponentStatusChangeReason = "ComponentUnavailable"
	ComponentDegraded    ComponentStatusChangeReason = "ComponentDegraded"
	ComponentUrlChanged  ComponentStatusChangeReason = "ComponentUrlChanged"
)

const (
	TribefireValidation DeploymentPhase = "TribefireValidation"
	DatabaseBootstrap   DeploymentPhase = "DatabaseBootstrap"
	SecretBootstrap     DeploymentPhase = "SecretBootstrap"
	BackendBootstrap    DeploymentPhase = "BackendBootstrap"
	ComponentDeployment DeploymentPhase = "ComponentDeployment"
	TribefireRunning    DeploymentPhase = "TribefireRunning"
	RbacBootstrap       DeploymentPhase = "RbacBootstrap"
)

const (
	EtcdBackend     MessagingBackend = "etcd"
	ActiveMqBackend MessagingBackend = "activemq"
)

const (
	CloudSqlDatabase DatabaseType = "cloudsql"
	LocalPostgresql  DatabaseType = "local"
)

var DefaultEtcdParams = []BackendParam{
	{
		Name:  "url",
		Value: "http://etcd-tribefire:2379",
	},
}

// +kubebuilder:doc:note=this is a note
// +kubebuilder:doc:warning=this is a warning
// +k8s:openapi-gen=true
// The top level description of a `TribefireRuntime`. Describes the list of desired components, which backend to use
// and more.
type TribefireSpec struct {
	// the domain name under which this TribefireRuntime will be reachable (via the Ingress). For instance,
	// if the domain is set to `tribefire.cloud` , the name of the runtime is `demo`, and the namespace of the
	// runtime is `documents`, then the TribefireRuntime will be available via `https:/demo-documents.tribefire.cloud/`
	// <br/><br/>
	// Possible values: any valid DNS name
	Domain string `json:"domain,omitempty"`

	// +kubebuilder:validation:Enum=cloudsql;local
	// The type of the database for this TribefireRuntime
	DatabaseType DatabaseType `json:"databaseType,omitempty"`

	// Any additional (external) database that the runtime might need, e.g. Documents database
	AdditionalDatabases []DatabaseSpec `json:"databases,omitempty"`

	// The backend configuration for this TribefireRuntime, e.g. configuration for `etcd` backend
	Backend BackendConfiguration `json:"backend,omitempty"`

	// config element for DCSA support
	Dcsa DcsaConfig `json:"dcsaConfig,omitempty"`

	// The list of components for this TribefireRuntime, i.e. `services`, 'control-center` and others
	Components []TribefireComponent `json:"components"`
}

// The configuration element for DCSA support
type DcsaConfig struct {

	// The connection string that identifies this database. Currently only JDBC URLs are supported <br/><br/>
	// Example: `jdbc:postgresql://1.2.3.4:5432/documents-demo`
	InstanceDescriptor string `json:"instanceDescriptor"`

	// This `secretRef` points to the secret that contains the database credentials, i.e. username and password
	// to connect to the database. The secret itself should have a `username` and a `password` key that have the
	// related values. <br/><br/>
	// Example: `database-credentials-secret`
	CredentialsSecretRef corev1.SecretReference `json:"credentialsSecretRef"`
}

// This element is used to set specific properties for the chosen Tribefire backend, e.g. URLs and credentials
type BackendParam struct {
	// name of the backend property<br/><br/>
	// Examples: `username`, `password`, `url`
	Name string `json:"name"`

	// the value of the backend configuration property
	// Examples: `https://tf-etcd.etcd.svc.cluster.local`, `etcd-user`, `etcd-password`
	Value string `json:"value"`
}

// This element is used to set the Tribefire backend as well as
// the configuration parameters for the chosen Tribefire backend.
// Currently, `etcd` and `activemq` are supported
type BackendConfiguration struct {
	// +kubebuilder:validation:Enum=etcd;activemq
	// The backend for Tribefire<br/><br/>
	// Possible values: `activemq`, `etcd` (note: ActiveMQ is supported for compatibility only)
	Type MessagingBackend `json:"type,omitempty"`

	// Configuration properties for the chosen Tribefire backend, e.g. the `etcd` URL<br/><br/>
	// Defaults (for etcd): `name='url'` `value='http://tf-etcd-cluster-client:2379'`
	Params []BackendParam `json:"parameters,omitempty"`
}

// this spec is used to attach external/custom databases
// the DatabaseDescriptor is used to refer to the existing database, e.g.
//
//	braintribe-databases:europe-west3:general-purpose=tcp:5555
type DatabaseSpec struct {
	// +kubebuilder:validation:MaxLength=63
	// +kubebuilder:validation:MinLength=3
	// a symbolic name to refer to this database.<br/><br/>
	// Example: `tribecell-production`
	Name string `json:"name"`

	// +kubebuilder:validation:Enum=cloudsql;local
	// The type of this database. If type is `cloudsql`, the operator will provision a Google CloudSQL database.
	// If type is `local`, the operator will deploy a local PostgreSQL container<br/><br/>
	// Possible Values: `cloudsql`, `local` <br/>
	// Example: `cloudsql`
	Type DatabaseType `json:"type"`

	// todo think about validation kubebuilder:validation:Pattern=^[^:]+:[^:]+:[^\=]\=tcp:[0-9]+$

	// The descriptor/connection string that identifies this database. This can either be a CloudSQL instance
	// descriptor or a JDBC url. <br/><br/>
	// Example: `jdbc:postgresql://1.2.3.4:5432/documents-demo`
	InstanceDescriptor string `json:"instanceDescriptor"`

	// For every database described by a `DatabaseSpec` there will be a set of environment variables exposed in the
	// `tribefire-services` pods. This set of env vars  contains the following variables:
	// <ul>
	//   <li> `${PREFIX}_DB_URL`
	//   <li> `${PREFIX}_DB_USER`
	//   <li> `${PREFIX}_DB_PASS`
	// </ul>
	// the values for this variables is taken from the `credentialsSecretRef` as well as the `instanceDescriptor` <br/><br/>
	// Example: `DOCUMENTS`
	EnvPrefixes []string `json:"envPrefixes"`

	// +kubebuilder:validation:MaxLength=60
	// +kubebuilder:validation:MinLength=3
	// The name of the database<br/><br/>
	// Example: `documents`
	DatabaseName string `json:"databaseName,omitempty"`

	// This `secretRef` points to the secret that contains the service account manifest for CloudSQL. Only needed
	// in case you want to connect to a CloudSQL database via the cloud-sql-proxy<br/><br/>
	// Example: `documents-cloudsql-secret`
	ServiceAccountSecretRef corev1.SecretReference `json:"serviceAccountSecretRef,omitempty"`

	// This points to the key of the `serviceAccountSecretRef` where to take the service account JSON from<br/><br/>
	// Example: `service-account.json`
	ServiceAccountSecretKey string `json:"serviceAccountSecretKey,omitempty"`

	// This `secretRef` points to the secret that contains the database credentials, i.e. username and password
	// to connect to the database. The secret itself should have a `username` and a `password` key that have the
	// related values. <br/><br/>
	// Example: `database-credentials-secret`
	CredentialsSecretRef corev1.SecretReference `json:"credentialsSecretRef"`
}

// a TribefireVolume is used to attach persistent storage to a component
type TribefireVolume struct {
	// +kubebuilder:validation:MaxLength=63
	// symbolic name of the volume<br/><br/>
	// Example: `nfs-documents`
	Name string `json:"name"`

	// +kubebuilder:validation:MaxLength=63
	// The name of the underlying `PersistentVolumeClaim`. Please note that you need to setup
	// the PVC before referencing it here.<br/><br/>
	// Example: `nfs-documents-claim`
	VolumeClaimName string `json:"volumeClaimName"`

	// +kubebuilder:validation:Pattern=^(/)?([^/\0]+(/)?)+$
	// The mount path where the PVC should be available inside the Tribefire pods.<br/><br/>
	// Example: `/nfs/documents`
	VolumeMountPath string `json:"volumeMountPath"`
}

type TribefireComponent struct {
	// +kubebuilder:validation:MaxLength=31
	// +kubebuilder:validation:MinLength=3
	// the name of this component
	Name string `json:"name"`

	// type of this component, e.g.`services` or `control-center`
	Type ComponentType `json:"type"`

	// +kubebuilder:validation:MinLength=10
	// Docker image to be used for this component.<br/><br/>
	// Example: `dockerregistry.example.com/cloud/tribefire-master`
	Image string `json:"image,omitempty"`

	// The image tag for the referenced Docker image<br/><br/>
	// Example: `2.0-latest`
	ImageTag string `json:"imageTag,omitempty"`

	// +kubebuilder:validation:Enum=SEVERE;WARNING;INFO;CONFIG;FINE;FINER;FINEST
	// The loglevel for this component.<br/><br/>
	// Possible values: `SEVERE`,`WARNING`,`INFO`,`CONFIG`,`FINE`,`FINER`,`FINEST`
	LogLevel string `json:"logLevel,omitempty"`

	// +kubebuilder:validation:Pattern=^/[a-z-]*$|^$
	// absolute path (starting with `/`) for this component<br/><br/>
	// Example: `/tribefire-control-center`
	ApiPath string `json:"apiPath,omitempty"`

	// +kubebuilder:validation:Maximum=10
	// +kubebuilder:validation:Minimum=1
	// number of replicas for this component<br/><br/>
	// Possible values: `1-10`
	Replicas int32 `json:"replicas,omitempty"`

	// +kubebuilder:validation:Pattern=`^https?:\/\/.*$`
	// The publicly accessible URL for this component<br/><br/>
	// Example: `https://demo-documents.tribefire.cloud/services`
	PublicUrl string `json:"publicUrl,omitempty"`

	// The environment for this component. Just a standard `env` section as in `ContainerSpec`
	Env []corev1.EnvVar `json:"env,omitempty"`

	// Use JSON logging for this component<br/><br/>
	// Possible values: `true`, `false`
	LogJson string `json:"logJson,omitempty"`

	// The resource requirements for this component. Standard `ResourceRequirements` as per `PodSpec`
	Resources corev1.ResourceRequirements `json:"resources,omitempty"`

	// Set of labels that should be attached to this component
	Labels map[string]string `json:"labels,omitempty"`

	// +kubebuilder:validation:Enum=http;https
	// The protocol that should be used for the public URL <br/><br/>
	// Possible values: `http`, `https`
	Protocol string `json:"protocol,omitempty"`

	// list of volumes that should be attached to this component. Should be used together with `persistentVolumeClaims`
	Volumes []TribefireVolume `json:"persistentVolumes,omitempty"`

	// enables remote debugging capabilities via JPDA<br/><br/>
	// Possible values: `true`, `false`
	EnableJpda string `json:"enableJpda,omitempty"`

	// can be used to specify a custom health check endpoint URI. The default is `/healthz` for non-cartridge
	// components and `/` for cartridges
	CustomHealthCheckPath string `json:"customHealthCheckPath,omitempty"`

	// set of labels for controlling node affinity
	NodeSelector map[string]string `json:"nodeSelector,omitempty"`
}

func (tf *TribefireRuntime) IsLocalDomain() bool {
	return tf.Spec.Domain == DefaultDomain
}

func (tf *TribefireRuntime) IsLocalDatabase() bool {
	return tf.Spec.DatabaseType == LocalPostgresql
}

func (tf *TribefireRuntime) HasAdditionalDatabases() bool {
	return len(tf.Spec.AdditionalDatabases) > 0
}

func (tf *TribefireRuntime) String() string {
	return fmt.Sprintf("name=%s status=%s", tf.Name, tf.Status.Message)
}

func (tf *TribefireRuntime) IsDcsaEnabled() bool {
	return tf.Spec.Dcsa.InstanceDescriptor != "" && tf.Spec.Dcsa.CredentialsSecretRef.Name != ""
}

func (ic *TribefireComponent) String() string {
	return fmt.Sprintf("name=%s type=%s image=%s image-tag=%s log-level=%s replicas=%d apiPath=%s publicUrl:%s "+
		"logJson:%s requestCpu:%s requestMemory:%s limitsCpu:%s limitsMemory:%s",
		ic.Name, ic.Type, ic.Image, ic.ImageTag, ic.LogLevel, ic.Replicas, ic.ApiPath,
		ic.PublicUrl, ic.LogJson, ic.Resources.Requests.Cpu().String(),
		ic.Resources.Requests.Memory().String(), ic.Resources.Limits.Cpu().String(), ic.Resources.Limits.Memory().String())
}

func (d *DatabaseSpec) String() string {
	return fmt.Sprintf("name=%s instance=%s", d.Name, d.InstanceDescriptor)
}

//
// status related types and implementations
//

// status information about a specific component
type TribefireComponentStatus struct {
	// name of this component<br/><br/>
	// Examples: `services`, `control-center`
	Name string `json:"name"`

	// status of this component<br/><br/>
	// Examples: `available`, `unavailable`, `degraded`
	Status ComponentStatus `json:"status"`

	// the URL(s) under which this component is reachable from the outside (via the ingress)<br/><br/>
	// Examples: `https://demo-documents.tribefire.cloud/services`,`https://demo-documents.tribefire.cloud/control-center`,
	Endpoints []string `json:"urls,omitempty"`
}

// High level status information for this TribefireRuntime
type TribefireStatus struct {
	// error status indicator. If set to `true`, somethings wrong with this TribefireRuntime. <br/><br/>
	// Possible values: `true`, `false`
	Error bool `json:"error,omitempty"`

	// a descriptive status message, such as `available`<br/>
	// Example: `available`
	Message string `json:"status,omitempty"`

	// the `DeploymentPhase` this TribefireRuntime is in. For details see the docs on the `DeploymentPhase`<br/><br/>
	// Example: `DatabaseBootstrap`
	Phase DeploymentPhase `json:"phase,omitempty"`

	// the status conditions for this TribefireRuntime. For details see the docs on the `TribefireRuntimeCondition`
	Conditions []TribefireRuntimeCondition `json:"conditions,omitempty"`

	// The list of `TribefireComponentStatus` information. For details, see the docs on `TribefireComponentStatus`
	ComponentStatus []TribefireComponentStatus `json:"components,omitempty"`

	// This field is used to track changes to the `TribefireRuntimeSpec`
	ObservedGeneration int64 `json:"observedGeneration,omitempty"`

	// Timestamp (ISO8601) when this TribefireRuntime was created. <br/><br/>
	// Example: `2019-03-20T17:41:09Z`
	Created string `json:"created,omitempty"`

	// Timestamp (ISO8601) when this TribefireRuntime was updated. <br/><br/>
	// Example: `2019-03-20T19:36:39ZZ`
	Updated string `json:"updated,omitempty"`
}

// <h5>Description</h5>
// These are valid conditions of a deployment. A condition is a standard Kubernetes concept that can be used
// to track lifecycle changes, e.g. when a component gets available or unavailable. For instance, you can
// `kubectl wait --for=condition=Available` to block until the TribefireRuntime is fully available.
// <h5>Possible Values</h5>
// `Available`, `EtcdAvailable`, `Progressing`
type TribefireRuntimeConditionType string

const (
	TribefireRuntimeAvailable   TribefireRuntimeConditionType = "Available"
	EtcdBackendAvailable        TribefireRuntimeConditionType = "EtcdAvailable"
	TribefireRuntimeProgressing TribefireRuntimeConditionType = "Progressing"
)

// DeploymentCondition describes the state of a deployment at a certain point.
type TribefireRuntimeCondition struct {
	// Type of deployment condition.
	Type TribefireRuntimeConditionType `json:"type"`
	// Status of the condition, one of True, False, Unknown.
	Status corev1.ConditionStatus `json:"status"`
	// The last time this condition was updated.
	LastUpdateTime metav1.Time `json:"lastUpdateTime,omitempty"`
	// Last time the condition transitioned from one status to another.
	LastTransitionTime metav1.Time `json:"lastTransitionTime,omitempty"`
	// The reason for the condition's last transition.
	Reason string `json:"reason,omitempty"`
	// A human readable message indicating details about the transition.
	Message string `json:"message,omitempty"`
}

func (s *TribefireComponentStatus) String() string {
	return fmt.Sprintf("name=%s status=%s endpoints=%s",
		s.Name, s.Status, strings.Join(s.Endpoints[:], ","))
}

func init() {
	SchemeBuilder.Register(&TribefireRuntime{}, &TribefireRuntimeList{})
}
