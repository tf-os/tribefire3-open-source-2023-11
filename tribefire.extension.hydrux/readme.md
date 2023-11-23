## HYDRUX?

### The name

**Hydrux** is one of the few remaining unused cool words that end with **UX**.

It is the brand name of our **TypeScript** framework for writing modular client applications based on **tribefire.js**.

The base of the word is in honor of **Hydra**, a fine lady from Lerna, Greece, known for her slightly unconventional looks and exceptional regeneration capabilities.

This name was chosen for similarity between the infamous Hydra and the intended architecture of a Hydrux-based application with a single platform and multiple modules.

Some people conspire the name implies endorsement of psychedelics consumption. This, however, are unfounded rumors, and all attempts at finding any evidence went up in smoke.

Hail Hydrux!

### The fame

Hydrux encourages (well, demands) a modular client application structure, i.e. one consisting of various components.

On **server side**, these components are configured as `meta-data` (**hydrux-deployment-model**) on an `Access` or a `Service Domain`. The top-level application is defined by model meta-data `HxApplication`, and components for viewing individual entity types are defined by `HxViewWith`. These meta-data contain an `HxView` instance, which denotes the actual view (i.e. visual component), whose`HxModule` denotes the actual JavaScript module.

On **client side** there is **hydrux-platform**, which is a **tribefire.js** based engine for the client application. Configured with basic information (tribefire-services URL, accessId/serviceDomainId) it:
* ensures user is authenticated
* connects to `tribefire-services` (as the authenticated user)
* retrieves the relevant model(s) (for given accessId/serviceDomainId)
* resolves the `Hx` meta-data on these models
* loads relevant `JS` modules (based on MD)
* wires `Hydrux` components together (based on MD)
* binds top-level component to the desired DOM element (html body)
