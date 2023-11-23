# tribefire.extension.messaging

## Building

Run `./tb.sh .` or `sh tb.sh .` in the `tribefire.extension.messaging` group.

## Setup

Run the following jinni command to set up a messaging server:

`./jinni.sh setup-local-tomcat-platform setupDependency=tribefire.extension.messagingx:messaging-setup#1.0 installationPath=<Your Path> --deletePackageBaseDir true --debugProject tribefire.extension.messaging:messaging-debug : options --verbose`

## Demo Properties

Configure your demo properties. Add the following properties to the `{yourTomcatPath}/conf/tribefire.properties`

For demo pulsar:

```
MESSAGING_DEMO_CONNECTOR=PULSAR
MESSAGING_DEMO_MODE_ACTIVE=true
MESSAGING_DEMO_CONNECTOR_URL=pulsar://localhost:6650
MESSAGING_DEMO_CONNECTOR_TOPIC=DemoPulsarTopic
```

For demo kafka:

```
MESSAGING_DEMO_CONNECTOR=KAFKA
MESSAGING_DEMO_MODE_ACTIVE=true
MESSAGING_DEMO_CONNECTOR_URL=localhost:29092
MESSAGING_DEMO_CONNECTOR_TOPIC=DemoKafkaTopic
```

For demo logging:

```
MESSAGING_DEMO_CONNECTOR=LOGGING
MESSAGING_DEMO_MODE_ACTIVE=true
```

# Start with kafka

Run `docker-compose up -d`, then `nc -z localhost 22181` and `nc -z localhost 29092`

Or go to `docker/kafka/docker-compose.yml` and run it.

