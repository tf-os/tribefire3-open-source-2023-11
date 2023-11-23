# tribefire 2.3.7 - 2023-06-28
Tribefire `2.3.7` is a patch release for tribefire `2.3`. These release notes list changes compared to [tribefire 2.3.6](release_tribefire-2.3.6.html).

## Update Instructions
To update to the new release adjust your repository settings to use the respective release repository `https://artifactory.server/artifactory/tribefire-2-3-7`. Note that the repository will not be updated anymore, i.e. there won't be any future fixes or improvements. To automatically stay on the latest tribefire `2.3` patch release use repository `https://artifactory.server/artifactory/tribefire-2-3` instead.

If you are updating from a previous major/minor version, e.g. tribefire `2.2`, also apply the update instructions of [tribefire 2.3.0](release_tribefire-2.3.0.html).

## New Features

## Changes & Improvements

### Okta Module

* The Okta Module version 1.3 has been extended to use wire templates for enhanced configuration possiblities.

## Updates

### Debian / Docker
Tribefire Docker images are now based on Debian Bookworm.

### Java
Tribefire now officially supports Java `20` and tribefire Docker images also use this version.

### ASM
Updated ASM to `9.5`.

### Groovy
Updated Groovy (for scripting support) to `3.0.17`.

### Postgres JDBC driver
Updated Postgres JDBC driver to `42.6.0`.