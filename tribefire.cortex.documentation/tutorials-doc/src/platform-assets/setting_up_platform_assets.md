# Setting up Platform Assets
In this procedure you will create a local directory structure and use Jinni to create a setup for further platform asset development. 

> For information on handling artifact bias, see [Handling Artifacts with .pc_bias](asset://tribefire.cortex.documentation:development-environment-doc/handling_artifacts_with_pc_bias.md).

## Installing Jinni
[](asset://tribefire.cortex.documentation:includes-doc/jinni_setup.md?INCLUDE)

## Preparing a Local Environment
We recommend you create a single `tf-setups` directory where you will keep all your assets. The structure we propose allows you to easily identify and separate different setups. Furthermore, the information in the this setup can be used by Jinni to deduce information from the current working directory instead of getting it explicitly from CLI parameters.
> You only create the `tf-setups` folder. The other folders are created automatically by Jinni.

* `tf-setups`
    * some-group.project-x-asset#1.0
        * package
        * installation
            * conf
            * storage
            * plugins
            * runtime
    * some-group.project-y-asset#1.0
        * package
        * installation
             * conf
             * storage
             * plugins
             * runtime


## Creating a New Platform Asset Project

In this example, you will create a project called `my-project` with a major-minor version `1.0` which is in the group `tutorial`.

To create a new platform asset project:

1. Open the terminal and execute the following command:
    ```
    jinni create-project qualifiedName=tutorial:my-project#1.0 dependencies=tribefire.cortex.assets:tribefire-standard-aggregator#2.0
    ```
    The result of this operation is a newly created asset artifact `tutorial:my-project#1.0.1-pc` with the `AssetAggregator` nature having the ranged dependency to the `tribefire.cortex.assets:tribefire-standard-aggregator#[2.0,2.1)`. 
    This artifact is installed into the local Maven repository which is configured via Maven settings. The local Maven repository is sufficient to resolve this new project during a setup, but note that the project is not yet deployed to a shared repository that should be considered as the actual persistence.
    
2. Navigate to the `tf-setups` directory and run the following command:
    ```
    jinni setup-local-tomcat-platform project=tutorial:my-project#1.0
    ```
    Running this command results in an executable tribefire installation for on your local port 8080. The `qualifiedName` of the project is used to create the following directory structure:
    * tf-setups
        * tutorial.my-project#1.0
            * package
            * installation
                * conf
                * storage
                * plugins
                * runtime
                    * host
                        * bin
                            * catalina.bat
                            * catalina.sh
3. Navigate to `tf-setups/tutorial.my-project#1.0/installation/runtime/host/bin` and start tribefire by running the `catalina start` command. Once it starts, you can open the tribefire services landing page at `http://localhost:8080`.

## Configuring a Platform Asset Repository

[](asset://tribefire.cortex.documentation:platform-assets-doc/maven_repo_configuration.md?INCLUDE)

## Managing Users

It might be necessary to create new user accounts so that your collaboration is more effective. 

To manage users:
1. In the right upper corner of Control Center, click on the cogwheel and select **Switch to -> Authentication and Authorization**. This opens an access where you can manage user accounts.
2. Create users and assign them appropriate roles, for example a `tutorial-user` user with the `tf-admin` role.
    >For the information on default user roles, see [Security Considerations](asset://tribefire.cortex.documentation:concepts-doc/features/security_considerations.md).

## What's Next?

For more information on how to handle local artifacts, see [Working with Platform Assets](working_with_platform_assets.md).