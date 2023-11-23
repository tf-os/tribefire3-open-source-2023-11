package controllers

import (
	"context"
	"fmt"
	"github.com/google/go-cmp/cmp"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/thoas/go-funk"
	apps "k8s.io/api/apps/v1"
	core "k8s.io/api/core/v1"
	net "k8s.io/api/networking/v1"
	meta "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"strings"
	"time"
	tribefirev1 "tribefire-operator/api/v1"
	. "tribefire-operator/common"
	"tribefire-operator/tribefire"
)

const (
	EndpointUrlUnavailable         = "unavailable"
	DefaultGetIngressMaxRetries    = 3
	DefaultGetIngressDelaySeconds  = 1
	DefaultCacheSettleRetries      = 3
	DefaultCacheSettleDelaySeconds = 3
)

var (
	GetIngressMaxRetries    = DefaultGetIngressMaxRetries
	GetIngressDelaySeconds  = DefaultGetIngressDelaySeconds
	CacheSettleRetries      = DefaultCacheSettleRetries
	CacheSettleDelaySeconds = DefaultCacheSettleDelaySeconds
)

type RuntimeStatusChecker interface {
	Check(tf *tribefirev1.TribefireRuntime) error
}

// sets the overall status of the initiative upon successful sync.
// this only updates the /status sub-resource so that no new generation of the spec is created
// note that this only calls Update() iff in fact something changed, hence the checks below. Otherwise
// we would end up in a loop of status updates
func (r *TribefireRuntimeReconciler) updateTribefireRuntimeStatus(tf *tribefirev1.TribefireRuntime) (bool, error) {
	componentStatuses := make([]tribefirev1.TribefireComponentStatus, len(tf.Spec.Components))
	degraded := false
	updateRequired := false

	for idx, component := range tf.Spec.Components {
		deployment, err := r.queryComponent(tf, &component)
		if err != nil {
			componentLabel := getPromLabel(&component, "query")
			totalReconcileErrors.With(prometheus.Labels{"origin": componentLabel}).Inc()
			L().Errorf("Cannot find deployment for tf %s: %v", tf.String(), err)
		}

		if deployment != nil {
			L().Debugf("Found deployment '%s'", deployment.Name)
		}

		endpointUrl := r.getEndpointUrl(tf, &component)
		status := getComponentStatus(deployment)
		componentStatus := tribefirev1.TribefireComponentStatus{
			Name:      component.Name,
			Status:    status,
			Endpoints: endpointUrl,
		}

		componentStatuses[idx] = componentStatus
		if status != tribefirev1.Available {
			degraded = true
		}

		L().Debugf("Checking status. EndpointUrl: '%s' status: '%s' componentStatus: '%s'",
			endpointUrl, status, componentStatus)

		for _, previousStatus := range tf.Status.ComponentStatus {
			if previousStatus.Name != componentStatus.Name {
				continue
			}

			if previousStatus.Status != componentStatus.Status {
				updateRequired = true
				message := fmt.Sprintf("Status for '%s' switched from '%s' to '%s'",
					componentStatus.Name, previousStatus.Status, componentStatus.Status)

				L().Debug(message)
				r.recordEventEventually(tf, nil, getReason(componentStatus), message)
				break
			}

			for _, currentUrl := range componentStatus.Endpoints {
				if !funk.Contains(previousStatus.Endpoints, currentUrl) {
					message := fmt.Sprintf("Endpoints changed for component '%s': Old: [%s] New: [%s]",
						componentStatus.Name, strings.Join(previousStatus.Endpoints, ","), strings.Join(componentStatus.Endpoints, ","))

					L().Debug(message)
					r.recordEventEventually(tf, nil, string(tribefirev1.ComponentUrlChanged), message)
					updateRequired = true
				}
			}
		}
	}

	tf.Status.ComponentStatus = componentStatuses
	tf.Status.Phase = tribefirev1.TribefireRunning

	// fetch tf from cache then update, might have changed in the meantime
	latest, err := r.fetchLatest(tf, true)
	if err != nil {
		totalReconcileErrors.With(prometheus.Labels{"origin": "resource.query"}).Inc()
		L().Debugf("Reconcile error: '%v' while fetching tf from cache", err)
		return updateRequired, err
	}

	// handle sync annotations
	syncTime := addFirstTimeSyncAnnotationIfInitial(latest)
	updateLastSyncTimeAnnotation(latest, syncTime)

	currentGen := latest.Generation
	latest.Status.ComponentStatus = componentStatuses
	latest.Status.ObservedGeneration = currentGen
	latest.Status.Message = string(tribefirev1.Available)

	latest = updateConditions(latest, degraded)
	if degraded {
		latest.Status.Message = string(tribefirev1.Degraded)
	}

	// when using an etcd backend, check if configured etcd cluster is available
	etcdErr := r.EtcdChecker.Check(tf)
	etcdOk := etcdErr == nil
	latest = updateBackendCondition(latest, etcdOk)

	if etcdErr != nil {
		L().Errorf("Etcd validation failed for runtime: %v", etcdErr)
		degraded = true
	}

	// check if update is required
	if latest.Status.Message != tf.Status.Message {
		updateRequired = true
	}

	if latest.Status.ObservedGeneration != tf.Status.ObservedGeneration {
		updateRequired = true
	}

	// if syncTime is not nil, then this is the first sync for this Runtime
	if updateRequired || syncTime != nil {
		totalTribefireRuntimeStatusUpdates.Inc()
		L().Debugf("Status update required for %s. First time update: %t", latest, syncTime != nil)
		err = r.Client.Status().Update(context.Background(), latest)
	}

	if err != nil {
		totalReconcileErrors.With(prometheus.Labels{PrometheusLabelsErrorOrigin: "status.update"}).Inc()
		L().Errorf("Cannot update status sub-resource for %s: %v", latest.String(), err)
		return updateRequired, err
	}

	// make sure that sync went through. It seems that there is race condition here
	// because the latest update on the /status subresource is sometimes not yet reflected when fetching the resource
	// from the cache
	var statusDiff string
	for retries := CacheSettleRetries; retries > 0; retries-- {
		synced, err := r.fetchLatest(latest, false)
		if err != nil {
			L().Debugf("Reconcile error: '%v' while syncing tf", err)
			return updateRequired, err
		}

		statusDiff = diff(&latest.Status, &synced.Status)
		if updateRequired && statusDiff != "" {
			L().Debugf("Status update for %s didn't persist, re-fetching resource...", latest.Name)
			time.Sleep(time.Duration(CacheSettleDelaySeconds) * time.Second)
			continue
		}

		break
	}

	// check if there is still a diff in the actual and latest status'es
	if updateRequired && statusDiff != "" {
		L().Errorf("Status from cache seems still not up-to-date: %s", statusDiff)
		err = tribefirev1.StatusUpdateNotPeristedError
	}

	L().Debugf("Finished updating 'TribefireRuntime' status. updateRequired: '%t' error: '%v'", updateRequired, err)
	return updateRequired, err
}

