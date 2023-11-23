Send a POST call to `/api/v1/authenticate` for authentication. Make sure to provide your credentials in the body of the request: 

Item | Description
---  | ----
Method and Link | `POST host:port/tribefire-services/api/v1/authenticate`
Body | ``` { "user": "cortex", "password": "cortex" } ```
Headers | Content-Type: `application/json`

You have to include the session ID in every REST call. You can include it in the following:
* URL parameter: `sessionId=yourSessionId`
* header parameter: `gm-session-id`

> You cannot include the session ID in the body of the request!

If you are not logged in (there is no valid session) and you paste a `GET` call in your browser, you will be redirected to a login screen. After logging in, you will see the result
of your `GET` call. 

> The redirect does not happen for the `/api/v1/authenticate` call.