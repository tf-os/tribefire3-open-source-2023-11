#!/usr/bin/env bash

############
#
# simple script to run some basic health checks for etcd-operator based etcd clusters
#
###########

INITIAL_SIZE="${INITIAL_SIZE:-3}"
SCALEUP_SIZE="${SCALEUP_SIZE:-5}"
ETCD_CLUSTER_NAME="${ETCD_CLUSTER_NAME:-tf-etcd-cluster}"
ENDPOINT_CHECK_ENABLED="${ENDPOINT_CHECK_ENABLED:-1}"
KILL_NODE_CHECK_ENABLED="${KILL_NODE_CHECK_ENABLED:-1}"
SCALE_CHECK_ENABLED="${SCALE_CHECK_ENABLED:-1}"
VERBOSE="${VERBOSE:-0}"


#
# prints given error message and exits
#
function bailout() {
  message=$1;
  echo "💣 error: $message";
  exit 1;
}


#
# returns the last N events for the test cluster
#
function get_last_events() {
	event_count=$1
	events=$(kubectl get ev  --field-selector involvedObject.name="${ETCD_CLUSTER_NAME}" --sort-by=".lastTimestamp" \
		-o 'go-template={{range .items}}{{.reason}}{{";"}}{{.message}}{{";"}}{{.type}}{{";"}}{{"\n"}}{{end}}' | tail -${event_count})
	echo "$events"
}


#
# scales test cluster using given desired size
#
function scale_etcd_cluster() {
	new_size=$1
	scale_output=$(kubectl patch etcdcluster "${ETCD_CLUSTER_NAME}" --type='json' -p="[{\"op\": \"replace\", \"path\": \"/spec/size\", \"value\":${new_size}}]")
}


#
# gets the list of all etcd-pods for the test cluster, lists the members and endpoint health by
# exec'ing into the etcd pods. does a basic health check by checking in endpoint health is "healthy"
# for every member/pod
#
function validate_endpoint_health() {
	etcd_pods=$(kubectl get pods -o=jsonpath='{.items[*].metadata.name}' -letcd_cluster="${ETCD_CLUSTER_NAME}")
	for etcd_pod in $etcd_pods; do
		etcd_members=$(kubectl exec -it "$etcd_pod" -c etcd -- etcdctl member list  | cut -d',' -f5 | tr -d ' ');
		etcd_member_list=""
		for member in $etcd_members; do
			etcd_member_list="$etcd_member_list, $member";
		done

		etcd_member_list_sane=$(echo "$etcd_member_list" | cut -d ',' -f2-5 | tr -d ' ')
		cluster_unhealthy=$(kubectl exec -it "$etcd_pod" -c etcd -- etcdctl endpoint health --endpoints "$etcd_member_list_sane" | grep -v healthy)
		if [[ -n "$cluster" ]]; then
			bailout "cluster seems unhealthy: $cluster_unhealthy";
		fi
	done
}

#
# reads the last N number of events from etcd-operator event log
# and checks if given message is contained within the event message
#
function verify_operator_events() {
	event_count=$1
	message_pattern=$2
	last_events=$(get_last_events "$event_count")

	IFS=$'\n' read -r -d '' -a last_events_array <<< "$last_events"

	for i in  "${!last_events_array[@]}"
	do
		event_message="${last_events_array[$i]}"
		expected_message=$(echo "$event_message" | grep -c "${message_pattern}")

		[[ $expected_message -eq 1 ]] || bailout "unable to find expected message in operator events. Expected \"${message_pattern}\" in \"${event_message}\""
	done
}

#
# TESTCASE 1:
# find etcd pods for given etcd cluster and get endpoint health by exec'ing into each pod
#
if [[ "$ENDPOINT_CHECK_ENABLED" -eq 1 ]]; then
	[[ $VERBOSE -eq 1 ]] && echo "⚙️ running endpoint health check"
	validate_endpoint_health
	[[ $VERBOSE -eq 1 ]] && echo "✅ endpoint health check OK"
	echo
fi