func getPromLabel(component *tribefirev1.TribefireComponent, action string) string {
	switch component.Type {
	case tribefirev1.Services:
		return "component.master." + action
	case tribefirev1.ControlCenter:
		return "component.controlcenter." + action
	case tribefirev1.Explorer:
		return "component.explorer." + action
	case tribefirev1.Modeler:
		return "component.modeler." + action
	case tribefirev1.WebReader:
		return "component.webreader." + action
	case tribefirev1.Cartridge:
		return "component.cartridge." + action
	default:
		return "component.UNKNOWN." + action

	}
}

// creates or updates the "Available" condition in the status section. This is important since
// the "Available" condition can be waited upon with `kubectl wait --for=condition=Available`
// Also updates an available status on etcd if etcd backend is configured
func updateConditions(tf *tribefirev1.TribefireRuntime, degraded bool) *tribefirev1.TribefireRuntime {
	availableConditionExist := false

	message := "TribefireRuntime fully available"
	reason := "TribefireRuntimeBecameAvailable"
	status := core.ConditionTrue
	if degraded {
		status = core.ConditionFalse
		message = "TribefireRuntime is degraded"
		reason = "TribefireRuntimeBecameUnavailable"
	}

	for i, condition := range tf.Status.Conditions {
		updateLastTransition := false

		if condition.Type != tribefirev1.TribefireRuntimeAvailable {
			continue
		}

		availableConditionExist = true

		if degraded {
			updateLastTransition = condition.Status != core.ConditionFalse
			condition.Status = core.ConditionFalse

		}

		if !degraded {
			updateLastTransition = condition.Status != core.ConditionTrue
			condition.Status = core.ConditionTrue
		}

		condition.Reason = reason
		condition.Message = message
		condition.LastUpdateTime = meta.NewTime(time.Now())

		if updateLastTransition {
			condition.LastTransitionTime = meta.NewTime(time.Now())
		}

		L().Debugf("Updating available condition to status=%s reason=%s message=%s",
			condition.Status, condition.Message, condition.Reason)

		tf.Status.Conditions[i] = condition
	}

	if !availableConditionExist {
		L().Debugf("Setting new available condition to status=%s reason=%s message=%s", status, message, reason)
		runtimeAvailable := tribefirev1.TribefireRuntimeCondition{
			Type:               tribefirev1.TribefireRuntimeAvailable,
			Status:             status,
			LastTransitionTime: meta.NewTime(time.Now()),
			LastUpdateTime:     meta.NewTime(time.Now()),
			Message:            message,
			Reason:             reason,
		}

		tf.Status.Conditions = append(tf.Status.Conditions, runtimeAvailable)
	}

	return tf
}

