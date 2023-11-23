# Validating Data

You can perform basic data validation checks using the Data Validation cartridge.

## General

Out-of-the-box data validation functionality that comes with the Data Validation cartridge performs two basic checks:

* data retrieval check, i.e. whether data from a given access can be retrieved
* entity type check, i.e. whether the data returned matches the data defined in the entity type

## Prerequisites

* Deployed and synchronized Data Validation cartridge

## Validating Data

To validate data for a given access:

1. In Control Center, navigate to **Service Processors** and deploy the Data Verification Service Processor.
2. Create a new `DataVerification` instance.
3. Assign the access you want to validate data for to your new `DataVerification` entity instance and commit your changes.
4. Create a new transient `VerifyData` instance. `VerifyData` is a DDSA service request that runs in the background and performs the actual data validation.
5. Assign your `DataVerification` entity instance to your new `VerifyData` service and click **Apply**.
6. Execute the request and refresh Control Center. Data validation takes place in the background.
7. Keep refreshing until you see that an instance of `DataVerificationResult` is assigned to your `DataVerificationResult` entity instance.
8. Download and inspect the data verification report.
