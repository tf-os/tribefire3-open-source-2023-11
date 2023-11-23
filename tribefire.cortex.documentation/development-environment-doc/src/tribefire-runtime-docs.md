### docs.md

Packages:

-   [tribefire.cloud](#tribefire.cloud)

[](#tribefirecloud)tribefire.cloud {#user-content-tribefire.cloud}
----------------------------------

Package v1alpha1 contains API Schema definitions for the tribefire
v1alpha1 API group

Resource Types:

### BackendConfiguration

This element is used to set the Tribefire backend as well as the
configuration parameters for the chosen Tribefire backend. Currently,
`etcd` and `activemq` are supported

 Field                                 Description                          
------
 `type`                                The backend for Tribefire\           
 *[MessagingBackend](#MessagingBacken  \                                    
 d)*                                    Possible values: `activemq`, `etcd` 
 `parameters`                          Configuration properties for the     
 *[[]BackendParam](#BackendParam)*     chosen Tribefire backend, e.g. the   
                                       `etcd` URL\                          
                                       \                                    
                                        Defaults (for etcd): `name='url'`   
                                       `value='http://tf-etcd-cluster-clien 
                                       t:2379'`                             


### BackendParam

Appears on [BackendConfiguration](#BackendConfiguration))

This element is used to set specific properties for the chosen Tribefire
backend, e.g. URLs and credentials

 Field                                 Description                          
------
 `name` *string*                       Name of the backend property. Examples: `username`, `password`, `url`   
 `value` *string*                      The value of the backend configuration property. Examples: `https://tf-etcd.etcd.svc.cluster.local`, `etcd-user`, `etcd-password` 

### ComponentStatus

(*Appears on:* [TribefireComponentStatus](#TribefireComponentStatus))

##### Description

The overall status of the related component, can be used as an indicator
on health/availability of the component. `degraded` means “partially
available”, i.e. when not all replicas of a component are available.

##### Possible Values

`available`, `degraded`, `unavailable`

### ComponentStatusChangeReason 

### ComponentType 

(*Appears on:* [TribefireComponent](#TribefireComponent))

Identifies the type of the component, that could be for instance
`control-center` or `modeler`.

Possible values:

`services`, `control-center`, `modeler`, `explorer`, `web-reader`,
`cartridge`.

### CustomEnvironment

Every `TribefireRuntimeComponent` can have a custom environment that is
structured the same as the standard `env` section on a `container`. Use
it to control the environment variables that a
`TribefireRuntimeComponent` container sees, for example:

```yaml
    - name: "PRODUCTION_MODE"
    value: "true"
```    

### DatabaseSpec

(*Appears on:* [TribefireSpec](#TribefireSpec))

this spec is used to attach external/custom databases the
DatabaseDescriptor is used to refer to the existing database, e.g.
braintribe-databases:europe-west3:general-purpose=tcp:5555

 Field                                 Description                          
---|---
| `name` *string* |                     a symbolic name to refer to this database.  Example: `tribecell-production`|     
| `type`           |                     The type of this database. If type *[DatabaseType](#DatabaseType)* is `cloudsql`, the operator will provision a Google CloudSQL database. If type is `local`, the operator will deploy a local PostgreSQL container. Possible Values: `cloudsql`, `local`  Example: `cloudsql`|
| `instanceDescriptor` *string* |        The descriptor/connection string that identifies this database. This can either be a CloudSQL instance descriptor or a JDBC url. Example: `jdbc:postgresql://1.2.3.4:5432/documents-demo` |                     
| `envPrefixes` *[]string*  |            For every database described by a    
                                       `DatabaseSpec` there will be a set  of environment variables exposed in the `tribefire-services` pods. This  set of env vars contains the following variables:<br>*  `${PREFIX}_DB_URL` <br> *  `${PREFIX}_DB_USER` <br> *  `${PREFIX}_DB_PASS` <br> the values for these variables are taken from the `credentialsSecretRef` as well as the `instanceDescriptor` Example: `DOCUMENTS`|                
| `databaseName` *string* |              The name of the database, for example: `documents`                
+--------------------------------------+--------------------------------------+
 `serviceAccountSecretRef`             This `secretRef` points to the       
 *[Kubernetes                          secret that contains the service     
 core/v1.SecretReference](https://kub  account manifest for CloudSQL. Only  
 ernetes.io/docs/reference/generated/  needed in case you want to connect   
 kubernetes-api/v1.13/#secretreferenc  to a CloudSQL database via the       
 e-v1-core)*                           cloud-sql-proxy\                     
                                       \                                    
                                        Example:                            
                                       `documents-cloudsql-secret`          
+--------------------------------------+--------------------------------------+
 `serviceAccountSecretKey` *string*    This points to the key of the        
                                       `serviceAccountSecretRef` where to   
                                       take the service account JSON from\  
                                       \                                    
                                        Example: `service-account.json`     
+--------------------------------------+--------------------------------------+
 `credentialsSecretRef` *[Kubernetes   This `secretRef` points to the       
 core/v1.SecretReference](https://kub  secret that contains the database    
 ernetes.io/docs/reference/generated/  credentials, i.e. username and       
 kubernetes-api/v1.13/#secretreferenc  password to connect to the database. 
 e-v1-core)*                           The secret itself should have a      
                                       `username` and a `password` key that 
                                       have the related values. \           
                                       \                                    
                                        Example:                            
                                       `database-credentials-secret`        
+--------------------------------------+--------------------------------------+

### DatabaseType

(*Appears on:* [DatabaseSpec](#DatabaseSpec),
[TribefireSpec](#TribefireSpec))

##### Description

The type of the referenced database. If type is `cloudsql`, a Google
CloudSQL database will be provisioned for this TribefireRuntime. If type
is `local`, a local postgresql container will be bootstrapped.

##### Possible Values

`cloudsql`, `local`

##### Example

`cloudsql`

### DeploymentPhase

(*Appears on:* [TribefireStatus](#TribefireStatus))

##### Description

The current phase of the TribefireRuntime deployment. This can be used
to determine how far the deployment has progressed, and can also be used
to understand what went wrong. For instance, if a TribefireRuntime
deployment does not progress any more and is in phase
`DatabaseBootstrap`, it might indicate that something went wrong during
the provisioning of the database.

##### Possible Values

`TribefireValidation`, `DatabaseBootstrap`, `SecretBootstrap`,
`BackendBootstrap`, `ComponentDeployment` `TribefireRunning`

### MessagingBackend

(*Appears on:* [BackendConfiguration](#BackendConfiguration))

##### Description

The backend that should be used for messaging, locking and leadership.
Please note that in case of `etcd`, you must take care of the
provisioning of the etcd cluster yourself. ActiveMQ will be deployed by
the operator, every TribefireRuntime gets its own ActiveMQ instance.

##### Possible Values

`activemq`, `etcd`

##### Default

`etcd`

### TribefireComponent

(*Appears on:* [TribefireSpec](#TribefireSpec))

+--------------------------------------+--------------------------------------+
 Field                                 Description                          
+======================================+======================================+
 `name` *string*                       the name of this component           
+--------------------------------------+--------------------------------------+
 `type`                                type of this component,              
 *[ComponentType](#ComponentType)*     e.g.`services` or `control-center`   
+--------------------------------------+--------------------------------------+
 `image` *string*                      Docker image to be used for this     
                                       component.\                          
                                       \                                    
                                        Example:                            
                                       `docker.artifactory.server/c 
                                       loud/tribefire-master`               
+--------------------------------------+--------------------------------------+
 `imageTag` *string*                   The image tag for the referenced     
                                       Docker image\                        
                                       \                                    
                                        Example: `2.0-latest`               
+--------------------------------------+--------------------------------------+
 `logLevel` *string*                   The loglevel for this component.\    
                                       \                                    
                                        Possible values:                    
                                       `DEBUG`,`INFO`,`WARN`,`ERROR`,`CRITI 
                                       CAL`,`FINE`,`FINER`,`FINEST`         
+--------------------------------------+--------------------------------------+
 `apiPath` *string*                    absolute path (starting with `/`)    
                                       for this component\                  
                                       \                                    
                                        Example:                            
                                       `/tribefire-control-center`          
+--------------------------------------+--------------------------------------+
 `replicas` *int32*                    number of replicas for this          
                                       component\                           
                                       \                                    
                                        Possible values: `1-10`             
+--------------------------------------+--------------------------------------+
 `publicUrl` *string*                  The publicly accessible URL for this 
                                       component\                           
                                       \                                    
                                        Example:                            
                                       `https://demo-documents.tribefire.cl 
                                       oud/services`                        
+--------------------------------------+--------------------------------------+
 `env` *[[]Kubernetes                  The environment for this component.  
 core/v1.EnvVar](https://kubernetes.i  Just a standard `env` section as in  
 o/docs/reference/generated/kubernete  `ContainerSpec`                      
 s-api/v1.13/#envvar-v1-core)*                                              
+--------------------------------------+--------------------------------------+
 `logJson` *bool*                      Use JSON logging for this component\ 
                                       \                                    
                                        Possible values: `true`, `false`    
+--------------------------------------+--------------------------------------+
 `resources` *[Kubernetes              The resource requirements for this   
 core/v1.ResourceRequirements](https:  component. Standard                  
 //kubernetes.io/docs/reference/gener  `ResourceRequirements` as per        
 ated/kubernetes-api/v1.13/#resourcer  `PodSpec`                            
 equirements-v1-core)*                                                      
+--------------------------------------+--------------------------------------+
 `labels` *map[string]string*          Set of labels that should be         
                                       attached to this component           
+--------------------------------------+--------------------------------------+
 `protocol` *string*                   The protocol that should be used for 
                                       the public URL \                     
                                       \                                    
                                        Possible values: `http`, `https`    
+--------------------------------------+--------------------------------------+
 `persistentVolumes`                   list of volumes that should be       
 *[[]TribefireVolume](#TribefireVolum  attached to this component. Should   
 e)*                                   be used together with                
                                       `persistentVolumeClaims`             
+--------------------------------------+--------------------------------------+

### TribefireComponentStatus

(*Appears on:* [TribefireStatus](#TribefireStatus))

status information about a specific component

+--------------------------------------+--------------------------------------+
 Field                                 Description                          
+======================================+======================================+
 `name` *string*                       name of this component\              
                                       \                                    
                                        Examples: `services`,               
                                       `control-center`                     
+--------------------------------------+--------------------------------------+
 `status`                              status of this component\            
 *[ComponentStatus](#ComponentStatus)  \                                    
 *                                      Examples: `available`,              
                                       `unavailable`, `degraded`            
+--------------------------------------+--------------------------------------+
 `urls` *[]string*                     the URL(s) under which this          
                                       component is reachable from the      
                                       outside (via the ingress)\           
                                       \                                    
                                        Examples:                           
                                       `https://demo-documents.tribefire.cl 
                                       oud/services`,`https://demo-document 
                                       s.tribefire.cloud/control-center`,   
+--------------------------------------+--------------------------------------+

### TribefireRuntime

+--------------------------------------+--------------------------------------+
 Field                                 Description                          
+======================================+======================================+
 `metadata` *[Kubernetes               Refer to the Kubernetes API          
 meta/v1.ObjectMeta](https://kubernet  documentation for the fields of the  
 es.io/docs/reference/generated/kuber  `metadata` field.                    
 netes-api/v1.13/#objectmeta-v1-meta)                                       
 *                                                                          
+--------------------------------------+--------------------------------------+
 `spec`                                \                                    
 *[TribefireSpec](#TribefireSpec)*      \                                   
                                       +----------------------------------- 
                                       ---+-------------------------------- 
                                       ------+                              
                                        `domain` *string*                  
                                           the domain name under which thi 
                                       s                                   
                                                                           
                                           TribefireRuntime will be reacha 
                                       ble                                 
                                                                           
                                           (via the Ingress). For instance 
                                       , if                                
                                                                           
                                           the domain is set to            
                                                                           
                                                                           
                                           `tribefire.cloud` , the name of 
                                        the                                
                                                                           
                                           runtime is `demo`, and the name 
                                       space                               
                                                                           
                                           of the runtime is `documents`,  
                                       then                                
                                                                           
                                           the Tribefire runtime will be   
                                                                           
                                                                           
                                           available via                   
                                                                           
                                                                           
                                           `https:/demo-documents.tribefir 
                                       e.clo                               
                                                                           
                                           ud/`                            
                                                                           
                                                                           
                                           \                               
                                                                           
                                                                           
                                           \                               
                                                                           
                                                                           
                                            Possible values: any valid DNS 
                                        name                               
                                       +----------------------------------- 
                                       ---+-------------------------------- 
                                       ------+                              
                                        `databaseType`                     
                                           The type of the database for th 
                                       is                                  
                                        *[DatabaseType](#DatabaseType)*    
                                           Tribefire runtime               
                                                                           
                                       +----------------------------------- 
                                       ---+-------------------------------- 
                                       ------+                              
                                        `databases`                        
                                           Any additional (external) datab 
                                       ase                                 
                                        *[[]DatabaseSpec](#DatabaseSpec)*  
                                           that the runtime might need, e. 
                                       g.                                  
                                                                           
                                           Documents database              
                                                                           
                                       +----------------------------------- 
                                       ---+-------------------------------- 
                                       ------+                              
                                        `backend`                          
                                           The backend configuration for t 
                                       his                                 
                                        *[BackendConfiguration](#BackendCo 
                                       nf  Tribefire runtime, e.g.         
                                                                           
                                        iguration)*                        
                                           configuration for `etcd` backen 
                                       d                                   
                                       +----------------------------------- 
                                       ---+-------------------------------- 
                                       ------+                              
                                        `components`                       
                                           The list of components for this 
                                                                           
                                        *[[]TribefireComponent](#Tribefire 
                                       Co  TribefireRuntime, i.e. `service 
                                       s`,                                 
                                        mponent)*                          
                                           ‘control-center\` etcd.         
                                                                           
                                       +----------------------------------- 
                                       ---+-------------------------------- 
                                       ------+                              
+--------------------------------------+--------------------------------------+
 `status`                                                                   
 *[TribefireStatus](#TribefireStatus)                                       
 *                                                                          
+--------------------------------------+--------------------------------------+

### TribefireRuntimeCondition

(*Appears on:* [TribefireStatus](#TribefireStatus))

DeploymentCondition describes the state of a deployment at a certain
point.

+--------------------------------------+--------------------------------------+
 Field                                 Description                          
+======================================+======================================+
 `type`                                Type of deployment condition.        
 *[TribefireRuntimeConditionType](#Tr                                       
 ibefireRuntimeConditionType)*                                              
+--------------------------------------+--------------------------------------+
 `status` *[Kubernetes                 Status of the condition, one of      
 core/v1.ConditionStatus](https://kub  True, False, Unknown.                
 ernetes.io/docs/reference/generated/                                       
 kubernetes-api/v1.13/#conditionstatu                                       
 s-v1-core)*                                                                
+--------------------------------------+--------------------------------------+
 `lastUpdateTime` *[Kubernetes         The last time this condition was     
 meta/v1.Time](https://kubernetes.io/  updated.                             
 docs/reference/generated/kubernetes-                                       
 api/v1.13/#time-v1-meta)*                                                  
+--------------------------------------+--------------------------------------+
 `lastTransitionTime` *[Kubernetes     Last time the condition transitioned 
 meta/v1.Time](https://kubernetes.io/  from one status to another.          
 docs/reference/generated/kubernetes-                                       
 api/v1.13/#time-v1-meta)*                                                  
+--------------------------------------+--------------------------------------+
 `reason` *string*                     The reason for the condition’s last  
                                       transition.                          
+--------------------------------------+--------------------------------------+
 `message` *string*                    A human readable message indicating  
                                       details about the transition.        
+--------------------------------------+--------------------------------------+

### TribefireRuntimeConditionType

(*Appears on:* [TribefireRuntimeCondition](#TribefireRuntimeCondition))

##### Description

These are valid conditions of a deployment. A condition is a standard
Kubernetes concept that can be used to track lifecycle changes, e.g.
when a component gets available or unavailable. For instance, you can
`kubectl wait --for=condition=Available` to block until the Tribefire
runtime is fully available.

##### Possible Values

`Available`, `EtcdAvailable`, `Progressing`

### TribefireSpec 

(*Appears on:* [TribefireRuntime](#TribefireRuntime))

The top level description of a `TribefireRuntime`. Describes the list of
desired components, which backend to use and more.

+--------------------------------------+--------------------------------------+
 Field                                 Description                          
+======================================+======================================+
 `domain` *string*                     the domain name under which this     
                                       TribefireRuntime will be reachable   
                                       (via the Ingress). For instance, if  
                                       the domain is set to                 
                                       `tribefire.cloud` , the name of the  
                                       runtime is `demo`, and the namespace 
                                       of the runtime is `documents`, then  
                                       the Tribefire runtime will be        
                                       available via                        
                                       `https:/demo-documents.tribefire.clo 
                                       ud/`                                 
                                       \                                    
                                       \                                    
                                        Possible values: any valid DNS name 
+--------------------------------------+--------------------------------------+
 `databaseType`                        The type of the database for this    
 *[DatabaseType](#DatabaseType)*       Tribefire runtime                    
+--------------------------------------+--------------------------------------+
 `databases`                           Any additional (external) database   
 *[[]DatabaseSpec](#DatabaseSpec)*     that the runtime might need, e.g.    
                                       Documents database                   
+--------------------------------------+--------------------------------------+
 `backend`                             The backend configuration for this   
 *[BackendConfiguration](#BackendConf  Tribefire runtime, e.g.              
 iguration)*                           configuration for `etcd` backend     
+--------------------------------------+--------------------------------------+
 `components`                          The list of components for this      
 *[[]TribefireComponent](#TribefireCo  TribefireRuntime, i.e. `services`,   
 mponent)*                             ‘control-center\` etcd.              
+--------------------------------------+--------------------------------------+

### TribefireStatus 
(*Appears on:* [TribefireRuntime](#TribefireRuntime))

High level status information for this Tribefire runtime

+--------------------------------------+--------------------------------------+
 Field                                 Description                          
+======================================+======================================+
 `error` *bool*                        error status indicator. If set to    
                                       `true`, somethings wrong with this   
                                       Tribefire runtime. \                 
                                       \                                    
                                        Possible values: `true`, `false`    
+--------------------------------------+--------------------------------------+
 `status` *string*                     a descriptive status message, such   
                                       as `available`\                      
                                        Example: `available`                
+--------------------------------------+--------------------------------------+
 `phase`                               the `DeploymentPhase` this Tribefire 
 *[DeploymentPhase](#DeploymentPhase)  runtime is in. For details see the   
 *                                     docs on the `DeploymentPhase`\       
                                       \                                    
                                        Example: `DatabaseBootstrap`        
+--------------------------------------+--------------------------------------+
 `conditions`                          the status conditions for this       
 *[[]TribefireRuntimeCondition](#Trib  Tribefire runtime. For details see   
 efireRuntimeCondition)*               the docs on the                      
                                       `TribefireRuntimeCondition`          
+--------------------------------------+--------------------------------------+
 `components`                          The list of                          
 *[[]TribefireComponentStatus](#Tribe  `TribefireComponentStatus`           
 fireComponentStatus)*                 information. For details, see the    
                                       docs on `TribefireComponentStatus`   
+--------------------------------------+--------------------------------------+
 `observedGeneration` *int64*          This field is used to track changes  
                                       to the `TribefireRuntimeSpec`        
+--------------------------------------+--------------------------------------+
 `created` *string*                    Timestamp (ISO8601) when this        
                                       Tribefire runtime was created. \     
                                       \                                    
                                        Example: `2019-03-20T17:41:09Z`     
+--------------------------------------+--------------------------------------+
 `updated` *string*                    Timestamp (ISO8601) when this        
                                       Tribefire runtime was updated. \     
                                       \                                    
                                        Example: `2019-03-20T19:36:39ZZ`    
+--------------------------------------+--------------------------------------+

### TribefireVolume

(*Appears on:* [TribefireComponent](#TribefireComponent))

a TribefireVolume is used to attach persistent storage to a component

+--------------------------------------+--------------------------------------+
 Field                                 Description                          
+======================================+======================================+
 `name` *string*                       symbolic name of the volume\         
                                       \                                    
                                        Example: `nfs-documents`            
+--------------------------------------+--------------------------------------+
 `volumeClaimName` *string*            The name of the underlying           
                                       `PersistentVolumeClaim`. Please note 
                                       that you need to setup the PVC       
                                       before referencing it here.\         
                                       \                                    
                                        Example: `nfs-documents-claim`      
+--------------------------------------+--------------------------------------+
 `volumeMountPath` *string*            The mount path where the PVC should  
                                       be available inside the Tribefire    
                                       pods.\                               
                                       \                                    
                                        Example: `/nfs/documents`           
+--------------------------------------+--------------------------------------+

* * * * *

*Generated with `gen-crd-api-reference-docs` on git commit `c93f789`.*

 
