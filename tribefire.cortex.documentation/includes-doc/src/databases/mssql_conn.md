
To create an MSSQL connection:
1. Download a JDBC connector for MSSQL and place the `connector.jar` file in the in the `<TRIBEFIRE_INSTALLATION_DIRECTORY>/tribefire/host/lib` directory.
1. Start the tribefire Server and open Control Center.
2. If your tribefire Server was running when you copied the connector file, you must restart it.
3. In Control Center, on the Workbench panel, click the **Connections** entry point, and click **New**.
4. Select the **HikariCpConnectionPool** entry and configure it as follows:

   |Name|Value|Description|
   |----|-----|------|
   |externalId|`msSQLConnection.local`	|External ID of the connection|
   |name|`msSQLConnection`|Internal name of the connection|
5. In the same modal window, next to the **connectionDescriptor** label, click **Assign**. New view is displayed.
6. In the **DatabaseConnectionDescriptor** view, select the **MssqlConnectionDescriptor**, and configure it as follows:

   |Name|Value|Description|
   |----|-----|------|
   |Database|tribefire|The name of the database inside MSSQL that should be accessed.|
   |Driver|`MicrosoftJdbc4Driver`|The name of the driver used to connect to the database.|
   |Instance|SQLExpress|The name of the MSSQL service instance that should be accessed.|
   |Version|SqlServer2012|The version of MSSQL being used.|
   |Host|localhost|The location of the MSSQL server. If running on the same computer, the value will be localhost. Otherwise, it will be the IP address of the computer on which MSSQL is installed.|
   |Port|1433|The open port through which MSSQL will be accessed.|
   |User|`cortex`|The user name of the account which has permissions to access and manipulate database.|
   |Password|`cortex`|The password of the account which has permissions to access and manipulate database.|
5.  In the **DatabaseConnectionDescriptor** view, click **Apply**. You can see that your database connection descriptor is added to the connection.
8. In the connection view, click **Apply**. Your new **mSSQLConnection** is displayed in a new tab. Click the **Commit** button.
9. Right-click your new connection and click **Deploy**. Your connection is deployed.
10. Right-click your deployed connection and click **Test Connection**. If you did everything correctly, the **Connection successfully tested** message is displayed at the top of the screen.
11. Right-click your deployed connection and click **Synchronize DB Schema**. This loads the rough types into tribefire.