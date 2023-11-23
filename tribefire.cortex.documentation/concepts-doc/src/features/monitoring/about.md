# About

The Tribefire About service provides a UI which displays information about your host and instance.

## General

Tribefire About reads the host and Tribefire-specific information and displays them on a single page.

Among others, About provides the following information:

* servers
* connectors
* engines
* thread pools
* ssl
* web applications
* operating system
* Java version
* environment variables

## Downloadable Resources

In the Diagnostic Package section, you can select a diagnostic package and download it as a `.zip` file. You can select any of the following packages:

Package | Contains
----- | ------
Diagnostic Package | - logs <br/> - hot threads <br/> - thread dump <br/> - platform and process JSON information <br/> - all `healthz` statuses of all nodes in the cluster <br/> For more information, see [Health Check](health_check.md)
Extended Diagnostic Package | - contents from the above package <br/> - heap dump


> You must attach the diagnostic package when filing a support request.  
