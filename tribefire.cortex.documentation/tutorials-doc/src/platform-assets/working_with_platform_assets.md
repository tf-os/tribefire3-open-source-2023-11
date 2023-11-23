# Working with Platform Assets
To fully show the power and the collaboration features behind platform assets, you will perform several different steps in this tutorial. The steps will include creating a model, assigning this model to an access, creating the respective platform assets, deploying them to a repository and setting up your platform from scratch based on your new assets.

### Prerequisites
It is vital to set up you environment correctly and to know what platform assets do and what different natures they come in before you dive into this tutorial.
>For more information, see [Platform Assets](asset://tribefire.cortex.documentation:concepts-doc/features/platform_assets.md) and perform the steps described on [Setting Up Environment for Platform Assets](setting_up_platform_assets.md).

## Creating an Asset
In this procedure you will create a simple model and package it as an asset with the `ManipulationPriming` nature.
>For more information about asset natures, see [Platform Assets](asset://tribefire.cortex.documentation:concepts-doc/features/platform_assets.md).

1. Using Modeler, create a new `car-model` model with only one entity type: `Car`.
    >For information how to use Modeler, see [Using Modeler](asset://tribefire.cortex.documentation:tutorials-doc/control-center/using_modeler.md).
2. Add two properties to the `Car` entity: a String `licensePlate` and an integer `power`.
3. Commit your changes.

## Closing an Asset 
When you feel you are done working on your asset, it's time to close it. 

As you may already know, all the manipulations are stored in the trunk asset of the respective access. Manipulations done in Control Center are stored in the `trunk-cortex` asset, while manipulations done in a custom `CollaborativeSmoodAccess` are stored in the respective trunk asset of that access.

Closing the asset we just created means that an artifact with the `ManipulationPriming` nature is being created, containing the manipulation files as well as zipped resources, if there are any.

A best practice is to give the asset a name which reflects the manipulations stored inside. Additionally, you can add a comment which is also stored as a manipulation (containing the comment description, the user who created the comment and the date). Closing an asset with adding a comment also wraps those manipulations in a block. You can think of this functionality as something similar to a commit in a version control system.

To close an asset:
1. In Control Center, switch to the Platform Setup access by clicking the cogwheel icon in the top-right corner and clicking **Switch to -> Platform Setup**.
2. In the Platform Setup access, navigate to the **Trunk Asset** entry point. Your trunk asset is visible there.
3. Right-click `trunk-cortex` and select **Platform Setup -> Close**. If you are on a different access than cortex, then your trunk asset's name is trunk-accessName, where accessName is the name of the access.
   >For more information on available actions, see [Platform Setup Access](asset://tribefire.cortex.documentation:concepts-doc/features/platform_assets.md#platform-setup-access).
4. Provide a name for your asset, for example `car-model-asset`, and click **Execute**. A notification is displayed and your new asset is created:
```
Successfully closed trunk asset (access: cortex)
Values: custom:car-model-asset#1.0
```
> When you successfully close an asset, it is no longer a part of the trunk asset - your closed asset becomes a standalone asset. 

## Deploying an Asset 
Deploying an asset means to transfer it as an artifact to a Maven-compatible repository to persist it and make it available for further setups. Deploying also increases the `resolvedRevision` property of the asset. Depending on whether it's a remote or a local repository, deploying an asset might mean that other people can download it and use it. This is, of course, the case if you're uploading an asset to a remote repository. 
>You can only deploy a closed asset.

To deploy an asset:
1. In the Platform Setup access, navigate to the **All Assets** entry point. 
2. Right-click your new `car-model-asset` and select **Platform Setup -> Deploy**. When the asset is successfully deployed to the repository you specified in your `CortexConfiguration`, a notification is displayed:
   ```
   Successfully deployed asset 'custom:car-model-asset#1.0'
   ```
   >For information how to set up your Maven for platform assets development, see [Setting Up Environment for Platform Assets](setting_up_platform_assets.md).


<!-- ## Creating a New Access Asset
Next step you are going to perform is to create a <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.smood}}">SMOOD</a> access, assign your `car-model` as its <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metamodel}}">metamodel</a>, and package it all as a brand new asset.

1. In Control Center, create a new collaborative SMOOD access and provide the necessary information, for example:
   * `externalId`: custom.car
   * `name`: car-smood-access
   * `metamodel`: car-model
   {%include tip.md content="For more information about different types of accesses, see [Smart Memory Object-oriented Database](smood.md)."%}
   Creating a new access creates new manipulations in the cortex access, which means a new `trunk` asset is created.
2. Switch to the Platform Setup access and close and deploy your new trunk asset. Give it a meaningful name, for example `car-access-asset`. For information on how to close and deploy an asset, see the previous sections of this tutorial.

3. In the **All Assets** entry point, expand the **car-access-asset** entry and display its dependencies. Note that there is a dependency to **car-model-asset** version 1.0. This is because you created the `car-access` after you had created the `car-model`. Assigning the `car-model` as the metamodel of your `car-access` has nothing to do with it. -->


## Adding Dependencies
You must add your new asset as a dependency to the project aggregator if you want to be able to set up your environment based on that asset. This is because it is the project aggregator that you pass as a parameter during the setup of your project.

1. Switch to the Platform Setup access and navigate to **Aggregators** . 
2. Find the`my-project` aggregator asset. This asset only appears in the list if you set up your environment as described on [Setting Up Environment for Platform Assets](setting_up_platform_assets.md). If you used a different name for your project, the name of the aggregator asset will be different.
3. Right-click the `my-project` asset and select **Platform Setup -> Add Dependencies**. A new window opens.
4. In the new window, add the `car-model-asset` as a dependency, mark the `as global setup candidate` checkbox and click **Execute**.
5. Still in the Platform Setup access, go to **Modified Assets**, right-click your `my-project` aggregator asset and select **Platform Setup -> Deploy**. This effectively persists the asset aggregator in your local Maven repository.

## Setting up Environment Based on Your New Assets
It's now time to use your new platform assets to set up a tribefire installation where they will be present out-of-the-box.

1. Create a new directory, for example `setupTest`.
2. In the `setupTest` directory, run the `jinni setup-local-tomcat-platform setupDependency=tutorial:my-project#1.0 installationPath=pathToThisDirectory` command. If you used a different name for your project, the name of the aggregator asset will be different.
3. Navigate to `setupTest/runtime/host/bin` and run `catalina start` to start the server. 
4. On the landing page, inspect if your model and access are part of your new installation.

