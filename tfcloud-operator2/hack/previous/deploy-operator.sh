#!/usr/bin/env bash

# For more info on what this thing does please see the README.md file in the /hack directory

# Reset in case getopts has been used previously in the shell.
OPTIND=1

# initialize our script options. Mandatory stuff
namespace=""
image_registry_username=""
image_registry_password=""
pull_policy="Always"

# initialize our script options. Optional stuff
verbose=0
dry_run=0
skip_crd=0
force=0
openshift=0
etcd_cluster_size=0
local_mode=0
operator_service_account=""
cloud_sql_service_account=""
image=dockerregistry.example.com/tribefire-cloud/operator-development
#image=dockerregistry.example.com/tribefire-cloud/tribefire-operator
tag=latest

#
#
# FUNCTIONS
#
#

#
# help screen
#
function show_help() {
  cmd=$(basename $0)
  echo "Usage: ${cmd} <options> where options is:"
  echo " -h               Show this message"
  echo " -v               Verbose mode"
  echo " -d               Dry run. Don't do anything, just show what would be done"
  echo " -s               Skips CRD check."
  echo " -f               Force redeployment of operator"
  echo " -l               Local mode. Sets image pull policy to 'Never'"
  echo " -e <num-nodes>   Deploys etcd cluster in given namespace with <num_nodes> members."
  echo " -n <namespace>   Use <namespace> for deployment. Mandatory"
  echo " -u <username>    Use <username> for image pull secret. Mandatory"
  echo " -p <password>    Use <password> for image pull secret. Mandatory"
  echo " -t <tag>         Use <tag> for operator image. Optional, default: ${tag}"
  echo " -o <filename>    Use <filename> for operator service account. Optional, default: ${operator_service_account}"
  echo " -c <filename>    Use <filename> for CloudSQL service account. Optional, default: ${cloud_sql_service_account}"
  echo
  echo "Example: ${cmd} -n test -u pd_cloud_read -p xxx_secret -o operator-service-account.json -c cloudsql-client-service-account.json -d"
  echo
  echo "To deploy in AWS we are not using service accounts: ${cmd} -v -n namespace -u docker_user -p docker_pass -e 3 -t 0.8.0 -f"
}

#
# check if namespace exists and create if it's not there
#
function check_create_namespace() {
  err=""

  exists=$(kubectl get ns ${namespace} 2>&1 >/dev/null)
  if [[ $? != "0" ]]; then
    echo "No such namespace: $namespace. Creating it..."
    if [[ dry_run -eq 0 ]]; then
      err=$(kubectl create ns ${namespace})
      err=$(kubectl label namespace ${namespace} name=${namespace})
    fi
  fi

  if [[ $? != "0" ]]; then
    echo "Unable to create namespace ${namespace}: ${err}"
    exit 1
  fi
}

#
# create the required secrets
#
function create_secrets() {
  err=""

  # check service account secret
  if [[ -n "${operator_service_account}" ]] && [[ -n "${cloud_sql_service_account}" ]]; then
    exists=$(kubectl get secret cloudsql-service-account -n ${namespace} 2>&1 >/dev/null)
    if [[ $? != "0" ]]; then
      echo "CloudSQL service account does not exists in namespace: $namespace. Creating it..."
      if [[ dry_run -eq 0 ]]; then
        err=$(kubectl create -n ${namespace} secret generic cloudsql-service-account \
          --from-file=service-account.json=${operator_service_account} \
          --from-file=system.json=${cloud_sql_service_account})
      fi
    fi

    if [[ $? != "0" ]]; then
      echo "Unable to create service account secrets in ${namespace}: ${err}"
      exit 1
    fi
  else
    echo "Skipping CloudSQL secrets creation"
  fi

  # check image pull secret
  exists=$(kubectl get secret bt-artifactory -n ${namespace} 2>&1 >/dev/null)
  if [[ $? != "0" ]]; then
    echo "Image pull secret does not exists in namespace: $namespace. Creating it..."
    if [[ dry_run -eq 0 ]]; then
      err=$(kubectl create -n ${namespace} secret docker-registry bt-artifactory --docker-server=dockerregistry.example.com \
        --docker-username=${image_registry_username} --docker-password=${image_registry_password} --docker-email=some@mail.com)
    fi
  fi

  if [[ $? != "0" ]]; then
    echo "Unable to create image pull secrets in ${namespace}: ${err}"
    exit 1
  fi

  exists=$(kubectl get secret bt-artifactory-bootstrap -n ${namespace} 2>&1 >/dev/null)
  if [[ $? != "0" ]]; then
    echo "Bootstrap image pull secret does not exists in namespace: $namespace. Creating it..."
    if [[ dry_run -eq 0 ]]; then
      err=$(kubectl create -n ${namespace} secret generic bt-artifactory-bootstrap --from-literal=username=${image_registry_username} \
        --from-literal=password=${image_registry_password})
    fi
  fi

  if [[ $? != "0" ]]; then
    echo "Unable to create bootstrap image pull secrets in ${namespace}: ${err}"
    exit 1
  fi
}

