# tribefire.extension.ldap

## Building

Run `./tb.sh .` in the `tribefire.extension.ldap` group.

## Setup

Run the following jinni command to setup a conversion server:

`./jinni.sh setup-local-tomcat-platform setupDependency=tribefire.extension.ldap:ldap-setup#3.0 installationPath=<Your Path>`

## Changelog

#### 4.4

* DEVCX-1466: Fixed the application of meta-data on the deployment model.