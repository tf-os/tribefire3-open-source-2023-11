package tribefire

import (
	apps "k8s.io/api/apps/v1"
	core "k8s.io/api/core/v1"
	meta "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
	tribefirev1 "tribefire-operator/api/v1"
)

func newDeployment(tf *tribefirev1.TribefireRuntime, app string, podSpec *core.PodTemplateSpec, replicas int32) *apps.Deployment {
	var maxUnavailable = intstr.FromInt(MaxUnavailable)
	var maxSurge = intstr.FromInt(MaxSurge)

	additionalLabels := make(map[string]string)
	labels := buildLabelSet(tf, app, additionalLabels)

	return &apps.Deployment{
		TypeMeta: meta.TypeMeta{
			Kind:       "Deployment",
			APIVersion: "apps/v1",
		},
		ObjectMeta: meta.ObjectMeta{
			Name:      DefaultResourceName(tf, app),
			Namespace: tf.Namespace,
			Labels:    labels,
		},
		Spec: apps.DeploymentSpec{
			Replicas: &replicas,
			Selector: &meta.LabelSelector{MatchLabels: labels},
			Template: *podSpec,
			Strategy: apps.DeploymentStrategy{
				Type: apps.RollingUpdateDeploymentStrategyType,
				RollingUpdate: &apps.RollingUpdateDeployment{
					MaxUnavailable: &maxUnavailable,
					MaxSurge:       &maxSurge,
				},
			},
		},
	}
}
