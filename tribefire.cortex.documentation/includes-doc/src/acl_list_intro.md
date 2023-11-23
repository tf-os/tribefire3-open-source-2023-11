# Access Control List

Access control list (ACL) is the new implementation of instance-level security which supports two basic ways of controlling an access to an entity:

* configuring an owner with an unlimited access
* configuring various entries that might grant or deny certain operations (e.g. read/write or even modify ACL permissions) to certain roles

Basically, using the access control list you can grant or deny CRUD and custom operation permissions to different roles on instance level.

Besides that, there is a way to override all these checks by configuring the `Administrable` meta-data. Since this defines a universal override of the configured security, we will mostly ignore this when discussing concrete use cases to avoid being repetitive.

[](asset://tribefire.cortex.documentation:includes-doc/administrable_general.md?INCLUDE)

[](asset://tribefire.cortex.documentation:includes-doc/administrable_general_section.md?INCLUDE)

> To use ACL in your model, make sure to have the `access-control-model` as a dependency.