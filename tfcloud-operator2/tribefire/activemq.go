package tribefire

import (
	apps "k8s.io/api/apps/v1"
	core "k8s.io/api/core/v1"
	meta "k8s.io/apimachinery/pkg/apis/meta/v1"
	tribefirev1 "tribefire-operator/api/v1"
)

const (
	ActiveMqImage   = "rmohr/activemq:latest"
	ActiveMqPort    = 61616
	ActiveMqAppName = "activemq"
)

func NewActiveMqService(tf *tribefirev1.TribefireRuntime) *core.Service {
	ports := []core.ServicePort{
		{Name: "activemq", Protocol: "TCP", Port: ActiveMqPort},
	}

	service := newService(tf, ActiveMqAppName, ports)
	dumpResourceToStdout(service)
	return service
}

func NewActiveMqDeployment(tf *tribefirev1.TribefireRuntime) *apps.Deployment {
	additionalLabels := make(map[string]string)
	podSpec := core.PodTemplateSpec{
		ObjectMeta: meta.ObjectMeta{
			Name:      tf.Name + "-" + ActiveMqAppName,
			Namespace: tf.Namespace,
			Labels:    buildLabelSet(tf, ActiveMqAppName, additionalLabels),
		},
		Spec: core.PodSpec{
			Containers: []core.Container{
				{
					Name:            ActiveMqAppName,
					Image:           ActiveMqImage,
					ImagePullPolicy: getPullPolicy(),
					Ports: []core.ContainerPort{
						{
							ContainerPort: ActiveMqPort,
							Protocol:      "TCP",
						},
					},
				},
			},
		},
	}

	deployment := newDeployment(tf, ActiveMqAppName, &podSpec, 1)
	addOwnerRefToObject(deployment, asOwner(tf))
	dumpResourceToStdout(deployment)
	return deployment
}
