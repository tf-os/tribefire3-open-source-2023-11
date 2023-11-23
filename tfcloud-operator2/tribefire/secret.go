package tribefire

import (
	"io/ioutil"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	tribefirev1 "tribefire-operator/api/v1"
	"tribefire-operator/providers"

	. "tribefire-operator/common"
)

const (
	SystemJsonServiceAccountFile              = "/cloudsql/system.json"
	CustomJsonServiceAccountFile              = "/cloudsql/custom.json"
	DefaultArtifactorySecretSuffix            = "bt-artifactory"
	DefaultCloudSqlServiceAccountSecretSuffix = "cloudsql-account"
	DefaultSystemDbSecretSuffix               = "systemdb"
)

func NewDatabaseSecrets(tf *tribefirev1.TribefireRuntime, dbDesc *providers.DatabaseDescriptor) *corev1.Secret {
	tfDbName := buildDefaultDbSecretName(tf)
	secret := &corev1.Secret{
		Type: corev1.SecretTypeOpaque,
		TypeMeta: metav1.TypeMeta{
			Kind:       "Secret",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      tfDbName,
			Namespace: tf.Namespace,
		},
		Data: map[string][]byte{
			"username": []byte(dbDesc.DatabaseUser),
			"password": []byte(dbDesc.DatabasePassword),
		},
	}

	addOwnerRefToObject(secret, asOwner(tf))
	dumpResourceToStdout(secret)
	return secret
}

func buildDefaultDbSecretName(tf *tribefirev1.TribefireRuntime) string {
	return tf.Name + "-" + DefaultSystemDbSecretSuffix
}

func NewServiceAccountSecret(tf *tribefirev1.TribefireRuntime) (*corev1.Secret, error) {
	secretName := buildDefaultCloudSqlServiceAccountSecretName(tf)
	systemJson, err := ioutil.ReadFile(SystemJsonServiceAccountFile)

	data := map[string][]byte{}

	if err != nil {
		L().Errorf("Cannot read system.json: %v", err)
		return nil, err
	}

	data["system.json"] = systemJson

	customJson, err := ioutil.ReadFile(CustomJsonServiceAccountFile)
	if err == nil {
		L().Debug("Adding custom.json")
		data["custom.json"] = customJson
	}

	secret := &corev1.Secret{
		Type: corev1.SecretTypeOpaque,
		TypeMeta: metav1.TypeMeta{
			Kind:       "Secret",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      secretName,
			Namespace: tf.Namespace,
		},
		Data: data,
	}

	addOwnerRefToObject(secret, asOwner(tf))
	dumpResourceToStdout(secret)
	return secret, nil
}

func NewImagePullSecret(tf *tribefirev1.TribefireRuntime) *corev1.Secret {
	dockerPullSecretBase64 := buildDockerConfigJson()
	secretName := buildDefaultImagePullSecretName(tf)

	secret := &corev1.Secret{
		Type: corev1.SecretTypeDockerConfigJson,
		TypeMeta: metav1.TypeMeta{
			Kind:       "Secret",
			APIVersion: "v1",
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      secretName,
			Namespace: tf.Namespace,
		},
		Data: map[string][]byte{
			corev1.DockerConfigJsonKey: dockerPullSecretBase64,
		},
	}

	addOwnerRefToObject(secret, asOwner(tf))
	dumpResourceToStdout(secret)
	return secret
}

func buildDefaultImagePullSecretName(tf *tribefirev1.TribefireRuntime) string {
	return tf.Name + "-" + DefaultArtifactorySecretSuffix
}

func buildDefaultCloudSqlServiceAccountSecretName(tf *tribefirev1.TribefireRuntime) string {
	return tf.Name + "-" + DefaultCloudSqlServiceAccountSecretSuffix
}
