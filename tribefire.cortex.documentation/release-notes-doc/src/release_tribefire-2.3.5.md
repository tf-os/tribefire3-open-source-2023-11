# tribefire 2.3.5 - 2023-03-07
Tribefire `2.3.5` is a patch release for tribefire `2.3`. These release notes list changes compared to [tribefire 2.3.4](release_tribefire-2.3.4.html).

## Update Instructions
To update to the new release adjust your repository settings to use the respective release repository `https://artifactory.server/artifactory/tribefire-2-3-5`. Note that the repository will not be updated anymore, i.e. there won't be any future fixes or improvements. To automatically stay on the latest tribefire `2.3` patch release use repository `https://artifactory.server/artifactory/tribefire-2-3` instead.

If you are updating from a previous major/minor version, e.g. tribefire `2.2`, also apply the update instructions of [tribefire 2.3.0](release_tribefire-2.3.0.html).

## New Features

# Okta Module

- Added new service requests: GetGroup, ListGroups, ListGroupMembers, ListAppGroups, and ListAppUsers

## Changes & Improvements

- ManipulationRecords (Audit Module) will not be registered if the manipulation has no effect (e.g., the previous value is the same as the new value)

## Updates

### Tomcat
Updated Tomcat to `9.0.73`.
