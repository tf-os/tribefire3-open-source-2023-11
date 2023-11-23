# Setting up IDE for Cartridge Development

You must make sure your IDE can manage all necessary dependencies before you start developing cartridges.

## General

<!-- test -->

This tutorial provides an end-to-end guidance on how to set up your IDE for cartridge development using the out-of-the-box **Simple Cartridge** as a framework. When you finish the procedure, you'll be able to start modifying the cartridge in Eclipse, and see your changes on a live Tomcat deployment.

## Prerequisites

Install the components mentioned below before proceeding.

### Base Components

Component    | Version
------- | -----------
Java 8  | JDK [](asset://tribefire.cortex.documentation:includes-doc/java_jdk_version.md?INCLUDE), or<br>OpenJDK [](asset://tribefire.cortex.documentation:includes-doc/java_openjdk_version.md?INCLUDE)

Apache Maven | 3.5.2 or later

> For the setup instructions for the base components, please refer to [Quick Installation](asset://tribefire.cortex.documentation:development-environment-doc/quick_installation_devops.md).

### Development Components

Component    | Version
------- | -----------
Eclipse IDE for Java Developers | 2018-09 (4.9.0) or later
Eclipse Tomcat Plugin | [](asset://tribefire.cortex.documentation:includes-doc/tomcat_version.md?INCLUDE)

Source code project **simple-cartridge**<br>from the Tribefire **Enablement Cartridges (Maven variant)** <br>(test and demo purposes)  | `artifacts` package version [](asset://tribefire.cortex.documentation:includes-doc/artifacts_version.md?INCLUDE)
.<br>You can find the link to the enablement cartridges source code on the [Resources](https://artifactory.server/artifactory/list/core-stable/tribefire/extension/enablement-maven/artifacts/) page.

## Simple Cartridge Setup

1. Make sure to have proper Maven configuration including `settings.xml` and environment variables as described in [Quick Installation](asset://tribefire.cortex.documentation:development-environment-doc/quick_installation_devops.md).
2. On your file system, create a new folder `enablement-cartridges` at a location of your choice and unzip the downloaded source code package there.
3. Navigate to the `artifacts` directory and open a terminal.
4. Perform a Maven build with the following command:

```bash
mvn clean install
```

> This build may take a few minutes when run for the first time.

5. Perform the Tribefire setup for the Simple Cartridge with the following command:

```bash
	jinni setup-local-tomcat-platform setupDependency=tribefire.extension.enablement-maven.simple:simple-cartridge-setup#2.0 installationPath=yourInstallationPath deletePackageBaseDir=true
```

Make sure to replace `yourInstallationPath` with the path to your actual installation directory. Example (Linux and macOS):

```bash
	jinni.sh setup-local-tomcat-platform setupDependency=tribefire.extension.enablement-maven.simple:simple-cartridge-setup#2.0 installationPath=$HOME/tribefire-setups/simple-cartridge deletePackageBaseDir=true
```

6. Start up Tribefire and inspect whether the Simple Cartridge functionality was set up correctly. On the Tribefire Services landing page, you should be able to find the following items:
* Models:
	* `simple-data-model`
	* `simple-deployment-model`
	* `simple-service-model`
* Service Domains:
	* `SimpleInMemoryAccess`
* Cartridges:
	* `simple.cartridge`
* Web Terminals:
	* `SimpleWebTerminal`

## Eclipse IDE Settings

As we are using Eclipse as our IDE of choice, the following settings are specific to that particular IDE. If you want to use another IDE, make sure to set the equivalent settings to their correct values.

### Eclipse Preferences

We strongly recommend that you use our prepared preferences file. To import it:
1. Download the file [bt_eclipse_preferences.epf](../files/bt_eclipse_preferences.epf) from this page.
2. In Eclipse, navigate to **File -> Import -> General -> Preferences**, select the file you downloaded in the previous step and mark the **Import all** checkbox. Then, click **Finish**.

### Maven Settings

1. In Eclipse, go to **Window -> Preferences -> Maven**.
2. Click **Installations -> Add**.
3. In the new window navigate to the root folder of your Maven-installation and click `Open`.
4. Click **Apply**.
5. Click **User Settings**. Inspect whether the following requirements are met:
* The `settings.xml` file is found.
* The path to **Local Repository** is parsed correctly.

## Tomcat Settings

1. In Eclipse, go to **Help -> Eclipse Marketplace**.
2. Using Eclipse Marketplace, install the **Eclipse Tomcat Plugin**, version [](asset://tribefire.cortex.documentation:includes-doc/tomcat_version.md?INCLUDE)
.
> If you experience errors during the download using Eclipse Marketplace, you can download the plugin directly from [http://tomcatplugin.sf.net/](http://tomcatplugin.sf.net/).
3. In Eclipse, go to **Window -> Preferences -> Tomcat**.
4. Select **Tomcat 8** and set the **Tomcat home** directory to the `host` directory of your Tribefire installation.
5. Click **Apply**.
6. Expand the **Tomcat** entry in the Eclipse preferences and select **JVM Settings**.
7. Using the drop down list on the top, make sure to have the correct **JRE** selected.
8. Click **Add** next to the **Append to JVM Parameters** field.
9. Enter the value `-Djava.util.logging.manager=com.braintribe.logging.juli.BtClassLoaderLogManager` and click **OK**.
10. Click the **Add** button again and enter the value `-Djava.util.logging.config.file=conf/logging.properties`, then click **OK**.
11. Click **Apply and Close**.

## Maven Project Import

1. In Eclipse, right-click within the **Package Explorer** panel and select **Import**.
2. Choose **Maven -> Existing Maven Projects**.
3. In the new window, click **Browse** and navigate to `path/to/enablement-cartridges/artifacts/simple-cartridge-parent`
4. Click **Open** and **Finish**. The included artifacts will now imported as Eclipse-projects. This may take a while.
> After the import and build of the projects, Eclipse will throw two errors about missing artifacts at the `pom` file of the `simple-cartridge-setup` project. This is a known issue that won't harm you during the development.

<!-- ## Live Deployment

Having set up the project, you can now extend it with your custom code. If you want to try out your implementation, you have the following options:

* Use live deployment directly from Eclipse and see your changes immediately. This is the recommended option.
* Build the project with Maven in the terminal again, perform the setup and start Tribefire.

To set up the `simple-cartridge` project for live deployment, perform the following steps:

1. Undeploy the application from the Tomcat server:
	1. In your web browser, navigate to [http://localhost:8080/manager](http://localhost:8080/manager).
	2. When asked, enter the credentials: **cortex**/**cortex**
	3. In the Applications table, locate the entry `/tribefire.extension.enablement-maven.simple.simple-cartridge`.
	3. Click **Undeploy** in this row.
2. Shut down Tribefire in the Terminal by simply pressing **CTRL + C**.
3. Announce the `simple-cartridge` Eclipse project as Tomcat web application:
	1. In Eclipse, right-click on the root entry of the `simple-catridge`-project.
	2. Go to **Tomcat project** and select **Update context definition**.
4. Startup Tomcat-server from Eclipse via respective button in the toolbar: {%include inline_image.html file="tomcat_logo.png"%} -->

## Development

The Simple Cartridge is now ready to be extended with custom implementations. Most of the changes will be visible on Tribefire immediately. Other, more fundamental, changes may require a restart of the Tomcat server. Remember to run Tomcat in the Eclipse context. 

## Build

After you are done with your development, simply perform the Maven build again:

```bash
mvn clean install
```

## What's Next?
When you have your IDE ready for cartridge development, it is time to start developing cartridges.
> For more information, see [Developing Cartridges](developing_cartridges.md).