// update the etcd availability in the conditions array
func updateBackendCondition(tf *tribefirev1.TribefireRuntime, available bool) *tribefirev1.TribefireRuntime {
	conditionExists := false

	message := "Etcd backend available"
	reason := "EtcdBackendBecameAvailable"
	status := core.ConditionTrue
	if !available {
		status = core.ConditionFalse
		message = "Etcd backend unavailable"
		reason = "EtcdBackendBecameUnavailable"
	}

	for i, condition := range tf.Status.Conditions {
		updateLastTransition := false

		if condition.Type != tribefirev1.EtcdBackendAvailable {
			continue
		}

		conditionExists = true

		if available {
			updateLastTransition = condition.Status != core.ConditionTrue
			condition.Status = core.ConditionTrue

		}

		if !available {
			updateLastTransition = condition.Status != core.ConditionFalse
			condition.Status = core.ConditionFalse
		}

		condition.Reason = reason
		condition.Message = message
		condition.LastUpdateTime = meta.NewTime(time.Now())

		if updateLastTransition {
			condition.LastTransitionTime = meta.NewTime(time.Now())
		}

		tf.Status.Conditions[i] = condition
	}

	if !conditionExists {
		etcdAvailable := tribefirev1.TribefireRuntimeCondition{
			Type:               tribefirev1.EtcdBackendAvailable,
			Status:             status,
			LastTransitionTime: meta.NewTime(time.Now()),
			LastUpdateTime:     meta.NewTime(time.Now()),
			Message:            message,
			Reason:             reason,
		}

		tf.Status.Conditions = append(tf.Status.Conditions, etcdAvailable)
	}

	return tf
}

// check if both status structs are the same
func diff(s1 *tribefirev1.TribefireStatus, s2 *tribefirev1.TribefireStatus) string {
	return cmp.Diff(s1, s2)
}

// returns a reasonable "reason" for emitting a kubernetes event
func getReason(status tribefirev1.TribefireComponentStatus) string {
	switch status.Status {
	case tribefirev1.Available:
		return string(tribefirev1.ComponentAvailable)
	case tribefirev1.Unavailable:
		return string(tribefirev1.ComponentUnavailable)
	case tribefirev1.Degraded:
		return string(tribefirev1.ComponentDegraded)
	default:
		L().Errorf("Unexpected component status: %s", status)
	}

	return "" // todo maybe another default
}

