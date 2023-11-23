# Example Deployment Files

Tribefire Cloud deployment solution allows you to deploy and manage tribefire components locally or to the Cloud using Kubernetes. You provide the configuration in the form of YAML files.

## CustomResourceDefinition.yaml

> Download the `CustomResourceDefinition.yaml` file [here](files/CustomResourceDefinition.yaml).

The following is a sample code for the `TribefireRuntime` custom resource definition.

```yaml
apiVersion: apiextensions.k8s.io/v1beta1
kind: CustomResourceDefinition
metadata:
  creationTimestamp: null
  labels:
    controller-tools.k8s.io: "1.0"
  name: tribefireruntimes.tribefire.cloud
spec:
  group: tribefire.cloud
  names:
    kind: TribefireRuntime
    plural: tribefireruntimes
    shortNames:
    - tf
  additionalPrinterColumns:
    - name: Status
      type: string
      description: A TribefireRuntime can be Available or Degraded
      JSONPath: .status.status
    - name: Age
      type: date
      description: When this TribefireRuntime was created
      JSONPath: .metadata.creationTimestamp
    - name: Domain
      type: string
      description: The domain used for the public URL (ingress)
      JSONPath: .spec.domain
      priority: 10
    - name: Database
      type: string
      description: Either local (Postgres) or cloudSQL (Google)
      JSONPath: .spec.databaseType
      priority: 10
    - name: Backend
      type: string
      description: The messaging backend used for this runtime. Currently etcd or activemq are supported
      JSONPath: .spec.backend.type
      priority: 10
    - name: Unavailable
      type: string
      description: List of unavailable components
      JSONPath: .status.components[?(@.status=="unavailable")].name
      priority: 10
  scope: Namespaced
  subresources:
    status: {}
  validation:
    openAPIV3Schema:
      properties:
        apiVersion:
          description: 'APIVersion defines the versioned schema of this representation
            of an object. Servers should convert recognized schemas to the latest
            internal value, and may reject unrecognized values. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#resources'
          type: string
        kind:
          description: 'Kind is a string value representing the REST resource this
            object represents. Servers may infer this from the endpoint the client
            submits requests to. Cannot be updated. In CamelCase. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#types-kinds'
          type: string
        metadata:
          type: object
        spec:
          properties:
            backend:
              properties:
                parameters:
                  items:
                    properties:
                      name:
                        type: string
                      value:
                        type: string
                    required:
                    - name
                    - value
                    type: object
                  type: array
                type:
                  enum:
                  - etcd
                  - activemq
                  type: string
              type: object
            components:
              items:
                properties:
                  apiPath:
                    description: absolute path or empty
                    pattern: ^/[a-z-]*$|^$
                    type: string
                  env:
                    items:
                      type: object
                    type: array
                  image:
                    description: todo this should be validated as a URL
                    minLength: 3
                    type: string
                  imageTag:
                    type: string
                  labels:
                    type: object
                  logJson:
                    type: boolean
                  logLevel:
                    enum:
                    - DEBUG
                    - INFO
                    - WARN
                    - ERROR
                    - CRITICAL
                    - FINE
                    - FINER
                    - FINEST
                    type: string
                  name:
                    maxLength: 30
                    minLength: 3
                    type: string
                  publicUrl:
                    pattern: ^https?:\/\/.*$
                    type: string
                  replicas:
                    format: int32
                    maximum: 10
                    minimum: 1
                    type: integer
                  resources:
                    type: object
                  type:
                    type: string
                required:
                - name
                - type
                type: object
              type: array
            databaseType:
              enum:
              - cloudsql
              - local
              type: string
            databases:
              items:
                properties:
                  credentialsSecretRef:
                    type: object
                  databaseName:
                    maxLength: 60
                    minLength: 3
                    type: string
                  envPrefixes:
                    description: 
                    items:
                      type: string
                    type: array
                  instanceDescriptor:
                    description: kubebuilder:validation:Pattern=^[^:]+:[^:]+:[^\=]\=tcp:[0-9]+$
                    type: string
                  name:
                    maxLength: 20
                    minLength: 3
                    type: string
                  serviceAccountSecretKey:
                    type: string
                  serviceAccountSecretRef:
                    type: object
                  type:
                    enum:
                    - cloudsql
                    - local
                    type: string
                required:
                - name
                - type
                - instanceDescriptor
                - envPrefixes
                - credentialsSecretRef
                type: object
              type: array
            domain:
              pattern: ^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,6}$
              type: string
          required:
          - components
          type: object
        status:
          properties:
            components:
              items:
                properties:
                  name:
                    type: string
                  status:
                    type: string
                  urls:
                    items:
                      type: string
                    type: array
                required:
                - name
                - status
                type: object
              type: array
            conditions:
              items:
                properties:
                  lastTransitionTime:
                    description: Last time the condition transitioned from one status
                      to another.
                    format: date-time
                    type: string
                  lastUpdateTime:
                    description: The last time this condition was updated.
                    format: date-time
                    type: string
                  message:
                    description: A human readable message indicating details about
                      the transition.
                    type: string
                  reason:
                    description: The reason for the condition's last transition.
                    type: string
                  status:
                    description: Status of the condition, one of True, False, Unknown.
                    type: string
                  type:
                    description: Type of deployment condition.
                    type: string
                required:
                - type
                - status
                type: object
              type: array
            created:
              type: string
            error:
              type: boolean
            observedGeneration:
              format: int64
              type: integer
            phase:
              type: string
            status:
              type: string
            updated:
              type: string
          type: object
      required:
      - metadata
      - spec
  version: v1alpha1
status:
  acceptedNames:
    kind: ""
    plural: ""
  conditions: []
  storedVersions: []
```