#
# deploy the required RBAC rules
#
function deploy_rbac() {
  exists=$(kubectl get serviceaccount -n ${namespace} tfcloud-operator 2>&1 >/dev/null)
  if [[ $? == "1" ]] || [[ "${force}" -eq 1 ]]; then
    read -r -d '' rbac_manifest <<-'EOF'
apiVersion: v1
kind: ServiceAccount
metadata:
  name: tfcloud-operator
  namespace: @@NAMESPACE@@

---

kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: tfcloud-operator
rules:
- apiGroups:
    - admissionregistration.k8s.io
  resources:
    - mutatingwebhookconfigurations
    - validatingwebhookconfigurations
  verbs:
    - get
    - list
    - watch
    - create
    - update
    - patch
    - delete
- apiGroups:
  - security.openshift.io
  resources:
  - securitycontextconstraints
  verbs:
  - update
  - get

---

kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: tfcloud-operator
  namespace: @@NAMESPACE@@
rules:
- apiGroups:
  - tribefire.cloud
  resources:
  - "*"
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
  - configmaps
  - secrets
  - serviceaccounts
  verbs:
  - "*"
- apiGroups:
  - apps
  resources:
  - deployments
  - daemonsets
  - replicasets
  - statefulsets
  verbs:
  - "*"
- apiGroups:
  - networking.k8s.io
  resources:
  - ingresses
  verbs:
  - "*"
- apiGroups:
  - "rbac.authorization.k8s.io"
  resources:
  - roles
  - rolebindings
  verbs:
  - "*"
@@OPENSHIFT_RULES@@
---

kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: tfcloud-operator
  namespace: @@NAMESPACE@@
subjects:
- kind: ServiceAccount
  name: tfcloud-operator
  namespace: @@NAMESPACE@@
roleRef:
  kind: Role
  name: tfcloud-operator
  apiGroup: rbac.authorization.k8s.io

---

kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: tfcloud-operator-@@NAMESPACE@@
subjects:
- kind: ServiceAccount
  name: tfcloud-operator
  namespace: @@NAMESPACE@@
roleRef:
  kind: ClusterRole
  name: tfcloud-operator
  apiGroup: rbac.authorization.k8s.io

EOF
    read -r -d '' openshift_rules <<-'EOF'
- apiGroups:
  - ""
  - route.openshift.io
  resources:
  - routes
  verbs:
  - create
  - delete
  - deletecollection
  - get
  - list
  - patch
  - update
  - watch
- apiGroups:
  - ""
  - route.openshift.io
  resources:
  - routes/custom-host
  verbs:
  - create
- apiGroups:
  - ""
  - route.openshift.io
  resources:
  - routes/status
  verbs:
  - get
  - list
  - watch
- apiGroups:
  - ""
  - route.openshift.io
  resources:
  - routes/status
  verbs:
  - update
EOF
    if [[ "${openshift}" -eq 1 ]]; then
      rules=$(echo "${openshift_rules}" | tr "\n" "~")
      rbac_manifest=$(echo "${rbac_manifest}" | sed "s|@@OPENSHIFT_RULES@@|${rules}|" | tr "~" '\n')
    else
      rbac_manifest=$(echo "${rbac_manifest}" | sed "s|@@OPENSHIFT_RULES@@||")
    fi

    echo "Deploying RBAC rules for operator in namespace ${namespace}"
    echo "${rbac_manifest}" | sed "s|@@NAMESPACE@@|${namespace}|" | kubectl apply -f -
  fi

}