#
# TESTCASE 2:
# get the first etcd pod, use it to put a key, kill it and check if etcd-operator acts accordingly, and
# finally exec into the new member pod and get the key value
#
if [[ "$KILL_NODE_CHECK_ENABLED" -eq 1 ]]; then
	[[ $VERBOSE -eq 1 ]] && echo "⚙️ running kill pod check "

	etcd_pod_to_kill=$(kubectl get pods -o=jsonpath='{.items[0].metadata.name}' -letcd_cluster="${ETCD_CLUSTER_NAME}")

	# put some random key/value
	key=key1
	val=$(uuidgen | tr -d "\n")

	[[ $VERBOSE -eq 1 ]] && echo "  🗝️ putting key/value ($key/$val)"
	put_output=$(kubectl exec -it "$etcd_pod_to_kill" -c etcd -- etcdctl put "${key}" "${val}")

	[[ $VERBOSE -eq 1 ]] && echo "  🗑️ deleting pod ${etcd_pod_to_kill}"
	delete_pod_output=$(kubectl delete pod "$etcd_pod_to_kill")

	# wait a bit for operator to fix the cluster
	sleep 30;

	# check if last three events in operator are expected. Dead member should be replaced/removed, and
	# a new member should be added
	last_events=$(get_last_events "3")

	[[ $VERBOSE -eq 1 ]] && echo "  🔍 verifying last events"
	being_replace_found=$(echo "$last_events" | grep -c "The dead member $etcd_pod_to_kill is being replaced")
	removed_found=$(echo "$last_events" | grep -c "Existing member $etcd_pod_to_kill removed from the cluster")
	member_added_found=$(echo "$last_events" | grep -c "New Member Added")
	member_added=$(echo "$last_events" | grep "New Member Added" | cut -d';' -f2)

	[[ $being_replace_found -eq 1 ]] || bailout "pod $etcd_pod_to_kill not replaced";
	[[ $removed_found -eq 1 ]] || bailout "pod $etcd_pod_to_kill not removed";
	[[ $member_added_found -eq 1 ]] || bailout "no new pod not added to $ETCD_CLUSTER_NAME";

	new_member=$(echo "$member_added" | sed -E 's/^New member (.+) added to cluster$/\1/')
	actual_val=$(kubectl exec -it "$new_member" -c etcd -- etcdctl get --print-value-only "${key}" | tr -d "\r\n")

	[[ $VERBOSE -eq 1 ]] && echo "  🔍 verifying key/value on new member ${new_member}"
	[[ "$actual_val" == "${val}" ]] || bailout "could not find expected value '${val}' for key '${key}' on member '${new_member}'. Got '${actual_val}'"

	[[ $VERBOSE -eq 1 ]] && echo "✅ kill pod check OK"
	echo
fi

#
# TESTCASE 3:
# scale up cluster, check pods, scale back to original size, check pods
#
if [[ $SCALE_CHECK_ENABLED -eq 1 ]]; then
	[[ $VERBOSE -eq 1 ]] && echo "⚙️ running scale check"
	[[ $VERBOSE -eq 1 ]] && echo "  ⬆️ scaling up"

	scale_etcd_cluster "$SCALEUP_SIZE"
	[[ $VERBOSE -eq 1 ]] && echo "  ⏳ waiting for 60 seconds for resize to settle"
	sleep 60

	verify_operator_events "2" "New Member Added"

	cluster_size=$(kubectl get pods  -letcd_cluster="${ETCD_CLUSTER_NAME}" | grep -vc NAME)
	[[ "${cluster_size}" -eq $SCALEUP_SIZE ]] || bailout "cluster resize check failed. Expected cluster size: \"${SCALEUP_SIZE}\" Was: \"${cluster_size}\""

	[[ $VERBOSE -eq 1 ]] && echo "  ⬇️ scaling down"
	scale_etcd_cluster "$INITIAL_SIZE"
	[[ $VERBOSE -eq 1 ]] && echo "  ⏳ waiting for 60 seconds for resize to settle"
	sleep 60

	verify_operator_events "2"  "Member Removed"

	cluster_size=$(kubectl get pods  -letcd_cluster="${ETCD_CLUSTER_NAME}" | grep -vc NAME)
	[[ "${cluster_size}" -eq $INITIAL_SIZE ]] || bailout "cluster resize check failed. Expected cluster size: \"${INITIAL_SIZE}\" Was: \"${cluster_size}\""

	[[ $VERBOSE -eq 1 ]] && echo "✅ scale check OK"
	echo
fi
