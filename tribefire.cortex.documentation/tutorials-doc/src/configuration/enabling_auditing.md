# Enabling Auditing

Auditing allows you to store the data about manipulations done on instance and model level.

## General

As opposed to logging, which records the information about the state of your tribefire instance (what is happening ), auditing allows you to track the manipulations done on entity instances and models (who did what) of a particular access. You can enable auditing on different accesses at the same time. Even though the audit logs are aggregated from different sources, they are all available in the same place, which is the Audit access view of Control Center.

By default, the instantiation, modification, and deletion of models and entities are audited.

> Users with the roles **noUI** and **tf-internal** are not being audited.

## Enabling Auditing for an Access

In this example, we use a newly created access.

1. In Control Center, navigate to the **Custom Accesses** entry point.
2. Right-click the access you want to enable audit on, and click **More -> Setup Aspects**. This creates a default set of aspects for your access.
   > If your access already has some aspects assigned, expand the `aspectConfiguration` property, right-click the aspects list and add the Default Audit Aspect.
3. Make sure all aspects are deployed and deploy (or redeploy) your access by right-clicking the access and clicking **Redeploy**.
4. Right-click your access and select **Switch to**. Explorer opens.
5. In Explorer, create a new instance or modify an existing instance. Once done, commit the changes.
6. Go to Control Center, and in the **Settings Panel**, click the cogwheel icon, and select **Switch to -> Audit**. The Audit access explorer view opens.
7. In the **Audit** access explorer view, click the **All Records** entry point.
   > Note that the only manipulations done in tribefire are audited. If you use a Hibernate access and modify entities directly in an external database, those manipulations are not audited.
