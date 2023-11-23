# Legacy REST Resource Streaming

The streaming operations allow you to upload, download, and delete resources using HTTP requests.

[](asset://tribefire.cortex.documentation:includes-doc/rest_old_tip.md?INCLUDE)

## Available Calls

Name    | Syntax | Methods | Parameters   
------- | -----------
[Download](rest_resource_streaming.md#download) | `.../tribefire-services/streaming` | `GET` | `sessionId`, `accessId`, `resourceId`
[Upload](rest_resource_streaming.md#upload) | `.../tribefire-services/streaming` | `POST` | `sessionId`, `accessId`
[Delete](rest_resource_streaming.md#delete) | `.../tribefire-services/streaming` | `DELETE` | `sessionId`, `accessId`, `resourceId`



## General

`GET` is used to download a resource, `POST` is used to upload resources where the file or files are given in the message's body, and `DELETE` is used to delete the binary data.

There are two key parameters required regardless of the method used:

* `sessionId`
* `accessId`

When downloading a resource, you, naturally, must define the `id` of the resource, and when uploading resources using the body content type `application/x-www-form-urlencoded` the parameter `fileName` is also required. This determines the resource's name when stored in tribefire.

### Download

The `GET` method is used to download a resource.

#### URL Syntax

```
GET
<protocol>://<host>:<port>/tribefire-services/streaming?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The external ID of the access where the resource can be found. | Yes
`resourceId` | The ID of the resource that should be downloaded. This can be obtained by viewing the resource in Control Center or using a REST query to discover said resource. | Yes
`noCache`	| Defaults to false, meaning that caching is active. If provided with the value `true` Cache-Control headers are present in the response header to avoid caching. | No
`download` | Boolean value, if `true`, the response header Content-Type is set to `application/download` instead of the resource's mime type and the response header `Content-Disposition` is set to `attachment` | No
`fileName` | If provided the file name is used as the `filename` attribute for the `Content-Disposition` response header | No

#### Request Headers

Request Header    | Description | Example Value
------- | -----------
`If-None-Match`  | You can use this header to provide the fingerprint (checksum) of the data. If the provided resource fingerprint matches the one from the server-side resource, the resource is not retrieved. This comes in handy when you want to check if you have to latest version of the resource, for example. In case of REST calls, the checksum of the data is the value of the `ETag` response header you receive after the GET method call. | `04c6d7f07d3238c38047bb3b0f6de55b`
`If-Modified-Since`  | You can use this header to provide a last modification date in the RFC 1123 format. If the provided resource was not modified since the provided date, the data is not retrieved. | `Sun, 24 Sep 1989 06:00:00 GMT`

#### Example

This example assumes there is a resource with the `id` of `12771a6c-58eb-4391-adbe-57e19b455d3b`.

##### Basic Download

Call:

```
GET
/tribefire-services/streaming?accessId=cortex&resourceId=12771a6c-58eb-4391-adbe-57e19b455d3b&sessionId=yourSessionId
```

Calling the basic download command returns the actual resource.

##### Download

You can use the `download` parameter to configure the resource to be downloaded as an attachment. Setting download to `true` changes the value of the `Content-Disposition` header to `attachment`, where by default it is set to `inline`.
Call:

```
GET
/tribefire-services/streaming?accessId=cortex&resourceId=12771a6c-58eb-4391-adbe-57e19b455d3b&sessionId=yourSessionId&download=true
```

Returns:

```
Cache-Control →private, max-age=86400, must-revalidate
Content-Disposition →attachment;filename="yourAttachment'sFilename"
Content-Type →application/download
Date →Tue, 20 Jun 2017 07:14:05 GMT
ETag →04c6d7f07d3238c38047bb3b0f6de55b
Expires →Thu, 01 Jan 1970 01:00:00 CET
Last-Modified →Mon, 19 Jun 2017 15:40:09 GMT
Transfer-Encoding →chunked
X-Content-Type-Options →nosniff
X-Frame-Options →SAMEORIGIN
X-XSS-Protection →1; mode=block
```

##### No Cache

The parameter `noCache` allows you to define whether `Cache-Control` headers should be included in the response. By default this parameter is set to `false`. Setting this parameter to `true` disables caching.
Call:

```
GET
/tribefire-services/streaming?accessId=cortex&resourceId=12771a6c-58eb-4391-adbe-57e19b455d3b&sessionId=yourSessionId&noCache=true
```

Returns:

```
Cache-Control →
no-store, no-cache

Content-Type →
image/png

Date →
Thu, 12 Nov 2015 09:26:54 GMT

Expires →
Thu, 01 Jan 1970 00:00:00 GMT

Last-Modified →
Thu, 12 Nov 2015 09:26:54 GMT

Pragma →
no-cache

Server →
Apache-Coyote/1.1

Transfer-Encoding →
chunked
```

##### FileName

The parameter `fileName` when downloading a file is used to determine the value of the `Content-Disposition` header.
Call:

```
GET
tribefire-services/streaming?accessId=cortex&sessionId=yourSessionId&resourceId=1c183bff-2d58-4e4c-a971-cd4ceb5b2d34&fileName=somename
```

Returns:

```
Cache-Control →private, max-age=86400, must-revalidate
Content-Disposition →inline;filename="somename"
Content-Type →image/png
Date →Tue, 20 Jun 2017 07:30:56 GMT
ETag →04c6d7f07d3238c38047bb3b0f6de55b
Expires →Thu, 01 Jan 1970 01:00:00 CET
Last-Modified →Mon, 19 Jun 2017 15:40:09 GMT
Transfer-Encoding →chunked
X-Content-Type-Options →nosniff
X-Frame-Options →SAMEORIGIN
X-XSS-Protection →1; mode=block
```

### Upload

The `POST` method is used to upload a resource.

#### URL Syntax

```
POST
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/streaming?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The external ID of the access where the resource can be found. | Yes
`responseMimeType` | When uploading a new resource it is created in tribefire associated with a `Resource` entity instance. This new `Resource` instance is returned in the body of the response and is by default of the type `application/json`. You can determine the type that response should take by using this parameter.  | No
`sourceType`	| Selects the designated `BinaryPersistence` capable of handling it, based on the `UploadWith` metadata configuration. If you do not specify a source type, `BinaryPersistence` associated with the `ResourceSource` type is used. | No
`useCase` | Describes the use case of a given resource and helps with the resolution of the `UploadWith` metadata configuration which provides `BinaryPersistence` to be used for creating the resource. | No
`fileName` | Determines the name of the new `Resource` created in tribefire. This parameter is only required when the upload is performed with the `application/x-www-form-urlencoded` body content type. | No

#### Body Content Type

Name    | Description
------- | -----------
`multipart/form-data`  | Multiple files can be uploaded in a single request. The value of the name parameter for each file should be given as `content`.
`application/x-www-form-urlencoded`  | Files must be contained in the uniform `POST` body. Only one file can be uploaded per request. This is the default option.

#### Example

##### Simple Upload

The following shows how to upload a simple `XML`. It uses the `POST` method to upload the file as well as the three required parameters: `accessId`, `sessionId`, `fileName`. Make sure to put the XML content in the body of the call.
Call:

```xml
POST /tribefire-services/streaming?accessId=cortex&sessionId=yourSessionId&fileName=testFile
BODY:
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?gm-xml version="3"?><gm-data>
<required-types>
<type>com.braintribe.model.deployment.DeploymentTrace</type>
<type>com.braintribe.model.extensiondeployment.script.Javascript</type>
<type>com.braintribe.model.deployment.DeploymentState</type>
<type>com.braintribe.model.processdefinition.ScriptedTransitionProcessor</type>
<type>com.braintribe.model.generic.i18n.LocalizedString</type>
</required-types>
<root-value>
<entity ref="0"/>
</root-value>
<pool>
<entity id="1" type="com.braintribe.model.generic.i18n.LocalizedString">
<property name="id">
<long>60857</long>
</property>
<property name="localizedValues">
<map>
<entry>
<key>
<string>default</string>
</key>
<value>
<string>Confidential Processor</string>
</value>
</entry>
</map>
</property>
</entity>
<entity id="2" type="com.braintribe.model.extensiondeployment.script.Javascript">
<property name="id">
<long>385</long>
</property>
<property name="source">
<string>$.process.invoiceState = 'FinalizeConfidential';</string>
</property>
</entity>
<entity id="3" type="com.braintribe.model.deployment.DeploymentTrace">
<property name="date">
<date>2015-09-29T10:33:00.127+0200</date>
</property>
<property name="event">
<string>deployment.action.deploy</string>
</property>
<property name="id">
<long>393</long>
</property>
<property name="initiator">
<string>internal</string>
</property>
<property name="kind">
<null/>
</property>
<property name="message">
<string>Deployment action triggered. Action: deploy</string>
</property>
</entity>
</pool>
</gm-data>
```

Returns:

```
Access-Control-Allow-Credentials →true
Access-Control-Allow-Origin →chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop
Access-Control-Expose-Headers →gm-rpc-body, gm-rpc-body-type, gm-rpc-client-id, gm-rpc-response-key, gm-rpc-response-key-algo
Cache-Control →no-store, no-cache
Content-Type →application/json
Date →Tue, 20 Jun 2017 07:40:13 GMT
Expires →Thu, 01 Jan 1970 00:00:00 GMT
Pragma →no-cache
Transfer-Encoding →chunked
Vary →Origin
X-Content-Type-Options →nosniff
X-Frame-Options →SAMEORIGIN
X-XSS-Protection →1; mode=block
```
Once the file is uploaded a new `Resource` entity instance is created. This instance is the returned as part of the response body:
```json
[
    {
        "_type": "com.braintribe.model.resource.Resource",
        "_id": "0",
        "_partial": "",
        "created": {
            "value": "2017-06-20T09:40:12.620+0200",
            "_type": "date"
        },
        "creator": "cortex",
        "fileSize": {
            "value": "1633",
            "_type": "long"
        },
        "globalId": "c0515114-a8da-44b2-9164-24950869b1f7",
        "id": "c0515114-a8da-44b2-9164-24950869b1f7",
        "md5": "bde75c6ef5847d9e1644632a41e35587",
        "mimeType": "application/xml",
        "name": "somename",
        "partition": "cortex",
        "resourceSource": {
            "_type": "com.braintribe.model.resource.source.FileSystemSource",
            "_id": "1",
            "globalId": "38813c99-8b0e-49b1-b062-0538bf9edbc1",
            "id": "217d0604-7bb7-4706-89d2-50286e59cc6c",
            "partition": "cortex",
            "path": "1706/2009/4012/927bab62-d232-4c93-9d75-c07bc6cd7c9c"
        }
    }
]
```

#### Response Mime Type

Once a file or files are uploaded to tribefire, a new `Resource` entity instance is created to represent these files. The created instance(s) are then returned as part of the response body. You can use the parameter `responseMimeType` to determine what Mime type the instance(s) should be returned as. If you do not add this parameter the default value `application/json` is used. Once the file is uploaded a new `Resource` entity instance is created. This instance is the returned as part of the response body.

##### Multi Upload

It is also possible to upload multiple resources to tribefire by defining the body content type as `multipart/form-data`, which is in contrast to the default value `application/x-www-urlencoded`. The files that are then attached to the request body have a key-value relationship where the value is the file itself while the key for each file being uploaded must be defined with the value `content`.

> If `content` is not given as the value of the key then no files are uploaded. Instead tribefire returns a `200 OK` code with an empty response body. This means that the request was successful from a technical standpoint but no new resources have been created in tribefire.

Once the files have been uploaded a new `Resource` entity instance is created for each file. These instances are returned as part of the response body:

```json
[
    {
        "_type": "com.braintribe.model.resource.Resource",
        "_id": "0",
        "_partial": "",
        "created": {
            "value": "2017-06-20T09:52:20.027+0200",
            "_type": "date"
        },
        "creator": "cortex",
        "fileSize": {
            "value": "17706",
            "_type": "long"
        },
        "globalId": "5a8b79c7-3559-4b53-8741-7e113e5faa81",
        "id": "5a8b79c7-3559-4b53-8741-7e113e5faa81",
        "md5": "c0ce4945296ea3f8373ce046cbdd7437",
        "mimeType": "image/png",
        "name": "bug1.png",
        "partition": "cortex",
        "resourceSource": {
            "_type": "com.braintribe.model.resource.source.FileSystemSource",
            "_id": "1",
            "globalId": "271abe12-d024-4446-ac60-e452df78758f",
            "id": "3d167c6f-2a4f-45e1-84fc-784c44b4ef11",
            "partition": "cortex",
            "path": "1706/2009/5220/46cf14b3-9882-454e-9334-4abcbcceab3f"
        }
    },
    {
        "_type": "com.braintribe.model.resource.Resource",
        "_id": "2",
        "_partial": "",
        "created": {
            "value": "2017-06-20T09:52:20.250+0200",
            "_type": "date"
        },
        "creator": "cortex",
        "fileSize": {
            "value": "10759",
            "_type": "long"
        },
        "globalId": "9f85aa80-3999-4848-90e1-afd43d508b31",
        "id": "9f85aa80-3999-4848-90e1-afd43d508b31",
        "md5": "0f3075c4c2c054a25bd4e3bacaa06acb",
        "mimeType": "image/png",
        "name": "test1.png",
        "partition": "cortex",
        "resourceSource": {
            "_type": "com.braintribe.model.resource.source.FileSystemSource",
            "_id": "3",
            "globalId": "6df015f8-5527-431f-a9be-6058e48f9b0c",
            "id": "bc1174cd-ea35-4ea2-a48d-90fc2c9a9ce2",
            "partition": "cortex",
            "path": "1706/2009/5220/d2892f51-e4ee-4486-adff-9e3f9ea955c5"
        }
    }
]
```

### Delete
The `DELETE` method is used to delete a resource.

#### URL Syntax

```
DELETE
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/streaming?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The external ID of the access where the resource can be found. | Yes
`resourceId` | The ID of the resource that should be deleted. This can be obtained by viewing the resource in Control Center or using a REST query to discover said resource. | Yes

#### Example

Once a file is uploaded to tribefire, you can delete it using a REST call. Let's say you have a file with the `resourceId` of `1dd5fff9-95a4-478d-bbfb-8e1b51713da3`.

In your REST client, you send a DELETE call to the following URL:

```
tribefire-services/streaming?accessId=cortex&sessionId=yourSessionId&resourceId=1dd5fff9-95a4-478d-bbfb-8e1b51713da3
```

Sending this call results in deleting the binary data of the resource. You know that the request has been successful when you the server responds with  `200 OK`.
