# Legacy REST Query

Query REST calls allow you to build your own query statements, using Generic Model Query Language (GMQL).

[](asset://tribefire.cortex.documentation:includes-doc/rest_old_tip.md?INCLUDE)

## Available Calls

Name    | Syntax | Methods | Parameters   
------- | -----------
[Query](rest_query.md#query) | `.../rest/query` | `GET, OPTIONS` | `sessionId`, `accessId`, `statement`
[Assembly Query](rest_query.md#assembly-query) | `.../rest/assembly-query` | `GET, POST, PUT, OPTIONS` | `sessionId`, `accessId`, `body`
[Assembly Stream Query](rest_query.md#assembly-stream-query) | `.../rest/assembly-stream-query` | `POST, PUT, OPTIONS` | `sessionId`, `accessId`

> For examples of query calls, see [Legacy REST query examples](rest_query_examples.md).

### Encoding over HTTP
When using a REST call over HTTP, you must ensure the call whitespace and other special characters are encoded properly. Most browsers, for example, do this automatically for you, but this is not guaranteed.

### Passing Numbers
There is also a special format for passing numbers using REST calls. For example, most ID properties are of the type `long`, meaning that `id = 5` is interpreted as an integer with the value of `5`. This causes type exceptions in some cases.

There are three possible number values that can be passed using REST calls. A simple number with no character attached is read as `Integer`, a number with `L` at the end is interpreted as a `Long`, and a number with `D` at the end is interpreted as a `Double`, for example:
* `5` - interpreted as an `Integer` with the value of `5`
* `10L` - interpreted as a `Long` with the value of `10`
* `25D` - interpreted as a `Double` with the value of `25`

For more information on GMQL syntax, see the Query API in our API documentation.


### Query

The query call allows you to build a query and receive the results.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/query?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | The valid session that grants the call access to tribefire. | Yes
`accessId`  | The access through which the query should be executed. | Yes
`statement` | The GMQL statement. | Yes

#### Projections

Name    | Description
------- | -----------
`first-of-payload`  | 	Returns the first object that matches the query.
`envelope`  | Returns the query object responsible for the returning the results.
`payload`  | Returns all the objects that match the query. This is the default projection.

### Assembly Query 
The query assembly request allows you to execute queries by passing a serialized JSON object using the `body` parameter.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/assembly-query?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | The valid session that grants the call access to tribefire. | Yes
`accessId`  | The access through which the query should be executed. | Yes
`body` | The serialized JSON object that contains the query.  | Yes

#### Projections

Name    | Description
------- | -----------
`first-of-payload`  | 	Returns the first object that matches the query.
`envelope`  | Returns the query object responsible for the returning the results.
`payload`  | Returns all the objects that match the query. This is the default projection.

### Assembly Stream Query

The Query Assembly Request allows you to execute queries by passing a serialized JSON object using the body parameter.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/assembly-stream-query?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | The valid session that grants the call access to tribefire. | Yes
`accessId`  | The access through which the query should be executed. | Yes
`body` | The serialized JSON object that contains the query.  | Yes

> The actual JSON object that defines the entity or entities that should be created or edited should be defined in the body of the REST call.

#### Projections

Name    | Description
------- | -----------
`first-of-payload`  | 	Returns the first object that matches the query.
`envelope`  | Returns the query object responsible for the returning the results.
`payload`  | Returns all the objects that match the query. This is the default projection.
