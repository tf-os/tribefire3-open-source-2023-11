# Creating a Smart Access

If you want your model to aggregate information coming from different sources, you need a smart access.

## General

The Smart Access is the delegating layer that controls the logic from which integration access data is read and written. This logic is provided by the integrated Smart Mapper which maps integration entities and properties to Smart Model equivalents, along with the relevant integration accesses through which the integration entities can be accessed.

For more information, see [Smart Access](asset://tribefire.cortex.documentation:concepts-doc/features/data-integration/smart_access.md)

### Prerequisites and Use-Case

In this tutorial, you will use the integration accesses you created as part of the [Connecting a MySQL Database](asset://tribefire.cortex.documentation:tutorials-doc/database-connections/mysql_db_connection.md) and [Connecting an Oracle Database](asset://tribefire.cortex.documentation:tutorials-doc/database-connections/oracle_db_connection.md) procedures to create an access which aggregates data from MySQL and Oracle. The two accesses you created contain the following entity types:

* `Person`
* `Company`

The smart access you create in this tutorial will work with the `AssigneeListModel` as its metamodel. The `AssigneeListModel` smart model will contain a list of `Person` entities with their respective `Company` entities assigned. The `Person` entity data will come from an MySQL database and the `Company` entity data will come from an Oracle database.

The use case can be summarized as follows:

1. Create database connections:
   1. [Connecting a MySQL Database](asset://tribefire.cortex.documentation:tutorials-doc/database-connections/mysql_db_connection.md)
   2. [Connecting an Oracle Database](asset://tribefire.cortex.documentation:tutorials-doc/database-connections/oracle_db_connection.md)
2. Create an aggregation `ParticipantModel` which will serve as a source to save the `Participant` entity and assign it to a newly created SMOOD access.
3. Create a `AssigneeListModel` combining all the entities and map the entities' properties.
4. Create a smart access.

## Creating a ParticipantModel

1. In Control Center, go to **Custom Models**, click **New** and configure your model as follows:

Name | Value
------ | ------
groupId | `custom.model`
name | `ParticipantModel`

2. Using Modeler, create a new `Participant` entity and add two properties to it:

Name | Data Type
------ | ------
personId | `integer`
companyId | `long`

   > The difference between the data types comes from how each database treats the ID property.

3. Make sure that your `Participant` has `StandardIntegerIdetifiable` as a supertype instead of the `RootModel`.
4. Click **Commit** to save your changes.

## Creating a SMOOD Access

1. In Control Center, go to **Custom Accesses** and click **New**.
2. Select **SmoodAccess** and configure it as follows:

   Name | Value | Description
   `externalId` | `smood.local` | External ID of your access.
   `name` | `smood` | Name of your access.
   `metaModel` | `ParticipantModel` | Model this access uses. Assign your newly created `ParticipantModel` here.

3. Click **Apply**, then **Commit**.
4. Right-click your new access and select **Deploy**.

## Changing the Company Model

Because the `Company` entity comes from an Oracle database and this might result in id translation errors, you must change it so that it has a `StandardIdentifiable` instead of the `RootModel` as a supertype. To change the model:

1. In Control Center, go to **Custom Models**.
2. Double-click the `Company` model to open Modeler.
3. In Modeler, click the `Company` entity and go to **Details -> superTypes**.
4. Unassign the `RootModel`, assign `StandardIdentifiable` as a super type, and commit your changes.

## Creating an AssigneeListModel

1. In Control Center, go to **Custom Models**, click **New** and configure your model as follows:

Name | Value
-----| -----
groupId | `custom.model`
name | `AssigneeListModel`
dependencies | `CompanyOracle` <br/> `PersonModelMySQL` <br/> `ParticipantModel`

   > Make sure to add all models you want to use in your model.

2. In Modeler, add the `Company` entity to the model.
   > Make sure the entity you add comes from your custom Oracle access. In other words, make sure the fully qualified name of the entity corresponds to the fully qualified name of the `Company` entity from `CompanyModel` you imported from the Oracle database.

3. In Modeler, add the `Person` entity to the model.
   > Make sure the entity you add comes from your custom MySQL access. In other words, make sure the fully qualified name of the entity corresponds to the fully qualified name of the `Person` entity from the `PersonModelMySQL` model you persisted in the MySQL database.

4. In Modeler, add the `Participant` entity to the model.
5. Create a `SmartParticipant` entity.
6. Drag and drop your mouse pointer from the `SmartParticipant` entity to the `Person` entity creating a single aggregation (has a) relation and name the relation `person`.
7. Drag and drop your mouse pointer from the `SmartParticipant` entity to the `Company` entity creating a single aggregation (has a) relation and name the relation `company`.

## Using Smart Mapper

Smart mapper allows you to create relationships between data structures coming from different data sources. To map your entities using smart mapper:

1. In Modeler, click the `Person` entity, navigate to the **Filter** tab, mark the **Mapper** checkbox, and click **Commit**.
2. On the `Person` entity, type the name of the entity you want to map the `Person` entity to in the **Choose mapping type** link. In this case, we want to map to the smart model `Person` entity to the `Person` entity 1:1. When you typed the name of the entity, click on its name. The mapping view is displayed.
3. In the mapping view, in the **IncrementalAccess** link, type the name of the access the mapping data comes from. In the case of the `Person` entity, this is the `mySQLAccess`, because the `Person` entity data is stored in a MySQL database.
4. Still in the mapping view, click **Assign all as is** and click **Commit**. This means you want to map the properties of your `Person` entity in the smart access to the properties of the `Person` entity coming from the MySQL database. Once you did that, click **Back**
5. Repeat steps 2 - 4 for the `Company` entity. Make sure to map the `Company` entity using the Oracle access, because that is where the `Company` entity data is stored.
6. Map the `SmartParticipant` entity to the `Participant` entity and open the mapping view.
7. In the mapping view, assign the SMOOD access you created to store the `Participant` entity data in the `IncrementalAccess` link.
8. Create a new join for the `company` property by selecting **Join** from the drop-down list that appears when you click the downward arrow button. The `company` property is the has-a relation between `SmartParticipant` and `Company` entities you created in Modeler. Map the `Participant.companyId` property to the C`ompany.id` property.
9. Create a new join for the `person` property by selecting **Join** from the drop-down list that appears when you click the downward arrow button. The `person` property is the has-a relation between `SmartParticipant` and `Property` entities you created in Modeler. Map the `Participant.personId` property to the `Person.id` property.
10. Assign the `id` property as is and commit your changes.

## Creating a Smart Access

To create a smart access:

1. In **Control Center**, navigate to **Custom Accesses**, click **New**, select the **SmartAccess** entry, and configure it as follows:

Name | Value
---- | -----
externalId | `access.smart`
name | `SmartAccess`
metaModel | `AssigneeListModel`
delegates | `smood` <br/> `mySQLAccess` <br/> `myOracleAccess`

2. Click **Apply**, then **Commit**, then **Deploy**.

## Testing a Smart Access

1. In **Control Center**, navigate to **Custom Accesses**, and switch to your smart access.
2. In Explorer, in the Quick Access search box, type `SmartParticipant`. A new, empty tab is opened.
3. In the **SmartParticipant** tab, click **New**. A new modal window is opened.
4. In the modal window, assign an instance of `Company` and an instance of `Person` to the respective properties, click **Apply**, and then **Commit**.
5. Your new instance of `SmartParticipant` is created.
