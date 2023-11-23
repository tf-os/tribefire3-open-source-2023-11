# Connecting a MySQL Database
In this tutorial you are connecting to an external data repository using an access. An access is a bridge between a model and the data stored in a specific repository.
The repository you want to connect is a MySQL database where you will have a new schema with a table that contains the properties of a `Person` entity. You will use that MySQL table to import the `Person` data into tribefire and then make sure the manipulations done in tribefire are reflected in MySQL.
>The **Person** entity you create in this tutorial is not the same as the **Person** entity in the Demo Cartridge. <br/> <br/> The following procedure also covers creating a tribefire model from database - see [Creating a MySQL Access](#creating-a-mysql-access) for more information.

### Steps
To connect a MySQL database, you must perform the following steps:
1. [Creating a MySQL Connection](#creating-a-mysql-connection)
2. [Creating a MySQL Access](#creating-a-mysql-access)
3. [Testing a MySQL Access](#testing-a-mysql-access)

### Prerequisites

MySQL database prepared as per the instructions in [Preparing a MySQL Database](#preparing-a-mysql-database).

#### Preparing a MySQL Database 
To prepare a MySQL database for a connection to tribefire:
1. Download and install MySQL (version 5.7 or higher).
   >For MySQL installation and configuration instructions, see [MySQL documentation](https://dev.mysql.com/doc/) .
2. Using MySQL Workbench, create a new schema called **tribefiremysql**.
3. In the **tribefiremysql** schema, create a new table called **person** and configure it as follows:

   | Column Name | Datatype| PK | NN | UQ | B | UN    | ZF     | AI     | G     |
   | :-----------| :-------| :--- |:------| :-----| :---- | :---| :-----| :-----| :-------|
   | idperson  | `INT`  | Yes | Yes | No|No||No| No| Yes | No|
   | firstName  |` VARCHAR(45)`  || No |  
   | lastName  | `VARCHAR(45)`  ||
   | dateOfBirth  | `DATE ` ||
   | placeOfBirth  |` VARCHAR(45)`  ||
   | nationality  | `VARCHAR(45)`  ||

   >Make sure that the ID property has three properties checked: `PK` (Primary Key), `NN` (Non-Nullable, means that data must be entered in this column when creating a new entry), and `AI` (Auto-Increment, means a number is automatically added when creating a new entry).
   You can also use the following SQL snippet:

  ```sql
   CREATE TABLE `tribefiremysql`.`person` (
  `idperson` INT NOT NULL AUTO_INCREMENT,
  `firstName` VARCHAR(45) NULL,
  `lastName` VARCHAR(45) NULL,
  `dateOfBirth` DATE NULL,
  `placeOfBirth` VARCHAR(45) NULL,
  `nationality` VARCHAR(45) BINARY NULL,
  PRIMARY KEY (`idperson`));
  ```

4. Using the **Users and Privileges** link of MySQL Workbench, add a new user account to MySQL and configure it as follows:

   |Tab of the Users and Privileges View | Property | Value |
   |---|---|---|
   |Login | Login Name | `cortexMySQL` |
   |Login | Password | `cortex` |
   |Administrative Roles | DBManager<br>DBDesigner<br>BackupAdmin |Yes<br>Yes<br>Yes |

5. Continue with [Creating a MySQL Connection](#creating-a-mysql-connection)

### Creating a MySQL Connection

[](asset://tribefire.cortex.documentation:includes-doc/databases/mysql_conn.md?INCLUDE)

Continue with [Creating a MySQL Access](#creating-a-mysql-access).

### Creating a MySQL Access
To create a MySQL Access:
1. In Control Center, on the Workbench panel, click **Custom Accesses**, and then click **New**.
2. Select **HibernateAccess** and configure it as follows:

   |Name|Value|Description|
   |----|-----|--------|
   |externalId|`mySQLAccess.local`|External ID of the access|
   |name|`mySQLAccess`|Internal name of the access|

3. In the same modal window, next to the connector label, click **Assign**. New view is displayed.
4. In the **DatabaseConnectionPool** view, select the **mySQLConnection** and click **Finish**. You can see that your database connection descriptor is added to the connection.
5. In the connection view, click **Apply**. Your access is opened in a new tab. Click **Commit**.
6. Right-click your new access and select **More -> Create Model from DB**. The **Create Model From DB Schema** modal window is displayed.
7. In the modal window, configure the new model as follows and click **Execute**:

   |Name|Value|Description|
   |-----|------|--------|
   |Name|`PersonModelMySQL`|Name of the model|
   |Group Id|`custom.model`|Group ID of the model|

   The database schema is retrieved and a model is created.
8. Right-click your access and click **Deploy**. Your new access is deployed and ready to be queried.
9. Continue with [Testing a MySQL Access](#testing-a-mysql-access)

### Testing a MySQL Access
To test the MySQL access:
1. In Control Center, on the Workbench panel, click **Custom Accesses**. The Custom Accesses tab is displayed.
2. Right-click your **mySQLAccess** and select **Switch To**. <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.explorer}}">Explorer</a> opens.
3. In Explorer, locate the **Quick Access...** search box at the top of the page. In the search box, type **Person** and select the `Person` type from the drop-down list. A new Person tab is displayed.
4. Notice there is no data in the new Person tab. That is because there are no records of the type `Person` in your MySQL database. Time to change that.
5. On the **Person** tab, click **New**. A new modal window is displayed. In the modal window, provide the necessary information.
   ![](images/testing_mysql_access_step_5.png)
   >Note that the input fields in the modal window correspond to the columns of your MySQL database schema.
6. In the modal window, click **Apply**. The modal window disappears and Explorer is displayed again. In Explorer, click **Commit**.
7. In MySQL Workbench, inspect the **person** table. The instance of the entity type `Person` which you just created is listed as the first record.
   ![](images/testing_mysql_access_step_7.png)
8. Still in MySQL Workbench, add a new row to the **person** schema, effectively creating a new instance of `Person`. Make sure to apply the changes.
9. In tribefire Explorer, on the **Action** Bar, click **Refresh**. The `Person` instance you added directly to the database schema is visible and available in tribefire.
