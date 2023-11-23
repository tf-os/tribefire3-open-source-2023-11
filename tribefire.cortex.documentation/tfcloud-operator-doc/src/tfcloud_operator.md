# Tribefire Cloud Operator
Tribefire Cloud Operator is a Kubernetes native operator used to deploy Tribefire manifests (essentially YAML configuration files) on cloud hosts. You can install and use the operator by [cloning](https://github.com/braintribehq/tfcloud-operator) it to your host and invoking the functions available in the provided [Makefile](https://github.com/braintribehq/tfcloud-operator/blob/master/Makefile).

## Prerequisites
* Access to a cloud-hosted database.

## Installing the Operator
To install the operator, you simply need to run `make initial-setup-cloud` on your host. This command, in turn, invokes the following commands:

* `checkup-cloud` - checks user's cloud access by verifying the `operator-service-account.json` and `cloudsql-client.json` files.
* `create-secrets` - creates secrets that host details about the service account (from `.json` files) and artifactory (from system variables).
* `create-prereq` - updates the configuration of namespaces and tribefire runtime.

<!-- Is the makefile configurable? Are the JSON files a thing on both GCP and AWS?-->
<!-- Is installation from makefile our permanent solution?-->

## Deploying the Operator
To deploy the operator, simply run `make deploy-operator-cloud`. Now you can proceed to deploying Tribefire manifests.

## Deploying Tribefire Manifests using the Operator
To deploy a sample Tribefire setup, run the following:

1. Run `make create-demo-namespace` to create tribefire namespace.
2. Run  `make deploy-tribefire` to deploy the tribefire runtime.

<!-- todo: information on what the above commands do + monitoring -->

## Monitoring
For monitoring information, see the [instruction within GitHub repository](https://github.com/braintribehq/tfcloud-operator#using-monitoring)

## Debugging
For debugging information, see the [instruction within GitHub repository](https://github.com/braintribehq/tfcloud-operator/blob/master/documentation/Development.md).

