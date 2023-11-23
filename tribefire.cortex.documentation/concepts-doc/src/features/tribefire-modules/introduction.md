# Introduction to modules
Tribefire offers a modular architecture for the developed application, consisting of a single *platform* and multiple *modules*.

## High level overview

**Tribefire modules** are **units of functionality and (configuration) data**, and **Tribefire platform**,  simply said, is a **container that binds these modules** together and to the concrete environment where our application runs.

In other words, **Tribefire platform is a virtual layer** on top of the underlying runtime environment (web, command line, ...), which **provides an interface for adding functional components and configuration data** from Tribefire modules to the application, and **maps the communication** with the outside world **over environment-specific interfaces** (e.g. REST, WEB-RPC, command-line arguments) **to standard Tribefire APIs** used by the functional components internally.

**For now, we only offer** a **WEB** platform implementation, which allows our modules to run in a web (servlet) container. **In the future, we** also **want to offer** a **CLI** implementation, which would allow us to run (compatible) modules as a command line application.

Beyond extending the platform, the **modules may also define their own extension points** for other modules to build upon.

This architecture allows code to be packaged with a **write once deploy anywhere** quality, unless its nature binds it to a specific platform/environment of course.

> For example, one might create a service which offers encryption and decryption of a given resource (file/stream). Such functionality may be useful anywhere, from a command line tool to a highly scalable/elastic cloud service. When packaged as a Tribefire module, one may add such service to his application simply by adding a dependency to his [project artifact](application-structure.md#project), and without any further configuration have it accessible from the outside via REST or from the command line or any other interfaces supported by the chosen tribefire platform.

## More details

More information about Tribefire's modular architecture, development tools and modules themselves can be found in the following dedicated articles.

**[Tribefire application structure](application-structure.md)** describes fundamental types of components (artifacts) like `project`, `platform` and `modules` that make up our application.

**[Web application development lifecycle + Jinni examples](web-application-development-lifecycle.md)** answers the following questions:
* How to define what our application consists of, i.e. specify the chosen Tribefire platform and modules to include?
* How to run the application from your IDE?
* How to update the application by adding/removing modules, libraries or any other assets?
* How to prepare an installation package for server deployment?

**[Developing Tribefire modules - Basics](module-development.md)** describes how to write a Tribefire module. It explains how to bind custom code and configuration data to our application and how to define module's own extension points.

**[Developing Tribefire modules - Advanced](development/overview.md)** is an ever-growing collection of tips and tutorials on various aspects of module development.

**[Troubleshooting Tribefire modules (AKA Module Hell)](troubleshooting/overview.md)** provides a summary for known issues and pitfalls and hints on how to fix them.

**[Tribefire module classpath and components compatibility](module-compatibility.md)** provides and in-depth explanation of module compatibility. As different modules might bring conflicting dependencies (different versions of the same library), this article explains in which cases this is allowed, and how this affects the actual development.

**[Tribefire module structure (for extra curious)](module-structure.md)** describes the structure of a Tribefire module as an artifact. This is here just in case you want to understand each part of the artifact, but all relevant files are created by the Jinni CLI tool and you wouldn't usually need to change them.
