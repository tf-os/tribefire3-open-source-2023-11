package tribefire

import (
	apps "k8s.io/api/apps/v1"
	core "k8s.io/api/core/v1"
	net "k8s.io/api/networking/v1"
	tribefirev1 "tribefire-operator/api/v1"
)

const (
	ExplorerAppName         = "tribefire-explorer"
	ExplorerHealthCheckPath = "/"
)

func NewTribefireExplorerService(tf *tribefirev1.TribefireRuntime) *core.Service {
	ports := []core.ServicePort{
		{Name: "http", Protocol: "TCP", Port: HttpPort},
	}

	return newService(tf, ExplorerAppName, ports)
}

func NewTribefireExplorerIngress(tf *tribefirev1.TribefireRuntime, c *tribefirev1.TribefireComponent) *net.Ingress {
	ingress := NewIngress(tf, c, ExplorerAppName, c.ApiPath)
	addOwnerRefToObject(ingress, asOwner(tf))
	dumpResourceToStdout(ingress)
	return ingress
}

func NewTribefireExplorerDeployment(tf *tribefirev1.TribefireRuntime, component *tribefirev1.TribefireComponent) *apps.Deployment {
	additionalLabels := make(map[string]string)
	additionalLabels["jsonlog"] = component.LogJson
	podSpec := createPod(tf, component, additionalLabels, ExplorerAppName, ExplorerHealthCheckPath)
	deployment := newDeployment(tf, ExplorerAppName, &podSpec, component.Replicas)
	addOwnerRefToObject(deployment, asOwner(tf))
	dumpResourceToStdout(deployment)
	return deployment
}