// build the URLs for the component's endpoint.
func (r *TribefireRuntimeReconciler) getEndpointUrl(
	tf *tribefirev1.TribefireRuntime, component *tribefirev1.TribefireComponent) []string {

	ingress := &net.Ingress{}

	name := tribefire.DefaultResourceName(tf, getAppFromComponentType(component))

	// todo hack for making multiple webreaders working
	if component.Type == tribefirev1.WebReader {
		name = tribefire.DefaultResourceName(tf, component.Name)
	}

	namespace := tf.Namespace

	// for now this will only hold one URL
	var urls []string

	//there is no ingress for catridges
	if component.Type != tribefirev1.Cartridge {

		// the ingress might not be available shortly after the initial deployment
		// hence we give it here some time to settle and try again
		// todo think again if this is the correct approach to handle this situation. Might be better to just re-queue the request
		for retry := 0; retry < GetIngressMaxRetries; retry++ {
			err := r.Client.Get(context.Background(), client.ObjectKey{Namespace: namespace, Name: name}, ingress)

			if err == nil {
				break
			}

			if retry < GetIngressMaxRetries {
				L().Warnf("Ingress not (yet) available for component '%s', retrying...", component.String())
				time.Sleep(time.Duration(GetIngressDelaySeconds) * time.Second)
				continue
			}

			L().Errorf("Cannot get ingress for component %s: %v", component.String(), err)
			return append(urls, EndpointUrlUnavailable)
		}

		for _, rule := range ingress.Spec.Rules {
			host := rule.Host
			path := rule.HTTP.Paths[0].Path

			if tf.IsLocalDomain() {
				urls = append(urls, fmt.Sprintf("http://%s:%d%s", host, tribefire.LocalModePort, path))
			} else {
				urls = append(urls, fmt.Sprintf("https://%s%s", host, path))
			}
		}

	}

	return urls
}

// fetch the resources represented by given component from the API server (or the cache)
func (r *TribefireRuntimeReconciler) queryComponent(
	tf *tribefirev1.TribefireRuntime,
	component *tribefirev1.TribefireComponent) (*apps.Deployment, error) {

	// todo this is just a quick hack for allowing multiple webreaders in the same TribefireRuntime
	app := getAppFromComponentType(component)
	if component.Type == tribefirev1.WebReader {
		app = component.Name
	}

	name := tribefire.DefaultResourceName(tf, app)
	deployment := &apps.Deployment{}
	namespace := tf.Namespace

	err := r.Client.Get(context.Background(), client.ObjectKey{Namespace: namespace, Name: name}, deployment)
	if err != nil {
		L().Errorf("Cannot get deployment %s in namespace %s: %v", name, namespace, err)
		return nil, err
	}

	return deployment, nil
}

// check the given Deployment's status and return a usable component status
func getComponentStatus(deployment *apps.Deployment) tribefirev1.ComponentStatus {
	if deployment == nil {
		return tribefirev1.Unavailable
	}

	status := deployment.Status
	L().Debugf("Status of deployment %s: replicas=%d available=%d unavailable=%d ready=%d updated=%d",
		deployment.Name, status.Replicas, status.AvailableReplicas, status.UnavailableReplicas, status.ReadyReplicas,
		status.UpdatedReplicas)

	if status.Replicas > 0 && status.Replicas == status.AvailableReplicas {
		return tribefirev1.Available
	}

	if status.UnavailableReplicas > 0 && status.AvailableReplicas > 0 {
		return tribefirev1.Degraded
	}

	return tribefirev1.Unavailable
}

// set the "initiative.tribefire.cloud/first-sync" annotation on the initiative iff it does not yet exist
func addFirstTimeSyncAnnotationIfInitial(tf *tribefirev1.TribefireRuntime) *string {
	if isInitialSync(tf) {
		syncTime := time.Now().UTC().Format(time.RFC3339)
		tf.Status.Created = syncTime
		return &syncTime
	}

	return nil
}

// update the "initiative.tribefire.cloud/last-sync" annotation on the initiative with
// given time iff not nil otherwise with time.Now()
func updateLastSyncTimeAnnotation(tf *tribefirev1.TribefireRuntime, syncTime *string) {
	if syncTime != nil {
		tf.Status.Updated = *syncTime
	} else {
		tf.Status.Updated = time.Now().UTC().Format(time.RFC3339)
	}
}

// todo this is stupid, just use the Component.Type throughout
func getAppFromComponentType(component *tribefirev1.TribefireComponent) string {
	switch component.Type {
	case tribefirev1.Services:
		return tribefire.MasterAppName
	case tribefirev1.ControlCenter:
		return tribefire.ControlCenterAppName
	case tribefirev1.Explorer:
		return tribefire.ExplorerAppName
	case tribefirev1.Modeler:
		return tribefire.ModelerAppName
	case tribefirev1.WebReader:
		return tribefire.WebReaderAppName
	case tribefirev1.Cartridge:
		return component.Name
	default:
		L().Errorf("None of known component types recognized. Got %v", component.Type)
		return "" // todo
	}
}
