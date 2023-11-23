package tribefire

import (
	net "k8s.io/api/networking/v1"
	meta "k8s.io/apimachinery/pkg/apis/meta/v1"
	tribefirev1 "tribefire-operator/api/v1"
)

var AllowMultipleIngressUrls = false

func NewIngress(tf *tribefirev1.TribefireRuntime, c *tribefirev1.TribefireComponent, app string, path string) *net.Ingress {

	additionalLabels := make(map[string]string)
	labels := buildLabelSet(tf, app, additionalLabels)

	pathType := net.PathTypePrefix
	ingressRuleValue := net.IngressRuleValue{
		HTTP: &net.HTTPIngressRuleValue{
			Paths: []net.HTTPIngressPath{
				{
					Path:     path,
					PathType: &pathType,
					//ServiceName: DefaultResourceName(tf, app),
					//ServicePort: intstr.FromInt(HttpPort),
					Backend: net.IngressBackend{
						Service: &net.IngressServiceBackend{
							Name: DefaultResourceName(tf, app),
							Port: net.ServiceBackendPort{
								Number: HttpPort,
							},
						},
					},
				},
			},
		},
	}

	ingressRules := []net.IngressRule{
		{
			Host:             buildBaseDomain(tf),
			IngressRuleValue: ingressRuleValue,
		},
	}

	if c.PublicUrl != "" {
		customHost := extractHostNamePort(c.PublicUrl)
		customIngressRule := net.IngressRule{Host: customHost, IngressRuleValue: ingressRuleValue}
		if AllowMultipleIngressUrls {
			ingressRules = append(ingressRules, customIngressRule)
		} else {
			ingressRules[0] = customIngressRule
		}
	}

	annotations := buildIngressAnnotations(tf, c)

	return &net.Ingress{
		TypeMeta: meta.TypeMeta{
			Kind:       "Ingress",
			APIVersion: "extensions/v1beta1",
		},
		ObjectMeta: meta.ObjectMeta{
			Name:        DefaultResourceName(tf, app),
			Namespace:   tf.Namespace,
			Labels:      labels,
			Annotations: annotations,
		},
		Spec: net.IngressSpec{
			Rules: ingressRules,
		},
	}
}

func buildIngressAnnotations(tf *tribefirev1.TribefireRuntime, c *tribefirev1.TribefireComponent) map[string]string {
	annotations := map[string]string{
		"kubernetes.io/ingress.class": "traefik",
	}

	//namePrefix := common.NamePrefix()
	middlewareNamespace := "traefik"
	//if namePrefix == "" {
	//	namePrefix = "tfcloud"
	//}

	if c.Type != tribefirev1.Services {
		//routerMiddleware := namePrefix + "-" + tf.Namespace + "-stripprefix@kubernetescrd"
		routerMiddleware := middlewareNamespace + "-stripprefix@kubernetescrd"
		annotations["traefik.ingress.kubernetes.io/router.middlewares"] = routerMiddleware
		return annotations
	}

	routerMiddleware := middlewareNamespace + "-redirectregex@kubernetescrd" +
		"," + middlewareNamespace + "-replacepathregex@kubernetescrd"

	//routerMiddleware := namePrefix + "-" + tf.Namespace + "-redirectregex@kubernetescrd" +
	//		"," +
	//		namePrefix + "-" + tf.Namespace + "-replacepathregex@kubernetescrd"

	annotations["traefik.ingress.kubernetes.io/router.entrypoints"] = "web"
	annotations["traefik.ingress.kubernetes.io/router.middlewares"] = routerMiddleware
	return annotations
}
