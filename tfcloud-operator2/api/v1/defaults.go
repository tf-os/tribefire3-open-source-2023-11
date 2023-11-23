package v1

import (
	//"github.com/braintribehq/tfcloud-operator/pkg/common"
	"k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
	"tribefire-operator/common"
)

const (
	DefaultImageServices      = "dockerregistry.example.com/tribefire-cloud/standard-cloud-setup/tribefire-master"
	DefaultImageControlCenter = "dockerregistry.example.com/tribefire-cloud/standard-cloud-setup/tribefire-control-center"
	DefaultImageModeler       = "dockerregistry.example.com/tribefire-cloud/standard-cloud-setup/tribefire-modeler"
	DefaultImageWebReader     = "dockerregistry.example.com/tribefire-cloud/standard-cloud-setup/tribefire-web-reader"
	DefaultImageExplorer      = "dockerregistry.example.com/tribefire-cloud/standard-cloud-setup/tribefire-explorer"

	DefaultImageTag = "latest"
	DefaultReplicas = 1
	DefaultLogLevel = "INFO"
	DefaultDomain   = "tribefire.local"

	DefaultIngressExplorerPath      = "/explorer"
	DefaultIngressModelerPath       = "/modeler"
	DefaultIngressWebReaderPath     = "/web-reader"
	DefaultIngressControlCenterPath = "/control-center"

	DefaultFinalizerName = "default.finalizers.tribefire.cloud"
)

func SetDefaults(tf *TribefireRuntime) (bool, error) {
	changed := false

	if tf.GetLabels()["stage"] == "" {
		if tf.GetLabels() == nil {
			tf.SetLabels(make(map[string]string))
		}

		tf.GetLabels()["stage"] = "default"
	}

	//tf.Status.Message = "initial"
	//tf.Status.Phase = TribefireValidation

	if tf.Spec.Backend.Type == "" {
		tf.Spec.Backend.Type = EtcdBackend
		tf.Spec.Backend.Params = DefaultEtcdParams
	}

	if tf.Spec.Backend.Type == EtcdBackend && len(tf.Spec.Backend.Params) == 0 {
		tf.Spec.Backend.Params = DefaultEtcdParams
	}

	if tf.Spec.Domain == "" {
		tf.Spec.Domain = DefaultDomain
		changed = true
	}

	if tf.Spec.DatabaseType == "" {
		tf.Spec.DatabaseType = LocalPostgresql
	}

	var initializedComponents []TribefireComponent

	for _, component := range tf.Spec.Components {
		if component.Replicas == 0 {
			component.Replicas = DefaultReplicas
			changed = true
		}

		if component.LogLevel == "" {
			component.LogLevel = DefaultLogLevel
			changed = true
		}

		if component.LogJson == "" {
			component.LogJson = "true"
			changed = true
		}

		if component.ImageTag == "" {
			component.ImageTag = DefaultImageTag
			changed = true
		}

		//TODO: I think there should be no default images at all
		if component.Image == "" {
			componentType := component.Type
			switch componentType {
			case Services:
				component.Image = DefaultImageServices
				changed = true
			case ControlCenter:
				component.Image = DefaultImageControlCenter
				changed = true
			case Modeler:
				component.Image = DefaultImageModeler
				changed = true
			case WebReader:
				component.Image = DefaultImageWebReader
				changed = true
			case Explorer:
				component.Image = DefaultImageExplorer
				changed = true
			case Cartridge:
				return false, MissingCartridgeImage
			default:
				return false, UnknownComponentError
			}
		}

		componentType := component.Type
		switch componentType {
		case ControlCenter:
			if component.ApiPath == "" {
				component.ApiPath = DefaultIngressControlCenterPath
			}
			changed = true
		case Modeler:
			if component.ApiPath == "" {
				component.ApiPath = DefaultIngressModelerPath
			}
			changed = true
		case WebReader:
			if component.ApiPath == "" {
				component.ApiPath = DefaultIngressWebReaderPath
			}
			changed = true
		case Explorer:
			if component.ApiPath == "" {
				component.ApiPath = DefaultIngressExplorerPath
			}
			changed = true
		}

		if common.DefaultComponentResourceConstraintsEnabled() {
			common.L().Debugf("Setting Resource defaults for component=%s", component.Name)
			component.Resources = setComponentResourceDefaults(&component)
			changed = true
		}

		initializedComponents = append(initializedComponents, component)
	}

	tf.Spec.Components = initializedComponents

	return changed, nil
}

func hasLimitsSet(component *TribefireComponent) bool {
	return len(component.Resources.Limits) > 0
}

func setComponentResourceDefaults(component *TribefireComponent) v1.ResourceRequirements {
	switch component.Type {
	case Services:
		return buildDefaultMasterResources(component.Resources)
	case Cartridge:
		return buildDefaultCartridgeResources(component.Resources)
	default:
		return buildDefaultComponentResources(component.Resources)
	}
}

func buildDefaultMasterResources(resources v1.ResourceRequirements) v1.ResourceRequirements {
	return buildDefaultResources(resources, 1000, 1000, 3000, 3000)
}

func buildDefaultCartridgeResources(resources v1.ResourceRequirements) v1.ResourceRequirements {
	return buildDefaultResources(resources, 2000, 250, 2000, 2000)
}

func buildDefaultComponentResources(resources v1.ResourceRequirements) v1.ResourceRequirements {
	return buildDefaultResources(resources, 500, 250, 350, 350)
}

func buildDefaultResources(requirements v1.ResourceRequirements, cpuLimits int64, cpuRequests int64, memLimits int64, memRequests int64) v1.ResourceRequirements {
	newRequirements := requirements.DeepCopy()

	if newRequirements.Limits == nil {
		newRequirements.Limits = make(v1.ResourceList)
	}

	if newRequirements.Requests == nil {
		newRequirements.Requests = make(v1.ResourceList)
	}

	//limits
	if requirements.Limits.Cpu().IsZero() {
		newRequirements.Limits["cpu"] = *resource.NewScaledQuantity(cpuLimits, resource.Milli)
	}

	if requirements.Limits.Memory().IsZero() {
		newRequirements.Limits["memory"] = *resource.NewScaledQuantity(memLimits, resource.Mega)
	}

	//requests
	if requirements.Requests.Cpu().IsZero() && requirements.Limits.Cpu().IsZero() {
		newRequirements.Requests["cpu"] = *resource.NewScaledQuantity(cpuRequests, resource.Milli)
	}

	if requirements.Requests.Memory().IsZero() && requirements.Limits.Memory().IsZero() {
		newRequirements.Requests["memory"] = *resource.NewScaledQuantity(memRequests, resource.Mega)
	}

	common.L().Debugf("Memory Limits: %s", newRequirements.Limits.Memory().String())
	common.L().Debugf("Memory Requests: %s", newRequirements.Requests.Memory().String())
	common.L().Debugf("Cpu Limits: %s", newRequirements.Limits.Cpu().String())
	common.L().Debugf("Cpu Requests: %s", newRequirements.Requests.Cpu().String())

	return *newRequirements
}
