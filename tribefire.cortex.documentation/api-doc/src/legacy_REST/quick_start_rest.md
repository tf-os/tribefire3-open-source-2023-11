# Legacy REST Sample Calls

Follow these instructions to quickly set up the environment for legacy REST development and develop your first app.

## Setting Up IDE

We provide a set of instructions which help you set up a default environment for developing with REST API.

### Required Components

Component    | Version  
------- | -----------
Oracle Java JDK  | [](asset://tribefire.cortex.documentation:includes-doc/java_jdk_version.md?INCLUDE) 
tribefire | 2.0 or higher  
Google Chrome   | any    
Postman | any

#### Oracle Java JDK

[](asset://tribefire.cortex.documentation:includes-doc/java_installation.md?INCLUDE)

#### tribefire

[](asset://tribefire.cortex.documentation:includes-doc/tribefire_quick_installation.md?INCLUDE)

## Developing a Simple App Using Legacy REST

The following example shows how to send simple REST calls useful when working with entity types or entity instances.


### Sending Simple Legacy REST Calls

To successfully send REST calls, you must first obtain a valid `sessionId`.

#### Obtaining a sessionId

1. Open Postman and send a `POST` request to the `http://localhost:8080/tribefire-services/rest/authenticate?user=cortex&password=cortex` address. This call is used for authentication and returns a session token based on the credentials provided.

2. Copy the `sessionId` from the response body and save it.

#### Reflecting a Model

As you know, every access has a metamodel assigned to it. It is the metamodel that provides the entities the access returns. To see what metamodel your access is using, perform the following steps:

1. In Postman, send a `GET` request to `http://localhost:8080/tribefire-services/rest/reflect-model?accessId=auth&sessionId=YOURSESSIONID`. This call returns a JSON representation of the metamodel.
> For the sake of simplicity, we are using the auth access in this tutorial.

2. Inspect the response body which should look as follows:

  ```json
    {
      "_type": "com.braintribe.model.meta.GmMetaModel",
      "_id": "0",
      "globalId": "model:com.braintribe.gm:user-model",
      "id": "model:com.braintribe.gm:user-model",
      "name": "com.braintribe.gm:user-model",
      "partition": "cortex",
      "version": "2.2.2"
    }
  ```

#### Returning a List of Users

Let's say you want to see all the available users.

1. In Postman, send a `GET` request to the `http://localhost:8080/tribefire-services/rest/fetch?accessId=auth&type=com.braintribe.model.user.User&sessionId=YOURSESSIONID` address. This results in returning all instances of the `com.braintribe.model.user.User` type associated to the **auth** access:

```json
[
 {
   "_type": "com.braintribe.model.user.User",
   "_id": "0",
   "firstName": "C.",
   "globalId": "78e72523-ff64-436e-8fed-1769fcd07f8a",
   "id": "6eff2613-82d1-426a-8b99-9c5ca06fdc85",
   "lastName": "Cortex",
   "name": "cortex",
   "partition": "auth",
   "password": "*****"
 },
 {
   "_type": "com.braintribe.model.user.User",
   "_id": "1",
   "firstName": "",
   "globalId": "5b29e469-4668-4ba7-8feb-5531c1c1a54d",
   "id": "66f5ac24-873d-412c-adaf-ee0f5793ba21",
   "lastName": "Locksmith",
   "name": "locksmith",
   "partition": "auth",
   "password": "*****"
 },
 {
   "_type": "com.braintribe.model.user.User",
   "_id": "2",
   "email": "mary.williams@braintribe.com",
   "firstName": "Mary",
   "globalId": "f56cad8a-830f-4116-98d2-ff81df7147e5",
   "id": "mary.williams",
   "lastName": "Williams",
   "name": "mary.williams",
   "partition": "auth",
   "password": "*****"
 },
 {
   "_type": "com.braintribe.model.user.User",
   "_id": "3",
   "email": "robert.taylor@braintribe.com",
   "firstName": "Robert",
   "globalId": "e9e1a139-cff7-43ec-8b6b-421bbe203bf1",
   "id": "robert.taylor",
   "lastName": "Taylor",
   "name": "robert.taylor",
   "partition": "auth",
   "password": "*****"
 },
 {
   "_type": "com.braintribe.model.user.User",
   "_id": "4",
   "email": "john.smith@braintribe.com",
   "firstName": "John",
   "globalId": "b158fbc3-5bf2-4dd0-8950-383f60d93e32",
   "id": "john.smith",
   "lastName": "Smith",
   "name": "john.smith",
   "partition": "auth",
   "password": "*****"
 },
 {
   "_type": "com.braintribe.model.user.User",
   "_id": "5",
   "email": "steven.brown@braintribe.com",
   "firstName": "Steven",
   "globalId": "3cfa9a3c-d487-4c55-8d8f-53012f45fb18",
   "id": "steven.brown",
   "lastName": "Brown",
   "name": "steven.brown",
   "partition": "auth",
   "password": "*****"
 }
]
```

> If you don't know what type your entity is, you can go find an instance of an entity in the tribefire Explorer and navigate to the General tab. They type is listed there.


#### Updating and Returning an Entity

1. In Postman, send a POST request to the `http://localhost:8080/tribefire-services/rest/update?accessId=auth&type=com.braintribe.model.user.User&id=john.smith&*lastName=Smithski&sessionId=YOURSESSIONID` address. This results in changing the value of the `lastName` attribute to the value provided for the user with the id **john.smith**.
> Using the asterisk \*, you specify which parameters are dynamic for a REST call.

2. Send a new GET request to the `http://localhost:8080/tribefire-services/rest/entity?accessId=auth&type=com.braintribe.model.user.User&id=john.smith&sessionId=YOURSESSIONID` address. This results in returning the JSON representation of the user with the id **john.smith**. Note that the value of the `lastName` attribute has changed:

```json
    {
      "_type": "com.braintribe.model.user.User",
      "_id": "0",
      "email": "john.smith@braintribe.com",
      "firstName": "John",
      "globalId": "b158fbc3-5bf2-4dd0-8950-383f60d93e32",
      "id": "john.smith",
      "lastName": "Smithski",
      "name": "john.smith",
      "partition": "auth",
      "password": "*****"
    }
```

> For information about available Legacy REST operations, see [Legacy Rest Introduction](rest_introduction.md)