# Creating a Scripted State Change Processor
In this tutorial we are going to create a scripted state change processor which automatically creates an e-mail address for instances of the `User` entity. The e-mail address will be created by appending the values of the `name` and `lastName` parameters and the `@braintribe.com` domain. When you create a new instance of `User`, you must provide the mandatory `name` parameter. As our e-mail will also use the value of the `lastName` parameter, you will need to provide it for the processor to work. 

 
When the value of the `lastName` attribute changes, the value of the `e-mail` attribute will be changed. This means that the `lastName` attribute is the **watched** property.

>For more information on scripting in general, see [Scripting](asset://tribefire.cortex.documentation:concepts-doc/features/scripting.md).


## Creating a New Scripted State Change Processor
To create a new scripted state change processor:

1. In Control Center, navigate to the **State Change Processors** entry point, and click **New**.
2. In the new window that opened, select **ScriptedStateChangeProcessor** and click **OK**.
3. Provide the `externalId` and `name` properties, click **Apply** and **Commit**. Your empty state change processor is saved

## Creating and Configuring a New Script Instance
Every scripted state change processor works based on the logic provided in the `Script` object. You can use any of the available scripting languages: Groovy, JavaScript, or Beanshell. 

To create and configure a new `Script` object:

1. In your newly created processor, select the `script` property and click **Assign**.
2. Select a scripting language, click **Finish**, and provide the logic using the scripting language you selected. In this tutorial, we are using Groovy to react to changes to the `lastName` property by changing the `e-mail` property:
  ```groovy
  def name = $context.entity.name;
  def lastName =  $context.entity.lastName;
  $context.entity.setEmail(name + "." + lastName + "@braintribe.com" );
  ```
  When you're done writing the script, click **OK** and **Commit**. The functionality available to your script is provided by the Java API `StateChangeContext`. You are not limited to using Groovy - you're free to use any of the supported scripting languages.

3. Right-click your processor, and click **Deploy**. Your processor is now deployed.


## Configuring the `OnChange` Metadata
A state change processor cannot function on its own. How would it know what change to react to? You must assign the `OnChange` metadata to the element you want to watch and trigger the logic on. In our case, it is the `lastName` property of the `User` entity type.

To configure the metadata:

1. In Control Center, navigate to **Base Models** and search for **User Model**.
2. Expand the **UserModel** and navigate to the properties of the **User** type. 
3. Expand the **User** type, navigate to its properties, expand the **lastName** property, right-click the **metaData** property and click **Add**. A search window opens.
4. In the new window, search for **OnChange** and click **Add and Finish**. A new window opens.
5. In the metadata configuration window, in the **processor** row, click the **Assign** link, select your scripted state change processor, click **Apply**, and then **Commit**.

## Testing Your Processor
To test your new scripted state change processor:

1. In Control Center, navigate to **Custom Accesses**, create a new SMOOD access, and assign the `UserModel` as its metamodel. <!--For information on how to create a SMOOD access, see the **Creating a SMOOD Access** section of the [Creating a Smart Access](http://localhost:4000/creating_smart_access.md#creating-a-smood-access) tutorial.-->
2. Switch to your new access, create a new instance of `User`, and commit your changes. 
3. Change the value of the `lastName` property and click **Commit**. Notice that the value of the `e-mail` attribute changes every time you change and commit the `lastName` property. 