#
# deploy the etcd cluster
#
function deploy_etcd_cluster() {
  exists=$(kubectl get etcd -n ${namespace} tf-etcd-cluster 2>&1 >/dev/null)
  rc=$?
  if [[ ${rc} -ne 0 ]] || [[ ${force} -eq 1 ]]; then
    if [[ ${rc} -eq 0 ]]; then
      echo "Deleting etcd cluster in namespace ${namespace}..."
      helm uninstall --namespace ${namespace} etcd-tribefire bitnami/etcd
    fi

    echo "Deploying etcd cluster in namespace ${namespace}..."
    helm install --namespace ${namespace} etcd-tribefire bitnami/etcd --set persistence.enabled=false,auth.rbac.create=false
    return
  fi

  echo "Seems like an etcd cluster already exists in namespace ${namespace}"
}
#
# deploy the operator
#
function deploy_operator() {
  exists=$(kubectl get deployment -n ${namespace} tfcloud-operator 2>&1 >/dev/null)
  if [[ $? == "1" ]] || [[ "${force}" -eq 1 ]]; then
    read -r -d '' operator_manifest <<-'EOF'
---
apiVersion: v1
kind: Secret
metadata:
  name: tribefire-runtime-admission-server-secret
  namespace: @@NAMESPACE@@
---

apiVersion: apps/v1
kind: Deployment
metadata:
  name: tfcloud-operator
  namespace: @@NAMESPACE@@
  labels:
    name: tfcloud-operator
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
      - name: bt-artifactory
      serviceAccountName: tfcloud-operator
      volumes:
      @@CLOUDSQL_VOLUME@@
      - name: cert
        secret:
          defaultMode: 420
          secretName: tribefire-runtime-admission-server-secret
      containers:
      - name: tfcloud-operator
        image: @@IMAGE@@:@@TAG@@
        ports:
        - containerPort: 60000
          name: metrics
        - containerPort: 9876
          name: webhook-server
          protocol: TCP
        command:
        - /manager
        imagePullPolicy: @@PULL_POLICY@@
        volumeMounts:
        @@CLOUDSQL_VOLUME_MOUNT@@
        - mountPath: /tmp/cert
          name: cert
          readOnly: true
        env:
        - name: OPERATOR_WATCH_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: OPERATOR_NAME
          value: "tfcloud-operator"
        - name: OPERATOR_LOGGING_EXTENDED
          value: "false"
        - name: TRIBEFIRE_PULL_SECRETS_USER
          valueFrom:
            secretKeyRef:
              key: username
              name: bt-artifactory-bootstrap
        - name: TRIBEFIRE_PULL_SECRETS_PASSWORD
          valueFrom:
            secretKeyRef:
              key: password
              name: bt-artifactory-bootstrap
        - name: TRIBEFIRE_OPERATOR_VERSION
          value: "@@TAG@@"
        - name: TRIBEFIRE_GCP_DATABASES_PROJECT_ID
          value: "tribefire-staging"
        - name: TRIBEFIRE_GCP_DATABASES_INSTANCE_ID
          value: "tfcloud-operator"
        - name: TRIBEFIRE_GCP_DATABASES_REGION
          value: "europe-west3"
        - name: TRIBEFIRE_OPERATOR_LOG_LEVEL
          value: "DEBUG"
        - name: TRIBEFIRE_OPERATOR_DUMP_RESOURCES_STDOUT
          value: "false"
        - name: TRIBEFIRE_IMAGE_PULL_POLICY
          value: "Always"
        - name: TRIBEFIRE_USE_POSTGRES_CHECKER_INIT_CONTAINER
          value: "true"
        - name: TRIBEFIRE_SYSTEM_DB_HOST_PORT
          value: "systemdb.staging.tribefire.cloud:5432"
        - name: TRIBEFIRE_SYSTEM_DB_OPTS
          value: "?ssl=required"
EOF
    echo "Deploying operator in namespace ${namespace}"
    # shellcheck disable=SC2006
    manifest=$(echo "${operator_manifest}" | sed "s|@@IMAGE@@|${image}|" |
      sed "s|@@PULL_POLICY@@|${pull_policy}|" |
      sed "s|@@TAG@@|$tag|" |
      sed "s|@@NAMESPACE@@|${namespace}|")

    if [[ -n "${cloud_sql_service_account}" ]] && [[ -n "${operator_service_account}" ]]; then
      read -r -d '' volume <<-EOF
      - name: cloudsql-service-account
        secret:
          secretName: cloudsql-service-account
EOF
      volume=$(echo "${volume}" | tr "\n" "~")
      manifest=$(echo "${manifest}" | sed "s|@@CLOUDSQL_VOLUME@@|${volume}|" | tr "~" '\n')
      read -r -d '' volume_mount <<-EOF
        - mountPath: "/cloudsql"
          name: cloudsql-service-account
EOF
      volume_mount=$(echo "${volume_mount}" | tr "\n" "~")
      manifest=$(echo "${manifest}" | sed "s|@@CLOUDSQL_VOLUME_MOUNT@@|${volume_mount}|" | tr "~" '\n')
    else
      manifest=$(echo "${manifest}" | sed "s|@@CLOUDSQL_VOLUME@@||" | sed "s|@@CLOUDSQL_VOLUME_MOUNT@@||")
    fi

    echo "${manifest}" | kubectl apply -f -
  fi

}

