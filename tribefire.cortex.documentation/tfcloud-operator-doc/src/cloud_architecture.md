# Cloud Architecture

We chose a containerized solution as the basis for our cloud offering.

## General

From a technical perspective, all management operations are performed from the command line and the actual deployment is based on Docker and Kubernetes.

You configure the different components that make up your environment in a manifest file which is later sent to Tribefire Operator for deployment. As of now, each component in a manifest is a preconfigured Docker image.

Our cloud deployment solution allows you to deploy and manage Tribefire components locally or to the cloud. In local mode, there is no dependency on a cloud hosted database - everything is done without sending any requests to the cloud like GCP or AWS. Instead, the operator deploys a PostgreSQL instance for every Tribefire initiative to host Tribefire related data, e.g. sessions.

We decided to go with the containerization approach because it is much faster and much more scalable than a virtual machine approach - you get the base system, the dependencies, and your application code in one place. On top of that, Docker offers a huge community of developers. There are certain areas where Docker is lacking though. It does not address the issues of multi-machine deployment, scheduling, scaling, or availability. To solve all those problems, we decided to use Docker along with Kubernetes.

In essence, Kubernetes is a set of APIs that manages cloud native applications at scale. Using Tribefire Operator on Kubernetes allows you to declare the state you want your deployments to be at, observe, and react if the actual state is different than the declared state.

You declare how your app should look like and how it should behave in a manifest file. Then, Kubernetes components make sure your app looks exactly like you described it. You don't need to worry about manually configuring machine types and their workloads or a node exceeding its assigned disk space as Kubernetes.

This isn't our first try at creating a comprehensive cloud solution. In the past, we used to have a custom Tribefire Cloud CLI and a Tribefire Controller cartridge but we decided to scratch that in favor of a Kubernetes-native Tribefire Operator.

> For more information, see [Deploying Tribefire Runtime in the Cloud with Kubernetes and Tribefire Cloud Operator](Deploying_tf_with_operator.md).