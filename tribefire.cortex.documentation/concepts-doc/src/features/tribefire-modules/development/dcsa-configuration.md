# DCSA Configuration

For Tribefire to use `DCSA` instead of just `CSA` we need to configure a `DCSA Shared Storage`. As always with Tribefire, we do this by providing a combination of a denotation instance and an expert bound from a module.

We'll use a `JDBC` based implementation, as there is currently no other implementation anyway. 

## Preparing denotation instance as YAML

First we prepare a YAML snippet describing `JdbcDcsaSharedStorage`, for example:

```yaml
!tribefire.extension.jdbc.dcsa.model.deployment.JdbcDcsaSharedStorage
project: "tribefire"
externalId: "dcsa.main.storage"
name: "The DCSA Shared Storage"
autoUpdateSchema: true
connectionPool: !com.braintribe.model.deployment.database.pool.HikariCpConnectionPool
  externalId: "dcsa.main.connectionPool"
  name: "The DCSA Connection Pool"
  connectionDescriptor: !com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor
    driver: "org.postgresql.Driver"
    password: "root"
    url: "jdbc:postgresql://localhost:5432/dcsa"
    user: "postgres"
```
> NOTE: The actual `name` and `externalId` values do not matter, they just must be set and no other deployables in our system can have the same `externalId`.

We can use this YAML in three different ways:

* [Static File](#static-file)
* [Jinni Argument](#jinni-argument)
* [Environment Variable](#environment-variable)

### Static File

In a **local setup** we can simply save the YAML as:
```${tribefire folder}/conf/default-dcsa-shared-storage.yml```. 

NOTE this file stays there even after we do a re-setup with `Jinni`.

### Jinni Argument

With `Jinni`, We can pass our snippet as a value of the `dcsaSharedStorage` property, which all the relevant setup commands support. `Jinni` then writes the `default-dcsa-shared-storage.yml`  (i.e. same as in static file example).

Q: How to pass a YAML snippet to `Jinni`?

This is straight forward when we use a snippet (file) for the entire setup command, e.g.:

```
jinni from-file my-local-setup.yml
```

When passing the arguments via command line, it is convenient to store the snippet in a separate file and reference it using the following syntax:

```
> jinni setup-local-tomcat-platform ... --dcsaSharedStorage @dss :dss from-file my-dcsa-shared-storage.yml
```

*This syntax defines a variable called 'dss' whose value is specified later, with a block starting with ':dss'.*

### Environment variable

There are also two variables:

* `TRIBEFIRE_DCSA_STORAGE` which specifies the YAML snippet directly
* `TRIBEFIRE_DCSA_STORAGE_LOCATION` which specifies the path to a YAML file containing the snippet

These variable have **higher priority than** the `default-dcsa-shared-storage.yml` file.

**Specifying both** variables results in an **error** and your server won't start ;)

## Adding the implementation

To be able to deploy the `JdbcDcsaSharedStorage` denotation our setup needs the corresponding module with the implementation, namely:

```xml
<dependency>
	<groupId>tribefire.extension.vitals.jdbc</groupId>
	<artifactId>jdbc-dcsa-storage-module-with-hikari</artifactId>
	<version>${V.tribefire.extension.vitals.jdbc}</version>
	<classifier>asset</classifier>
	<type>man</type>
	<?tag asset?>
</dependency>
```

> NOTE our configuration uses another deployable - `HikariCpConnectionPool`, so we use the `jdbc-dcsa-storage-module-with-hikari`. This exists for convenience, as `hikari` is the only connection pool implementation for now (and we don't plan to add another).

But if using `PostgreSQL`, as in the example above, we need the JDBC driver on the classpath, which we add as a [platform library](../application-structure.md#platform-libraries) like this:

```xml
<dependency>
	<groupId>tribefire.extension.jdbcdriver</groupId>
	<artifactId>postgresql-9</artifactId>
	<version>${V.tribefire.extension.jdbcdriver}</version>
	<classifier>asset</classifier>
	<type>man</type>
	<?tag asset?>
</dependency>
```

See the `tribefire.extension.jdbcdriver` group for other drivers.