#
# deploy SCC for operators
#
function deploy_scc() {
  exists=$(kubectl get scc -n ${namespace} tribefire-scc 2>&1 >/dev/null)
  if [[ $? == "1" ]]; then # dont enable force for overwrite since it will remove all scc users
    read -r -d '' scc <<-'EOF'
allowHostDirVolumePlugin: true
allowHostIPC: true
allowHostNetwork: true
allowHostPID: true
allowHostPorts: true
allowPrivilegeEscalation: true
allowPrivilegedContainer: true
allowedCapabilities:
- '*'
allowedUnsafeSysctls:
- '*'
apiVersion: v1
requiredDropCapabilities: []
defaultAddCapabilities: []
priority: 0
fsGroup:
  type: RunAsAny
groups: []
kind: SecurityContextConstraints
metadata:
  annotations:
    kubernetes.io/description: 'privileged scc for tfcloud-operator'
  name: tribefire-scc
readOnlyRootFilesystem: false
runAsUser:
  type: RunAsAny
seLinuxContext:
  type: RunAsAny
seccompProfiles:
- '*'
supplementalGroups:
  type: RunAsAny
users: []
volumes:
- '*'
EOF

    echo "${scc}" | kubectl apply -f -
  else
    echo "Not creating SCC since it already exists"
  fi

  scc_user="system:serviceaccount:${namespace}:tfcloud-operator"
  kubectl patch scc tribefire-scc --type='json' -p='[{"op": "add", "path": "/users/-", "value":"'"${scc_user}"'"}]'
}

#
#
# MAIN
#
#

# parse options
while getopts "h?vt:n:c:o:u:p:fldsre:" opt; do
  case "$opt" in
  h | \?)
    show_help
    exit 0
    ;;
  v)
    verbose=1
    ;;
  d)
    dry_run=1
    ;;
  s)
    skip_crd=1
    ;;
  f)
    force=1
    ;;
  l)
    local_mode=1
    ;;
  r)
    openshift=1
    ;;
  e)
    etcd_cluster_size=$OPTARG
    ;;
  n)
    namespace=$OPTARG
    ;;
  u)
    image_registry_username=$OPTARG
    ;;
  p)
    image_registry_password=$OPTARG
    ;;
  c)
    cloud_sql_service_account=$OPTARG
    ;;
  o)
    operator_service_account=$OPTARG
    ;;
  t)
    tag=$OPTARG
    ;;
  esac
done

shift $((OPTIND - 1))
[[ "${1:-}" == "--" ]] && shift

# check mandatory options
if [[ -z "${namespace}" ]]; then
  echo "No namespace set. Use -n <namespace>"
  exit 1
fi

if [[ -z "${image_registry_username}" ]]; then
  echo "No username for image registry set. Use -u <username>"
  exit 1
fi

if [[ -z "${image_registry_password}" ]]; then
  echo "No password for image registry set. Use -p <password>"
  exit 1
fi

if [[ verbose -eq 1 ]]; then
  echo "verbose=$verbose, namespace='${namespace}', tag='${tag}'"
  echo "dry_run='${dry_run}', skip_crd='${skip_crd}', force='${force}'"
  echo "image_registry_username='${image_registry_username}', image_registry_password='${image_registry_password}'"
  echo "cloud_sql_service_account='${cloud_sql_service_account}', operator_service_account='${operator_service_account}'"
  echo "Leftovers: $@"
fi

if [[ dry_run -eq 1 ]]; then
  echo "---------------------------------------------"
  echo " Running in DRY mode, not executing actions  "
  echo "---------------------------------------------"
fi

# check if CRD is deployed and exit if not
if [[ skip_crd -eq 0 ]]; then
  err=$(kubectl get crd tribefireruntimes.tribefire.cloud)
  if [[ $? != "0" ]]; then
    echo "CRD tribefireruntimes.tribefire.cloud does not exist, deploying it..."
    err=$(kubectl create -f ../config/crds/tribefire_v1alpha1_tribefireruntime.yaml)
  fi
  if [[ $? != "0" ]]; then
    echo "Unable to deploy CRD tribefireruntimes.tribefire.cloud: ${err}"
    exit 1
  fi
fi

# check if local mode was selected
if [[ local_mode -eq 1 ]]; then
  pull_policy="Never"
fi

#check_create_namespace
create_secrets
#deploy_rbac
if [[ ${etcd_cluster_size} -gt 0 ]]; then
  deploy_etcd_cluster
fi

#deploy_operator
#
#if [[ openshift -eq 1 ]]; then
#  deploy_scc
#fi
