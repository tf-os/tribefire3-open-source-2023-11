# Asset Management

In order to provide a convenient and easy way to manage your assets, the _Platform Setup Access_ has been introduced. It provides a set of operations which is described in the following sections.

## Trunk Assets

The trunk asset contains all the manipulations you make to a given access (so, for example everything you do in Control Center). 

Trunk assets have the `ManipulationPriming` nature and represent data that was recorded in `CollaborativeSmoodAccess` instances since your last setup, merge or close operation. 
>For more information on SMOOD accesses, see [Smart Memory Object-oriented Database](asset://tribefire.cortex.documentation:concepts-doc/features/smood.md).

A trunk asset is created when the first manipulation (like creating or changing a model, creating or changing an access, etc. ) is made in the respective access. Trunk assets contain transient data which you should deploy to a repository in order to persist them. 
Manipulations made in Control Center are stored in the trunk asset of the `cortex` access. 

Once a trunk asset is created, you can close the trunk asset effectively creating a new artifact.

By clicking the arrow next to the **Commit** button, you open the **Advanced Commit** dialog box where you can select what you want to do with the asset. 

> For more information on Control Center options, see the [Control Center Integration](asset://tribefire.cortex.documentation:concepts-doc/features/platform_assets.md#control-center-integration) section of this document.

>Any new manipulation creates a new trunk asset if none exists already.

When you close an asset, it is saved and appears in the **All Assets** section of the Platform Setup access.

## Merge

Merging allows you to append the manipulations that were recorded in a trunk asset to another asset with a `ManipulationPriming` nature or the trunk asset's predecessor. Note there must exist exactly one `ManipulationPriming` predecessor in the dependency chain. Only trunk assets can be merged.

The trunk asset itself will disappear after a successful merge. The asset to which it was merged will be marked as having unsaved changes which means that a transfer operation (install/deploy) is needed to finally persist the changes.

* Merge Programmatically

    Execute the request `com.braintribe.model.platformsetup.api.request.MergeTrunkAsset` via one of the endpoints (e.g. webRPC or REST).
* Merge via UI

    You can find all current trunk assets by clicking on the **Trunk Assets** node in the Platform Setup access. Select the asset to be merged and execute the action **Merge**.

## Close
Closing a trunk asset means that the trunk asset is getting a fully qualified identification by specifying a `groupId`, a `name` and `version` for it. The trunk asset will disappear after a successful close operation and will reoccur on further manipulations being made.

* Close Programmatically

    Execute the request `com.braintribe.model.platformsetup.api.request.CloseTrunkAsset` via one of the endpoints (e.g. webRPC or REST).

* Close via UI

    You can find all current trunk assets by clicking on the **Trunk Assets** node in the Platform Setup access. Select the asset to be merged and execute the action **Close**. A pop-up opens where the you must define the following values:
    * Group id: The group id which also defines the directory path in your repository (e.g. `my.group.id`)
    * Asset name: A lowercase dashed name (e.g. `my-asset-name`)
    * Version: The version of the asset (default is `1.0`)

## Add Dependencies

Adding and qualifying a dependency allows you to build up an ordered aggregation of assets and making it context sensitive. This also allows to incrementally extend the platform.

It's important to note that dependencies are transitively processed before the actual asset is being processed.

The action of adding an asset dependency enables you to further qualify the condition under which the dependency should be processed. You can either use a custom selector and/or a number of predefined and conjuncted selector shortcuts:
  * global setup candidate
  * design time only
  * runtime only
  * restricting to stages (e.g. dev, test, prod)
  * restricting to tags that support custom filtering

* Add Dependencies Programmatically

    Execute the request `com.braintribe.model.platformsetup.api.request.AddAssetDependencies` via one of the endpoints (e.g. webRPC or REST).

* Add Dependencies via UI
    
    In the Platform Setup access, click the **All Assets** node. Select the asset want to add a dependency to and execute the action **Add Dependencies**. A pop-up opens where you select the assets to be added as dependencies and further qualify them with the qualification settings.

## Install

Installing an asset means to transfer it as an artifact to the configured local maven repository in order to locally persist it and make it available for further setups. The asset will be installed with its current `resolvedRevision` which won't be increased in this operation.

Only non-trunk assets can be installed.

* Install Programmatically

    Execute the request `com.braintribe.model.platformsetup.api.request.TransferAsset` via one of the endpoints (e.g. webRPC or REST).

* Install via UI

    In the Platform Setup access, click the **All Assets** node. Select the asset to be installed and execute the action **Install**.

## Deploy

Deploying an asset means to transfer it as an artifact to a Maven-compatible repository to persist it and make it available for further setups. The asset will be deployed with its increased `resolvedRevision`.

You can deploy the assets of the following natures:
* `ManipulationPriming`
* `AssetAggregator`
* `ContainerProjection`
* `RuntimeProperties`

Only non-trunk assets can be deployed.

* Deploy Programmatically

    Execute the request `com.braintribe.model.platformsetup.api.request.TransferAsset` via one of the endpoints (e.g. webRPC or REST).

* Deploy via UI

    In the Platform Setup access, click the **All Assets** node. Select the asset to be installed and execute the action **Deploy**.

## Combined Operations

You can use several combined operations to increase the convenience of operations belonging together.

### Merge and Transfer

After an asset was successfully [merged](#merge), it can be transfered to the repository. This action is a combination of the Merge and Install actions.

* Merge and Transfer Programatically

    Execute the [```com.braintribe.model.platformsetup.api.request.MergeTrunkAsset```]<!--(javadoc:com.braintribe.model.platformsetup.api.request.MergeTrunkAsset)--> request via one of the endpoints (e.g. webRPC or REST).

* Merge and Transfer via User Interface

    [](asset://tribefire.cortex.documentation:platform-assets-doc/open-setup-ui.md?INCLUDE)

    You can find all current trunk assets by clicking on _Trunk Assets_ in the workbench. Select the asset to be merged and execute action _Merge and Transfer_.  

    Either check the __merge to predecessor__ box or select the __target asset__ to where the manipulations should be merged into.

    A pop-up opens where the transfer operation can be specified. Supported transfer operations:
        * [Deploy](#deploy)
        * [Install](#install)

### Close and Transfer

After an asset was successfully closed, it can be directly installed to the repository. This action is a combination of the Close and Install actions.

* Close and Transfer via UI
    
    In the Platform Setup access, click the **Trunk Assets** node. Select the asset to be merged and execute action **Close and Transfer**. A pop-up opens where the transfer operation can be specified. The following transfer operations are supported:

    * [Deploy](#deploy)
    * [Install](#install)