## etcdOperator.yaml

> Download the `etcdOperator.yaml` file [here](files/etcd_operator.yaml).

This file is used to deploy the etcd operator.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: etcd-operator
  namespace: etcd
spec:
  replicas: 1
  selector:
    matchLabels:
      name: etcd-operator
  template:
    metadata:
      labels:
        name: etcd-operator
    spec:
      serviceAccountName: etcd
      containers:
      - name: etcd-operator
        image: quay.io/coreos/etcd-operator:dev
        command:
        - etcd-operator
        - -cluster-wide
        env:
        - name: MY_POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: MY_POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
```

## etcdCluster.yaml

> Download the `etcdCluster.yaml` file [here](files/etcd_cluster.yaml).

This file is used to set up an etcd cluster.

```yaml
apiVersion: "etcd.database.coreos.com/v1beta2"
kind: "EtcdCluster"
metadata:
  name: "tf-etcd-cluster"
  namespace: etcd
  annotations:
    etcd.database.coreos.com/scope: clusterwide
  labels:
    app: etcd
spec:
  pod:
    etcdEnv:
    - name: ETCD_AUTO_COMPACTION_RETENTION
      value: "6"
    - name: ETCD_DEBUG
      value: "false"
  size: 1
  version: "3.3"
```

## rbac.yaml

> Download the `rbac.yaml` file [here](files/rbac.yaml).

This file is used to apply role-based access control (RBAC) rules.

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: etcd
  namespace: etcd
---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRole
metadata:
  name: etcd-operator
rules:
- apiGroups:
  - etcd.database.coreos.com
  resources:
  - etcdclusters
  - etcdbackups
  - etcdrestores
  verbs:
  - "*"
- apiGroups:
  - apiextensions.k8s.io
  resources:
  - customresourcedefinitions
  verbs:
  - "*"
- apiGroups:
  - ""
  resources:
  - pods
  - services
  - endpoints
  - persistentvolumeclaims
  - events
  verbs:
  - "*"
- apiGroups:
  - apps
  resources:
  - deployments
  verbs:
  - "*"
---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: etcd-operator
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: etcd-operator
subjects:
- kind: ServiceAccount
  name: etcd
  namespace: etcd
```

