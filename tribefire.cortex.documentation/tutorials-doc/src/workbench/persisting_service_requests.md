# Persisting Service Requests

You can persist transient service requests for later reuse in a workbench access.

## General

You can store a service request template in the cortex workbench access. This allows you to run your custom service requests directly from the cortex workbench access.

## Adding Service Requests to Cortex Workbench

As cortex workbench access is the default view in tribefire, you can add your custom service requests to it.

To add a service request to the cortex workbench access:

1. Create and configure new transient service request.
   > You must use the **New Transient** option because it allows you to instantiate all entities from your `ServiceModel`, which usually contains the `ServiceRequests`. The **New** option allows you to instantiate entities that are part of your persisted `DataModel`.
2. On the **Action Bar**, select the **Save to Workbench** option. Your new service request is now persisted in the cortex workbench access.
