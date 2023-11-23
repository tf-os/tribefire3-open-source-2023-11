# tribefire 2.3.3 - 2022-12-05
Tribefire `2.3.3` is a patch release for tribefire `2.3`. These release notes list changes compared to [tribefire 2.3.2](release_tribefire-2.3.2.html).

## Update Instructions
To update to the new release adjust your repository settings to use the respective release repository `https://artifactory.server/artifactory/tribefire-2-3-3`. Note that the repository will not be updated anymore, i.e. there won't be any future fixes or improvements. To automatically stay on the latest tribefire `2.3` patch release use repository `https://artifactory.server/artifactory/tribefire-2-3` instead.

If you are updating from a previous major/minor version, e.g. tribefire `2.2`, also apply the update instructions of [tribefire 2.3.0](release_tribefire-2.3.0.html).

## Fixes

* The Shiro configuration initialization now obfuscates secrets, client IDs and keys when logging the INI configuration.

## Updates

### Postgres JDBC driver
Updated Postgres JDBC driver to `42.5.1`.

### Tomcat
Updated Tomcat to `9.0.69`.