## TFCloudOperator.yaml

> Download the `TF_Cloud_operator.yaml` file [here](files/TF_Cloud_operator.yaml).

Note that you must replace `@@IMAGE:@@TAG@@` with Docker `image:tag` that contain the Tribefire cloud operator.


```yaml
---
apiVersion: v1
kind: Secret
metadata:
  name: tribefire-runtime-admission-server-secret
  namespace: tfdemo

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tfcloud-operator
  namespace: tfdemo
spec:
  replicas: 1
  selector:
    matchLabels:
      name: tfcloud-operator
  template:
    metadata:
      labels:
        name: tfcloud-operator
    spec:
      imagePullSecrets:
      - name: your-artifactory
      serviceAccountName: tfcloud-operator
      volumes:
      - name: cloudsql-service-account
        secret:
          secretName: cloudsql-service-account
      - name: cert
        secret:
          defaultMode: 420
          secretName: tribefire-runtime-admission-server-secret

      containers:
      - name: tfcloud-operator
        image: @@IMAGE@@:@@TAG@@ ##Docker image and tag of tfcloude operator
        ports:
        - containerPort: 60000
          name: metrics
        - containerPort: 9876
          name: webhook-server
          protocol: TCP
        command:
        - tfcloud-operator
        imagePullPolicy: Always
        volumeMounts:
        - mountPath: "/cloudsql"
          name: cloudsql-service-account
        - mountPath: /tmp/cert
          name: cert
          readOnly: true
        env:
        - name: WATCH_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: OPERATOR_NAME
          value: "tfcloud-operator"
        - name: OPERATOR_LOGGING_EXTENDED
          value: "true"
        - name: OPERATOR_LOGGING_JSON
          value: "true"
        - name: TRIBEFIRE_PULL_SECRETS_USER
          valueFrom:
            secretKeyRef:
              key: username
              name: your-artifactory-bootstrap
        - name: TRIBEFIRE_PULL_SECRETS_PASSWORD
          valueFrom:
            secretKeyRef:
              key: password
              name: your-artifactory-bootstrap

        - name: TRIBEFIRE_GCP_DATABASES_PROJECT_ID
          value: "tribefire-staging"
        - name: TRIBEFIRE_OPERATOR_VERSION
          value: "@@TAG@@"
        - name: TRIBEFIRE_GCP_DATABASES_INSTANCE_ID
          value: "tfcloud-operator"
        - name: TRIBEFIRE_GCP_DATABASES_REGION
          value: "europe-west3"
        - name: TRIBEFIRE_OPERATOR_LOG_LEVEL
          value: "DEBUG"
        - name: TRIBEFIRE_OPERATOR_DUMP_RESOURCES_STDOUT
          value: "false"
        - name: TRIBEFIRE_IMAGE_PULL_POLICY
          value: "IfNotPresent"
        - name: TRIBEFIRE_USE_POSTGRES_CHECKER_INIT_CONTAINER
          value: "true"

```

## TribefireRuntime.yaml

> Download the `TribefireRuntime.yaml` file [here](files/TribefireRuntime.yaml).

This is a sample file for a `TribefireRuntime` manifest.

Note that you must replace the `image` and `imageTag` parts with docker image and tag that contain the component you want to deploy.

```yaml
apiVersion: "tribefire.cloud/v1alpha1"
kind: "TribefireRuntime"
metadata:
  name: minimal-demo
  labels:
    stage: staging
spec:
  domain: tribefire.local
  databaseType: local
  backend:
    type: etcd
  components:
    - name: tribefire-services
      type: services
      image: docker.artifactory.server/tribefire-cloud/internal/test/operator-demo-staging-cluster-tfdemo-dev/tribefire-services
      imageTag: 2.0-latest
      env:
        - name: TRIBEFIRE_CHECK_HEALTH_LEADERSHIP
          value: "false"
        - name: TRIBEFIRE_CHECK_HEALTH_LOCK
          value: "false"

```