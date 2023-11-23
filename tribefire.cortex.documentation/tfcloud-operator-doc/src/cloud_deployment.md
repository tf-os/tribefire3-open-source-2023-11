# Cloud Deployment

Our Cloud deployment solution allows you to deploy and manage tribefire components locally or to the Cloud.

## Prerequisites

* [tfcloud tool](https://github.com/braintribehq/tfcloud-cli/releases) (add it to the `Path` system variable) 
* Docker version `17.0` or later
* Log in to `docker.artifactory.server` (you can use the `docker login` command)

## General

From a technical perspective, all management operations are performed from the command line and the actual deployment is based on Docker and Kubernetes. You configure the different components that make up your environment (called an initiative) in an `Initiativefile` which is later sent using a tfcloud CLI to Tribefire Cloud Controller for the deployment. As of now, each component in an initiative is a preconfigured Docker image.

> At the moment, tfcloud should not run on Windows due to issues while pulling Docker images.

### tfcloud CLI

tfcloud CLI allows you to deploy and manage your initiatives. With tfcloud CLI you can upload to either:

* Kubernetes cluster
* local Docker environment using Docker compose

> tfcloud CLI uses REST calls to communicate.

### Tribefire Cloud Controller

Tribefire Cloud Controller is a tribefire-based application which runs on a Kubernetes cluster and triggers the deployment of tribefire Docker containers.

Given that each tribefire component can run in their own docker container, they need to know about each other to communicate properly. Tribefire Cloud Controller uses container-specific variables for that. You, as a user, do not need to specify any of those variables - they are fully maintained by Tribefire Cloud Controller. All you need to do is to create your initiative.

There are two different components used for cartridge-master communication, based on the deployment type:

* Apache ActiveMQ is used for local communication
* etcd is used for communication in the Cloud

## Creating an Initiative File

To create your initiative file, follow the steps below:

1. Run the `tfcloud create my-initiative` command (`tfcloud-win create my-initiative` on Windows). A folder called `my-initiative` is created in the current directory, containing the YAML `InitiativeFile`.
2. Open the `InitiativeFile` in a code editor. Several default tribefire components should be listed, including the Demo Cartridge:

```yml
    components:
    name: tribefire-demo-cartridge      # Name of the component.
    type: cartridge                     # 'cartridge' type requires specifying a docker image and will automatically be connected to tribefire services.
    image: docker.artifactory.server/tribefire-demo-cartridge:2.0.0-rc6          # A valid cartridge docker image. e.g. cartridge webapp deployed at ROOT of tribefire-base-image server.
    env:                                # A map of environment variables to set inside the container.          
        somekey: somevalue
```

3. We can add another component to this file to be deployed later, for example the **Simple Cartridge**:

 ```yml
    components:
    name: tribefire-simple-cartridge      # Name of the component.
    type: cartridge                     # 'cartridge' type requires specifying a docker image and will automatically be connected to tribefire services.
    image: docker.artifactory.server/tribefire-simple-cartridge:2.0.0-rc6          # A valid cartridge docker image. e.g. cartridge webapp deployed at ROOT of tribefire-base-image server.
    env:                                # A map of environment variables to set inside the container.          
        somekey: somevalue
```

4. Add other components to the file if needed, and proceed to deployment.

## Deploying Initiative Components Locally

Run the command `tfcloud deploy --local` from the directory of your initiative file. The docker images of all components in the file should be fetched from repository, and deployed locally. To access the deployed initiative, map it in the `hosts` file (on Windows it should be available under `\System32\drivers\etc`), as in:

```yml
#<ip-address>   <hostname.domain.org>   <hostname>
<... other entries ...>
127.0.0.1    my-initiative.local
```

Once mapped, tribefire services should be available under `my-initiative.local:38000`.

## Additional Information

You can check the latest developer documentation under [https://github.com/braintribehq/tfcloud-cli/blob/master/README.md](https://github.com/braintribehq/tfcloud-cli/blob/master/README.md).

<!-- 



MEETING WITH OLIVER

tfcloud cli is gone an you just need kubectrl and custom resource definition

still pretty internal - public clound offering is not going to be present anytime soon

it's easier now to use tribefire in the cloud - easier to install, even for customers

it will be easier to adopt the cloud solution for customers

some docu already exists (readme.md) - local development and local operator setup

new approach can now do cloud and local deployment. wes till have to consider customers using docker-compose

if you want to run tf locally now it is the same setup as in the cloud (minikube or docker for desktop - local)

our operator requires kubernetes 1.11!!!

dockerized tribefire runtimes locally == the same as in the cloud (look into the readme)

what operator does: creates tf runtimes of kubernetes 

TF SETUP MUST BE AVAILABLE ON THE INTERNET!!!!!!!!!!!! WE will create a docu artifact in the repo.  We will contact Neidhart.  -->