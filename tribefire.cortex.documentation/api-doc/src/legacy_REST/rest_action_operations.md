# Legacy REST Action Operations

Action calls allow you to execute actions saved in the Workbench using a REST call.

[](asset://tribefire.cortex.documentation:includes-doc/rest_old_tip.md?INCLUDE)

## Available Calls

Name    | Syntax | Methods | Parameters   
------- | -----------
[Workbench Action](rest_action_operations.md#workbench-action) | `.../rest/action` | `GET, POST, OPTIONS` | `sessionId`, `accessId`, `name`

### Workbench Action

This REST call allows you to execute actions saved in a workbench using a REST call. This done by using the name of the folder which has the required action attached to it.

This call can trigger the following actions:

* RPC Actions
* `SimpleQueryAction`
* `PrototypeQueryAction`
* `TemplateQueryAction`
* `SimpleInstantiationAction`
* `TemplateInstantiationAction`

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/action?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The access through which the REST can gain access to the needed workbench access. | Yes
`name`  | The name of the folder, as defined by the folder's name property, to which the folder content belongs. The folder content is the action which provides the information returned. | Yes
`*Variable_Name`  | The name of a variable, if required by the action. To use this parameter enter the name of the variable with an asterisk in front and then its value: `&*VariableName=VariableValue`. In the case of a workbench action operation, this parameter could be required to define the value variables in a Template Query Action, or to provide new values for an entity instantiated using either the Simple Query Action or Template Instantiation Action, for example. | No

#### Projections

##### RPC Actions

Name    | Description
------- | -----------
`envelope`  | Returns a `RpcResponse `
`payload`  | Returns the `ReturnValue` property of the `RpcResponse` object

##### Query Actions

Name    | Description
------- | -----------
`envelope`  | Returns the object responsible for handling the query results.
`payload`  | (Default) Returns the entities that are matched by the simple query action.
`first-of-payload` | Returns the first element of the payload projection.

##### Instantiation Actions

Name    | Description
------- | -----------
`envelope`  | Returns the manipulation response that is responsible for the instantiation of the new entity.
`payload`  | Returns the `ChangeValueManipulation` that is responsible for creating the new entity.
`id` | Returns the `id` property of the newly instantiated entity.
`entity` | Returns the newly instantiated entity.

#### Example

In this example we are using a folder called `persons` which has a Simple Query Action assigned as its content. This executes a simple query based on the entity `Person` in the Demo Access.
There are three required properties for this call: `sessionId`, `accessId` and `name`.

Call:

```
POST
http://localhost:8080/tribefire-services/rest/action?sessionId=yourSessionId&accessId=access.demo&name=persons
```

Response:

```json
[
    {
        "_type": "tribefire.demo.model.data.Person",
        "_id": "0",
        "firstName": "James",
        "gender": {
            "value": "male",
            "_type": "tribefire.demo.model.data.Gender"
        },
        "id": {
            "value": "36",
            "_type": "long"
        },
        "lastName": "Doe",
        "partition": "access.demo",
        "ssn": "444"
    },
    {
        "_type": "tribefire.demo.model.data.Person",
        "_id": "1",
        "firstName": "Sue",
        "gender": {
            "value": "female",
            "_type": "tribefire.demo.model.data.Gender"
        },
        "id": {
            "value": "31",
            "_type": "long"
        },
        "lastName": "Doe",
        "partition": "access.demo",
        "ssn": "333"
    },
    {
        "_type": "tribefire.demo.model.data.Person",
        "_id": "2",
        "anything": 1,
        "firstName": "John",
        "gender": {
            "value": "male",
            "_type": "tribefire.demo.model.data.Gender"
        },
        "id": {
            "value": "21",
            "_type": "long"
        },
        "lastName": "Doe",
        "partition": "access.demo",
        "ssn": "111"
    },
    {
        "_type": "tribefire.demo.model.data.Person",
        "_id": "3",
        "firstName": "J.J.",
        "gender": {
            "value": "male",
            "_type": "tribefire.demo.model.data.Gender"
        },
        "id": {
            "value": "41",
            "_type": "long"
        },
        "lastName": "Doe",
        "partition": "access.demo",
        "ssn": "555"
    },
    {
        "_type": "tribefire.demo.model.data.Person",
        "_id": "4",
        "firstName": "Mary",
        "gender": {
            "value": "female",
            "_type": "tribefire.demo.model.data.Gender"
        },
        "id": {
            "value": "46",
            "_type": "long"
        },
        "lastName": "Doe",
        "partition": "access.demo",
        "ssn": "666"
    },
    {
        "_type": "tribefire.demo.model.data.Person",
        "_id": "5",
        "anything": "MyTest",
        "firstName": "Jane",
        "gender": {
            "value": "female",
            "_type": "tribefire.demo.model.data.Gender"
        },
        "id": {
            "value": "26",
            "_type": "long"
        },
        "lastName": "Doe",
        "partition": "access.demo",
        "ssn": "222"
    }
]
```

> If you don't know what is the name of a particular action, switch to the workbench access and search for the name of the folder as its displayed in the access.
