package tribefire

import (
	apps "k8s.io/api/apps/v1"
	core "k8s.io/api/core/v1"
	net "k8s.io/api/networking/v1"
	tribefirev1 "tribefire-operator/api/v1"
)

const (
	ControlCenterAppName         = "tribefire-control-center"
	ControlCenterHealthCheckPath = "/"
)

func NewTribefireControlCenterService(tf *tribefirev1.TribefireRuntime) *core.Service {
	ports := []core.ServicePort{
		{Name: "http", Protocol: "TCP", Port: HttpPort},
	}

	return newService(tf, ControlCenterAppName, ports)
}

func NewTribefireControlCenterIngress(tf *tribefirev1.TribefireRuntime, c *tribefirev1.TribefireComponent) *net.Ingress {
	ingress := NewIngress(tf, c, ControlCenterAppName, c.ApiPath)
	addOwnerRefToObject(ingress, asOwner(tf))
	dumpResourceToStdout(ingress)
	return ingress
}

func NewTribefireControlCenterDeployment(tf *tribefirev1.TribefireRuntime, component *tribefirev1.TribefireComponent) *apps.Deployment {
	additionalLabels := make(map[string]string)
	additionalLabels["jsonlog"] = component.LogJson
	podSpec := createPod(tf, component, additionalLabels, ControlCenterAppName, ControlCenterHealthCheckPath)
	deployment := newDeployment(tf, ControlCenterAppName, &podSpec, component.Replicas)
	addOwnerRefToObject(deployment, asOwner(tf))
	dumpResourceToStdout(deployment)
	return deployment
}
