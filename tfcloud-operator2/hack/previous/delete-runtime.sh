#!/usr/bin/env bash

#
# Usage: delete-runtime.sh <namespace> <name_of_runtime> [--yes]
#
# this script helps with deleting TribefireRuntimes in the foreground by not using "kubectl delete" but
# instead deleting the TribefireRuntime directly via an API call and setting the "propagationPolicy" to "Foreground"
#
# providing --yes will silently skip the delete confirmation
#

namespace=$1
runtime=$2
yes=$3
timeout=${4:-60s}

if [[ -z ${runtime} ]] || [[ -z ${namespace} ]]; then
    echo "Usage: $0 <namespace> <runtime> [--yes]";
    exit 1;
fi

# start a kube proxy for easy access to API server
kubectl proxy --port=18080 2>&1 > /dev/null &
sleep 2
proxy_pid=$!
if [[ $? -ne 0 ]]; then
    echo "Cannot start kube proxy"
    exit 1;
fi

delete_json=`cat <<JSON
{"kind":"DeleteOptions","apiVersion":"v1","propagationPolicy":"Foreground"}
JSON`

delete_url="localhost:18080/apis/tribefire.cloud/v1alpha1/namespaces/${namespace}/tribefireruntimes/${runtime}"
delete_curl="curl -s -f -X DELETE -H \"Content-Type: application/json\" ${delete_url} -d '${delete_json}' 2>&1 > /dev/null"

# check if given Runtime exists
crd=$(curl localhost:18080/apis/tribefire.cloud/v1alpha1/namespaces/${namespace}/tribefireruntimes 2>/dev/null | jq -r '.items[0].metadata.name')
if [[ ${crd} != ${runtime} ]]; then
    echo "Runtime ${runtime} not found in namespace ${namespace}: ${crd}";
    kill ${proxy_pid}
    exit 1;
fi

# delete the runtime with foreground deletion
if [[ ${yes} != "--yes" ]]; then
    read -p "Delete runtime ${runtime} in ${namespace} (yes/NO): " confirm;
    if [[ ${confirm} == "yes" ]]; then
        eval ${delete_curl}
    else
        kill ${proxy_pid}
        exit 1
    fi
else
    eval ${delete_curl}
fi

# shutdown the proxy
kill ${proxy_pid}

# wait on the TribefireRuntime to be deleted
kubectl wait -n ${namespace} tf/${runtime} --for=delete --timeout=60s

