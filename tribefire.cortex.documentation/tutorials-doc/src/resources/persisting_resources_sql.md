# Persisting Resources in SQL Database

You can change the default location of where your resources are persisted.

## Prerequisites

* Hibernate access pointing to an SQL database
* metamodel attached to your access must have `ResourceModel` as a dependency

## Persisting Resources in an SQL Database

1. Create an instance of the `SqlBinaryProcessor`, and attach the SQL connection that points to your SQL database to the `connectionPool` property.
   > You can also choose to overwrite the default `tableMapping` if you want to use custom table names.

2. Create an instance of the `UploadWith` metadata and attach your instance of the `SqlBinaryProcessor` to the `persistence` property. Click **Apply** and **Commit**.

3. Create an instance of the `StreamWith` metadata and attach your instance of the `SqlBinaryProcessor` to the `retrieval` property. Click **Apply** and **Commit**.

4. Navigate to **Custom Models** and select the model bound to your Hibernate access. Go to **Type Overrides** and create a new `GmEntityTypeOverride` instance.

5. Set the `declaringModel` property to `ResourceModel`, `entityType` to `ResourceSource`, and add your `UploadWith` metadata instance (the one where you attached the `SqlBinaryProcessor` to the `persistence` property) to the `metaData` list. Click **Apply and finish**, then **Apply** and then **Commit**.

6. Still in the model bound to your Hibernate access, create a `GmEntityTypeOverride` instance.

7. Set the `declaringModel` property to `ResourceModel`, `entityType` to `SQLSource`, and add your `StreamWith` metadata instance (the one where you attached the `SqlBinaryProcessor` to the `retrieval` property) to the `metaData` list. Click **Apply and finish**, then **Apply** and then **Commit**.
