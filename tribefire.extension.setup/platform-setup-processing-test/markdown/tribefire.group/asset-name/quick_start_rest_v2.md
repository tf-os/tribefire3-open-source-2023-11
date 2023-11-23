---
title: Quick Start with REST v2
# if you add a new tag, make sure to add it to tags.yml and create a new page in pages/tags
tags: [REST_v2]
# use datatable: true for tables with lots of data
datatable: false
last_updated: 10.04.2018
summary: "Follow these instructions to quickly set up the environment for REST development and try out the API."
sidebar: quickstart
layout: page
permalink: quick_start_rest_v2.html
# hide_sidebar: true
# toc: false
# hide_feedback: true
# exclude_from_search: true
# example link: [alias](permalink.html)
# {% include filename.html content=optionalContent,DependsOnAFile %}
# glossary tooltip: <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>
---

## Setting Up IDE
We provide a set of instructions which help you set up a default environment for developing with REST API.

### Required Components

<div class="datatable-begin"></div>

Component    | Version  
------- | -----------
Oracle Java JDK  | 1.8.x   
tribefire | 2.0 or higher  
Google Chrome   | any    
Postman | any

<div class="datatable-end"></div>


#### Oracle Java JDK
{% include /quick_start/java_installation.md %}

#### tribefire
{% include /quick_start/tribefire_quick_installation.md %}

## Working with Entities and Properties
The following example shows how to send simple REST calls useful when working with <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_instance}}">entity instances</a> and service requests.

{% include tip.html content="For general information about REST v2, see [REST v2 Introduction](rest_v2_introduction.html)."%}


### Obtaining a `sessionId`

It is advisable to use the following headers for all REST calls:

Header | Value
---- | -----
Accept | `application/json`
Content-Type | `application/json`


1. Open Postman and send a `POST` request to the `POST https://localhost:8080/tribefire-services/rest/v1/authenticate?user=cortex&password=cortex` address. This call is used for authentication and returns a session token based on the credentials provided.

2. Copy the `sessionId` from the response body and save it.

### Returning a List of User for an Access
The Authentication and Authorization access provides users available in tribefire. We will now use a REST v2 call to get a list off all users associated with that access.

1. In Postman, send a `GET` request to `https://localhost:8080/tribefire-services/rest/v2/entities/auth/com.braintribe.model.user.User?sessionId=sessionId`. This call returns a JSON representation of all available users.
   {% include note.html content="You might have noticed the **auth** part in the URL. That is the `globalId` of the access you want to query." %}
   {% include tip.html content="For more information about CRUD operations of entities, see [REST v2 /rest/v2/entities](rest_v2_rest_v2_entities.html)."%}


2. Inspect the response body which should look as follows:
```json
[
{
	"_type": "com.braintribe.model.user.User",
	"_id": "0",
	"firstName": "C.",
	"globalId": "a2453b3c-3133-4b09-9b3d-b825efcce0cb",
	"id": "3ae8e20e-6c9d-421f-904b-51b9988340d5",
	"lastName": "Cortex",
	"name": "cortex",
	"partition": "auth",
	"password": "*****",
	"roles": {
		"_type": "set",
		"value": [
			{
				"_type": "com.braintribe.model.user.Role",
				"_id": "1",
				"description": {
					"_type": "com.braintribe.model.generic.i18n.LocalizedString",
					"_id": "2",
					"globalId": "d36642d9-b9dd-4879-9601-03d3b80b98e4",
					"id": "d36642d9-b9dd-4879-9601-03d3b80b98e4",
					"localizedValues": {
						"_type": "map",
						"value": [
							{
								"key": "default",
								"value": "admin role that can be used to configure the system."
							}
						]
					},
					"partition": "auth"
				},
				"globalId": "5d39ffbb-de26-4899-bfde-6b14ef13da23",
				"id": "86de0470-2370-4be6-83a1-c23db00587e4",
				"localizedName": {
					"_type": "com.braintribe.model.generic.i18n.LocalizedString",
					"_id": "3",
					"globalId": "22f81c16-0ba8-4bfa-892e-c32006a0a609",
					"id": "22f81c16-0ba8-4bfa-892e-c32006a0a609",
					"localizedValues": {
						"_type": "map",
						"value": [
							{
								"key": "default",
								"value": "tribefire Admin Role"
							}
						]
					},
					"partition": "auth"
				},
				"name": "tf-admin",
				"partition": "auth"
			}
		]
	}
},
{
	"_type": "com.braintribe.model.user.User",
	"_id": "4",
	"firstName": "",
	"globalId": "a0783b0f-d449-4eea-a6a4-fe7b1a45022d",
	"id": "7aee493c-2dd7-4291-b9b0-b3577b631514",
	"lastName": "Locksmith",
	"name": "locksmith",
	"partition": "auth",
	"password": "*****",
	"roles": {
		"_type": "set",
		"value": [
			{
				"_type": "com.braintribe.model.user.Role",
				"_id": "5",
				"description": {
					"_type": "com.braintribe.model.generic.i18n.LocalizedString",
					"_id": "6",
					"globalId": "a7380bc5-3ef4-48d1-804f-f6c65f1dae48",
					"id": "a7380bc5-3ef4-48d1-804f-f6c65f1dae48",
					"localizedValues": {
						"_type": "map",
						"value": [
							{
								"key": "default",
								"value": "role having various security settings disabled in order to repair broken configuration."
							}
						]
					},
					"partition": "auth"
				},
				"globalId": "02a49e7a-45dd-4a0a-bc88-6cac190191ec",
				"id": "c927b448-5958-48b4-9b9b-ffcb8289988b",
				"localizedName": {
					"_type": "com.braintribe.model.generic.i18n.LocalizedString",
					"_id": "7",
					"globalId": "b28d61a7-3ae4-427e-a36d-10dd11b90b8e",
					"id": "b28d61a7-3ae4-427e-a36d-10dd11b90b8e",
					"localizedValues": {
						"_type": "map",
						"value": [
							{
								"key": "default",
								"value": "tribefire Locksmith Role"
							}
						]
					},
					"partition": "auth"
				},
				"name": "tf-locksmith",
				"partition": "auth"
			}
		]
	}
}
]
```


