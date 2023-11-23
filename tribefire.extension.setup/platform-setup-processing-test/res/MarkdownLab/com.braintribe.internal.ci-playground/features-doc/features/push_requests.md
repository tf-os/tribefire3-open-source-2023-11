# Push Requests
>tribefire allows you to send service requests to web clients over a Websocket endpoint.

## General
`tribefire-services` offer a Websocket endpoint where web-based clients (<a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a> and <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.explorer}}">Explorer</a> ) can receive serialized service requests used to trigger certain activities. Those service requests are called push requests.


You can target push requests at specific users (by role) and web clients (by client ID). Once a push request is processed, the receiving endpoint may return a push response back to the original sender of the request.

Push requests can be sent from any tribefire component capable of working with service requests including:

* Control Center
* cartridges
* REST


The most basic example of this functionality is sending a notification from a tribefire instance (either using Control Center or a cartridge) which is displayed in Explorer or Control Center.

{% include note.html content="As of now, the only service request you can add to a push request is `com.braintribe.model.notification.Notify`. "%}

## `InternalPushRequest` and `PushRequest` Entity Types
You may notice there are two different push request types:
* `InternalPushRequest`
* `PushRequest`

When you create a push request, you use the `PushRequest` type. When you evaluate (send) a push request, the tribefire instance that receives the push request via the Websocket endpoint, clones that request into an `InternalPushRequest` and sends it to all tribefire instances - even to itself. The actual processing is done using the `InternalPushRequest` entity.

## Connection Details
Websocket endpoints are registered and sessions are opened when you start tribefire. When you evaluate a service request, the actual call is then sent to the registered endpoints.

* Websocket Endpoint: `ws://<tribefire-services-url/websocket?<parameters>`
* Secure Websocket Endpoint: `wss://<tribefire-services-url/websocket?<parameters>`

Parameter | Description
---       | ---
`sessionId` |  A valid tribefire session ID used to initiate a Websocket connection. <br/> <br/> This parameter is mandatory.
`clientId`  |  Any meaningful identification of the connecting client which can be used to address push requests to certain clients. For more information, see the **Client IDs** section of this document. <br/> <br/> This parameter is mandatory.
`accept`   | The prefered serialization format of pushed payloads. If not specified` gm/json` is used. <br/> <br/> This parameter is optional.

## Client IDs
tribefire clients (Control Center and Explorer) support Websocket communication by default. You can send a push request to selected clients by using the following client IDs in the details of your request:

Client | Client ID
---    |  ---
Control Center | `tribefire-control-center.<accessId>`
Explorer | `tribefire-explorer.<accessId>`


## Request Details
The type used for sending targeted service requests over the Websocket endpoint is `com.braintribe.model.service.api.PushRequest`. It allows you to specify the service request that should be pushed to the client (its payload) and specify the potential addressees of said service request. 

{% include apidoc_url.html className="PushRequest" link="interfacecom_1_1braintribe_1_1model_1_1service_1_1api_1_1_push_request.html" %}

{% include push_request_details.md %}
{%include note.html content="If a pattern isn't specified, all registered clients match, which means if you don't specify any of the above options, all registered clients will receive the push request."%}
{% include tip.html content="For more information about sending and saving push requests, see [Creating Push Requests](creating_push_requests.html)" %}

## Response Details
Every push request returns a response, which is a notification to the original sender about the success or failure of sending of the attached service request to its recipients. 

Each `PushResponseMessage` entity contains the information about the client that received the request, its `originId` entity containing the application and the node IDs, a message, and whether the service request was evaluated successfully.

`originId` entities are instance IDs of master `tribefire-services`. Every tribefire instance that receives internal push requests, accumulates those success/failure messages and returns them to the tribefire instance where the call originated from. Then the origin tribefire instances do the same for all of those instances and returns them to the client sender.

