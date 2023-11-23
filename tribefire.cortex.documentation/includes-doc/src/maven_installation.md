
1. Navigate to [https://maven.apache.org/download.cgi/](https://maven.apache.org/download.cgi), download, and install Maven 3.5.2 or later.
2. Make sure that the `PATH` system environment variable is set to the root directory of your Maven installation (one containing the `bin` folder). You can check it by opening a command prompt and running the `mvn --version` command.
3. Add the following system environment variables (we're using them later in the provided Maven settings file):

Name | Value
---  | ---
`MYPROJECT_LOCAL_REPOSITORY` |Path to your local Maven repository
`MYPROJECT_TRIBEFIRE_REPOSITORY_USER` |Your user name on the remote Tribefire repository
`MYPROJECT_TRIBEFIRE_REPOSITORY_PASSWORD`|Your password on the remote Tribefire repository
`MYPROJECT_TRIBEFIRE_REPOSITORY_NAME`| The name of a repository you want to download dependencies from, typically `core-stable`. For more information, see the section below.
`MYPROJECT_DISTRIBUTION_REPOSITORY_USER` |Your user name on the remote distribution repository (one where you would save your Tribefire artifacts, for example your company's remote repository). If you don't have access to such repository, ignore this variable.
`MYPROJECT_DISTRIBUTION_REPOSITORY_PASSWORD`|Password to the remote distribution repository
`MYPROJECT_DISTRIBUTION_REPOSITORY_NAME`|Name of the remote distribution repository where you will save your Tribefire artifacts. If you don't have access to such repository, you'll need to modify the settings.xml template to point to a local folder instead (explained below). In any case, you need to add this repository in `Control Center/CortexConfiguration` once Tribefire is installed, so that Tribefire knows where to save your artifacts.

> Note that the `MYPROJECT` part of the variable is a placeholder used to describe the project/company you work for, for example: `ACME_LOCAL_REPOSITORY` or `PROJECTX_TRIBEFIRE_REPOSITORY_USER`. Variable names do not matter as long as you are consistently using them in `settings.xml`.

4. Make sure you have the correct configuration in your `.m2/settings.xml` file:
	> If you have no access to a distribution repository, you need to replace it in the `setting.xml` file with a local remote repository. You can use the snippet below:

    ```xml
    <repository>
		<id>localRemoteRepository</id>
		<layout>default</layout>
		<url>LOCAL_FOLDER_PATH</url>
		<snapshots>
			<enabled>false</enabled>
		</snapshots>
		<releases>
			<enabled>true</enabled>
			<updatePolicy>always</updatePolicy>
		</releases>
	</repository>
    ```
   You can download the `settings.xml` template [here](asset://tribefire.cortex.documentation:includes-doc/files/settingsXML.xml).

   > Make sure to provide the correct values for `localRepository`, `localRemoteRepository`, `username`, and `password`.
