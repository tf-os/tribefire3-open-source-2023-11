# Connecting a PostgreSQL Database
In this tutorial you are connecting to an external data repository using an access. An access is a bridge between a model and the data stored in a specific repository.
The repository you want to connect is an empty PostgreSQL database. 

### Steps
To connect a PostgreSQL database, you must perform the following steps:
1. [Creating a PostgreSQL Connection](#creating-a-postgresql-connection)
2. [Creating a PostgreSQL Access](#creating-a-postgresql-access)
3. [Testing a PostgreSQL Access](#testing-a-postgresql-access)

### Prerequisites
A running PostgreSQL database. In the context of this procedure it does not matter if use a local or a dockerized PostgreSQL installation. In this procedure, we assume the username for the database is `cortex`, the password is also `cortex`, and the database runs on port `5432`. We will also use the `UserModel` as the metamodel for your access.

### Creating a PostgreSQL Connection

[](asset://tribefire.cortex.documentation:includes-doc/databases/postgres_conn.md?INCLUDE)

Continue with [Creating a PostgreSQL Access](#creating-a-postgresql-access).

### Creating a PostgreSQL Access 
To create a PostgreSQL Access:
1. In Control Center, on the Workbench panel, click **Custom Accesses**, and then click **New**.
2. Select **HibernateAccess** and configure it as follows:

   |Name|Value|Description|
   |----|-----|--------|
   |externalId|`myPostgreSQLAccess.local`|External ID of the access|
   |name|`myPostgreSQLAccess`|Internal name of the access|
   |metamodel|`UserModel`|The model this access operates on. |

3. In the same modal window, next to the connector label, click **Assign**. New view is displayed.
4. In the **DatabaseConnectionPool** view, select the **myPostgreSQLConnection** and click **Finish**. You can see that your database connection descriptor is added to the connection.
5. In the connection view, click **Apply**. Your access is opened in a new tab. Click **Commit**.
6. Right-click your access and click **Deploy**. Your new access is deployed and ready to be queried.
7. Continue with [Testing a PostgreSQL Access](#testing-a-postgresql-access)

### Testing a PostgreSQL Access
To test the PostgreSQL access:
1. In Control Center, on the Workbench panel, click **Custom Accesses**. The Custom Accesses tab is displayed.
2. Right-click your **myPostgreSQLAccess** and select **Switch To**. Explorer opens.
3. In Explorer, locate the **Quick Access...** search box at the top of the page. In the search box, type **User** and select the `User` type from the drop-down list. A new User tab is displayed.
4. Notice there is no data in the new User tab. That is because there are no records of the type `User` in your PostgreSQL database. Time to change that.
5. On the **User** tab, click **New**. A new modal window is displayed. In the modal window, provide the value for the `name` parameter.
6. In the modal window, click **Apply**. The modal window disappears and Explorer is displayed again. In Explorer, click **Commit**.
7. In Explorer, on the **Action** Bar, click **Refresh**. The `User` instance you added directly to the database schema is visible and available in tribefire.