## Connect via UI
Use the `Kafka Tool GUI utility` (https://www.kafkatool.com/) to establish a connection with Kafka serve.

## Connect via command line
- download kafka - https://kafka.apache.org/downloads
- produce: `echo "mytest" | kafka-console-producer.sh --topic test --bootstrap-server=localhost:29092`
- consume: `kafka-console-consumer.sh --topic test --bootstrap-server=localhost:29092`

# Start with pulsar

Run `docker run -it -p 6650:6650  -p 8080:8080 --mount source=pulsardata,target=/pulsar/data --mount source=pulsarconf,target=/pulsar/conf apachepulsar/pulsar:2.9.1 bin/pulsar standalone`

# Setting up a demo environment
Using the environment variable `MESSAGING_CREATE_DEFAULT_SETUP` a simple demo environment get setup. This contains so far the `MessagingProcessor` and the `MessagingAspect` - this means it works right now for producing messages. With the `ProduceDemoMessage` request we have a configured service which then gets intercepted by the `MessagingAspect`. It is also right now hard coded to use Kafka with URL `localhost:29092`. It is also only intercepting the `ProduceDemoMessage` and nothing more.

Or go to `docker/pulsar/docker-compose.yml` and run it.
# Architecture
## General Producer Component overview

![/](resources/Producer%20Architecture.png)

As seen from above schema - producing set consists of 2 mandatory deployables, an abstract producer connector with one or several instances of particular connector implementations.
To set one up a MessagingTemplateContext has to be configured and passed over to `MessagingTemplateSpace.setupMessaging(MessagingContext context)` method.
### MessagingAspect
Is designed to be deployed in single instance per context to avoid possibility of sending several messages to same destination and reduce overall system load.
MessagingAspect incorporates following variables:
1. MessagingProcessor
2. Cache containing ProducerEventRules
3. Cache containing MessagingTypeSignatureMd metadata
4. Cache containing MessagingPropertyMd metadata

During the MessagingAspect configuration a global interception EntityType is configured, so all requests extending this type would be intercepted by MessagingAspect and an application of ProducerEventRules is made on the request. 
The MessagingProcessor is set during deployment process of MessagingAspect as it is also a deployable and is persistent through all lifecycle of MessagingAspect.
All Cache components are subjects to update from DB in case if Caches are empty or `flushCaches()` method is triggered on MessagingAspect.

MessagingAspect takes care of filtering incoming requests and applying ProducerEventRules to them if applicable. It forms a message appropriate to ProducerEventRules matched and passes it on to MessagingProcessor.

### MessagingProcessor
Is designed to be deployed in single instance per context to avoid possibility of sending several messages to same destination and reduce overall system load.
MessagingProcessor incorporates following variables:
1. Cache containing ProducerMessagingConnectors

During the initiation of the current deployable request/service metadata is registered to bind MessagingRequest to particular instance of MessagingProcessor.
Cache is bound to lifecycle of the MessagingProcessor and poses `flushProducers()` service method, so it can be called from other services/experts which is also called from MessagingAspect in `flushCaches` method.
MessagingProcessor incorporates logics for maintaining producer connectors cache and closing the ones that are not being used for more than 10 minutes. It also incorporates logics to create and add producer connectors to cache that are required for message delivery, so if some producer connectors are missing to deliver particular message, they are to be created and added to the cache.
MessagingProcessor also includes logic to pick corresponding connector to deliver the message to particular mq and topic.

### AbstractProducerMessagingConnector
It contains all the common logic for all ProducerMessagingConnectors which includes message marshalling and attached resource persistence.

### Actual Producer Implementations
Include the connection/disconnection/destruction/message delivery implementations for particular type of mq.

## General Consumer Component overview

![/](resources/Consumer%20Architecture.png)

As seen from above schema - consuming set consists of 2 mandatory deployables, an abstract consumer connector with one or several instances of particular connector implementations.
To set one up a MessagingTemplateContext has to be configured and passed over to `MessagingTemplateSpace.setupMessaging(MessagingContext context)` method.
### MessagingWorker
Is designed to be deployed in single instance per context to reduce overall system load.
MessagingWorker incorporates following variables:
1. Cache containing ConsumerEventRules
2. Cache containing ConsumerMessagingConnectors

MessagingWorker is performing querying of preconfigured mqs in an infinite loop. MQs connection configuration is done with ConsumerEventRules and can be changed in any moment of Worker lifecycle.
All Cache components are subjects to update from DB in case if Caches are empty or `flushCaches()` method is triggered on MessagingWorker.

MessagingWorker takes care of retrieving of messages from mqs, collecting corresponding resources and passing them on to corresponding post-processor for further evaluation.

### PostProcessor
Is a custom Processor (`implements ServiceProcessor<ProcessConsumedMessage, ProcessConsumedMessageResult>`). Should be introduced(developed) by consuming party to further process the consumed messages.
Can contain whichever logics satisfying the needs of the customer. The only limitation is Processing request should extend `ProcessConsumedMessage` interface and the response should extend `ProcessConsumedMessageResult` accordingly.
see TestReceiveMessagingProcessor.java as example.

### AbstractConsumerMessagingConnector
It contains all the common logic for all ConsumerMessagingConnectors. (At the moment only unmarshalling is included).

### Actual Consumer Implementations
Include the connection/disconnection/destruction/message consumption implementations for particular type of mq.

# Event Configuration:
## Types of configuration and main differences:
### Producer configuration -> ProducerEventRule:
`ProducerEventRule` is an abstraction for 2 subtypes `ProducerStandardEventRule` and `ProducerDiffEventRule`. Both of which are used to state the settings for message emission through corresponding `ProducerConnectors` to the queue.
Both types of rules share some properties:
```
requestTypeCondition -> `TypeCondition` that represents the particular requestType to which this rule is bound to (eg: `TypeCondition` is set to `RenameEntry` type, than all other request types would be considered inapplicable for this rule)
requestPropertyCondition -> `PropertyCondition` that represents condition that certain request property matches/not matches some sort of condition (eg: `RenameEntry` property `name` equals `AAAA`, the rule would be applicable only to a request where property `name` matches `AAAA`)
interceptionTarget -> `InterceptionTarget` is a property describing what should be done when rule is applied. Has values: REQUEST,RESPONSE,BOTH,DIFF. (eg: REQUEST/RESPONSE/BOTH set in this field will indicate that request/response/request&response should be added to the message values. DIFF is only set for `ProducerDiffEventRule`)
fieldsToInclude -> a List of TypesProperties has different application for Standard and Diff EventRules (see below)
filePersistenceStrategy -> ResourceBinaryPersistence containing values: NONE,TRANSIENT,ALL. Manages file persistance behaviour if some files are added to request. 
```
#### ProducerStandardEventRule
This type of producer rule is used when you want to send request, response or both in your message values.
property description:
```
fieldsToInclude -> List of TypesProperties pointing to properties to be extracted from request/response/both to be added to the message values(eg: TestGetObjectRequest -> TestGetObjectRequest.relatedObjId in conjuction with property interceptionTarget: REQUEST would mean that we want to send the id extracted from request in the message)
```

#### ProducerDiffEventRule
This type of rule is used when you need to calculate a diff of objects states, before and after the execution of the service request you've intercepted.
property description:
```
diffType -> DiffType states the type of diff that should be performed on the objects states. Has values: ALL,CHANGES_ONLY,UN_CHANGED_ONLY. (eg: Setting this value to CHANGES_ONLY would return a ComparisonResult containing only properties that have changed in between the states of the object)
diffLoader -> DiffLoader states the method used to obtain the objects states. Has values: QUERY,SERVICE.(eg: if QUERY is set in this property MessagingAspect would use EntityQuery to obtain the states of the respective object)
listedPropertiesOnly -> Boolean value describing if only the properties from the 'fieldsToInclude' should be scanned for diff". (eg: when set to TRUE only the properties from 'fieldsToInclude' would be checked for diff, all the other would be completely ignored, though would not apper in the ComparisonResult, and only with properties would affect the general object matching state)
addEntries -> AddEntries value defines which complex(GenericEntity based) values are to be added to Diffs, is used to reduce message size and avoind unnesessary dupes of same complex values. Has values: NEW,OLD,BOTH,NONE. (eg: setting this value to OLD will only add 'before processing' state of the value to the Diff)
extractionPropertyPaths -> Set of TypesProperty defining properties to be extracted from the loaded objects for comparison. Is useful in case of SERVICE loading of objects where your result is some response wrapper around the object of interest(more description and setting see below).
fieldsToInclude -> List of TypesProperties pointing to the properties of interest for the comparison. When the comparison is carried out the properties from this list would be added to the expectedDiffs list in ComparisonResult, and expectedValuesDiffer property of ComparisonResult would be filled depending on these diffs, all the rest of Diffs would be added to unexpectedDiffs and in conjuction with expectedDiffs would affect the value of valuesDiffer in ComparisonResult.
```

##### Setup required for Diff calculation:
In order to properly setup your Messaging Producer to perform Diff calculations on objects in DB you need to register several MetaData:
1. MessagingTypeSignatureMd - this metadata is required to inform MessagingAspect where to get object ids from (REQUEST/RESPONSE). It is bound to particular ServiceRequest entityType. eg:

```
    MessagingTypeSignatureMd tsMd = cortexSession.create(MessagingTypeSignatureMd.T);
    tsMd.setIdObjectType(RelatedObjectType.REQUEST);
    
    serviceModelEditor.onEntityType(TestUpdateRequest.T).addMetaData(tsMd);
```
Here TestUpdateRequest represents service request which is intercepted by MessagingAspect and thanks to md is informed that id object id should be extracted from the request itself.
##### Why we need that information: 
Due to different types of object manipulations system is not aware of the operation done by the request `CRUD`, on Create operation usually there are no ids in the request, and they are only present in response, whereas fo all other types of manipulations in most cases you have ids in the request.

2. MessagingPropertyMd - this metadata is required to point Messaging Aspect of the Property containing the id in the service request/response, so it can further be extracted and used in a service/query to obtain corresponding entity and type of entity to be loaded or service request type necessary to  'get' the entity. eg:
```
    MessagingPropertyMd ppMd = cortexSession.create(MessagingPropertyMd.T);
    ppMd.setLoadedObjectType(TestObject.T.getTypeSignature());
    ppMd.setGetterEntityType(TestGetObjectRequest.T.getTypeSignature());
	
    serviceModelEditor.onEntityType(TestUpdateRequest.T).addPropertyMetaData(TestUpdateRequest.relatedObjId, ppMd);
    serviceModelEditor.onEntityType(TestGetObjectRequest.T).addPropertyMetaData(TestGetObjectRequest.relatedObjId, ppMd);
```
Here we create md and set:

loadedObjectType property a value `TestObject` what points to entityType of the object we would load if query is used
getterEntityType property value `TestGetObjectRequest` that points to service request type that should be used to load the object in case of service request usage

After that we register property md on `TestUpdateRequest` on property `TestUpdateRequest.relatedObjectId` what points to id property in the corresponding service request so MessagingAspect knows where to obtain the ids from, 
and we register another property md on `TestGetObjectRequest` on property `TestGetObjectRequest.relatedObjectId` that points to id property in the service request to get the object using service

So for 2 loading types we need as follows:

For all approaches we do require `MessagingTypeSignatureMd`!
1. Load using QUERY -> We need MessagingPropertyMd on entityType: `TestUpdateRequest` on property `TestUpdateRequest.relatedObjId` and property `loadedObjectType` set to `TestObject.T.getTypeSignature()`
2. Load using SERVICE without extraction -> We need MessagingPropertyMd on entityType: `TestUpdateRequest` on property `TestUpdateRequest.relatedObjId` and property `getterEntityType` set to `TestGetObjectRequest.T.getTypeSignature()` and another one on entityType: `TestGetObjectRequest.T` on property `TestGetObjectRequest.relatedObjId`

##### fieldsToInclude for diff in the ProducerDiffEventRule
In any ProducerEventRule we have a `List<TypesProperties> fieldsToInclude` in case of Diff calculation this field points 
to the properties of an object that should be included in `ComparisonResult.expectedDiffs` they are extracted from the 
ProducerEventRule in `MessageComposer` after the object states were loaded using comparison of EntityType of the object to the entityType in the `TypesProperties` in the list of `fieldsToInclude`
And as in ProducerDiffEventRule we have an option to extract nested object for diff using `extractionPropertyPaths` there 
would be a difference in setting of fieldsToInclude depending on necessity to extract the nested object.

For example, if we are loading a TestObject using a QUERY, and we want to properties name and embeddedObject to be expected for diff we should add TypesProperties as follows:
```
TypesProperties ts = session.create(TypesProperties.T);
GmEntityType type = query(cortexSession, GmEntityType.T, "typeSignature", TestObject.T.getTypeSignature());
ts.setEntityType(type);
ts.setProperties(Set.of(TestObject.name, TestObject.embeddedObject));
```
So here we bind properties `name` and `embeddedObject` to entityType `TestObject` and as our comparison object type would be `TestObject` it is going to be picked by the mechanism to perform diff using these properties of the `TestObject`

On the other hand if we're using a SERVICE request to get the object states we most probably will receive a wrapper `TestGetObjectResponse` containing our `TestObject` as one of its properties,
in case we want to compare these `TestGetObjectResponse` we would simply add `TypesProperties` same way as before mentioning `TestGetObjectResponse` and properties like `TestGetObjectResponse.testObject.name` and `TestGetObjectResponse.testObject.embeddedObject`.

But in case we want to directly compare `TestObject` nested inside `TestGetObjectResponse` we would need to configure `extractionPropertyPaths` with `TypesProperty`
containing information `entityType : TestGetObjectResponse.T` and `property: TestGetObjectResponse.testObject` -> this would point out mechanism to extract the nested property out of the wrapper before performing actual comparison.
And we also have to add TypesProperties to fieldsToInclude as follows:
```
TypesProperties ts = session.create(TypesProperties.T);
GmEntityType type = query(cortexSession, GmEntityType.T, "typeSignature", TestGetObjectResponse.T.getTypeSignature());
ts.setEntityType(type);
ts.setProperties(Set.of(TestObject.name, TestObject.embeddedObject));
```
As you can see the type is set to `TestGetObjectResponse` as the `fieldsToInclude` are extracted from the `ProducerDiffEventRule` before we extract the nested object from the wrapper, but the properties in the list are pointing directly to the nested objects properties.

### Consumer configuration -> ConsumerEventRule:
`ConsumerEventRule` at the moment there is the only rule type for consumption. In case you will need more please make it abstract and derrive appropriate children from it as it is done in ProducerEventRule.
It is used to state the settings for message consumption through corresponding `ConsumerConnectors` from the queue.
It has the following properties:
```
postProcessorType -> `GmEntityType` that represents the type of PostProcessor to be deployed for message post processinng.
postProcessorRequestType -> GmEntityType` that represents the requestType that should be used to send the consumed message to corresponnding PostProcessor.  
```

Current implementation of ConsumerEvents allow you to change the EventEnpointConfigs on the go and immediately apply them to corresponding connections, but to be able to reconfigure the post-processor a restart/sync operation is required as post-processor is a deployable.