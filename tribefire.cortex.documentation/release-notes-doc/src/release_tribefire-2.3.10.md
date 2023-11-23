# tribefire 2.3.10 - 2023-10-03
Tribefire `2.3.10` is a patch release for tribefire `2.3`. These release notes list changes compared to [tribefire 2.3.9](release_tribefire-2.3.9.html).

## Update Instructions
To update to the new release adjust your repository settings to use the respective release repository `https://artifactory.server/artifactory/tribefire-2-3-10`. Note that the repository will not be updated anymore, i.e. there won't be any future fixes or improvements. To automatically stay on the latest tribefire `2.3` patch release use repository `https://artifactory.server/artifactory/tribefire-2-3` instead.

If you are updating from a previous major/minor version, e.g. tribefire `2.2`, also apply the update instructions of [tribefire 2.3.0](release_tribefire-2.3.0.html).

## Fixes

### About Page
- Fixed missing property
- Fixed Apple M1 incompatibility issue

## Updates

### Java
Tribefire now officially supports Java `21` and tribefire Docker images also use this version.
