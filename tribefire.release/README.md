# Tribefire Releases

## Introduction
This group used to create tribefire releases. These releases are described by `tribefire-release-view` which is a so-called _repository view_ (artifact) or just a _view_.

Attention: Note that views are still in development and there is no official support yet! Many things may still change!

For now this group is used for view tests and demos. For some first local tests one can run the steps described below.

## Preparation
Create a base directory for views tests and let environment variable `VIEWS_TESTS_DIR` point to that directory. The purpose of the variable is just to be able to conveniently run the commands from this `README`. It's not needed for views in general.

Clone this Git repository in that folder. The following command must list this README:

	ls ${VIEWS_TESTS_DIR}/tribefire.release/README.md

If you want to run tests against a `settings.xml` in a custom location (to e.g. use a custom local repository), create the respective settings and point to the file:

	export ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS=${VIEWS_TESTS_DIR}/maven-settings/settings.xml
	# also unset standard Maven variables, just to make sure they won't be used accidentally
	unset M2_HOME
	unset M2_CONF
	unset M2_REPO

The `settings.xml` can contain standard settings. Repositories `core-dev`, `third-party` and `devrock` are required.

## Define Release Dependencies
Before we can create a release, i.e. lock respective versions, we need to define what should be part of our release. This is done via an artifact which depends on everything to be included in the release, see `tribefire-release-deps/pom.xml`. It's a normal artifact which can be built and installed the usual way:

	cd ${VIEWS_TESTS_DIR}/tribefire.release
	ant -Drange=tribefire-release-deps]

Before we can create (or update) a release view based on `tribefire-release-deps`, it must be published though. This is because otherwise we'd lock `-pc` versions, which obviously don't exist in the repository.
To continue with this little demo one can alternatively also just delete `tribefire.release:tribefire-release-deps` from the local repository.

## Update Release View
To determine the locks and update the release view run the following Jinni command:

	jinni.sh lock-versions --terminals tribefire.release:tribefire-release-deps#3.0 --targetFile ${VIEWS_TESTS_DIR}/tribefire.release/tribefire-release-view/repositoryview.yaml --includeAlreadyLockedVersions --markAsRelease : options --log stdout

The release view could be published now as usual via the CI. Alternatively (for a local test) just re-install `tribefire-release-view` locally:

	ant -Drange=tribefire-release-view]

## Set Up Repository Configuration
To use the release one has to create a repository configuration based on the release view. Note that this will be done on-the-fly in the future (with new MC). For now there is a Jinni request:

	jinni.sh setup-repository-configuration --views tribefire.release:tribefire-release-view#3.0 --installationPath ${VIEWS_TESTS_DIR}/tribefire-release-repository-configuration : options --log stdout

One can activate the repository configuration via environment variable:

	export DEVROCK_REPOSITORY_CONFIGURATION=${VIEWS_TESTS_DIR}/tribefire-release-repository-configuration/repository-configuration.yaml

Repository configuration is still work in progress:
Jinni 2.0 can process the configuration as is, but it will only read the filters from there, i.e. one still needs a separate `settings.xml`.
Jinni 2.1 can read the full repository configuration, but that also means the file has to be complete and e.g. contain credentials. We plan to use so-called repository enrichments for this in the future. Furthermore we will support environment variables in the repository configuration. But for now please just edit the file manually and add `user` and `password` for each repository and also the `localRepositoryPathlocal` and let the `url`s point to actual repositories:

	!com.braintribe.devrock.model.repository.RepositoryConfiguration
	repositories:
	  - !com.braintribe.devrock.model.repository.MavenHttpRepository
	    ...
	    url: "https://artifactory.example.org/artifactory/core-dev"
	    user: "example-user"
	    password: "example-password"
	  - !com.braintribe.devrock.model.repository.MavenHttpRepository
	    ...
	    name: "third-party"
	    url: "https://artifactory.example.org/artifactory/third-party"
	    user: "example-user"
	    password: "example-password"
	localRepositoryPath: "/path/to/local-repository"

## Run Example Jinni Setup
We can now use our repository configuration, e.g. with a Jinni setup:

	jinni.sh setup-local-tomcat-platform --setupDependency tribefire.extension.simple:simple-setup#3.0 --installationPath ${VIEWS_TESTS_DIR}/tribefire-release-example-installation : options --log stdout --verbose

Afterwards one can start tribefire as usual:

	cd ${VIEWS_TESTS_DIR}/tribefire-release-example-installation/runtime/host/bin
	./catalina.sh run

Provided that `DEVROCK_REPOSITORY_CONFIGURATION` is set (as we did above), Jinni only uses artifacts matched by the artifact filter in the repository configuration. In our example this means it only uses the locked versions, even if newer versions exist.

To verify this one can edit the locked versions and re-run the setup. For example, if `tribefire.extension.simple:simple-data-model` is locked to `3.0.3`, this can be changed to `3.0.2`. The setup should still work then (provided that `3.0.2` exists in the repository) and Jinni will use that older version although a newer one exists. Likewise one can specify a version which does not exist (e.g. `3.0.999`) or which no longer exists (e.g. `3.0.1`). The setup will fail then.

This demonstrates that based on the `tribefire-release-view` one can create an immutable (i.e. reproducible) tribefire setup.
