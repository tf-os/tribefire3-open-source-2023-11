# Legacy REST DDSA Service Evaluation

A DDSA call allows you evaluate DDSA requests.

[](asset://tribefire.cortex.documentation:includes-doc/rest_old_tip.md?INCLUDE)

## Available Calls

Name    | Syntax | Methods | Parameters   
------- | -----------
[Evaluate](rest_ddsa.md#evaluate) | `.../tribefire-services/rest/eval` | `GET, POST, PUT, OPTIONS` | `sessionId`, `accessId`, `type`

### Evaluate 

This REST call allows you to evaluate a DDSA service based on its type signature.

#### URL Syntax

```
<protocol>://<host>:<port>/tribefire-services/rest/eval?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The external ID of the access where the service request can be found. | Yes
`type`  | The type signature of DDSA service request to be created. | Yes
`*PropertyName`  | You can add property values to the entity by adding them to the REST call. To do so, you add an asterisk `*` in front of the property name, and then add a value, as in `&*PropertyName=PropertyValue`. See the example in [Synchronizing a Cartridge](#synchronizing-a-cartridge) to get an idea of how to use this parameter. | No


#### Projections

Name    | Description
------- | -----------
`envelope`  | The evaluation result.
`message`  | The success/fail message, if the DDSA service has one.

#### Example

In this example we are using REST to send a DDSA service evaluation function for detecting and synchronizing a cartridge.

##### Detecting a Cartridge

There are three required parameters for the streaming operation to function correctly. They are:

* `sessionId`
* `accessId`
* `type`

First, however, you must know the type signature of the DDSA service you want to evaluate. In this example, we use the Detect Cartridges service to detect a cartridge. For this example to work, make sure to download the Simple Cartridge and deploy it.

Call:

```
GET
/tribefire-services/rest/eval?accessId=cortex&type=com.braintribe.model.deploymentapi.cartridge.DetectCartridges&sessionId=YourSessionID
```

Response:

```json
200 OK
"Detected new Cartridges"
```

This means your Simple Cartridge was detected. You can check that by opening Control Center and navigating to the **Cartridges** section and clicking **Show All**.

##### Synchronizing a Cartridge

Now that your cartridge is detected, we use a REST call to synchronize its contents with tribefire.

The service we use in this example is the Synchronize Cartridge service. Even though a regular DDSA service evaluation call takes three arguments, the service we use in this example takes one additional parameter: `cartridge`. The cartridge parameter is necessary because the DDSA service we are evaluating must know which cartridge to synchronize.

Call:

```
GET
/tribefire-services/rest/eval?accessId=cortex&type=com.braintribe.model.deploymentapi.cartridge.SynchronizeCartridge&sessionId=YourSessionId&*cartridge=cartridge:simple.cartridge
```

> Note that the `cartridge` parameter is preceded by an `*` asterisk. That is because it is a dynamic parameter. For more information on dynamic parameters, see [Dynamic Parameters](rest_introduction.md#dynamic-parameters).
Response:

```
200 OK
"Synchronized cartridge: simple.cartridgeSynchronized cartridge: simple.cartridge!\nPlease consider reloading the ControlCenter!\n\n Do you want to reload now?Cartridge successfully synchronized: simple.cartridgeSuccessfully finalized import for cartridge: simple.cartridgeSuccessfully imported 'other' payloads from cartridge: simple.cartridgeSuccessfully imported and refreshed cortex model after cortex payload import from cartridge: simple.cartridgeSuccessfully imported 'cortex' payloads from cartridge: simple.cartridgeSuccessfully imported and refreshed cortex model after cortex payload import from cartridge: simple.cartridgeCreating new CartridgeCortexModel with name: cartridge.model:simple.cartridge/cortexServiceCreating new CartridgeCortexModel with name: cartridge.model:simple.cartridge/cortexDeploying cartridge cortex model 'tribefire.extension.simple:simple-data-model_MD' for cartridge: simple.cartridgeDeploying cartridge cortex model 'tribefire.extension.simple:simple-data-model' for cartridge: simple.cartridgeDeploying cartridge cortex model 'tribefire.extension.simple:simple-service-model' for cartridge: simple.cartridgeDeploying cartridge cortex model 'tribefire.extension.simple:simple-deployment-model' for cartridge: simple.cartridgeSuccessfully imported 'cortex skeletons' payloads from cartridge: simple.cartridgeStart synchronizing cartridge : simple.cartridge"
```
