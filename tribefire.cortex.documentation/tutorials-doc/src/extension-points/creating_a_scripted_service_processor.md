# Creating a Scripted Service Processor
In this tutorial you are going to create a scripted service processor which creates a new user from Control Center without the need to switch to the **Authentication and Authorization** access. 

To achieve that, you will model the service request first and then implement and attach a Groovy-based processor. 

>For best practices on scripting in general, see [Scripting](asset://tribefire.cortex.documentation:concepts-doc/features/scripting.md).

After implementing the processor, you will test the functionality. During the evaluation of your service processor, you will have to provide the name and the password for your new user. The processor will then create a session in the **Authentication and Authorization** access and instantiate the new user. Finally, you will have to switch to the **Authentication and Authorization** access to confirm the user has been created.

## Modeling the Request
As everything in tribefire, also service requests are modeled - this is done in a service-model. 

>For more information on creating service models, see [Creating a Service Model - Best Practices](asset://tribefire.cortex.documentation:concepts-doc/features/service-model/service_model.md).

To create a service model:
1. In Control Center, navigate to the **Custom Models** entry point, and click **New**.
2. Create a new model with the name `user-service-model` and add the following dependencies:
    * `user-model`: required for obvious reasons. First of all, we need to create a user instance and secondly your service response will also be a **User** entity type.
    * `access-request-model`: used because it allows to do service requests on accesses.
3. Open the **user-service-model** in Modeler and create a new `CreateUserRequest` type. Make sure it derives from three supertypes (generalization):
    * `User`: used in the request to provide proper configuration options.
    * `AuthorizedRequest`: required whenever authorization is needed during a service evaluation.
    * `AccessRequest`: needed when using an access session.
    >For information on using Modeler, see [Using Modeler](asset://tribefire.cortex.documentation:tutorials-doc/control-center/using_modeler.md).
4. Commit your changes.

>You don't have to add any additional properties - everything required is inherited from the supertypes.

## Defining the Return Type
As the result, you want to retrieve your newly created user instance and return it. 

To define the return type:

1. Still in Control Center, make sure, the **CreateUserRequest** type is still selected.
2. In the panel on the right side, switch to the **Details** tab.
3. In the **evaluatesTo** row, click the **Assign** link. A search window opens.
4. In the search window, search for **User** and double click on the `User` type.
5. Click **Commit**.

## Making the Request Executable in Control Center
As you want to use your service call from the Control Center - or to be more specific from the `cortex` access - you need to merge your `user-service-model` into the `tribefire-cortex-model`.

To make your request executable from Control Center:

1. In Control Center, navigate to **Custom Models**, right-click the **user-service-model** and select  **More -> Add to CortexModel**. Your `user-service-model` is now a dependency of `tribefire-cortex-model` and can be used in the `cortex` service domain.

## Creating a New Scripted Service Processor
To create a new scripted service processor:

1. In Control Center, navigate to the **Service Processors** entry point, and click **New**.
2. In the new window that opened, select **ScriptedServiceProcessor** and click **OK**.
3. Provide values for the `externalId` and `name` properties, click on **Apply** and **Commit**. Your empty service processor is saved.

## Creating and Configuring a New Script Instance
Every scripted processor works based on the logic provided in the `Script` object. 
Currently, the supported scripting languages are:
* Groovy
* JavaScript
* BeanShell

To create and configure a new `Script` object:

1. In Control Center, right-click your newly created processor, select the `script` property and click **Assign**. A new search window appears.
2. Select `Groovy` and click **Finish**.
3. In the **source** property, provide the following logic:
    ```groovy
    import com.braintribe.model.user.User;

    session = $sessionFactory.newSession("auth");
    User user = session.create(User.T);
    user.name = $request.name;
    user.password = $request.password;
    session.commit();

    query = "from com.braintribe.model.user.User where name = '"+$request.name+"'";
    return session.query().entities(query).first();
    ```
    
    In the script the following steps are performed:

    * the `User` type is imported
    * a new session on the **Authentication and Authorization** access (`externalId`: `auth`) is created. To create a new session a session factory is used, available by default via `$sessionFactory`
    * on the session, you create a new instance of `User`
    * the `name` and `password` properties are set with the retrieved values from the `$request` object, also available by default
    * the manipulations are committed to the session
    * a simple query that returns the result is performed

4. After you are done writing the script, click **Apply** and **Commit**. The functionality available to your script is provided by the Java API's [AccessRequestContext](javadoc:com.braintribe.model.processing.accessrequest.api.AccessRequestContext). 
   >You are not limited to using Groovy - you're free to use any of the supported scripting languages.
5. Finally, right-click your processor, and click **Deploy**. Your processor is now deployed and ready to be used.

## Configuring the `ProcessWith` Metadata
In the previous steps you modeled your service request and created your service processor. What's left to do is to bind the processor to the request type. This is achieved via the **ProcessWith** metadata</a>.

To configure the metadata:

1. In Modeler, make sure your **CreateUserRequest** type is selected.
2. In the panel on the right, choose the **Details** tab.
3. In the `metaData` section, click **Add**. A search window opens.
4. In the search window, search for **ProcessWith** and double-click on the respective entry in the **Types**-section. Again, a new window opens.
5. In the new window, in the **processor** row, click the **Assign** link. Another search window opens. 
6. Search for your just created service processor and double-click it. 
7. Click **Apply** and then **Commit**. You have now assigned the metadata with your service processor to the request. As a result, this request will be processed by this processor.

## Apply Model Changes
To make sure all changes on the models - especially the `cortex-model` - are applied at the `cortex` access, you have to restart tribefire or reload the the **tribefire-services** via the **Servlet Manager**. 

## Testing the Processor
To test your new scripted service processor:

1. In Control Center, you use the **Quick Access** box - the search field on the top of the screen - and search for **CreateUserRequest**. 
   >This type appears twice: once in the **Types** section and a second time in the section for **Service Requests**.
2. Double-click on **CreateUserRequest** in the **ServiceRequests** section. A new tab with a form allowing to configure and evaluate the service request opens.
3. In the form, enter values for the **name** and **password** fields. You can leave the rest empty.
4. Click **Evaluate**. The service is triggered and executed. As a result, you get back a single `User` object, which corresponds to the values you entered.
7. On the **tribefire-services** landing page, click the cogwheel icon, switch to the **Authorization and Authentication** access and run the provided query for **Users**. Your newly created instance is visible in the list of results.