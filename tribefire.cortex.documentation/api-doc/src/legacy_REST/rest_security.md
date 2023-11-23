# Legacy REST Security Calls

The security REST calls deal with the authentication of a session and the logging out (or ending) of said session.

[](asset://tribefire.cortex.documentation:includes-doc/rest_old_tip.md?INCLUDE)

## Available Calls

Name    | Syntax | Methods | Parameters   
------- | -----------
[Authenticate](rest_security.md#authenticate) | `.../rest/authenticate` | `POST, OPTIONS` | `user`, `password`
[Logout](rest_security.md#logout)  | `.../rest/logout` | `POST, OPTIONS` | `sessionId`

Since you are required by all other REST calls to pass a valid session `id`, you must use the `authenticate` call before any others.

### Authenticate

The `authenticate` call provides you with a valid session `id`, and using the `reachable` value on the `depth` parameter, you can also receive more information regarding the session.

The session ID is used for further operations, until:

* the session is terminated by a logout call
* the session expires

You must provide valid credentials for this call to succeed.

#### URL Syntax

```
POST
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/authenticate?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`user`  | The username required to provide a `sessionId` | Yes
`password`  | The password required to provide a `sessionId` | Yes

#### Projections

Name    | Description
------- | -----------
`id`  | (Default) Returns the `sessionId`
`envelope`  | Returns a `AuthenticationResponse`
`payload`  | Returns the full `UserSession` object, which contains the `sessionId`
`none`  | Returns nothing but still creates the session

#### Example

Call:

```
POST
http://localhost:8080/tribefire-services/rest/authenticate?user=cortex&password=cortex
```

Response:

```json
{
  "_id" : "0",
  "_type" : "com.braintribe.model.securityservice.UserSession",
  "creationDate" : {
    "_type" : "date",
    "value" : "2014-10-13T17:09:58.149+0200"
  },
  "effectiveRoles" : null,
  "id" : null,
  "isInvalidated" : false,
  "lastAccessedDate" : {
    "_type" : "date",
    "value" : "2014-10-13T17:09:58.149+0200"
  },
  "maxIdleTime" : null,
  "referenceCounter" : 1,
  "sessionId" : "fe85fbb4-601a-4939-9ba2-a6869a57aea4",
  "user" : null
}
```

### Logout

The `logout` call ends the user session being used to validate the REST calls. Any calls made after a logout call require a new session ID.

#### URL Syntax

```
POST
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/logout?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | The ID of the session that should be ended. | Yes
