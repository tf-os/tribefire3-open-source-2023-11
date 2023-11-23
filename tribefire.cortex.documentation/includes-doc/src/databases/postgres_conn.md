
To create a PostgreSQL connection:
1. Download a JDBC connector for PostgreSQL and place the connector `.jar` file in the `<TRIBEFIRE_INSTALLATION_DIRECTORY>/tribefire/host/lib` directory.
   >For PostgreSQL connector download page, see: [JDBC Home](https://jdbc.postgresql.org/).
2. Start the tribefire Server and open Control Center.
   If your tribefire Server was running when you copied the connector file, you must restart it.
3. In Control Center, on the Workbench panel, click the **Connections** entry point, and click **New**.
4. Select the `HikariCpConnectionPool` entry and configure it as follows:

   |Name|Value|Description|
   |----|-----|------|
   |externalId|`myPostgreSQLConnection.local`	|External ID of the connection|
   |name|`myPostgreSQLConnection`|Internal name of the connection|
5. In the same modal window, next to the `connectionDescriptor` label, click **Assign**. New view is displayed.
6. In the DatabaseConnectionDescriptor view, select the `GenericDatabaseConnectionDescriptor`, and configure it as follows:

   |Name|Value|Description|
   |----|-----|-----|
   |driver|`org.postgresql.Driver`|Class name of the driver|
   |password|`cortex`|Password for the database schema you are connecting to.|
   |url|`jdbc:postgresql://localhost:5432/cortex`|URL to the database schema.|
   |user|`cortex`|User you use to connect to the database.|
7. In the **DatabaseConnectionDescriptor** view, click **Apply**. You can see that your database connection descriptor is added to the connection.
8. In the connection view, click **Apply**. Your new **myPostgreSQLConnection** is displayed in a new tab. Click the **Commit** button.
9. Right-click your new connection and click **Deploy**. Your connection is deployed.
10. Right-click your deployed connection and click **Test Connection**. If you did everything correctly, the **Connection successfully tested** message is displayed at the top of the screen.