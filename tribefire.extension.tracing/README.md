# tribefire.extension.tracing

## Deployment using Docker

```bash
docker run -d --name jaeger -e COLLECTOR_OTLP_ENABLED=true -p 14269:14269 -p 16686:16686 -p 4317:4317 jaegertracing/all-in-one:latest
```

### Ports
- 14269: Admin port: health check at / and metrics at /metrics (use e.g. http://localhost:14269/metrics)
- 16686: To access the Jaeger UI
- 4317: Accepts traces in OpenTelemetry OTLP format

## Deployment on K8S

Based on the instruction on https://www.jaegertracing.io/docs/1.18/operator/, https://github.com/jaegertracing/jaeger-operator


### Deployment of the Jager Operator
```bash
kubectl create namespace observability
kubectl create -f https://raw.githubusercontent.com/jaegertracing/jaeger-operator/master/deploy/crds/jaegertracing.io_jaegers_crd.yaml
kubectl create -n observability -f https://raw.githubusercontent.com/jaegertracing/jaeger-operator/master/deploy/service_account.yaml
kubectl create -n observability -f https://raw.githubusercontent.com/jaegertracing/jaeger-operator/master/deploy/role.yaml
kubectl create -n observability -f https://raw.githubusercontent.com/jaegertracing/jaeger-operator/master/deploy/role_binding.yaml
kubectl create -n observability -f https://raw.githubusercontent.com/jaegertracing/jaeger-operator/master/deploy/operator.yaml

kubectl create -f https://raw.githubusercontent.com/jaegertracing/jaeger-operator/master/deploy/cluster_role.yaml
kubectl create -f https://raw.githubusercontent.com/jaegertracing/jaeger-operator/master/deploy/cluster_role_binding.yaml

#check if operator is running
kubectl get deployment jaeger-operator -n observability
```

### Enable ingress for Docker-For-Desktop - nginx
Based on https://kubernetes.github.io/ingress-nginx/deploy/, https://kubernetes.github.io/ingress-nginx/user-guide/exposing-tcp-udp-services/
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.34.1/deploy/static/provider/cloud/deploy.yaml

#check if ingress is up and running
kubectl get pods -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx --watch
```


### Enable ingress for Docker-For-Desktop - traefik
TODO: remove
Based on https://docs.traefik.io/v1.7/user-guide/kubernetes/
```bash
kubectl apply -f https://raw.githubusercontent.com/containous/traefik/v1.7/examples/k8s/traefik-rbac.yaml
kubectl apply -f https://raw.githubusercontent.com/containous/traefik/v1.7/examples/k8s/traefik-deployment.yaml
kubectl apply -f https://raw.githubusercontent.com/containous/traefik/v1.7/examples/k8s/traefik-ds.yaml

```

### Deployment of a simplest setup
```bash
kubectl apply -f simplest.yaml

#add ingress for connector
kubectl apply -f ingress.yaml

#check if jaeger instances are there
kubectl get jaegers --all-namespaces
#check pods
kubectl get pods -l app.kubernetes.io/instance=simplest
#check ingress
kubectl get ingress --all-namespaces
#check services
kubectl get service --all-namespaces
```


## Configuration

Either sending traces to an agent (sidecar) or directly to the collector.