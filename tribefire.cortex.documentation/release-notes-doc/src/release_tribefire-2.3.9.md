# tribefire 2.3.9 - 2023-09-07
Tribefire `2.3.9` is a patch release for tribefire `2.3`. These release notes list changes compared to [tribefire 2.3.8](release_tribefire-2.3.8.html).

## Update Instructions
To update to the new release adjust your repository settings to use the respective release repository `https://artifactory.server/artifactory/tribefire-2-3-9`. Note that the repository will not be updated anymore, i.e. there won't be any future fixes or improvements. To automatically stay on the latest tribefire `2.3` patch release use repository `https://artifactory.server/artifactory/tribefire-2-3` instead.

If you are updating from a previous major/minor version, e.g. tribefire `2.2`, also apply the update instructions of [tribefire 2.3.0](release_tribefire-2.3.0.html).

## Changes & Improvements

### GME
In GME, specifically within the _Property Panel_, `Boolean` properties now have a clear action available within the menu, to set their values to `null`.

## Fixes

### GME
Fixed the issue that the content preview window was not closed when applying the same shortcut used for opening it.

### Accesses
* When deleting an entity, also references to that entity from key-set of some map are handled (i.e. such entries are removed from the map).
* In `Hibernate` access, conditions on Map keys which are entities are now encoded properly.

## Updates

### Tomcat
Updated Tomcat to `9.0.80`.

### Groovy
Updated Groovy (for scripting support) to `3.0.19`.
