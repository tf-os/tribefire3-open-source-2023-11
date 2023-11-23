package tribefire

import (
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	tribefirev1 "tribefire-operator/api/v1"
)

func newService(tf *tribefirev1.TribefireRuntime, app string, ports []corev1.ServicePort) *corev1.Service {
	additionalLabels := make(map[string]string)
	labels := buildLabelSet(tf, app, additionalLabels)

	service := &corev1.Service{
		TypeMeta: metav1.TypeMeta{
			Kind:       "Service",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      DefaultResourceName(tf, app),
			Namespace: tf.Namespace,
			Labels:    labels,
		},
		Spec: corev1.ServiceSpec{
			Ports:    ports,
			Selector: labels,
		},
	}

	addOwnerRefToObject(service, asOwner(tf))
	dumpResourceToStdout(service)
	return service
}
