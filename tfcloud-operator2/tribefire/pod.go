package tribefire

import (
	core "k8s.io/api/core/v1"
	meta "k8s.io/apimachinery/pkg/apis/meta/v1"
	tribefirev1 "tribefire-operator/api/v1"
	"tribefire-operator/common"
)

const (
	LivenessProbeInitialDelay  = 300
	ReadinessProbeInitialDelay = 7
)

var (
	TribefireMasterPriorityClassName    = "tribefire-master"
	TribefireCartridgePriorityClassName = "tribefire-cartridge"
	TribefireComponentPriorityClassName = "tribefire-component"
)

func createPod(tf *tribefirev1.TribefireRuntime, component *tribefirev1.TribefireComponent,
	additionalLabels map[string]string, appName string, healthCheckPath string) core.PodTemplateSpec {

	healthCheckPath = getHealthCheckPath(component, healthCheckPath)

	readinessCheckPath := healthCheckPath
	readinessCheckPath = getReadinessCheckPath(component, readinessCheckPath)

	pod := core.PodTemplateSpec{
		ObjectMeta: meta.ObjectMeta{
			Name:      DefaultResourceName(tf, appName),
			Namespace: tf.Namespace,
			Labels:    buildLabelSet(tf, appName, additionalLabels),
		},
		Spec: core.PodSpec{
			ImagePullSecrets: []core.LocalObjectReference{
				{
					Name: buildDefaultImagePullSecretName(tf),
				},
			},
			ServiceAccountName: buildDefaultServiceAccountName(tf),
			Containers: []core.Container{
				{
					Name:            appName,
					Image:           component.Image + ":" + component.ImageTag,
					Env:             *buildEnvVars(tf, component),
					ReadinessProbe:  newReadinessProbe(readinessCheckPath, HealthCheckPort, ReadinessProbeInitialDelay),
					LivenessProbe:   newLivenessProbe(healthCheckPath, HealthCheckPort, LivenessProbeInitialDelay),
					ImagePullPolicy: getPullPolicy(),
					Ports:           []core.ContainerPort{{Name: "http", ContainerPort: HttpPort, Protocol: "TCP"}},
				},
			},
		},
	}

	if len(component.Volumes) > 0 {
		pod = *addPersistentVolumes(component, &pod)
	}

	if component.Resources.Size() > 0 {
		pod.Spec.Containers[0].Resources = component.Resources
	}

	if component.EnableJpda == "true" {
		addDebuggingContainerPort(&pod.Spec.Containers[0])
	}

	if common.PodPriorityClassesEnabled() {
		pod.Spec.PriorityClassName = getPriorityClassName(component)
	}

	if len(component.NodeSelector) > 0 {
		pod.Spec.NodeSelector = component.NodeSelector
	}

	return pod
}

func getPriorityClassName(component *tribefirev1.TribefireComponent) string {
	switch component.Type {
	case tribefirev1.Services:
		return TribefireMasterPriorityClassName
	case tribefirev1.Cartridge:
		return TribefireCartridgePriorityClassName
	default:
		return TribefireComponentPriorityClassName
	}
}

func addDebuggingContainerPort(container *core.Container) {
	jpdaPort := core.ContainerPort{Name: "jpda", ContainerPort: JpdaPort, Protocol: "TCP"}
	container.Ports = append(container.Ports, jpdaPort)
}

func addPersistentVolumes(component *tribefirev1.TribefireComponent, pod *core.PodTemplateSpec) *core.PodTemplateSpec {
	for _, tfVolume := range component.Volumes {
		volume := core.Volume{
			Name: tfVolume.Name,
			VolumeSource: core.VolumeSource{
				PersistentVolumeClaim: &core.PersistentVolumeClaimVolumeSource{
					ReadOnly:  false,
					ClaimName: tfVolume.VolumeClaimName,
				},
			},
		}

		mount := core.VolumeMount{
			Name:      tfVolume.Name,
			MountPath: tfVolume.VolumeMountPath,
		}

		// todo currently we only mount the additional volume in the first container
		pod.Spec.Containers[0].VolumeMounts = append(pod.Spec.Containers[0].VolumeMounts, mount)
		pod.Spec.Volumes = append(pod.Spec.Volumes, volume)
	}

	return pod
}

func getHealthCheckPath(component *tribefirev1.TribefireComponent, healthCheckUri string) string {
	if customHealthCheckUri := common.CustomCartridgeHealthCheckPath(); component.Type == tribefirev1.Cartridge && customHealthCheckUri != "" {

		healthCheckUri = customHealthCheckUri
	}

	if customHealthCheckPath := component.CustomHealthCheckPath; customHealthCheckPath != "" {
		healthCheckUri = customHealthCheckPath
	}

	return healthCheckUri
}

func getReadinessCheckPath(component *tribefirev1.TribefireComponent, readinessCheckUri string) string {
	if customReadinessCheckUri := common.CustomCartridgeReadinessCheckPath(); component.Type == tribefirev1.Cartridge && customReadinessCheckUri != "" {

		readinessCheckUri = customReadinessCheckUri
	}

	return readinessCheckUri
}
