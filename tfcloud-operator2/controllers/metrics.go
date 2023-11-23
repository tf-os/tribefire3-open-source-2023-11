package controllers

import (
	"context"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"sigs.k8s.io/controller-runtime/pkg/client"
	tribefirev1 "tribefire-operator/api/v1"
	. "tribefire-operator/common"
)

// metrics
var (
	totalReconcileRuns = promauto.NewCounter(prometheus.CounterOpts{
		Name: "tribefire_operator_reconcile_runs_total",
		Help: "The total number of reconcile iterations",
	})

	totalReconcileErrors = promauto.NewCounterVec(prometheus.CounterOpts{
		Name: "tribefire_operator_reconcile_errors_total",
		Help: "The total number of errors during reconcile iterations",
	}, []string{PrometheusLabelsErrorOrigin})

	totalTribefireRuntimeStatusUpdates = promauto.NewCounter(prometheus.CounterOpts{
		Name: "tribefire_operator_status_updates_total",
		Help: "The total number of reconcile iterations",
	})
)

// collectors
type TribefireRuntimeStatusCollector struct {
	totalDeployment     prometheus.GaugeFunc
	degradedDeployments prometheus.GaugeFunc
	deployedComponents  prometheus.GaugeFunc
	degradedComponents  prometheus.GaugeFunc
}

func (c TribefireRuntimeStatusCollector) Collect(ch chan<- prometheus.Metric) {
	ch <- c.totalDeployment
	ch <- c.degradedDeployments
}

func (c TribefireRuntimeStatusCollector) Describe(ch chan<- *prometheus.Desc) {
	prometheus.DescribeByCollect(c, ch)
}

func NewTribefireRuntimeStatusCollector(r *TribefireRuntimeReconciler) *TribefireRuntimeStatusCollector {
	return &TribefireRuntimeStatusCollector{
		totalDeployment: prometheus.NewGaugeFunc(prometheus.GaugeOpts{
			Name: "tribefire_runtime_deployments",
			Help: "The current number of deployed tribefire runtimes"},
			func() float64 {
				list := &tribefirev1.TribefireRuntimeList{}
				namespace := WatchNamespace()
				err := r.Client.List(context.Background(), list, &client.ListOptions{Namespace: namespace})
				if err != nil {
					L().Errorf("Cannot list TribefireRuntimes for metrics collection: %v", err)
					return -1
				}

				return float64(len(list.Items))
			}),
		degradedDeployments: prometheus.NewGaugeFunc(prometheus.GaugeOpts{
			Name: "tribefire_runtime_deployments_degraded",
			Help: "The current number of deployed but degraded tribefire runtimes"},
			func() float64 {
				list := &tribefirev1.TribefireRuntimeList{}
				namespace := WatchNamespace()
				err := r.Client.List(context.Background(), list, &client.ListOptions{Namespace: namespace})
				if err != nil {
					L().Errorf("Cannot list TribefireRuntimes for metrics collection: %v", err)
					return -1
				}

				degraded := 0
				for _, tf := range list.Items {
					if tf.Status.Message == string(tribefirev1.Degraded) {
						degraded++
					}
				}

				return float64(degraded)
			}),
	}
}
