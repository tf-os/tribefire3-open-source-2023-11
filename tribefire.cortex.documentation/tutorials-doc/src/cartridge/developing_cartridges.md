# Developing Cartridges (legacy)

Using a cartridge, you can extend the tribefire platform by implementing custom logic.

## General

A Tribefire cartridge allows you to extend the platform. Using a cartridge you can create a new  extension point implementation and then integrate it into tribefire via Control Center.

You can develop your custom implementations of regular tribefire extension points:

* access
* connection
* web terminal
* authentication service
* state change processor
* action processor
* transition processor
* condition processor
* app
* worker
* resource streamer

A typical cartridge development flow looks like this:

1. Creating a new cartridge
2. Creating denotation and expert types
3. Implementing logic
4. Wiring them together
5. Testing
6. Deploying the cartridge to a tribefire instance

> This tutorial assumes you have set up your IDE and the Simple Cartridge as per the instructions here: [Setting Up IDE](setting_up_ide.md).

## Creating a New Cartridge

The easiest way of creating a new cartridge is to copy the structure of one of the enablement cartridges (Simple or Demo Cartridge).

Not only does this save your time, but also reduces the risk of potential errors connected to creating the cartridge structure all by yourself.

1. Open the installation folder of your enablement cartridges (the `artifacts` folder mentioned in [Setting Up IDE](setting_up_ide.md)). The parent folders of Simple and Demo Cartridges should be available inside.
2. Create a copy of one of the `parent` folders (either Demo or Simple Cartridge, depending on your preferences). Components of that folder will be the foundation of your new cartridge.
3. Open the newly created copy. Rename all folders inside it so that they match the name of your new cartridge (for example `tribefire-demo-cartridge` -> `access-cartridge`).
4. Open the newly created folder in your code editor. Use the **find and replace** function to rename all components accordingly (find `tribefire-demo`, replace with `access`, following the example in point 3).
5. Run `mvn clean install` in the newly created folder. Your cartridge is now added to your local repository.
6. Run the Jinni command `jinni setup-local-tomcat-platform setupDependency=your.group.id:your-cartridge-setup#version installationPath=your_installation_target_location -verbose true`. Your cartridge is now installed in the target directory.
7. Open your platform, and verify that the cartridge has been installed.
   >You need to repeat the installation process starting from step 5 each time you add new changes.

## Creating a Denotation Type
In this tutorial, you are extending tribefire by introducing a new access called `ReadOnlyAccess`. This access behaves as all other accesses, but instead of saving changes done to the models, it displays an error message, effectively making it a simple read-only access. This chapter of the tutorial focuses on creating the deployment model for your access.

A deployment model, in the context of cartridges, is used to deliver the denotation types and other required entities to Control Center. The denotation types are linked to their implementations (called experts) using Wire and it is the experts who have the actual logic. Denotation types contain method headers only. 

> As the cartridge you create is based on the simple cartridge, some package names may reflect that.

1. Create a new `ReadOnlyAccess` interface in the `access-deployment-model` project, in the `com.braintribe.tribefire.cartridge.simple.model.deployment.access` package.
2. Make sure that your interface extends the `com.braintribe.model.accessdeployment.IncrementalAccess` interface. As each deployment model must inherit from the `Deployable` interface, you must make sure to implement it explicitly or implicitly. This particular `IncrementalAccess` class is the basic extension class you can use when introducing custom accesses so it already inherits from the `Deployable` interface.

```java
   public interface ReadOnlyAccess extends IncrementalAccess
```

3. Allow your new interface to have access to reflection by introducing the immutable variable `T`:

```java
   final EntityType<ReadOnlyAccess> T = EntityTypes.T(ReadOnlyAccess.class);
```
> For more information on reflection, see [Reflection](asset://tribefire.cortex.documentation:concepts-doc/features/reflection.md).

4. As you want your access to behave like a normal access but display an error message when trying to save, the easiest way is to introduce a delegate to your interface. You could, of course, write the implementation for all methods yourself, but let's keep it simple for the sake of this tutorial. So, basically, you want to have two new properties: the delegate (which does all the actual work apart from saving) and the error message to be displayed when trying to save. Introduce two String properties to your interface:

```java
   public static final String access = "access";
   public static final String exceptionMessage = "exceptionMessage";
```

> These are the properties which are visible in Control Center. Make sure that the variable name is the same as its value. The properties are declared as Strings, because the functionality which binds the denotation type (so your interface) to its expert type (actual implementation) expects String values.

5. Specify the method headers for setters and getters for each of the properties:

```java
    void setAccess (IncrementalAccess access);
    IncrementalAccess getAccess();

    void setExceptionMessage (String input);
    String getExceptionMessage();
```

> Note that the input arguments for the setter and the return types for the getter methods are actual types, not Strings.

6. Save your changes.

## Creating an Expert

Now that you have your deployment model ready, it is time to create a Java class which contains the actual logic of what you want to do with the access, which is to have all the calls except for one delegated to an existing access. The one call that is not delegated to an existing access is the `applyManipulation()` call. In cartridge context, expert types are Java classes which you use to introduce your custom logic. The expert types are linked to their denotation types using Wire. Expert types are regular Java classes.

1. Create a new `ReadOnlyAccess` class in the `access-cartridge` project. In this tutorial, we recommend to use the `com.braintribe.tribefire.cartridge.simple.deployables.access` package.
2. Make sure that your class implements the `com.braintribe.model.access.IncrementalAccess` interface.

```java
   public class ReadOnlyAccess implements IncrementalAccess
```

3. Create two class fields respective to the two properties you implemented in your denotation type interface:

```java
   private IncrementalAccess delegate;
   private String exceptionMessage;
```

4. Implement getters and setters for the two fields:

```java
   @Required
    public void setDelegate(IncrementalAccess delegate) {
    this.delegate = delegate;
    }

    @Required
    public void setExceptionMessage(String exceptionMessage){
    this.exceptionMessage = exceptionMessage;
    }
```

> Make sure to place the `@Required` annotation before the methods.

5. Implement all the methods from the `com.braintribe.model.access.IncrementalAccess` interface but override them so that your delegate does the work:

```java
   @Override
   public String getAccessId() {
    return delegate.getAccessId();
   }
```

6. Change the logic of the `applyManipulation()` method to throw an exception (with the `exceptionMessage` field as the message body) when the method is called:

```java
   @Override
   public ManipulationResponse applyManipulation(ManipulationRequest arg0) throws ModelAccessException {
     throw new ModelAccessException(exceptionMessage);
   }
```

> This is effectively the logic of the whole service. All other methods are delegated to `IncrementalAccess`.

7. Save your changes.

## Wiring Denotation Type to the Expert Type

In a cartridge context, denotation types are Java interfaces with method headers that you use to model your extension point. Denotation types are linked to their implementations (called experts) using Wire, and it is the experts who have the actual logic. Denotation types contain method headers only.

> For more information about Wire see [Wire in Detail](asset://tribefire.cortex.documentation:concepts-doc/features/wire/wire_in_detail.md).

1. In the `access-cartridge` project, in the `DeployablesSpace` class of the `.com.braintribe.cartridge.extension.wire.space` package, create a new public `readOnlyAccess(ExpertContext<ReadOnlyAccess> context)` method. Make sure to put the denotation type (`com.braintribe.tribefire.cartridge.simple.model.deployment.access.ReadOnlyAccess`) as the `context` parameter of the method. You may need to put the fully qualified name of the class as the parameter.

```java
   public ReadOnlyAccess readOnlyAccess(ExpertContext<ReadOnlyAccess> context)
```

> This method's return type is `com.braintribe.tribefire.cartridge.simple.deployables.access.ReadOnlyAccess`, so the expert type.

2. Create a `denotationType` variable of the denotation type's fully qualified name, `com.braintribe.tribefire.cartridge.simple.model.deployment.access.ReadOnlyAccess` (so the denotation type), and assign it the returned value of the `context.getDeployable()` method.

```java
   com.braintribe.tribefire.cartridge.simple.model.deployment.access.ReadOnlyAccess denotationType = context.getDeployable();
```

3. Create a new variable called `access` of the type `com.braintribe.tribefire.cartridge.simple.deployables.access.ReadOnlyAccess` and instantiate the expert type - `com.braintribe.tribefire.cartridge.simple.deployables.access.ReadOnlyAccess`.

```java
   com.braintribe.tribefire.cartridge.simple.deployables.access.ReadOnlyAccess access = new com.braintribe.tribefire.cartridge.simple.deployables.access.ReadOnlyAccess();
```

4. Bind the exception message property from the denotation type to the expert type:

```java
   access.setExceptionMessage(denotationType.getExceptionMessage());
```

5. Bind the access property from the denotation type to the expert type by creating a new `delegate` variable of the type `com.braintribe.model.access.IncrementalAccess`, assigning an expert to the variable, and assigning the delegate to your bean:

```java
   com.braintribe.model.access.IncrementalAccess delegate = context.resolve(denotationType.getAccess(), com.braintribe.model.accessdeployment.IncrementalAccess.T);
	access.setDelegate(delegate);

   return access;
```
> When you assign properties from a deployable to an expert, make sure to assign each property separately: <br/> `bean.setMyProperty1(deployable.getMyProperty1);` <br/> `bean.setMyProperty2(deployable.getMyProperty2);`

6. Navigate to the `extensions()` method definition in the `CustomCartridgeSpace` class of the `com.braintribe.cartridge.extension.wire.space` package.
7. Enable component proxying in the component provider space.

```java
   masterComponents.enableIncrementalAccessProxy(bean);
```
The line `masterComponents.enableIncrementalAccessProxy(bean)` enables the proxying of `IncrementalAccess.T` components, which means that this cartridge creates proxies for `IncrementalAccess` components deployed to other cartridges, and therefore, is able to resolve them within its `deployables::readOnlyAccess`, method.
> You can use the various `enable...Proxy()` methods to enable proxying for specific component types exposed by the components space. For more details on the method variants, refer to the JavaDoc of `com.braintribe.cartridge.common.wire.contract.ComponentProxyingEnablingContract` class, a super contract of both `CommonComponentsContract` and `MasterComponentsContract`.

8. Still in the `extensions()` method, bind the `ReadOnlyAccess` denotation type (`com.braintribe.tribefire.cartridge.simple.model.deployment.access.ReadOnlyAccess`) to its deployable supplier:

```java
   bean.bind(ReadOnlyAccess.T)
		.component(masterComponents.incrementalAccess())
		.expertFactory(deployables::readOnlyAccess);
```

9. Open the command line in the root folder of your cartridge and run `mvn clean install`.

## Testing

In this tutorial, testing consists in creating the Read Only Access and making sure its functionality works the way we intended.


1. Set up your local environment to use your cartridge by running the command from step 8 from the **Creating a New Cartridge** section of this tutorial.
2. Start the server (`runtime/bin/startup.bat`), open Control Center and click the Custom Accesses link.
3. Create a new a Smood Access, for example `MyNewSmoodAccess` and assign it a model to operate on, for example `UserModel`. Add some entries to your new access, for example User 1 and User 2.
3. Create a new Read Only Access, and assign it the following:

Parameter | Description | Example
----- | ----- | -----
`exceptionMessage` | The message displayed when trying to save the changes. | Cannot save changes - this access is read-only.
`access` | The access our Read Only Access delegates calls to. You can select any access present in the system, like the `MyNewSmoodAccess` access you created before. | `MyNewSmoodAccess`
`cartridge` | The cartridge where the denotation type of the access is stored. Even though this parameter is not marked as mandatory, your access will not work if you leave this parameter blank. | `mynewcartridge.cartridge`
`metaModel` | The model this access operates on. As your Read Only Access forwards all but one request to a delegate access, assign the metamodel of the delegate access here. | `UserModel`

4. Click Apply and then Commit.
5. Right-click your Read Only Access and select **More->Deploy**.
6. Once the access is deployed, right-click your access again and select the **Switch To** option.
7. In the Explorer window, search for the entities you created and try to change a property, for example name. Then, click **Commit**. You see an exception message which reads the value of your `exceptionMessage` parameter.
> Note that the changes have not been applied, which means the logic you implemented works.
<!-- 
## Packaging

In this tutorial, we are using Ant to create a `.war` file with our cartridge. To create a `.war` file, perform the steps below:

1. Navigate to the folder where you checked out/unzipped the Simple Cartridge source files. Then, navigate to `SimpleCartridgeRoot` folder there.
2. Open the command prompt and run the `ant -Dartifact=com.braintribe.product.tribefire.cartridges.mynewcartridge:MyNewCartridge#2.0 install-deps download-deps install` command. This command downloads all necessary dependencies and builds the `.war` file.
3. Navigate to the `SimpleCartridgeRoot\com\braintribe\product\tribefire\cartridges\mynewcartridge\MyNewCartridge\2.0\dist\assembled` directory. You can find your `MyNewCartridgeCartridge.war` file there. -->

## What's Next?

Now that you have your custom cartridge, it is time to package it as an platform asset. To do that, you must first configure a platform asset repository for your tribefire. For instructions on how to do that, perform the steps described in the following sections:

1. [Configuring a Platform Asset Repository](asset://tribefire.cortex.documentation:tutorials-doc/platform-assets/setting_up_platform_assets.md#configuring-a-platform-asset-repository) 
2. [Maven Repository for Deploy Transfer](asset://tribefire.cortex.documentation:tutorials-doc/platform-assets/setting_up_platform_assets.md#maven-repository-for-deploy-transfer)
3. [Maven Repository for Deploy Transfer](asset://tribefire.cortex.documentation:tutorials-doc/platform-assets/setting_up_platform_assets.md#maven-repository-for-install-transfer)

Once you performed the procedures above, continue with the [Working with Platform Assets](asset://tribefire.cortex.documentation:tutorials-doc/platform-assets/working_with_platform_assets.md) document.