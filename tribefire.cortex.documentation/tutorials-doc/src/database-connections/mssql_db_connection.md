# Connecting a MSSQL Database
Using Control Center you can create a connection to an MSSQL database, allowing you to manipulate the data stored there. Once you have created a connection, you can import the database schema and automatically generate models from these tables, which gives you the opportunity to create, edit, delete entries in the database, as well as being able to execute queries from within Control Center.


### Steps
To connect a MSSQL database, you must perform the following steps:
1. [Creating an MSSQL Connection](#creating-an-mssql-connection)
2. [Creating an MSSQL Access](#creating-an-mssql-access)
3. [Testing an MSSQL Access](#creating-an-mssql-access)

### Prerequisites
An MSSQL database prepared as per the instructions in [Preparing an MSSQL Database](#preparing-an-mssql-database)

#### Preparing an MSSQL Database
To prepare an MSSQL database for a connection to tribefire:
1. Download and install MSSQL from [here](https://www.microsoft.com/en-us/download/details.aspx?id=29062).
12. Log in to MSSQL server and do the following:
      1. Right click on the **Database** folder, found in the Object Explorer, and select **New Database**.
      2. Enter **tribefire** in the **Database name**, and click **OK**.
      3. Create a table called **person** and configure it as follows:

      | Column Name | Datatype| PK | NN | UQ | B | UN    | ZF     | AI     | G     |Is Identity
      | :-----------| :-------| :--- |:------| :-----| :----| :---| :----| :-----| :-------|---|
      | idperson  | `INT`  || Yes | Yes || No|No||No| No|| Yes | No|Yes|
      | firstName  | `VARCHAR(45)`  || No |  
      | lastName  | `VARCHAR(45)`  ||
      | dateOfBirth  | `DATE`  ||
      | placeOfBirth  | `VARCHAR(45)`  ||
      | nationality  | `VARCHAR(45)`  ||

13. In Microsoft SQL Server Management Studio, right-click **Security**, and select **New > Login**.
14. Create a new user account and configure it as follows:

    |Page|Property|Value|
    |----|----|------|
    |General|Login name|`cortex`|
    |General|SQL Server authentication|Yes|
    |General|Password|`cortex`|
    |General|Enforce password policy|Yes|
    |User Mapping|Map|tribefire|
    |User Mapping|Database role membership for: tribefire|db owner<br>public|
    |Server Roles|Server roles|public|

15. Open System Databases and do the following:
    1. Right-click **master** and click **Properties**.
    2. Select **Permissions**, and then click **Search**.
    3. In the **Select Users or Roles** window, click **Browse**.
    4. Select the **[public]** role. Click **OK**.
    5. Select the **public** role.
    6. Under Permissions for public, select **Deny** next to the **Select** permission.
    7. Click **OK**.

### Creating an MSSQL Connection

[](asset://tribefire.cortex.documentation:includes-doc/databases/mssql_conn.md?INCLUDE)

Continue with [Creating a MSSQL Access](#creating-an-mssql-access).

### Creating an MSSQL Access
To create a MSSQL Access:
1. In Control Center, on the Workbench panel, click **Custom Accesses**, and then click **New**.
2. Select **HibernateAccess** and configure it as follows:

   |Name|Value|Description|
   |----|-----|--------|
   |externalId|`msSQLAccess.local`|External ID of the access|
   |name|`msSQLAccess`|Internal name of the access|

3. In the same modal window, next to the connector label, click **Assign**. New view is displayed.
4. In the **DatabaseConnectionPool** view, select the **msSQLConnection** and click **Finish**. You can see that your database connection descriptor is added to the connection.
5. In the connection view, click **Apply**. Your access is opened in a new tab. Click **Commit**.
6. Right-click your new access and select **More -> Create Model from DB**. The **Create Model From DB Schema** modal window is displayed.
7. In the modal window, configure the new model as follows and click **Execute**:

   |Name|Value|Description|
   |-----|------|--------|
   |Name|`PersonModelMsSQL`|Name of the model|
   |Group Id|`custom.model`|Group ID of the model|

   The database schema is retrieved and a model is created.
8. Right-click your access and click **Deploy**. Your new access is deployed and ready to be queried.
9. Continue with [Testing an MsSQL Access](#testing-an-mssql-access).

### Testing an MSSQL Access
To test the MSSQL access:
1. In MSSQL insert data into the **person** table by running the following query:
   ```sql
   INSERT into dbo.person (firstName,secondName,dateOfBirth,birthplace,nationality)
      VALUES
    ('Robert', 'Smith', '1986-08-23','Manchester','British'),
    ('Lisa', 'Robertson','1984-09-26','London', 'British'),
    ('Hans', 'Hueber', '1979-04-30','Vienna', 'Austrian'),
    ('Maria', 'Goesser','1990-04-12','Graz','Austrian'),
    ('Peter','Stubbs', '1992-03-18','Detroit','American');
   ```
2. In Control Center, on the Workbench panel, click **Custom Accesses**. The Custom Accesses tab is displayed.
2. Right-click your **msSQLAccess** and select **Switch To**. tribefire Explorer opens.
3. In tribefire Explorer, locate the **Quick Access...** search box at the top of the page. In the search box, type **Person** and select the Person type from the drop-down list. A new Person tab containing the data is displayed.
