# tribefire 2.3.0 - 14/04/2022

## Update Instructions
To update to the new release adjust your repository settings to use the respective release repository `https://artifactory.server/artifactory/tribefire-2-3-0`. Note that the repository will not be updated anymore, i.e. there won't be any future fixes or improvements. To automatically stay on the latest tribefire 2.3 patch release use repository `https://artifactory.server/artifactory/tribefire-2-3` instead.

Furthermore, check whether any of the versions of your dependencies have changed and adjust them accordingly.
For example, most likely your artifact group depends on group `tribefire.setup.classic.env`, e.g. `env-aware-standard-setup`. In that case please switch from version `2.2` (or whichever version of tribefire you used before) to `2.3`. The full list of branched artifact groups can be found below.

### Branched Artifact Groups
Several artifact groups are branched with each release, which means the respective major.minor versions change.
Please find below the list of groups and the respective versions that belong to this tribefire release.

#### Standard Setups
- tribefire.setup.classic 2.3
- tribefire.setup.classic.env 2.3

#### Clients
- tribefire.app.explorer 2.3
- tribefire.app.modeler 2.3

#### Extensions
- tribefire.extension.activemq 2.4
- tribefire.extension.audit 1.2
- tribefire.extension.aws 2.7
- tribefire.extension.demo 2.3
- tribefire.extension.elastic 6.1
- tribefire.extension.email 3.2
- tribefire.extension.etcd 1.3
- tribefire.extension.gcp 2.8
- tribefire.extension.html-marshalling 2.2
- tribefire.extension.jdbcdriver 2.3
- tribefire.extension.kubernetes 2.3
- tribefire.extension.ldap 4.3
- tribefire.extension.okta 1.1
- tribefire.extension.shiro 3.4
- tribefire.extension.simple 2.3
- tribefire.extension.spreadsheet 1.2
- tribefire.extension.tracing 1.2
- tribefire.extension.vitals.jdbc 2.3

### Removed Document.one Artifact Groups
The following artifact groups are no longer part of tribefire releases:
- tribefire.app.web-reader
- tribefire.extension.conversion
- tribefire.extension.document.common
- tribefire.extension.documents
- tribefire.extension.wopi

They are now developed and released by the Document.one team.

### Cartridges
In case you are still using cartridges, please don't forget to re-build all cartridges against the new release repository. Please also note that there should be no reason to still use cartridges and it's strongly recommended to switch to modules. The next major release, tribefire 3.0, will no longer support cartridges.

## Java Version Support
Tribefire now officially supports Java 18 and tribefire Docker images also use this version. When running tribefire on-prem, one can still use Java 8 or Java 11, but it is recommended to update to (at least) Java 17, since this is the latest LTS version. Note that tribefire 3.0 will require Java 17.

## New reason: ParseError
Added `ParseError` to `essential-reason-model`

## Querying
Pagination limit of <= 0 is considered as +infinity.

## Hibernate Access
Fixing DbUpdateStatement/Discriminator value resolution when multiple inheritance involved.
Better Oracle dialect detection based on regex.

## DCSA
Improved compatibility with jinni output - when re-setup was run, changes to DCSA config were not considered.

## Modules
Added `DeploymentContract.deployedComponentResolver()` to `module-api`.

## tribefire.js
Fixing virtual properties - allowing null as a valid value if type is nullable + exception if number assigned to a long.

## Bugfixes
Access/Domain model accessory cache purging.
Concurrency in SMOOD indices.
Ensuring id has String TypeSpecification for every CSA, not just cortex.

## Tooling
Jinni/ant-task upgraded to work on M1 chips.
Replaced usage of build commands in ant scripts by native ant tasks.
Parallel build now prints the failed tasks as the last ones.
Skip support for parallel build.
Jinni's `update-devrock-ant-tasks` now looks for latest version
Jinni renders a resolution tree for each module when verbose.


## GME
Texts typed using the HtmlEditor are no longer escaped while viewing them. That editor has also further configuration aspects.
External components action opener now handle configured modelPaths.
Improved support for readonly properties.
Many improvements for using external components within GME (including a new metadata for using them as Detail - DetailWithUiComponent - and a new metadata for initializing external modules before actually using them - UxModulesInitializer).
Fixing undoing manipulation when an autocommit fails somehow.
Improved the dialog for showing Reason exceptions.

## WebTerminals
Support for authorizable web terminals

## Marshalling
StaxMarshaller fully supports EntityVisitor

## GenericEntity
consolidated logic of string methods on GenericEntity
- toString()
- asString()
- stringify()