### Adding a New User
Let's say you want to add a new user to the `auth` access.

1. In Postman, send a `POST` request to the `https://localhost:8080/tribefire-services//tribefire-services/rest/v2/entities/auth/com.braintribe.model.user.User?sessionId=sessionId` address. Add the following snippet as the body:
  ```json
    {
	    "name": "Larry"
    }
  ```
2. This call returns an `id` for the newly created entity:
  ```json
  "43b052c4-d751-45fa-8253-a59ea623eef6"
  ```


### Adding a Complex Property
When you returned the list of users earlier, you might have noticed two collection properties each `User` has: `groups` and `roles`. Let's see what groups and roles our new user Larry has.

1.  In Postman, send a GET request to the `https://localhost:8080/tribefire-services/rest/v2/entities/auth/com.braintribe.model.user.User/43b052c4-d751-45fa-8253-a59ea623eef6?sessionId=sessionId` address. This returns the following:
  ```json
  {
      `"_type": "com.braintribe.model.user.User",
      "_id": "0",
      "globalId": "43b052c4-d751-45fa-8253-a59ea623eef6",
      "id": "43b052c4-d751-45fa-8253-a59ea623eef6",
      "name": "Larry",
      "partition": "auth",
      "password": "*****"`
  }
  ```
  You might have noticed there are no `groups` and `roles` in the returned JSON. That is because those properties are empty and empty properties are not returned by default. It's time to see all the properties Larry has.

2. In Postman, send a GET request to the `https://localhost:8080/tribefire-services/rest/v2/entities/auth/com.braintribe.model.user.User/43b052c4-d751-45fa-8253-a59ea623eef6?sessionId=sessionId&writeEmptyProperties=true` address. This call returns the following:
  ```json
  {
      "_type": "com.braintribe.model.user.User",
      "_id": "0",
      "description": null,
      "email": null,
      "firstName": null,
      "globalId": "43b052c4-d751-45fa-8253-a59ea623eef6",
      "groups": {
          "_type": "set",
          "value": []
      },
      "id": "43b052c4-d751-45fa-8253-a59ea623eef6",
      "lastLogin": null,
      "lastName": null,
      "name": "Larry",
      "partition": "auth",
      "password": "*****",
      "picture": null,
      "roles": {
          "_type": "set",
          "value": []
      }
  }
```
You can now see all properties, even the ones that are null. You will now add a set of `Role` objects to the `roles` property.

