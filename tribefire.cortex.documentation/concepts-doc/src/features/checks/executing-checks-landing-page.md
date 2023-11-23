## Execution of Checks via Landing Page

The Landing Page is the central entry point of a tribefire instance. It can be reached via:
```
https://localhost:port
```

Starting from there, different kinds of checks are reachable:
* [Platform Vitality Checks](#platform-vitality-checks)
* [Module Health Checks](#module-checks-and-health-checks)
* [Module Checks](#module-checks-and-health-checks)

### Platform Vitality Checks

The landing page section "Administration" contains entry "Runtime".  
"Runtime" provides a link to the system **Health**. The system filters for checks with coverage set to `vitality` and `isPlatformRelevant` set to `true` and executes them distributed.

### Module Checks and Health Checks

The landing page shows all `Configurational Modules` (e.g. Initializing Modules) and `Functional Modules`. Each `Functional Module` provides two links. One pointing to the Module's vitality checks (coverage set to `vitality`), the other one to the Module's general checks. In order to relate a certain `CheckBundle` to a Module, qualify it by setting property `module`.

## See also
* [Execution of Checks via REST](executing-checks-rest.md)
* [Health Checks via cURL](executing-health-checks.md)
