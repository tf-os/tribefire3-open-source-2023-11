To create a MySQL connection:
1. Download a JDBC connector for MySQL and place the connector .jar file in the <TRIBEFIRE_INSTALLATION_DIRECTORY>/tribefire/host/lib directory.
   >For MySQL connector download page, see: https://dev.mysql.com/downloads/connector/.
2. Start the tribefire Server and open Control Center.
   If your tribefire Server was running when you copied the connector file, you must restart it.
3. In Control Center, on the Workbench panel, click the **Connections** entry point, and click **New** .
4. Select the `HikariCpConnectionPool` entry and configure it as follows:

   |Name|Value|Description|
   |----|-----|------|
   |externalId|`mySQLConnection.local`	|External ID of the connection|
   |name|`mySQLConnection`|Internal name of the connection|
5. In the same modal window, next to the `connectionDescriptor` label, click **Assign**. New view is displayed.
6. In the DatabaseConnectionDescriptor view, select the `GenericDatabaseConnectionDescriptor`, and configure it as follows:

   |Name|Value|Description|
   |----|-----|-----|
   |driver|`com.mysql.jdbc.Driver`|Name of the driver|
   |password|`cortex`|Password for the database scheme you are connecting to.<br>This is the same password you provided when you created the database. |
   |url|`jdbc:mysql://localhost:3306/tribefiremysql`|URL to the database schema.|
   |user|`cortexMySQL`|User you use to connect to the database.<br>This is the same user you created during the creation of the database. |
7. In the **DatabaseConnectionDescriptor** view, click **Apply**. You can see that your database connection descriptor is added to the connection.
8. In the connection view, click **Apply**. Your new **mySQLConnection** is displayed in a new tab. Click the **Commit** button.
9. Right-click your new connection and click **Deploy**. Your connection is deployed.
10. Right-click your deployed connection and click **Test Connection**. If you did everything correctly, the **Connection successfully tested** message is displayed at the top of the screen.
11. Right-click your deployed connection and click **Synchronize DB Schema**. This loads the rough types into tribefire.