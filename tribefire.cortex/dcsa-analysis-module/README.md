# dcsa-analysis-module

**Module for DCSA analysis**

This module helps you to download the content of the DCSA Shared Storage of a running tribefire and import it into another TF where this module is included. 

### General

### Downloading DCSA content from a running TF

**Simple way**: Use the *res/download-shared-storage.sh.txt* script. Read the text inside for instructions! The *txt* suffix is here to avoid accidental execution from IDE.

**General way**: You can download the Diagnostic Package using the GetDiagnosticPackage service request. This is exposed via REST, e.g.:

    http://localhost:8080/tribefire-services/api/v1/diagnostic?includeHeapDump=false&includeLogs=false&projection=diagnosticPackage&download-resource=true

Inside the zip there is a zip file prefixed with *shared-storage-cortex*. Extract this content to an empty directory.

### Importing the shared storage to a local file

Assuming this module is part of your setup, you can use *FillCortexDcsaSharedStorage* request in Control Center (for access cortex) to do the import, passing the folder with the shared storage files as a parameter.

Just to avoid possible problems, start your TF with an empty shared storage before the import.

## bindHardwired()

Binds a service processor:

- | -
-|-
Name | `Cortex DCSA SS Filler`
ExternalId | `cortexDcsaFiller`
Domain |  `cortex` 
Request Type | `FillCortexDcsaSharedStorage`
Service API Model | `tribefire.cortex:dcsa-analysis-service-model`