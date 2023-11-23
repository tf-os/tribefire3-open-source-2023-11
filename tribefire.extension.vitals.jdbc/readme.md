## JDBC DCSA Storage

### DCSA

`DCSA` is an extension of `CSA` for clustered environments (ones running multiple copies of the same `Tribefire` instance).

It uses `DCSA Shared Storage` to synchronize the state of the nodes in a cluster.

For a single node:
* **before read/write** operation the local state is update from the shared storage
* **every write** stores the operation performed in the shared storage

Note that write operations are not only new manipulations, but also stage management, e.g. renaming a stage

### Implementation via JDBC

This group contains a `JDBC` based implementation, built using the `jdbc-gm-support` library (`com.braintribe.gm`).

It creates two tables, one for the operations, and a separate one for resources, i.e. payloads of `CsaStoreResource` operations.

BTW: the second table also makes consolidation of the first table more efficient. Consolidation might include creating copies of some rows within the table, and there seems to be no way to copy a BLOB/CLOB from one row to another, other than downloading it to a client and uploading it again.

