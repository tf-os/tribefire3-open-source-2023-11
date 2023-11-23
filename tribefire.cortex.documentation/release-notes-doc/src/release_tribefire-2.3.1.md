# tribefire 2.3.1 - 04/10/2022
Tribefire `2.3.1` is a patch release for tribefire `2.3`. These release notes list changes compared to [tribefire 2.3.0](release_tribefire-2.3.0.html).

## Update Instructions
To update to the new release adjust your repository settings to use the respective release repository `https://artifactory.server/artifactory/tribefire-2-3-1`. Note that the repository will not be updated anymore, i.e. there won't be any future fixes or improvements. To automatically stay on the latest tribefire 2.3 patch release use repository `https://artifactory.server/artifactory/tribefire-2-3` instead.

If you are updating from a previous major/minor version, e.g. tribefire `2.2`, also apply the update instructions of [tribefire 2.3.0](release_tribefire-2.3.0.html).

## Fixes

### Model handling with RPC
* ITW: When weaving an entity-type, prioritize the entity interface on classpath. This fixes a bug that a `GmEntityType` with information incompatible with the classpath was woven.
* Remote service processing unmarshalls the model leniently.

### Hibernate access
* Passing forgotten `HibernateAccess.durationWarningThreshold` to the implementation on deployment.
* Attempting to suppress internal logging by `Hibernate` when converting `HQL` to `SQL` via Hibernate's internal classes when logging.
* Making `HibernateDialect` a property GM enum (by implementing `EnumBase`). Relevant mostly for `tribefire.js`.
* Added option to disables the schema update on model changes

### Binary/blob data upload
The upload of binary data blob via `tribefire.js` requests was fixed. Most notably this allows the upload of ZIP files into the backend.

### Resources
Fixed duplicate resources on persist async resources. (D1-3138)

### Various client fixes
* Fixed the display of reloaded data (when there was a deleted entry, it was still shown in the PP if it was selected). (TFSTUD-380)
* Fixed issue in user profile view.
* Now ignoring global shortcuts (KeyConfiguration) within the GIMA Dialog.
* Fixed issue where some actions where not shown when they were using different locales. (PRNGLINTRA-340)
* Fixed issue when displaying select query results and using inner Joins. (PRNGLINTRA-341)
* Fixed issue in about dialog style. (PRNGLINTRA-342)
* Now checking for the presence of the bind methods for avoiding errors in older versions of external components. (PRNGLINTRA-343)
* Fixed issue in `.map` method (key vs value) in `tf.js`.

### Etcd extension
* Fixed NullPointerException in the Leadership Manager.

## Changes & Improvements

### UnsatisfiedBy metadata
Introduced `UnsatisfiedBy` metadata including annotation support

### OpenAPI reflection
Various improvements in OpenAPI reflection.

### Azure extension
Merged back the Azure extension, since it wasn't part of initial tribefire `2.3` release.

### Various client improvements
* Improved the layout of the Reasons Error Dialog. (TFSTUD-381)
* Sending a new message for external components when the document Preview window has been resized.
* Adding the API for external components being able of manipulating (closing, maximizing, restoring) the window where they are displayed.
* Supporting blobs in web-client by default.
* Added new `IsNullVDE`.

### StaxMarshaller (XML)
* fixed EntityVisitor support in StaxMarshaller (which raised an issue in resource processing of tribefire.js )

### Miscellaneous
* Reduced regular log output.
* Added support for persisting result resources from asynchronous request.
* Standard exception handler will include the client's IP address for better observability.
* Improved handling of connection pools if outgoing HTTP connection is stale.
* Allowing REST service processor to access the incoming original JSON.
* Added support for Basic Authentication in REST calls.
* Erroneous JSON requests that use the MIME type `text/plain` are supported now.
* Improved logging when outgoing HTTP requests produce an error.
* Added analysis information to the Logs-Servlet.
* Added full list of dependencies to About page.

## Updates

### Postgres JDBC driver
Updated Postgres JDBC driver to `42.5.0`.

### Tomcat
Updated Tomcat to `9.0.67`.

### Groovy
Updated Groovy to `3.0.13`.

### ASM
Artifact `btasm` is now based on ASM `9.4`.

### Docker images
The tribefire Docker images (built by Jinni) now use OpenJDK `19`. The images are (still) based on latest Debian Bullseye.

### Jackson
Updated Jackson FasterXML to `2.13.3`.

### Guava
Updated Guava to `31.1-jre`.
