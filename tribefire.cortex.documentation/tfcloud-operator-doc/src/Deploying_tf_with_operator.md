# Deploying Tribefire Runtime in the Cloud with Kubernetes and Tribefire Cloud Operator

The Tribefire Cloud deployment solution allows you to deploy and manage Tribefire components locally or to the Cloud using Kubernetes. This is done by adding Tribefire runtime as a custom resource in Kubernetes, and using Tribefire Cloud Operator to trigger and manage Tribefire runtime deployments.

> This tutorial focuses on deploying Tribefire cloud operator in a local Kubernetes cluster, not one in the cloud.

Overall, a Kubernetes deployment is composed of three stages:

1. In a Kubernetes cluster, add the custom resource definition of Tribefire runtime. See [Deploying Tribefire Cloud Operator](#deploying-tribefire-cloud-operator).
	> For more information on available runtime components, see [Tribefire Cloud API](Tribefire_Cloud_API_v1alpha1.md).
2. Deploy Tribefire cloud operator in a specific namespace in the same cluster as in the previous step. See [Deploying Tribefire Cloud Operator](#deploying-tribefire-cloud-operator).
3. Deploy TribefireRuntime custom resources in the same namespace as in the previous step.

> For general information on the approach we took for Cloud deployments, see [Cloud Architecture](cloud_architecture.md).

## Tribefire Cloud Operator

Tribefire Cloud Operator is our means to deploy and run containerized applications in the cloud. Tribefire Cloud Operator is an easy way to deploy Tribefire runtime instances and manage them in Kubernetes. It takes control of the TF runtime, meaning every component gets a reference to the owning TF runtime. As all Kubernetes operators, it reuses a range of tools and CLIs, and enables you to apply CRUD operations on your CustomResource which is the TribefireRuntime.

TF Cloud Operator maps TF runtime as a custom resource to Kubernetes native objects. This allows for performing appropriate CRUD operations on Kubernetes native object as a response to any changes in TribefireRuntime resources. See the figure below.

![Tribefire Operator](../images/tf_operator.png)

> Before deploying Tribefire Cloud Operator, make sure you have the prerequisites fulfilled, and then proceed to [Deploying Tribefire Cloud Operator](#deploying-tribefire-cloud-operator).

### Prerequisites

- local Kubernetes cluster
  - Docker for Mac >= `18.0.6` [recommended]
  - minikube [useful for some development tasks]
- `kubectl` >= `1.12.0`
- `dnsmasq` >= `2.8.0`
- (`kctx`)

#### Installing Docker/Kubernetes for Mac

- [https://docs.docker.com/docker-for-mac/install/](https://docs.docker.com/docker-for-mac/install/)
- [https://docs.docker.com/docker-for-mac/#kubernetes](https://docs.docker.com/docker-for-mac/#kubernetes)

Install Docker and activate the Kubernetes support in Docker preferences.

![Activating Kubernetes in Docker](../images/KubernetesSupportInDocker.png)

#### Installing minikube

- Install `VirtualBox` from [https://www.virtualbox.org/](https://www.virtualbox.org/)
- [https://github.com/kubernetes/minikube/releases](https://github.com/kubernetes/minikube/releases)

Installing minikube:

```bash
> brew cask install minikube
```

To be able to access the instances via browser (e.g. http://demo.tribefire.local:30080/services) a port forwarding is necessary - check the settings of the `minikube` VM in `VirtualBox`:

![minikube - add port forwarding](../images/MinikubePortForwarding.png)

Starting minikube:

```bash
> export MINIKUBE_ARGS = --kubernetes-version v1.11.2 --extra-config=apiserver.v=8 \
	--memory=4096 --bootstrapper=kubeadm \
	--extra-config=kubelet.authentication-token-webhook=true --extra-config=kubelet.authorization-mode=Webhook \
	--extra-config=scheduler.address=0.0.0.0 --extra-config=controller-manager.address=0.0.0.0
> minikube start $MINIKUBE_ARGS && eval $(minikube docker-env)
```

Connecting to Docker inside minikube:

```bash
> eval $(minikube docker-env)
```

#### Install `kubectl` for Mac

```bash
> brew install kubernetes-cli
> kubectl version
```

### Deploying Tribefire Cloud Operator

1. Make sure you have the following YAML files prepared before proceeding:
	* [CustomResourceDefinition.yaml](example_deployment_files.md#customresourcedefinitionyaml)
	* [etcdOperator.yaml](example_deployment_files.md#etcdoperatoryaml)
	* [etcdCluster.yaml](example_deployment_files.md#etcdclusteryaml)
	* [RBAC.yaml](example_deployment_files.md#rbacyaml)
	* [TFCloudOperator.yaml](example_deployment_files.md#tfcloudoperatoryaml)
	* [TribefireRuntime.yaml](example_deployment_files.md#tribefireruntimeyaml)
	* 
2. Deploy the `CustomResourceDefinition` for TribefireRuntime:
	```bash
	kubectl create -f CustomResourceDfinition.yaml
	```
3. Deploy the etcd operator and Role-based access control (RBAC) rules:
	```bash
	kubectl create ns etcd
	kubectl create -f etcdOperator.yaml
	kubectl create -f etcdCluster.yaml
	kubectl create -f RBAC.yaml
	```
4. Create a namespace. For example, create the **tfdemo** namespace.

	```bash
	kubectl create ns tfdemo 
	kubectl label namespace tfdemo name=tfdemo
	```
5. Create image pull secrets:
	To retrieve the docker image of the operator, you must create the following secrets. Names of these secrets are then passed under the `imagePullSecrets` tag in the `spec:` section in Tribefire operator's manifest.

	```bash
	# This secret is used for pulling the operator's image from the docker registry

	kubectl create -n tfdemo secret docker-registry  your-artifactory --docker-server=docker.artifactory.yourOrganization.com  --docker-username=your_docker_username --docker-password=your_docker_password --docker-email=email@domain.com
	```
	```bash
	# This secret is used for pulling the images of Tribefire components from the repository where they are located
	kubectl create -n tfdemo secret generic your-artifactory-bootstrap --from-literal=username=your_docker_registry_name  --from-literal=password=your_docker_registry_password
	```
6. Create CloudSQL service accounts secrets (not needed for local deployment)
	```bash
	kubectl create -n tfdemo secret generic cloudsql-service-account               --from-file=service-account.json=your_operator_service_account --from-file=system.json=your_cloud_sql_service_account
	```
7. Deploy tf-operator in tfdemo namespace:
	```bash
	Kubectl create -n tfdemo -f TFCloudOperator.yaml
	```
	> You need to replace `@@IMAGE:@@TAG@@` with docker image and tag that contain the Tribefire Cloud Operator.

### Deploying TribefireRuntime Resources in Kubernetes

After you have deployed the Tribefire cloud operator, you can go ahead and deploy Tribefire runtime as a custom resource as follows:

```bash
Kubectl create -n tfdemo -f TribefireRuntime.yaml
```

## What's Next?

Take a look at the Tribefire Cloud Operator API on [Tribefire Cloud API v1alpha1](Tribefire_Cloud_API_v1alpha1.md).