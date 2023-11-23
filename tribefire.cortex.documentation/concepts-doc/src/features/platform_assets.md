# Platform Assets
>Platform assets are Maven artifacts classified by a nature element which serve the purpose to setup a tribefire instance in an incremental and reproducible way. Depending on the nature, the assets can range from data to code.

## General
You may think of platform assets as puzzle pieces that let you add and remove functionality using a central platform asset repository. This functionality doesn't only include tribefire-built apps and components, but also the tribefire platform itself. The assets that make up the platform are called tribefire assets.

Using platform assets allows you to easily create and enrich your tribefire instance. Moreover, you keep track of changes in tribefire elements as each asset is versioned. 

You can use platform assets to store:
* tribefire elements, such as entity types, models, accesses, and others as manipulation files
* environment configuration files
* custom code as cartridges

With platform assets, you can package single tribefire elements as well as whole sets of models, experts, their configurations, cartridges with custom code, apps built on tribefire, and even the platform itself. 

>You can store platform assets in any Maven-compatible repository.

### Setup
During setup, an asset is resolved along with its transitive dependencies. Downloading and setting up your tribefire to work with particular assets is done using a command line tool called Jinni. 
>For more information on setting up platform assets, see [Setting Up Environment for Platform Assets](asset://tribefire.cortex.documentation:tutorials-doc/platform-assets/setting_up_platform_assets.md).

[](asset://tribefire.cortex.documentation:platform-assets-doc/asset_natures.md?INCLUDE)

[](asset://tribefire.cortex.documentation:platform-assets-doc/asset-deprecation.md?INCLUDE)
## Platform Setup Access
To support ease of management, we introduced the Platform Setup access, which allows you to manage your assets.

To open the Platform Setup access:

1. In Control Center, click the cogwheel icon in the top-right corner and select **Switch to -> Platform Setup**.

Node | Description | Available Actions
-----| ------------|------------------
All Assets | Displays all assets in a list. |  - Deploy <br/> - Install <br/> - Add Dependencies <br/> - Merge <br/> - Close <br/> - Merge and Transfer <br/> - Close and Transfer
Modified Assets | Displays assets which have unsaved changes in comparison to its deployed state in a remote repository. The changes might be: <br/> - Manipulations which are not deployed (e.g. trunk assets or merged manipulations from a trunk asset) <br/> - Modified Dependencies <br/> - Nature changes | - Add Dependencies <br/> - Merge <br/> - Close <br/> - Merge and Transfer <br/> - Close and Transfer
Trunk Asset | Displays the trunk asset. |  - Add Dependencies <br/> - Merge <br/> - Close <br/> - Merge and Transfer <br/> - Close and Transfer
Assets by Nature | Displays asset grouped by their nature | - Deploy <br/> - Install <br/> - Add Dependencies <br/> - Rename Asset

[](asset://tribefire.cortex.documentation:platform-assets-doc/asset_dependency_rules.md?INCLUDE)

[](asset://tribefire.cortex.documentation:platform-assets-doc/management.md?INCLUDE)

[](asset://tribefire.cortex.documentation:platform-assets-doc/asset-services.md?INCLUDE)
