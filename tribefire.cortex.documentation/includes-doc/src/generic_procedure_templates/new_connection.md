<!--MAKE SURE YOU CHANGE THE FIELD NAMES AND VALUES ACCORDING TO CONNECTION TYPE-->
To create a *** connection:
1. Download a <!--JDBC--> connector for <!--MSSQL--> and place the `connector.jar` file in the in the `<TRIBEFIRE_INSTALLATION_DIRECTORY>/tribefire/host/lib` directory.
1. Start the tribefire Server and open Control Center.
2. If your tribefire Server was running when you copied the connector file, you must restart it.
3. In Control Center, on the Workbench panel, click the **Connections** entry point, and click **New**.
4. Select the **** entry and configure it as follows:

   |Name|Value|Description|
   |----|-----|------|
   |externalId|
   |name|
5. In the same modal window, next to the **connectionDescriptor** label, click **Assign**.
6. In the **DatabaseConnectionDescriptor** view, select the **MssqlConnectionDescriptor**, and configure it as follows:

   |Name|Value|Description|
   |----|-----|------|
   |Database||
   |Driver||
   |Instance||
   |Version||
   |Host||
   |Port||
   |User||
   |Password||
5.  In the **DatabaseConnectionDescriptor** view, click **Apply**. You can see that your database connection descriptor is added to the connection.
8. In the connection view, click **Apply**. Your new **** is displayed in a new tab. Click the **Commit** button.
9. Right-click your new connection and click **Deploy**. Your connection is deployed.
10. Right-click your deployed connection and click **Test Connection**. If you did everything correctly, the **Connection successfully tested** message is displayed at the top of the screen.
11. Right-click your deployed connection and click **Synchronize DB Schema**. This loads the rough types into tribefire.
12. Continue with XXXXXXXXXXXXX.
