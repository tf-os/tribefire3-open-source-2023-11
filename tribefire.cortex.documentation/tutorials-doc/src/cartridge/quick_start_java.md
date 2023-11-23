# Quick Start with Java

Follow these instructions to quickly set up an IDE and start coding in Java.

## Setting Up IDE

We provide a set of instructions which help you set up a default environment for developing with Java.

### Required Components

Component    | Version  
------- | -----------
Oracle Java JDK  | [](asset://tribefire.cortex.documentation:includes-doc/java_jdk_version.md?INCLUDE)
tribefire | 2.0 or higher  
Eclipse IDE for Java Developers   | Neon release or higher
Eclipse Tomcat Plugin | any

#### Oracle Java JDK

[](asset://tribefire.cortex.documentation:includes-doc/java_installation.md?INCLUDE)

#### tribefire

[](asset://tribefire.cortex.documentation:includes-doc/tribefire_quick_installation.md?INCLUDE)

#### Eclipse IDE for Java Developers

1. Navigate to [https://eclipse.org/](https://eclipse.org) and download the Eclipse IDE version Neon or higher.
2. Create a new workbench and, in Eclipse, go to **Help -> Eclipse Marketplace**.
3. Using **Eclipse Marketplace**, download any Eclipse Tomcat plugin.
4. In Eclipse, go to **Window -> Preferences -> Tomcat**.
5. Select Tomcat 8 and set the Tomcat home directory to the `host` directory of your tribefire installation.
6. Navigate to **Window -> Preferences ->Tomcat -> JVM Settings** and append the following to JVM parameters:

```xml
   -Djava.util.logging.manager=com.braintribe.logging.juli.BtClassLoaderLogManager
   -Djava.util.logging.conf.file=conf/logging.properties
```

7. Download the [`bt_eclipse_preferences.epf`](../files/bt_eclipse_preferences.epf) file from this page.
8. In Eclipse, navigate to **File -> Import -> General -> Preferences**, select the file you downloaded in the previous step and mark the **Import all** checkbox. Then, click **Finish**.

## Developing a Simple App Consuming Query API

In this procedure, you connect to a tribefire instance and use the out-the-box Java APIs.

All the dependencies you need for working with tribefire APIs are located in the tribefire Repository provided as part of any standard tribefire installation. Additionally, any custom models required as part of the development project are also deployed to the tribefire Repository.
This procedure makes use of Maven to create a new project and import the required dependencies. You can either use your local Maven instance or use the one shipped with Eclipse. If you decide to use a local instance, you must  have Maven installed and configured on your environment before attempting this procedure.

1. In Eclipse, navigate to **File -> New- > Maven Project**.
2. When asked, create a new project with the group ID of `com.braintribe.standalone.test` and project name **StandaloneTest**.
3. Follow the on screen instructions to finish creating your Maven project.
> You can also perform the steps above in the command prompt. <br/> <br/> 1. Open the command prompt and execute the following command to create a new project with the group ID of `com.braintribe.standalone.test` and project name StandaloneTest: `mvn archetype:generate -DgroupId=com.braintribe.standalone.test -DartifactId=StandaloneTest -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false` <br/> <br/> 2. In Eclipse, select **File -> Import -> Maven -> Existing Maven Projects**. <br/> <br/> 3. Using the **Browse** button, navigate to where your newly created Maven project resides. Select the project and click **Finish**. The project is then  added to your Package Explorer.

### Adding Required Dependencies
You can add Java API dependencies in a number of ways. Depending on how you want to manage dependencies, continue with one of the following:

* [With Maven](#with-maven).
* [With Maven and Devrock](#with-maven-and-devrock)
* [As an External Library](#as-an-external-library)

#### With Maven

The new Maven project contains only the basic structure for your app, including the source folders, base dependencies (the Maven project was generated with dependencies for Junit testing), and lastly the POM file. Before you can begin to develop your application, you must first add the required tribefire dependencies that provide the Java Client API and thus allow you to connect to tribefire-services.

You can find the POM file at the top level of your project.

So that the required tribefire dependencies can be downloaded to your Maven repository, a reference to the tribefire Repository is required. There are two ways of adding the tribefire Repository reference: either by editing your `settings.xml` (found at` .../m2/setting.xml`) or by adding it directly to your POM file.

The tribefire Repository, which can be browsed via the tribefire-services landing page, contains the required dependencies not only for connecting to tribefire but also to carry out other operations on entities and properties. In addition, if you have any business models that you have modeled in tribefire, you can deploy them to the repository, making them available for your application to use.

If you want to follow the pom.xml approach, the procedure is as follows:

1. Open the `pom.xml` file and add the tribefire Repository:

```xml
    <repositories>
        <repository>
            <id>central</id>
            <name>Braintribe Tribefire Repository</name>
            <layout>default</layout>
            <url>https://artifactory.server/artifactory/core-stable/</url>
        </repository>
    </repositories>
```

2. Now you need to add the dependencies required for your app. The easiest way to do it is to use the `tribefire-client default-deps` artifact, which has all the dependencies developers trying to use Tribefire Java API would need. Add the following dependency:

```xml
  <dependencies>
	<dependency>
	    <groupId>tribefire.cortex</groupId>
	    <artifactId>tribefire-client-default-deps</artifactId>
	    <version>[2.0,2.1)</version>
	</dependency>
  </dependencies>
```

3. Save the `pom.xml` file and ensure that the tribefire Repository you added in step 1 is online. The dependencies are downloaded to your local Maven repository and displayed in your Maven Dependencies archive shown in the package explorer. You can now proceed to [Connecting to tribefire](#connecting-to-tribefire)!

#### With Maven and Devrock
Having [Devrock](asset://tribefire.cortex.documentation:development-environment-doc/devrock/devrock.md) installed, you can set up your Maven project in the following way:

1. In a directory of your choice, run `jinni-create-parent-artifact`. This is required as all Devrock artifacts have a `parent` by default.
2. From the parent directory, build the parent artifact (for example by running `ant`).
3. Run `jinni create-library-artifact --artifactId myNewArtifact` in a directory of your choice. This creates the artifact you will use in Eclipse.
4. In Eclipse, select **File/Open Projects from File System...**.
5. Enter the path to the newly created `myNewArtifact` and click **Finish**. The artifact is now available as a project in Eclipse.
6. Open the `pom.xml` of `myNewArtifact` and add the following entries (as with Maven):


    ```xml
        <repositories>
            <repository>
                <id>central</id>
                <name>Braintribe Tribefire Repository</name>
                <layout>default</layout>
                <url>https://artifactory.server/artifactory/core-stable/</url>
            </repository>
        </repositories>
    ```

    > There are two ways of adding the tribefire Repository reference: either by editing your `settings.xml` (found at` .../m2/setting.xml`) or by adding it directly to your POM file (as above).

    ```xml
    <dependencies>
        <dependency>
            <groupId>tribefire.cortex</groupId>
            <artifactId>tribefire-client-default-deps</artifactId>
            <version>[2.0,2.1)</version>
        </dependency>
    </dependencies>
    ```

You can now proceed to [Connecting to tribefire](#connecting-to-tribefire)!

#### As an External Library
If you don't want to use Maven, you can add the depencencies as an external library:

1. [Download](https://artifactory.server/artifactory/webapp/#/artifacts/browse/tree/General/core-dev/tribefire/cortex/tribefire-client-default-deps) the dependency artifact `tribefire-client default-deps` and unzip to a location of your choice. 
1. In Eclipse, r-click your project and select **Properties**.
2. Go to **Java Build Path**, then select the **Libraries** tab.
3. Click **Add External JARs...**.
4. Navigate to the folder where you unzipped the dependency artifact and select all JARs.
5. Click **Apply**. Libraries containing the necessary dependencies are now added to your project. You can now proceed to [Connecting to tribefire](#connecting-to-tribefire)!

### Connecting to tribefire

You can now begin to create your application. The most important object when manipulating or reading entities and their properties is the `GmSession`. This is associated to an access and is what connects to the entities contained within. There are different types of sessions, but the key one for carrying out CRUD operations is the `PersistenceGmSession`, which are introduced below.

Connecting to tribefire services is done in two steps: first the object `GmSessionFactories` is used to provide a `PersistenceGmSessionFactory`. After which, the session factory is used to provide a valid `PersistenceGmSession` associated with a particular access.

The `GmSessionFactories` provides the location of the location of tribefire-services that should be accessed along with authorization credentials. If the information passed is validated, a session factory is returned.

> Alternatively, instead of providing authentication credentials, you can also provide the `GmSessionFactories` an existing User Session via tribefire's `Supplier<UserSession>`.

1. Create a new instance of `PersistenceGmSessionFactory`. The remote method receives a string defining the location of the tribefire-services that we wish to access, in this case one running on a local machine. The authorization credentials are then provided via the authentication method. Here, we provide a valid username and password; however, it is possible to use other credentials to provide authentication.

```java
    //connect using http via port 8080
    PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote("http://localhost:8080/tribefire-services").authentication("cortex", "cortex").done();
    //connect using https via port 8443
    PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote("https://localhost:8443/tribefire-services").authentication("cortex", "cortex").done();
```

> In the Java snippet above, the URL is passed to the remote method as a string. It is also possible to use the Java URL class to define the tribefire-services location.

2. Generate a new session using the session factory's `newSession()` method. The `newSession()` method requires the external ID of the access that you wish to associate your session with. You may create multiple sessions connected to different accesses, but each session can only be associated with one access.

In the example below, a new session is created by invoking the newSession method of the session factory, and passing the external ID "auth". This represents the Authentication and Authorization, a system access contained in tribefire.

```java
    PersistenceGmSession session = sessionFactory.newSession("auth");
```

> The `PersistenceGmSession` functions similarly to a Hibernate session, providing access to the persistence layer, allowing CRUD operations to be carried out on entities and their properties. It provides transactional support, allowing for the rollback or forward of changes within a `TransactionFrame`; only after the `commit()` method is called on the session object are any changes made permanent. Consult the JavaDoc documentation for more information.

### Testing

You have now connected to tribefire-services and generated a valid session. You can now being to implement your application. In the following example the session is queried to produce all instances of the User entity stored within the Authentication and Authorization access, since that is the access associated with the session, before all results are printed to the console.

1. Implement the query logic using the tribefire Query API and return all users from that query:

```java
    EntityQuery query = EntityQueryBuilder.from(User.class).paging(20, 0).done();
    List<User> users = session.query().entities(query).list();

    if (users != null) {
        for (User user : users) {
            System.out.println(user.getName());
        }
    }
```

2. Save your code and run it as a Java application. The result should be a list of users, similar to the one below:

```bash
    Nov 17, 2015 11:59:47 AM com.braintribe.model.generic.GmPlatformProvider
    INFO: Try to load platform.implementation from META-INF.
    locksmith
    steven.brown
    robert.taylor
    mary.williams
    cortex
    john.smith
```
