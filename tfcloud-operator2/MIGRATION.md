# Migrating to the tfcloud-operator 2.x from 0.x

## Migration procedure
1. Undeploy old traefik and etcd operator
1. Backup DB secrets from the namespace
1. Delete the namespace
1. Recreate the namespace using the `OPERATOR_NAMESPACE=ns-name make deploy` target
1. Restore DB secrets
1. Update the manifest and deploy it

## The Manifest
1. Version has changed: `apiVersion: tribefire.cloud/v1alpha1` needs to be changed to `apiVersion: tribefire.cloud/v1`
1. DCSA configuration was changed: `dcsaConfig.name` and `dcsaConfig.type` are no longer used:
    ```
    dcsaConfig:
        credentialsSecretRef:
            name: database-credentials
        instanceDescriptor: jdbc:postgresql://dbhost:5432/dbname
        name: adx
        type: cloudsql
    ```
    becomes
    ```
    dcsaConfig:
        credentialsSecretRef:
            name: database-credentials
        instanceDescriptor: jdbc:postgresql://dbhost:5432/dbname
    ```

## Traefik
Traefik was updated to version 2. The new version uses `middleware` instead of old rewrite rules. It is covered by `make deploy-traefik` target.

## Etcd
Etcd is set up using updated version of the etcd operator, check make target `deploy-etcd` for details.

## CRD
New CRD is generated on-the-fly when setting up a new namespace.

## CertManager
CertManager is required for management of self-signed certificates used by validating and mutating admission webhooks. It is deployed using a helm chart.

# Migration from 2.0 to 2.1
2.1 release switched back to etcd operator and brings multiple bug fixes, for more details please see the [README](README.md).

## Migration procedure
1. Backup your custom resources in the namespace, e.g. database secrets `kubectl -n namespace get secret yoursecret -o yaml > secret.yaml`
1. Backup TF resources from the namespace `kubectl -n namespace get tf -o yaml > tf.yaml`
1. Deploy etcd operator `make deploy-etcd`
1. Delete the old namespace `OPERATOR_NAMESPACE="namespace" make undeploy`. Ignore etcd errors this will produce, make sure that the namespace was deleted.
1. Create the namespace `DOCKER_HOST="your.docker.host" OPERATOR_NAMESPACE="namespace" make deploy`, make sure that etcd cluster is up `kubectl -n namespace get pods`
1. Restore backup `kubectl apply -f tf.yaml -f secret.yaml`
1. Check TF status `kubectl -n namespace get po`
