# Simple Example Project
This is a simple example project which used for first view tests and demos. The sources are just based on `tribefire.extension.simple`.
Note that many things related to views can and will still change!

If you haven't done so already, make sure to check `tribefire.release`.
It also has a `README` with similar steps and it's recommended to do these first.

## Preparation
Create a base directory for views tests and let environment variable `VIEWS_TESTS_DIR` point to that directory. The purpose of the variable is just to be able to conveniently run the commands from this `README`. It's not needed for views in general.

Clone this Git repository in that folder. The following command must list this README:

	ls ${VIEWS_TESTS_DIR}/tribefire.cortex.views-playground.simple-example-project/README.md

If you want to run tests against a `settings.xml` in a custom location (to e.g. use a custom local repository), create the respective settings and point to the file:

	export ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS=${VIEWS_TESTS_DIR}/maven-settings/settings.xml
	# also override standard Maven variables, just to make sure they won't be used accidentally
	export M2_HOME=/should-not-be-used
	export M2_CONF=/should-not-be-used
	export M2_REPO=/should-not-be-used

The settings.xml can be standard settings.xml which points to repositories `core-dev`, `third-party` and `devrock`.

## Building the Development View
Build `simple-development-view` (and `parent`):
	cd ${VIEWS_TESTS_DIR}/tribefire.cortex.views-playground.simple-example-project
	ant -Drange=simple-development-view]

## Set Up Repository Configuration
To use the development view one has to create a repository configuration based on it. Note that this will be done on-the-fly in the future (with new MC). For now there is a Jinni request:

	jinni.sh setup-repository-configuration --views tribefire.cortex.views-playground.simple-example-project:simple-development-view#2.0 --installationPath ${VIEWS_TESTS_DIR}/simple-development-repository-configuration : options --log stdout

Old MC can already process this configuration, but it will only read the filters from there, i.e. one still needs a separate `settings.xml`. In new MC this will be improved to make things more convenient.

For now one can activate the repository configuration via environment variable:

	export DEVROCK_REPOSITORY_CONFIGURATION=${VIEWS_TESTS_DIR}/simple-development-repository-configuration/repository-configuration.yaml

## Building the Group
To build the full group just run the usual command

	ant -Drange=.

Provided that `DEVROCK_REPOSITORY_CONFIGURATION` is set (as we did above), the build process only uses artifacts (and artifact versions) matched by the respective artifact filter in the repository configuration. In our example this means that many tribefire/GM groups including some tribefire extensions will be locked. Also access to certain third party libraries which are used in tribefire such as `org.hibernate:hibernate-core` is limited to the version used in tribefire.

One can test this by letting e.g. `simple-data-model` depend on a specific older version of the `root-model` such as `1.0.17`:

	<dependency>
		<groupId>com.braintribe.gm</groupId>
		<artifactId>root-model</artifactId>
		<version>1.0.17</version>
		<?tag asset?>
	</dependency>

This will fail even, if the version exists in the repository, because the `root-model`'s version is locked.

This demonstrates that based on the `simple-development-view` one can create a stable, reproducible development environment.

---

ATTENTION: everything below is work in progress and there are still quite a few open questions.

## Adding New Dependencies
During development one sometimes has to add a new dependency. In general, this works as usual, i.e. one just adds the dependency to the respective POM. And, of course, one can use ranges. For example, to add the demo extension add the following dependency to `simple-setup`:

	<dependency>
		<groupId>tribefire.extension.demo</groupId>
		<artifactId>demo-setup</artifactId>
		<version>${V.tribefire.extension.demo}</version>
	</dependency>

Furthermore, add the respective group version variable in the `parent`:

	<V.tribefire.extension.demo>[2.1,2.2)</V.tribefire.extension.demo>

What's important now is whether `simple-development-view` already grants access to the dependency. There are several cases:

### Dependency already included
The first case is that the dependency is already included in the development view. In that case there is nothing more to do.

In this example the `simple-development-view` depends on `tribefire-release-view` and that includes the demo extension, hence no additional tasks required.

This is also how project developers will work with essential extensions, i.e. there will be a release view which provides not only the `tribefire-web-platform`, but also clients, essential extensions and testing tools, i.e. everything that's usually needed for tribefire project development.

### Dependency not included, but there is in another release
The second case is that the dependency is not included yet, but a release view is available. This should be the usual case for everything built based on tribefire. For example, to depend on ADx one would let `simple-development-view` depend on `adx-release-view`.

The same approach can also be used to add latest extension releases. For example, if the most recent `tribefire-release-view` depends on `demo-release-view#2.1.7` but the latest demo extension bug fixes are only available in `demo-release-view#2.1.8`, one can add it as an additional `simple-development-view` dependency.

### Dependency not included and not released
The last case is that the dependency is not included in any release, e.g. because it's a new extension which hasn't even been released yet. In that case, one can adjust the filter configuration in the `simple-development-view` accordingly.
