# tribefire 2.3.4 - 2023-01-23
Tribefire `2.3.4` is a patch release for tribefire `2.3`. These release notes list changes compared to [tribefire 2.3.3](release_tribefire-2.3.3.html).

## Update Instructions
To update to the new release adjust your repository settings to use the respective release repository `https://artifactory.server/artifactory/tribefire-2-3-4`. Note that the repository will not be updated anymore, i.e. there won't be any future fixes or improvements. To automatically stay on the latest tribefire `2.3` patch release use repository `https://artifactory.server/artifactory/tribefire-2-3` instead.

If you are updating from a previous major/minor version, e.g. tribefire `2.2`, also apply the update instructions of [tribefire 2.3.0](release_tribefire-2.3.0.html).

## Fixes

### Processing Engine
- Fixed `candidateEntityType` acquiration that broke the restart (on edge-transition) functionality.
- Adding process traces will no longer load existing traces

## Updates

### Tomcat
Updated Tomcat to `9.0.71`.

### AssertJ
Updated AssertJ to `3.24.2`. This library is only used for testing, i.e. it is not part of a deployed tribefire.
