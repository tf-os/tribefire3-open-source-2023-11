# Creating Service Requests

Along with the developing a service processor, creating a service request is an essential part of the process of implementing custom functionality into Tribefire.

> For more information on service processors and their relations to service requests and responses, see [Service Processor](asset://tribefire.cortex.documentation:concepts-doc/features/service-model/service_processor.md). For information on creating service models, see [Creating a Service Model - Best Practices](asset://tribefire.cortex.documentation:concepts-doc/features/service-model/service_model.md).

In this example, we will create a service request and a service processor programmatically and later trigger the request from Control Center. The service request will, when triggered, populate the `cortex` access with a list of `User` instances. For reasons of efficiency we will not focus on creating a separate cartridge to host the service request code in, but will use the simple cartridge instead.

> For information how to develop cartridges, see [Developing Cartridges](asset://tribefire.cortex.documentation:tutorials-doc/cartridge/developing_cartridges.md).


## Prerequisites

* prepared environment as per the instructions on [Setting Up IDE for Cartridge Development](asset://tribefire.cortex.documentation:tutorials-doc/cartridge/setting_up_ide.md)

## Creating a Service Processor Denotation Type

It is a good idea to create your service processor first - this way you will know what exact types and properties you must implement in your service request. Let's start with creating the denotation type for your service processor.

1. In your IDE, go to the `simple-deployment-model` project and, in the `com.braintribe.tribefire.cartridge.simple.model.deployment.service` package, create a new `PopulateAccessService` interface.
2. Make sure your interface extends the `AccessRequestProcessor` and provide your service access to reflection:

```java
package com.braintribe.tribefire.cartridge.simple.model.deployment.service;

import com.braintribe.model.extensiondeployment.access.AccessRequestProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface PopulateAccessService extends AccessRequestProcessor {
	
	EntityType<PopulateAccessService> T = EntityTypes.T(PopulateAccessService.class);
	
}
```

> For more information on reflection, see [Reflection](asset://tribefire.cortex.documentation:concepts-doc/features/reflection.md)

## Creating a Service Processor Expert

Now that you have your denotation type, it is time to implement the expert. Experts in Tribefire are always Java classes that implement the actual business logic.

1. In your IDE, go to the `simple-cartridge` project and, in the `com.braintribe.tribefire.cartridge.simple.deployables.service` package, create a new `PopulateAccessService` class.
2. Make sure your service implements the `AccessRequestProcessor<REQUEST, RESPONSE>` interface. Note that you must specify the service request and the service response in the class header.

```java
package com.braintribe.tribefire.cartridge.simple.deployables.service;

import java.util.List;

import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.user.User;
import com.braintribe.tribefire.cartridge.simple.model.service.PopulateAccessWithDataRequest;

public class PopulateAccessService implements AccessRequestProcessor<PopulateAccessWithDataRequest, List<User>> {
	// your logic goes here 
}
```

> Note that your IDE will produce errors here because you have not yet implemented the `PopulateAccessWithDataRequest`. You will implement it later.

3. As you are implementing the `AccessRequestProcessor`, you must add its methods to your class and implement them. The processor you are implementing has only one method: `process()`, which provides the appropriate context for your logic (and the context takes your service request as an argument). What's more, the `process()` method returns the exact type you specified in the class header as the second argument: `AccessRequestProcessor<PopulateAccessWithDataRequest, List<User>>`.

Below you can find an example implementation of the method:

```java
@Override
	public List<User> process(AccessRequestContext<PopulateAccessWithDataRequest> context) {
		
		PersistenceGmSession session = context.getSession();
		
		List<User> usersToCreate = context.getRequest().getUsers();
		List<User> createdUsers = new ArrayList<User>();
		
		for (User user: usersToCreate) {
			User createdUser = session.create(User.T);
			createdUser.setFirstName(user.getFirstName());
			createdUser.setName(user.getName());
			createdUsers.add(createdUser);
		}
		session.commit();

		return createdUsers;
	}
```

The first thing we do is get the current session. Then we specify the `usersToCreate` list variable and assign it the value of the (not existing yet) service request's `getUsers()` method. At this point you already know that your service request must have a `getUsers()` method whose return type is a `List<User>`.

Once we have the list of users to create, we loop over the items of that list and create a new `User` instance within the session for every item found in the list. Moreover, we assign the values of the `name` and `firstName` properties to the new `User` entities inside the session. The loop finishes with adding a new `User` instance to the previously created list referenced by the `createdUsers` variable. Once the loop has finished, all the changes are committed to the session and the list of created users is returned.

## Creating a Service Request Denotation Type

You coded your processor so you know exactly what properties and methods your request must have. 

1. In your IDE, go to the `simple-service-model` project and create a new `PopulateAccessWithDataRequest` interface. 
2. Make sure your interface extends the `AccessDataRequest` and `AuthorizedRequest` interfaces.

```java
public interface PopulateAccessWithDataRequest extends AccessDataRequest, AuthorizedRequest {

}
```
3. Add a dependency to `user-model` to this project's POM and refresh the projects in the IDE:
```xml
<dependency>
			<groupId>com.braintribe.gm</groupId>
			<artifactId>user-model</artifactId>
			<version>${V.com.braintribe.gm}</version>
			<?tag asset?>
</dependency>
```

4. Provide your service request denotation type access to reflection and define its necessary methods. You already know what method your service processor will use - now it's time to define them:

```java
package com.braintribe.tribefire.cartridge.simple.model.service;

import java.util.List;
import com.braintribe.model.accessapi.AccessDataRequest;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.user.User;

public interface PopulateAccessWithDataRequest extends AccessDataRequest, AuthorizedRequest {
	
	EntityType<PopulateAccessWithDataRequest> T = EntityTypes.T(PopulateAccessWithDataRequest.class);
	
		
	//list of Users to populate the access with - you used this method in the service processor
	List<User> getUsers();
	void setUsers(List<User> users);

}
```

5. Go to the the `simple-cartridge` project and, in the `com.braintribe.tribefire.cartridge.simple.deployables.service` package, open the `PopulateAccessService` class and import the service request denotation type you just created.

## Wiring the Service Processor Denotation Type to the Expert

Now that you have all the necessary elements of your service request, it is time to wire them together.

> For more information on Wire, see [Wire](asset://tribefire.cortex.documentation:concepts-doc/features/wire/wire.md) and [Wire in Detail](asset://tribefire.cortex.documentation:concepts-doc/features/wire/wire_in_detail.md).

1. In your IDE, go to the `simple-cartridge` project and open the `DeployablesSpace` class.
2. Register your service processor as a deployable by creating the appropriate method:

```java
public PopulateAccessService populateAccessService(ExpertContext<com.braintribe.tribefire.cartridge.simple.model.deployment.service.PopulateAccessService> context) {
		return new PopulateAccessService();
	}
```
This method's **return** type `com.braintribe.tribefire.cartridge.simple.deployables.service.PopulateAccessService` so the expert type. Note that it is the **denotation** type (`com.braintribe.tribefire.cartridge.simple.model.deployment.service.PopulateAccessService`) that is provided as the parameter of the `populateAccessService()` method. 

3. Go to the `CustomCartridgeSpace` class and find the definition of the `extensions()` method.
4. Bind your service processor denotation type to its deployable supplier:
```java
@Managed
	public DenotationTypeBindingsConfig extensions() {
		DenotationTypeBindingsConfig bean = new DenotationTypeBindingsConfig();

		//other bean bindings

bean.bind(PopulateAccessService.T)
			.component(commonComponents.accessRequestProcessor())
			.expertFactory(deployables::populateAccessService);
```

## Configuring the Initializer

1. In your IDE, go to the `simple-cartridge-initializer` project and, in the `SimpleCartridgeInitializer` class, add a new `configurePopulateAccessService()` method. This project is an asset of the type `PluginPriming` which means that is incrementally adds programmatic information to Tribefire. You must add and configure your new service processor there.
> For more information about `PluginPriming` see [Platform Assets](asset://tribefire.cortex.documentation:concepts-doc/features/platform_assets.md#pluginpriming)
2. Implement the method as follows:

```java
private void configurePopulateAccessService() {
		PopulateAccessService service = session.create(PopulateAccessService.T);
		service.setExternalId("populateAccessService");
		service.setGlobalId("custom:" + service.getExternalId());
		service.setId(service.getGlobalId());

		service.setName(PopulateAccessService.T.getShortName());
		service.setCartridge(cartridge);

		ProcessWith processWithMetadata = session.create(ProcessWith.T);
		processWithMetadata.setGlobalId("custom:md/processWith:" + service.getExternalId());
		processWithMetadata.setId(processWithMetadata.getGlobalId());
		processWithMetadata.setProcessor(service);

		new BasicModelMetaDataEditor(serviceModel).onEntityType(PopulateAccessWithDataRequest.T).addMetaData(processWithMetadata);
	}
```

It is worth to note the existence of the `ProcessWith` metadata which binds the processor to the request type: `new BasicModelMetaDataEditor(serviceModel).onEntityType(PopulateAccessWithDataRequest.T).addMetaData(processWithMetadata);`.

3. Find the definition of the `initializeData()` method and call the `configurePopulateAccessService()` method you just created from within its body.

## Metadata Validation

[](asset://tribefire.cortex.documentation:includes-doc/service_processor_metadata_validation.md?INCLUDE)

## Building and Setting Up Tribefire

When you have all your code in place, it is time to build the software and set up your tribefire based on the enhanced simple cartridge. To do so:

1. From within the folder which contains all the simple cartridge projects, run `mvn clean install`. 
2. When you see **BUILD SUCCESSFUL**, create a new directory and set up tribefire based on the cartridge you just built using Jinni: `jinni setup-local-tomcat-platform setupDependency=tribefire.extension.enablement-maven.simple:simple-cartridge-setup#2.0 installationPath=PATH_TO_A_DIRECTORY deletePackageBaseDir=true`.
> Do not forget to change the value of the `installationPath` parameter.

## Testing

1. Start the server (`runtime/bin/startup.bat`), open Control Center and, using the **Quick Access** search bar, search for `PopulateAccessWithDataRequest`.
2. Click the **Add** link and create several instances of `User`. Remember that the **name** parameter is mandatory.
3. Click **Evaluate**. Your service request has been called.
4. In the **Quick Access** search box, search for `User`. You will see a list of all instances of `User`, including the ones you added as a part of the service request.