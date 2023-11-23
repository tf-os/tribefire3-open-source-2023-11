
1. Download a JDBC connector for Oracle and place the connector `.jar` file in the `TRIBEFIRE_INSTALLATION_DIRECTORY>/tribefire/host/lib` directory.
   >For the Oracle connector download information, see [Downloading JDBC Drivers](http://www.oracle.com/technetwork/database/features/jdbc/jdbc-drivers-12c-download-1958347.md)
2. Start the tribefire Server and open Control Center.
   >If your tribefire Server was running when you copied the connector file, you must restart it.
3. In Control Center, on the Workbench panel, click the **Connections** entry point, and click the **New** button.
4. Select the `HikariCpConnectionPool` entry and configure it as follows:

   |Name|Value|Description|
   |externalId|`myOracleConnection.local`	|External ID of the connection|
   |name|`myOracleConnection`|Internal name of the connection|

5. In the same modal window, next to the `connectionDescriptor` label, click **Assign**. New view is displayed.
6. In the DatabaseConnectionDescriptor view, select the `GenericDatabaseConnectionDescriptor`, and configure it as follows:

   |Name|Value|Description|
   |-------|------------|--------------|
   |password|`cortex`|Password for the database scheme you are connecting to.<br/>This is the same password you provided when you prepared the Oracle database. |
   |url|`jdbc:oracle:thin:@localhost:1521:XE`|URL to the database schema.<br/>The <b>jdbc:oracle:thin:</b> part of the URL is always the same. The localhost and 1521 parameters are self-explanatory (hostname and port). The <b>XE</b> parameter is the Service ID (SID) of your Oracle connection.|
   |user|`tribefireOracle`|User you use to connect to the database. This is the same user you created when you prepared the Oracle database. |

7. In the <b>DatabaseConnectionDescriptor</b> view, click <b>Apply</b>. You can see that your database connection descriptor is added to the connection.
8. In the connection view, click <b>Apply</b>. Your new <b>myOracleConnection</b> is displayed in a new tab. Click the <b>Commit</b> button.
9. Right-click your new connection and click <b>Deploy</b>. Your connection is deployed.
10. Right-click your deployed connection and click <b>Test Connection</b>. If you did everything correctly, the **Connection successfully tested** message is displayed at the top of the screen.