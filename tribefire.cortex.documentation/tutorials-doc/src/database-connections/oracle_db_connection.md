# Connecting an Oracle Database
In this tutorial you are connecting to an external data repository using an access. An access is a bridge between a model and the data stored in a specific repository.
The repository you want to connect is an empty Oracle database where you will persist your new `Company` entity and make sure the manipulations done in tribefire are reflected in Oracle.

>In this procedure, you save your model in a database. For information on how to create a Tribefire model from a database, see [Connecting a MySQL Database](mysql_db_connection.md).

### Steps
To connect an Oracle database, you must perform the following steps:
1. [Create an Oracle Connection](#creating-an-oracle-connection)
2. [Create an Oracle Access](#creating-an-oracle-access)
3. [Test the Oracle Access](#testing-an-oracle-access)

### Prerequisites

Oracle database prepared as per the instructions in **Preparing an Oracle Database** section of this document.

#### Preparing an Oracle Database
To prepare a Oracle database for a connection to tribefire:
1. Download and install the Oracle database. For the purposes of this tutorial, we recommend to use the Express Version.
   >For installation and configuration instructions, see [Oracle DB Documentation](http://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.md)
2. Download and install Oracle SQL Developer.
3. Using the Oracle command line, create a database user as follows:

   | Name | Value|
   |-------|--------|
   | username | tribefireOracle |
   | password | cortex |
   | roles |`CREATE SESSION, ALTER SESSION, CREATE DATABASE LINK,CREATE MATERIALIZED VIEW, CREATE PROCEDURE, CREATE PUBLIC SYNONYM,CREATE ROLE, CREATE SEQUENCE, CREATE SYNONYM, CREATE TABLE,CREATE TRIGGER, CREATE TYPE, CREATE VIEW, UNLIMITED TABLESPACE`|

   >For more information on creating users in Oracle, [Creating a Database User](https://docs.oracle.com/cd/E17781_01/admin.112/e18585/toc.htm#XEGSG110).
4. Using Oracle SQL Developer, create a database connection for the `tribefireOracle` user, and configure it as follows:

   | Name | Value|
   |-------|--------|
   | Connection Name | `oracleLocal`|
   | Username | `tribefireOracle` |
   | Password |`cortex`|
   |Hostname|`localhost`|
   |Port|`1521`|
   |SID|`xe`|

5. Continue with [Creating an Oracle Connection](#creating-an-oracle-connection)

### Creating an Oracle Connection

[](asset://tribefire.cortex.documentation:includes-doc/databases/oracle_conn.md?INCLUDE)

Continue with [Creating an Oracle Access](#creating-an-oracle-access).

### Creating an Oracle Access
1. In Control Center, on the Workbench panel, click <b>Custom Accesses</b>, and then click <b>New</b>.
2. Select **HibernateAccess** and configure it as follows:

   |Name|Value|Description|
   |-------|------------|--------------|
   |externalId|`myOracleAccess.local`|External ID of the access.|
   |name|`myOracleAccess`|Internal name of the access.|
3. In the same modal window, next to the **connector**  label, click **Assign**. New view is displayed.
4. In the **DatabaseConnectionPool** view, select **myOracleConnection** and click **Finish**. You can see that your database connection descriptor is added to the connection.
5. In the **connection** view, click **Apply**. Your access is opened in a new tab. Click **Commit**.
6. Continue with [Testing an Oracle Access](#testing-an-oracle-access)

### Testing an Oracle Access
You have performed all of the steps to be able to manipulate your Oracle database data in tribefire. Once your data is in tribefire, you can expose it to various applications using Java, REST, or JavaScript. Before you do that, you must create your model, assign it to the access, persist the model in the database, and test if the communication between tribefire and the database works in both directions.
#### Creating a Company Model
To create a simple model:
1. In Control Center, go to **Custom Models** and click **New**.
2. Create a **CompanyOracle** model, which has a single Company entity with the following specification:

   |Property|Type|
   |-----|-------------|
   |name|`String`|
   |revenue|`double`|
   |location|`String`|

#### Assigning a Custom MetaModel to Your Oracle Access
A metamodel of an access defines the data the access operates on.
To assign a custom model as a metamodel to your access:
1. In Control Center, navigate to **Custom Accesses**.
2. Right-click **myOracleAccess** and select **Edit**.
3. In the new modal window, next to the **metaModel** label, click **Assign**. New view is displayed.
4. Select **CompanyOracle** model and click **Finish**.
5. Click Apply and Commit.
6. Right-click your access and select Redeploy to redeploy it.
>Redeploying your access with the new metamodel assigned results in the creation of a database schema reflecting your model.

#### Testing Your Oracle Access
To test the Oracle access:
1. In Control Center, on the Workbench panel, click **Custom Accesses**. The Custom Accesses tab is displayed.
2. Right-click your **myOracleAccess** and select **Switch To**. Explorer opens.
3. In Explorer, locate the **Quick Access...** search box at the top of the page. In the search box, type **Company** and select the `Company` type from the drop-down list. A new Company tab is displayed.
4. Notice there is no data in the new Company tab. That is because there are no records of the type `Company` in your Oracle database. Time to change that.
5. On the **Company** tab, click **New**. A new modal window is displayed. In the modal window, provide the necessary information.
   ![](testingOracleConnection.png)
   >The input fields in the modal window correspond to the columns of the generated Oracle database schema.
6. In the modal window, click **Apply**. The modal window disappears and Explorer is displayed again. In Explorer, click **Commit**.
7. In Oracle SQL Developer, inspect the **COMPANY** table. The instance of the entity type `Company` which you have created is listed as the first record.
   ![](OracleDBTable.png)
   This means that the communication between tribefire and your database is working correctly. All manipulations done using the **myOracleAccess** are persisted in your Oracle database.
8. Still in Oracle SQL Developer, add a new row to the **company** schema, effectively creating a new instance of `Company`. Make sure to apply the changes.
9. In Explorer, on the **Action** Bar, click **Refresh**. The `Company` instance you added directly to the database schema is visible and available in tribefire.
   ![](TestingOracleAccess.png)
