# Importing Artifacts

Importing artifacts from public repositories into a another repository can improve the resilience of your development, because you get independent from the availability and direct vulnerability of public repositories.

The whole process of importing artifacts consist of a number of steps

## Setting up a Development Environment for Import

Setting up a development environment helps to take control over the specific repository configuration for download and upload as well as for where the download cache

Create a folder structure according to the following example.

* *import-env*
  * `dev-environment.yaml` (currently empty file)
  * *artifacts*
    * `repository-configuration.yaml` (contains information from where to download and where to upload)
    * *repo* (local cache for artifact resolution)
  * **download**

```yaml
# example content of repository-configuration.yaml
!com.braintribe.devrock.model.repository.RepositoryConfiguration {
  localRepositoryPath: "${config.base}/repo",
  repositories: [
    # maven central public repository (remove if not required)
    !com.braintribe.devrock.model.repository.MavenHttpRepository {
      name: "maven-central", 
      url: "https://repo1.maven.org/maven2/"
    },
    # add your repositories and or remove the maven-central
  ],
  # change this to match you upload repo (can also be a MavenFileSystemRepository)
  uploadRepository: !com.braintribe.devrock.model.repository.MavenHttpRepository {
    name: "upload", 
    url: "https://your-domain/path/to/repo",
    user: "your-user",
    password: "your-user-password"
  }
}
```

## Downloading Artifacts

## Validating Artifacts

## Uploading Artifacts