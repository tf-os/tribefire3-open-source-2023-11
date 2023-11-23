# System Requirements

Your machine must meet the system requirements to smoothly run Tribefire.

## General

tribefire can be installed in a variety of systems. At its core, it is contained in a JEE Container, and all cartridges, clients, and core services deployed there. braintribe recommends that you use Tomcat as your container of choice, and our installer will automatically deploy tribefire within this container. However, it is also possible to use other solutions, if so required.

As tribefire is based on Java it can be deployed to any operating system.

## Hardware

Processors | Minimum 4 cores, recommended 8 cores (physical CPUs or virtualized cores)
RAM | Minimum 8GB, recommended 16 GB
HDD | Minimum 30 GB

## OS
As tribefire is a Java-based application, Java is required.

* Java - the Java Development Kit (JDK [](asset://tribefire.cortex.documentation:includes-doc/java_jdk_version.md?INCLUDE)) is required.
* Apache Tomcat - the tribefire installer is packaged with Apache Tomcat [](asset://tribefire.cortex.documentation:includes-doc/tomcat_version.md?INCLUDE)
  * Any Java EE Container can also be used

Using the tribefire installer, the following ports are configured by default – it is possible, however, to change these during the installation process. Please ensure that you open these ports on your firewall for access:

* HTTP - 8080
* HTTPS - 8443

## Browser

The following browsers are recommended:

[](asset://tribefire.cortex.documentation:includes-doc/browsers_version.md?INCLUDE)

