# Tribefire v1alpha1 API group

<p>Packages:</p>
<ul>
<li>
<a href="#tribefire.cloud">tribefire.cloud</a>
</li>
</ul>
<h2 id="tribefire.cloud">tribefire.cloud</h2>
<p>
<p>Package v1alpha1 contains API Schema definitions for the tribefire v1alpha1 API group</p>
</p>
Resource Types:
<ul></ul>
<h3 id="BackendConfiguration">BackendConfiguration
</h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireSpec">TribefireSpec</a>)
</p>
<p>
<p>This element is used to set the Tribefire backend as well as
the configuration parameters for the chosen Tribefire backend.
Currently, <code>etcd</code> and <code>activemq</code> are supported</p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>type</code></br>
<em>
<a href="#MessagingBackend">
MessagingBackend
</a>
</em>
</td>
<td>
<p>The backend for Tribefire<br/><br/>
Possible values: <code>activemq</code>, <code>etcd</code> (note: ActiveMQ is supported for compatibility only)</p>
</td>
</tr>
<tr>
<td>
<code>parameters</code></br>
<em>
<a href="#BackendParam">
[]BackendParam
</a>
</em>
</td>
<td>
<p>Configuration properties for the chosen Tribefire backend, e.g. the <code>etcd</code> URL<br/><br/>
Defaults (for etcd): <code>name='url'</code> <code>value='http://tf-etcd-cluster-client:2379'</code></p>
</td>
</tr>
</tbody>
</table>
<h3 id="BackendParam">BackendParam
</h3>
<p>
(<em>Appears on:</em>
<a href="#BackendConfiguration">BackendConfiguration</a>)
</p>
<p>
<p>This element is used to set specific properties for the chosen Tribefire backend, e.g. URLs and credentials</p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>name</code></br>
<em>
string
</em>
</td>
<td>
<p>name of the backend property<br/><br/>
Examples: <code>username</code>, <code>password</code>, <code>url</code></p>
</td>
</tr>
<tr>
<td>
<code>value</code></br>
<em>
string
</em>
</td>
<td>
<p>the value of the backend configuration property
Examples: <code>https://tf-etcd.etcd.svc.cluster.local</code>, <code>etcd-user</code>, <code>etcd-password</code></p>
</td>
</tr>
</tbody>
</table>
<h3 id="ComponentStatus">ComponentStatus
(<code>string</code> alias)</p></h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireComponentStatus">TribefireComponentStatus</a>)
</p>
<p>
<p><h5>Description</h5>
The overall status of the related component, can be used as an indicator on health/availability of the component.
<code>degraded</code> means &ldquo;partially available&rdquo;, i.e. when not all replicas of a component are available.
<h5>Possible Values</h5>
<code>available</code>, <code>degraded</code>, <code>unavailable</code></p>
</p>
<h3 id="ComponentStatusChangeReason">ComponentStatusChangeReason
(<code>string</code> alias)</p></h3>
<p>
</p>
<h3 id="ComponentType">ComponentType
(<code>string</code> alias)</p></h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireComponent">TribefireComponent</a>)
</p>
<p>
<p><h5>Description</h5>
This identified the type of the component, that could be for instance <code>control-center</code> or <code>modeler</code>
<h5>Possible Values</h5>
<code>services</code>, <code>control-center</code>, <code>modeler</code>, <code>explorer</code>, <code>web-reader</code>, <code>cartridge</code></p>
</p>
<h3 id="CustomEnvironment">CustomEnvironment
(<code>map[string]string</code> alias)</p></h3>
<p>
<p><h5>Description</h5>
Every <code>TribefireRuntimeComponent</code> can have a custom environment that is structured the same as the standard <code>env</code>
section on a <code>container</code>. Use it to control the environment variables that a <code>TribefireRuntimeComponent</code> container
sees.
<h5>Example</h5></p>
<pre><code>- name: &quot;PRODUCTION_MODE&quot;
value: &quot;true&quot;
</code></pre>
</p>
<h3 id="DatabaseSpec">DatabaseSpec
</h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireSpec">TribefireSpec</a>)
</p>
<p>
<p>this spec is used to attach external/custom databases
the DatabaseDescriptor is used to refer to the existing database, e.g.
braintribe-databases:europe-west3:general-purpose=tcp:5555</p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>name</code></br>
<em>
string
</em>
</td>
<td>
<p>a symbolic name to refer to this database.<br/><br/>
Example: <code>tribecell-production</code></p>
</td>
</tr>
<tr>
<td>
<code>type</code></br>
<em>
<a href="#DatabaseType">
DatabaseType
</a>
</em>
</td>
<td>
<p>The type of this database. If type is <code>cloudsql</code>, the operator will provision a Google CloudSQL database.
If type is <code>local</code>, the operator will deploy a local PostgreSQL container<br/><br/>
Possible Values: <code>cloudsql</code>, <code>local</code> <br/>
Example: <code>cloudsql</code></p>
</td>
</tr>
<tr>
<td>
<code>instanceDescriptor</code></br>
<em>
string
</em>
</td>
<td>
<p>The descriptor/connection string that identifies this database. This can either be a CloudSQL instance
descriptor or a JDBC url. <br/><br/>
Example: <code>jdbc:postgresql://1.2.3.4:5432/documents-demo</code></p>
</td>
</tr>
<tr>
<td>
<code>envPrefixes</code></br>
<em>
[]string
</em>
</td>
<td>
<p>For every database described by a <code>DatabaseSpec</code> there will be a set of environment variables exposed in the
<code>tribefire-services</code> pods. This set of env vars  contains the following variables:
<ul>
<li> <code>${PREFIX}_DB_URL</code>
<li> <code>${PREFIX}_DB_USER</code>
<li> <code>${PREFIX}_DB_PASS</code>
</ul>
the values for this variables is taken from the <code>credentialsSecretRef</code> as well as the <code>instanceDescriptor</code> <br/><br/>
Example: <code>DOCUMENTS</code></p>
</td>
</tr>
<tr>
<td>
<code>databaseName</code></br>
<em>
string
</em>
</td>
<td>
<p>The name of the database<br/><br/>
Example: <code>documents</code></p>
</td>
</tr>
<tr>
<td>
<code>serviceAccountSecretRef</code></br>
<em>
<a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.13/#secretreference-v1-core">
Kubernetes core/v1.SecretReference
</a>
</em>
</td>
<td>
<p>This <code>secretRef</code> points to the secret that contains the service account manifest for CloudSQL. Only needed
in case you want to connect to a CloudSQL database via the cloud-sql-proxy<br/><br/>
Example: <code>documents-cloudsql-secret</code></p>
</td>
</tr>
<tr>
<td>
<code>serviceAccountSecretKey</code></br>
<em>
string
</em>
</td>
<td>
<p>This points to the key of the <code>serviceAccountSecretRef</code> where to take the service account JSON from<br/><br/>
Example: <code>service-account.json</code></p>
</td>
</tr>
<tr>
<td>
<code>credentialsSecretRef</code></br>
<em>
<a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.13/#secretreference-v1-core">
Kubernetes core/v1.SecretReference
</a>
</em>
</td>
<td>
<p>This <code>secretRef</code> points to the secret that contains the database credentials, i.e. username and password
to connect to the database. The secret itself should have a <code>username</code> and a <code>password</code> key that have the
related values. <br/><br/>
Example: <code>database-credentials-secret</code></p>
</td>
</tr>
</tbody>
</table>
<h3 id="DatabaseType">DatabaseType
(<code>string</code> alias)</p></h3>
<p>
(<em>Appears on:</em>
<a href="#DatabaseSpec">DatabaseSpec</a>, 
<a href="#TribefireSpec">TribefireSpec</a>)
</p>
<p>
<p><h5>Description</h5>
The type of the referenced database. If type is <code>cloudsql</code>, a Google CloudSQL database will be provisioned for this
TribefireRuntime. If type is <code>local</code>, a local postgresql container will be bootstrapped.
<h5>Possible Values</h5>
<code>cloudsql</code>, <code>local</code>
<h5>Example</h5>
<code>cloudsql</code></p>
</p>
<h3 id="DcsaConfig">DcsaConfig
</h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireSpec">TribefireSpec</a>)
</p>
<p>
<p>The configuration element for DCSA support</p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>databaseUrl</code></br>
<em>
string
</em>
</td>
<td>
<p>The connection string that identifies this database. Currently only JDBC URLs are supported <br/><br/>
Example: <code>jdbc:postgresql://1.2.3.4:5432/documents-demo</code></p>
</td>
</tr>
<tr>
<td>
<code>credentialsSecretRef</code></br>
<em>
<a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.13/#secretreference-v1-core">
Kubernetes core/v1.SecretReference
</a>
</em>
</td>
<td>
<p>This <code>secretRef</code> points to the secret that contains the database credentials, i.e. username and password
to connect to the database. The secret itself should have a <code>username</code> and a <code>password</code> key that have the
related values. <br/><br/>
Example: <code>database-credentials-secret</code></p>
</td>
</tr>
</tbody>
</table>
<h3 id="DeploymentPhase">DeploymentPhase
(<code>string</code> alias)</p></h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireStatus">TribefireStatus</a>)
</p>
<p>
<p><h5>Description</h5>
The current phase of the TribefireRuntime deployment. This can be used to determine how
far the deployment has progressed, and can also be used to understand what went wrong. For instance,
if a TribefireRuntime deployment does not progress any more and is in phase <code>DatabaseBootstrap</code>, it might
indicate that something went wrong during the provisioning of the database.
<h5>Possible Values</h5>
<code>TribefireValidation</code>, <code>DatabaseBootstrap</code>, <code>SecretBootstrap</code>, <code>BackendBootstrap</code>, <code>ComponentDeployment</code>
<code>TribefireRunning</code></p>
</p>
<h3 id="MessagingBackend">MessagingBackend
(<code>string</code> alias)</p></h3>
<p>
(<em>Appears on:</em>
<a href="#BackendConfiguration">BackendConfiguration</a>)
</p>
<p>
<p><h5>Description</h5>
The backend that should be used for messaging, locking and leadership. Please note that in case of <code>etcd</code>, you must
take care of the provisioning of the etcd cluster yourself. ActiveMQ will be deployed by the operator, every
TribefireRuntime gets its own ActiveMQ instance.
<h5>Possible Values</h5>
<code>activemq</code>, <code>etcd</code>
<h5>Default</h5>
<code>etcd</code></p>
</p>
<h3 id="TribefireComponent">TribefireComponent
</h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireSpec">TribefireSpec</a>)
</p>
<p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>name</code></br>
<em>
string
</em>
</td>
<td>
<p>the name of this component</p>
</td>
</tr>
<tr>
<td>
<code>type</code></br>
<em>
<a href="#ComponentType">
ComponentType
</a>
</em>
</td>
<td>
<p>type of this component, e.g.<code>services</code> or <code>control-center</code></p>
</td>
</tr>
<tr>
<td>
<code>image</code></br>
<em>
string
</em>
</td>
<td>
<p>Docker image to be used for this component.<br/><br/>
Example: <code>docker.artifactory.server/cloud/tribefire-master</code></p>
</td>
</tr>
<tr>
<td>
<code>imageTag</code></br>
<em>
string
</em>
</td>
<td>
<p>The image tag for the referenced Docker image<br/><br/>
Example: <code>2.0-latest</code></p>
</td>
</tr>
<tr>
<td>
<code>logLevel</code></br>
<em>
string
</em>
</td>
<td>
<p>The loglevel for this component.<br/><br/>
Possible values: <code>SEVERE</code>,<code>WARNING</code>,<code>INFO</code>,<code>CONFIG</code>,<code>FINE</code>,<code>FINER</code>,<code>FINEST</code></p>
</td>
</tr>
<tr>
<td>
<code>apiPath</code></br>
<em>
string
</em>
</td>
<td>
<p>absolute path (starting with <code>/</code>) for this component<br/><br/>
Example: <code>/tribefire-control-center</code></p>
</td>
</tr>
<tr>
<td>
<code>replicas</code></br>
<em>
int32
</em>
</td>
<td>
<p>number of replicas for this component<br/><br/>
Possible values: <code>1-10</code></p>
</td>
</tr>
<tr>
<td>
<code>publicUrl</code></br>
<em>
string
</em>
</td>
<td>
<p>The publicly accessible URL for this component<br/><br/>
Example: <code>https://demo-documents.tribefire.cloud/services</code></p>
</td>
</tr>
<tr>
<td>
<code>env</code></br>
<em>
<a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.13/#envvar-v1-core">
[]Kubernetes core/v1.EnvVar
</a>
</em>
</td>
<td>
<p>The environment for this component. Just a standard <code>env</code> section as in <code>ContainerSpec</code></p>
</td>
</tr>
<tr>
<td>
<code>logJson</code></br>
<em>
string
</em>
</td>
<td>
<p>Use JSON logging for this component<br/><br/>
Possible values: <code>true</code>, <code>false</code></p>
</td>
</tr>
<tr>
<td>
<code>resources</code></br>
<em>
<a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.13/#resourcerequirements-v1-core">
Kubernetes core/v1.ResourceRequirements
</a>
</em>
</td>
<td>
<p>The resource requirements for this component. Standard <code>ResourceRequirements</code> as per <code>PodSpec</code></p>
</td>
</tr>
<tr>
<td>
<code>labels</code></br>
<em>
map[string]string
</em>
</td>
<td>
<p>Set of labels that should be attached to this component</p>
</td>
</tr>
<tr>
<td>
<code>protocol</code></br>
<em>
string
</em>
</td>
<td>
<p>The protocol that should be used for the public URL <br/><br/>
Possible values: <code>http</code>, <code>https</code></p>
</td>
</tr>
<tr>
<td>
<code>persistentVolumes</code></br>
<em>
<a href="#TribefireVolume">
[]TribefireVolume
</a>
</em>
</td>
<td>
<p>list of volumes that should be attached to this component. Should be used together with <code>persistentVolumeClaims</code></p>
</td>
</tr>
<tr>
<td>
<code>enableJpda</code></br>
<em>
string
</em>
</td>
<td>
<p>enables remote debugging capabilities via JPDA<br/><br/>
Possible values: <code>true</code>, <code>false</code></p>
</td>
</tr>
<tr>
<td>
<code>customHealthCheckPath</code></br>
<em>
string
</em>
</td>
<td>
<p>can be used to specify a custom health check endpoint URI. The default is <code>/healthz</code> for non-cartridge
components and <code>/</code> for cartridges</p>
</td>
</tr>
</tbody>
</table>
<h3 id="TribefireComponentStatus">TribefireComponentStatus
</h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireStatus">TribefireStatus</a>)
</p>
<p>
<p>status information about a specific component</p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>name</code></br>
<em>
string
</em>
</td>
<td>
<p>name of this component<br/><br/>
Examples: <code>services</code>, <code>control-center</code></p>
</td>
</tr>
<tr>
<td>
<code>status</code></br>
<em>
<a href="#ComponentStatus">
ComponentStatus
</a>
</em>
</td>
<td>
<p>status of this component<br/><br/>
Examples: <code>available</code>, <code>unavailable</code>, <code>degraded</code></p>
</td>
</tr>
<tr>
<td>
<code>urls</code></br>
<em>
[]string
</em>
</td>
<td>
<p>the URL(s) under which this component is reachable from the outside (via the ingress)<br/><br/>
Examples: <code>https://demo-documents.tribefire.cloud/services</code>,<code>https://demo-documents.tribefire.cloud/control-center</code>,</p>
</td>
</tr>
</tbody>
</table>
<h3 id="TribefireRuntime">TribefireRuntime
</h3>
<p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>metadata</code></br>
<em>
<a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.13/#objectmeta-v1-meta">
Kubernetes meta/v1.ObjectMeta
</a>
</em>
</td>
<td>
Refer to the Kubernetes API documentation for the fields of the
<code>metadata</code> field.
</td>
</tr>
<tr>
<td>
<code>spec</code></br>
<em>
<a href="#TribefireSpec">
TribefireSpec
</a>
</em>
</td>
<td>
<br/>
<br/>
<table>
<tr>
<td>
<code>domain</code></br>
<em>
string
</em>
</td>
<td>
<p>the domain name under which this TribefireRuntime will be reachable (via the Ingress). For instance,
if the domain is set to <code>tribefire.cloud</code> , the name of the runtime is <code>demo</code>, and the namespace of the
runtime is <code>documents</code>, then the TribefireRuntime will be available via <code>https:/demo-documents.tribefire.cloud/</code>
<br/><br/>
Possible values: any valid DNS name</p>
</td>
</tr>
<tr>
<td>
<code>databaseType</code></br>
<em>
<a href="#DatabaseType">
DatabaseType
</a>
</em>
</td>
<td>
<p>The type of the database for this TribefireRuntime</p>
</td>
</tr>
<tr>
<td>
<code>databases</code></br>
<em>
<a href="#DatabaseSpec">
[]DatabaseSpec
</a>
</em>
</td>
<td>
<p>Any additional (external) database that the runtime might need, e.g. Documents database</p>
</td>
</tr>
<tr>
<td>
<code>backend</code></br>
<em>
<a href="#BackendConfiguration">
BackendConfiguration
</a>
</em>
</td>
<td>
<p>The backend configuration for this TribefireRuntime, e.g. configuration for <code>etcd</code> backend</p>
</td>
</tr>
<tr>
<td>
<code>dcsaConfig</code></br>
<em>
<a href="#DcsaConfig">
DcsaConfig
</a>
</em>
</td>
<td>
<p>config element for DCSA support</p>
</td>
</tr>
<tr>
<td>
<code>components</code></br>
<em>
<a href="#TribefireComponent">
[]TribefireComponent
</a>
</em>
</td>
<td>
<p>The list of components for this TribefireRuntime, i.e. <code>services</code>, &lsquo;control-center` and others</p>
</td>
</tr>
</table>
</td>
</tr>
<tr>
<td>
<code>status</code></br>
<em>
<a href="#TribefireStatus">
TribefireStatus
</a>
</em>
</td>
<td>
</td>
</tr>
</tbody>
</table>
<h3 id="TribefireRuntimeCondition">TribefireRuntimeCondition
</h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireStatus">TribefireStatus</a>)
</p>
<p>
<p>DeploymentCondition describes the state of a deployment at a certain point.</p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>type</code></br>
<em>
<a href="#TribefireRuntimeConditionType">
TribefireRuntimeConditionType
</a>
</em>
</td>
<td>
<p>Type of deployment condition.</p>
</td>
</tr>
<tr>
<td>
<code>status</code></br>
<em>
<a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.13/#conditionstatus-v1-core">
Kubernetes core/v1.ConditionStatus
</a>
</em>
</td>
<td>
<p>Status of the condition, one of True, False, Unknown.</p>
</td>
</tr>
<tr>
<td>
<code>lastUpdateTime</code></br>
<em>
<a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.13/#time-v1-meta">
Kubernetes meta/v1.Time
</a>
</em>
</td>
<td>
<p>The last time this condition was updated.</p>
</td>
</tr>
<tr>
<td>
<code>lastTransitionTime</code></br>
<em>
<a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.13/#time-v1-meta">
Kubernetes meta/v1.Time
</a>
</em>
</td>
<td>
<p>Last time the condition transitioned from one status to another.</p>
</td>
</tr>
<tr>
<td>
<code>reason</code></br>
<em>
string
</em>
</td>
<td>
<p>The reason for the condition&rsquo;s last transition.</p>
</td>
</tr>
<tr>
<td>
<code>message</code></br>
<em>
string
</em>
</td>
<td>
<p>A human readable message indicating details about the transition.</p>
</td>
</tr>
</tbody>
</table>
<h3 id="TribefireRuntimeConditionType">TribefireRuntimeConditionType
(<code>string</code> alias)</p></h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireRuntimeCondition">TribefireRuntimeCondition</a>)
</p>
<p>
<p><h5>Description</h5>
These are valid conditions of a deployment. A condition is a standard Kubernetes concept that can be used
to track lifecycle changes, e.g. when a component gets available or unavailable. For instance, you can
<code>kubectl wait --for=condition=Available</code> to block until the TribefireRuntime is fully available.
<h5>Possible Values</h5>
<code>Available</code>, <code>EtcdAvailable</code>, <code>Progressing</code></p>
</p>
<h3 id="TribefireSpec">TribefireSpec
</h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireRuntime">TribefireRuntime</a>)
</p>
<p>
<p>The top level description of a <code>TribefireRuntime</code>. Describes the list of desired components, which backend to use
and more.</p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>domain</code></br>
<em>
string
</em>
</td>
<td>
<p>the domain name under which this TribefireRuntime will be reachable (via the Ingress). For instance,
if the domain is set to <code>tribefire.cloud</code> , the name of the runtime is <code>demo</code>, and the namespace of the
runtime is <code>documents</code>, then the TribefireRuntime will be available via <code>https:/demo-documents.tribefire.cloud/</code>
<br/><br/>
Possible values: any valid DNS name</p>
</td>
</tr>
<tr>
<td>
<code>databaseType</code></br>
<em>
<a href="#DatabaseType">
DatabaseType
</a>
</em>
</td>
<td>
<p>The type of the database for this TribefireRuntime</p>
</td>
</tr>
<tr>
<td>
<code>databases</code></br>
<em>
<a href="#DatabaseSpec">
[]DatabaseSpec
</a>
</em>
</td>
<td>
<p>Any additional (external) database that the runtime might need, e.g. Documents database</p>
</td>
</tr>
<tr>
<td>
<code>backend</code></br>
<em>
<a href="#BackendConfiguration">
BackendConfiguration
</a>
</em>
</td>
<td>
<p>The backend configuration for this TribefireRuntime, e.g. configuration for <code>etcd</code> backend</p>
</td>
</tr>
<tr>
<td>
<code>dcsaConfig</code></br>
<em>
<a href="#DcsaConfig">
DcsaConfig
</a>
</em>
</td>
<td>
<p>config element for DCSA support</p>
</td>
</tr>
<tr>
<td>
<code>components</code></br>
<em>
<a href="#TribefireComponent">
[]TribefireComponent
</a>
</em>
</td>
<td>
<p>The list of components for this TribefireRuntime, i.e. <code>services</code>, &lsquo;control-center` and others</p>
</td>
</tr>
</tbody>
</table>
<h3 id="TribefireStatus">TribefireStatus
</h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireRuntime">TribefireRuntime</a>)
</p>
<p>
<p>High level status information for this TribefireRuntime</p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>error</code></br>
<em>
bool
</em>
</td>
<td>
<p>error status indicator. If set to <code>true</code>, somethings wrong with this TribefireRuntime. <br/><br/>
Possible values: <code>true</code>, <code>false</code></p>
</td>
</tr>
<tr>
<td>
<code>status</code></br>
<em>
string
</em>
</td>
<td>
<p>a descriptive status message, such as <code>available</code><br/>
Example: <code>available</code></p>
</td>
</tr>
<tr>
<td>
<code>phase</code></br>
<em>
<a href="#DeploymentPhase">
DeploymentPhase
</a>
</em>
</td>
<td>
<p>the <code>DeploymentPhase</code> this TribefireRuntime is in. For details see the docs on the <code>DeploymentPhase</code><br/><br/>
Example: <code>DatabaseBootstrap</code></p>
</td>
</tr>
<tr>
<td>
<code>conditions</code></br>
<em>
<a href="#TribefireRuntimeCondition">
[]TribefireRuntimeCondition
</a>
</em>
</td>
<td>
<p>the status conditions for this TribefireRuntime. For details see the docs on the <code>TribefireRuntimeCondition</code></p>
</td>
</tr>
<tr>
<td>
<code>components</code></br>
<em>
<a href="#TribefireComponentStatus">
[]TribefireComponentStatus
</a>
</em>
</td>
<td>
<p>The list of <code>TribefireComponentStatus</code> information. For details, see the docs on <code>TribefireComponentStatus</code></p>
</td>
</tr>
<tr>
<td>
<code>observedGeneration</code></br>
<em>
int64
</em>
</td>
<td>
<p>This field is used to track changes to the <code>TribefireRuntimeSpec</code></p>
</td>
</tr>
<tr>
<td>
<code>created</code></br>
<em>
string
</em>
</td>
<td>
<p>Timestamp (ISO8601) when this TribefireRuntime was created. <br/><br/>
Example: <code>2019-03-20T17:41:09Z</code></p>
</td>
</tr>
<tr>
<td>
<code>updated</code></br>
<em>
string
</em>
</td>
<td>
<p>Timestamp (ISO8601) when this TribefireRuntime was updated. <br/><br/>
Example: <code>2019-03-20T19:36:39ZZ</code></p>
</td>
</tr>
</tbody>
</table>
<h3 id="TribefireVolume">TribefireVolume
</h3>
<p>
(<em>Appears on:</em>
<a href="#TribefireComponent">TribefireComponent</a>)
</p>
<p>
<p>a TribefireVolume is used to attach persistent storage to a component</p>
</p>
<table>
<thead>
<tr>
<th>Field</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td>
<code>name</code></br>
<em>
string
</em>
</td>
<td>
<p>symbolic name of the volume<br/><br/>
Example: <code>nfs-documents</code></p>
</td>
</tr>
<tr>
<td>
<code>volumeClaimName</code></br>
<em>
string
</em>
</td>
<td>
<p>The name of the underlying <code>PersistentVolumeClaim</code>. Please note that you need to setup
the PVC before referencing it here.<br/><br/>
Example: <code>nfs-documents-claim</code></p>
</td>
</tr>
<tr>
<td>
<code>volumeMountPath</code></br>
<em>
string
</em>
</td>
<td>
<p>The mount path where the PVC should be available inside the Tribefire pods.<br/><br/>
Example: <code>/nfs/documents</code></p>
</td>
</tr>
</tbody>
</table>
<hr/>
<p><em>
Generated with <code>gen-crd-api-reference-docs</code>
on git commit <code>a3a05848</code>.
</em></p>