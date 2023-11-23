# Importing/Exporting Assemblies

You can import and export assemblies using certain predefined service requests and before actually synchronizing the data with your instance, you have the possibility of looking up what is inside a resource or a package.

## General

It may happen that you need to transfer data assemblies from one tribefire instance to another.
> We use the term **transient** to refer to entity instances which are not persisted and exist only in the client session.

This functionality is supported by the following actions:

* Write to resource
* Read from resource
* Export
* Export and write
* Read
* Import
* Read and Import

All the actions are available via Control Center.
>For instructions on how to perform the actions in Control Center, see [Importing/Exporting Assemblies in Control Center](asset://tribefire.cortex.documentation:tutorials-doc/control-center/importing_exporting_control_center.md).

## Write to Resource

This operation serializes (marshalls) an assembly in a specific format. The result of this processing is a newly created resource containing the serialized (marshalled) assembly.

Essentially, a snapshot of an assembly is created, which you can then inspect on another tribefire instance.  

> This request traverses the full assembly tree of the input assembly and serializes all reachable transitive references.

The available write to formats include:

* `.xml`
* `.json`
* binary

## Read from Resource

This operation allows you to open a previously saved snapshot of an assembly. That is an opposite operation that lets you deserialize (unmarshall) an assembly from a saved resource.

> The assemblies are transient, and are not stored in the cortex access.

If no explicit encoding type is provided, the encoding type is detected automatically based on the extension of the resource file. The result of this operation is a transient assembly deserialized (unmarshalled) from the resource file.

## Exchange Packages

An exchange package is a virtual container you can use to transfer data (assemblies) between tribefire instances.

The default export behavior takes a static list of entities (which you select in Control Center) and exports those selected entities. The export functionality follows all references of the top level entities down until a system instance is reached. A system instance is an entity which is a part of an initial tribefire installation (for example: `BaseModels`, System Accesses, etc). Since such instances are expected in any tribefire installation, there is no need to export them. They are marked as `externalReference` in the Exchange Package.

> By default, references to cartridges (for example, a cartridge assigned to a custom deployable) are not exported as external references on the target system. To import such an assembly properly, you must detect required cartridges on the target system in advance.

### Export

The export operation creates a snapshot of the exchange package. The snapshot contains transient instances which are detached from the current cortex session. This means all entity instances displayed as part of the export action exist only in client memory and are not persisted.

> Unusually large exchange packages may impact the performance of your tribefire instance.

As the instances are not persisted, manipulations done on them are transient. If you want to persist the changes made to the assemblies supplied by an exchange package, you must use the Export and Write action.

The export operation does not automatically export the required models for your exported assembly. However, the notification list provides information on which models are required in the target access. This allows you to directly export another exchange package containing the required models.

### Export and Write

This operation is a combination of the Export action and the Write to Resource action.

The Export and Write action allows you to create a `.zip` file with a snapshot of the exchange package. Once created, you can download the package from Control Center.

### Read Action

The Read operation is used to deserialize (unmarshall) an exchange package to obtain a transient instance of the package's contents. As exchange packages are always `.zip` files, before the assemblies are unmarshalled, the package is decompressed.

> The resulting assembly is a transient instance of exchange package. <br/> The entities you see in Control Center are transient instances of the content of the exchange package which is not synced into the current access. You can inspect the content of that exchange package and import it later using the Import action.

### Import

The Import operation synchronizes the content of an exchange package with the target access.

### Read and Import

This operation is a combination of the Read action and the Import action.

The Read and Import operation allows you to import and synchronize an exchange package, which means an exchange package is decompressed, the content is unmarshalled and the resulting instances are imported into the target access.
