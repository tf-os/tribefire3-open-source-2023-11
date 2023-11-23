# Tribefire JS Development

## Using Windows?

Windows follows security constraints when it comes to the usage of `symbol links`. As Jinni uses this feature for linking JS libraries (via `assemble-js-deps`)
and later on we might need to manually create `symbol links` as well, the feature needs to be enabled:

* Open the Local Security Policy Administration via `secpol.msc`
* Navigate to `Local Policies` > `User Right Assignments` 
* Search for entry `Create Symbolic Links` and add your user to get priviledge

TODO: Developer Mode