3. In Postman, send a POST request to the `https://localhost:8080/tribefire-services/rest/v2/properties/auth/com.braintribe.model.user.User/43b052c4-d751-45fa-8253-a59ea623eef6/roles?sessionId=sessionId` address. Add the following to the body and execute the call:
  ```json
{
            "_type": "set",
            "value": [
                {
                    "_type": "com.braintribe.model.user.Role",
                    "_id": "3",
                    "description": {
                        "_type": "com.braintribe.model.generic.i18n.LocalizedString",
                        "_id": "4",
                        "globalId": "d36642d9-b9dd-4879-9601-03d3b80b98e4",
                        "id": "d36642d9-b9dd-4879-9601-03d3b80b98e4",
                        "localizedValues": {
                            "_type": "map",
                            "value": [
                                {
                                    "key": "default",
                                    "value": "admin role that can be used to configure the system."
                                }
                            ]
                        },
                        "partition": "auth"
                    },
                    "globalId": "5d39ffbb-de26-4899-bfde-6b14ef13da23",
                    "id": "86de0470-2370-4be6-83a1-c23db00587e4",
                    "localizedName": {
                        "_type": "com.braintribe.model.generic.i18n.LocalizedString",
                        "_id": "5",
                        "globalId": "22f81c16-0ba8-4bfa-892e-c32006a0a609",
                        "id": "22f81c16-0ba8-4bfa-892e-c32006a0a609",
                        "localizedValues": {
                            "_type": "map",
                            "value": [
                                {
                                    "key": "default",
                                    "value": "tribefire Admin Role"
                                }
                            ]
                        },
                        "partition": "auth"
                    },
                    "name": "tf-admin",
                    "partition": "auth"
                }
            ]
        }
  ```
  This call returns `true` which means the property `roles` has been updated. For more information about CRUD operations on properties, see [REST v2 /rest/v2/properties](rest_v2_rest_v2_properties.html)

4. In Postman, send a GET request to the `https://localhost:8080/tribefire-services/rest/v2/entities/auth/com.braintribe.model.user.User/43b052c4-d751-45fa-8253-a59ea623eef6?sessionId=sessionId` address to inspect Larry and his newly updated `roles` property. Executing the call returns the following:
  ```json
{
    "_type": "com.braintribe.model.user.User",
    "_id": "0",
    "globalId": "43b052c4-d751-45fa-8253-a59ea623eef6",
    "id": "43b052c4-d751-45fa-8253-a59ea623eef6",
    "name": "Larry",
    "partition": "auth",
    "password": "*****",
    "roles": {
        "_type": "set",
        "value": [
            {
                "_type": "com.braintribe.model.user.Role",
                "_id": "1",
                "description": {
                    "_type": "com.braintribe.model.generic.i18n.LocalizedString",
                    "_id": "2",
                    "globalId": "d36642d9-b9dd-4879-9601-03d3b80b98e4",
                    "id": "d36642d9-b9dd-4879-9601-03d3b80b98e4",
                    "localizedValues": {
                        "_type": "map",
                        "value": [
                            {
                                "key": "default",
                                "value": "admin role that can be used to configure the system."
                            }
                        ]
                    },
                    "partition": "auth"
                },
                "globalId": "5d39ffbb-de26-4899-bfde-6b14ef13da23",
                "id": "86de0470-2370-4be6-83a1-c23db00587e4",
                "localizedName": {
                    "_type": "com.braintribe.model.generic.i18n.LocalizedString",
                    "_id": "3",
                    "globalId": "22f81c16-0ba8-4bfa-892e-c32006a0a609",
                    "id": "22f81c16-0ba8-4bfa-892e-c32006a0a609",
                    "localizedValues": {
                        "_type": "map",
                        "value": [
                            {
                                "key": "default",
                                "value": "tribefire Admin Role"
                            }
                        ]
                    },
                    "partition": "auth"
                },
                "name": "tf-admin",
                "partition": "auth"
            }
        ]
    }
}
  ```

## Working with Service Requests
You will now execute a simple service request that returns the user your current authentication session is based on.

1. In Postman, send a GET request to the `https://localhost:8080/tribefire-services/api/v1/com.braintribe.model.securityservice.GetCurrentUser?sessionId=sessionId` address to inspect the current session. Executing the call returns the following:
  ```json
  {
    "_type": "com.braintribe.model.user.User",
    "_id": "0",
    "firstName": "C.",
    "globalId": "e2ac2455-133f-4f94-8fd9-e35a076061f4",
    "id": "652c755b-578d-417b-899a-3ed85a5e1750",
    "lastName": "Cortex",
    "name": "cortex",
    "partition": "user-sessions"
  }
  ```
{% include tip.html content="For more information about service request calls, see [REST v2 /api/v1](rest_v2_api_v1.html)."%}
