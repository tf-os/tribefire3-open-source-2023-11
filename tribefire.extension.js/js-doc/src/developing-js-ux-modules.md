Session "Intended tribefire.js Usage"

1. jinni create-js-library my-lib

// Going inside this folder and open pom.xml
// Add dependency to com.braintribe.gm:gm-core-api and com.braintribe.gm:transient-resource-model

/*
* Takes dependencies from pom.xml and resolves them via:
*/
jinni assemble-js-deps

// Opening project in VSC
// As we can see, it resolved dependencies and created symbol-links
// We also got dependencies that brm brought with it
// Also dependencies from gm-core-api

/* Showing typescript files and namespaces inside.
 * All typescript are ambient modules: No module here, just declaring namespaces.
  As those symbol links point into a shared directory (showing symbol links in command line(), all of this is immutable content!
  This means that Jinni sets those to: read-only.
  */

2. Always work with sources coming from the lib folder: _src
Why? If we have imports in .js we alwas have the special part: ../other.artifact.a-1.0~/index.js
We would not end up in a proper solution going from source, but from lib (symbolically linkes doruce folder),
all resolutions will go one step up: into the lib folder, and there we can find the projects inside it.

// How to work with Ambient Modules

var fileResource = $T.com.braintribe.model.resource.FileResource.create();
fileResource.

3. Authentication with tribefire-js-module

$tf.remote.X


HIGHLIGHTS:
- Minimal invasive algorithm
- 

