#!/usr/bin/env bash

#
# Usage: shutdown-namespace.sh <namespace> [--force]
#
# this script helps with removing namespaces that have TribefireRuntimes and/or operators deployed.
# if you just delete a namespace where an operator manages TribefireRuntimes and forget to undeploy
# those runtimes at a time where the operator was still deployed, then those TribefireRuntimes will
# remain in the namespace and prevent finalization.
#
# This script first checks if there are any TribefireRuntimes in the given namespace, undeploys them
# and then - if --force is provided as well - also removes the namespace. It also checks for stuck
# TribefireRuntimes and removes their finalizer eventually if they cannot be undeployed.
#

delete_etcd=0
force=0

for arg in "$@"; do
    if [[ "$arg" == "--delete-etcd" ]]; then
        delete_etcd=1
        continue
    fi

    if [[ "$arg" == "--force" ]]; then
        force=1
        continue
    fi

    namespace=${arg}
    if [[ -z "$namespace" ]]; then
        echo "Usage: $0 [--force] [--delete-etcd] <namespace1,namespace2,...> ";
        exit 1;
    fi

    exists=$(kubectl get ns ${namespace} 2>&1 > /dev/null)
    if [[ $? != "0" ]]; then
        echo "No such namespace: $namespace";
        continue;
    fi

    echo "Shutting down namespace $namespace..."

    tf_runtimes=$(kubectl get tf -n ${namespace} -ojsonpath='{.items[*].metadata.name}')
    if [[ $? != "0" ]]; then
        echo "Cannot list TribefireRuntime in ${namespace}: ${tf_runtimes}";
        continue;
    fi

    if [[ -z "${tf_runtimes}" ]]; then
        echo "No TribefireRuntimes deployed to namespace ${namespace}"
    fi

    for runtime in ${tf_runtimes}; do
        echo "Undeploying ${runtime}...";
        rc=$(kubectl delete -n ${namespace} tf ${runtime} --wait=false 2>&1 > /dev/null)
        if [[ $? != "0" ]]; then
            echo "Undeploy TribefireRuntime in namespace ${namespace} failed: ${rc}";
            continue
        fi

        wait=0
        max_wait=10
        rc=$(kubectl get -n ${namespace} tf ${runtime} 2>&1 > /dev/null)
        until [[ $? != "0" ]] || [[ wait -eq max_wait ]]; do
           sleep $(( wait++ ))
           if [[ wait -eq 1 ]]; then
             echo "TribefireRuntime still there, retrying."
           else
             printf "."
           fi

           rc=$(kubectl get -n ${namespace} tf ${runtime} 2>&1 > /dev/null)
        done

        if [[ wait -eq max_wait ]]; then
            echo "TribefireRuntime ${tf_runtimes} seems stuck. Removing finalizer..."
            rc=$(kubectl patch tf -n ${namespace} ${tf_runtimes} -p '{"metadata":{"finalizers":[]}}'  --type=merge)
        fi

    done

    #
    # eventually delete the etcd cluster for the namespace
    #
    if [[ "$delete_etcd" -eq 1 ]]; then
        echo "Deleting etcd cluster from namespace ${namespace}";
        etcd_cluster_name=`kubectl get etcd --all-namespaces -o=jsonpath='{ .items[0].metadata.name }' 2>&1`;
        if [[ $? -ne 0 ]]; then
            echo "Unable to find etcd cluster in namespace ${namespace}";
        else
            delete=`kubectl delete etcd -n ${namespace} ${etcd_cluster_name} 2>&1`
            if [[ $? -ne 0 ]]; then
                echo "Unable to delete etcd cluster '${etcd_cluster_name}' in namespace ${namespace}: ${delete}";
            else
                echo "Etcd cluster ${etcd_cluster_name} deleted.";
            fi
        fi
    fi

    #
    # eventually delete the namespace if --force
    #
    if [[ "$force" -eq 1 ]]; then
        echo "Deleting namespace: ${namespace}"
        rc=$(kubectl delete ns ${namespace})
        if [[ $? != "0" ]]; then
            echo "Deleting namespace ${namespace} failed: ${rc}";
        fi
    fi

done









