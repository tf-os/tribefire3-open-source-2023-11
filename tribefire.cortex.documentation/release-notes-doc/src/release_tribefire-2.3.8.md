# tribefire 2.3.8 - 2023-07-26
Tribefire `2.3.8` is a patch release for tribefire `2.3`. These release notes list changes compared to [tribefire 2.3.7](release_tribefire-2.3.7.html).

## Update Instructions
To update to the new release adjust your repository settings to use the respective release repository `https://artifactory.server/artifactory/tribefire-2-3-8`. Note that the repository will not be updated anymore, i.e. there won't be any future fixes or improvements. To automatically stay on the latest tribefire `2.3` patch release use repository `https://artifactory.server/artifactory/tribefire-2-3` instead.

If you are updating from a previous major/minor version, e.g. tribefire `2.2`, also apply the update instructions of [tribefire 2.3.0](release_tribefire-2.3.0.html).

## Fixes

### Image Specification Detector
Lenient behavior in case of an error - no exception is thrown if an error happens while reading size/number of images.

## Updates

### Tomcat
Updated Tomcat to `9.0.78`.

### Groovy
Updated Groovy (for scripting support) to `3.0.18`.
