# Adding Static Resources to Cartridges

You can package static resources with your cartridge. Such resources can be images, JavaScript files, .zip files, and other files.

## General

You can directly reference these resources in a Web Terminal (Servlet) to style the page or to store `.js`, `.html`, or `.css` files. Also, you can store icons and other holding resources to be used in a workbench access.

## Adding Static Resources to a Cartridge

To add static resources to your cartridge you must place these files in the `static` folder as follows.
In your cartridge directory, place your files in the `src/main/webapp/WEB-INF/static` folder. For example, `MySimpleCartridge\src\main\webapp\WEB-INF\static`.

> If the `static` folder doesn't exist, you must create it.

## Accessing Static Resources

Cartridges, by definition, should not be accessed directly. Instead, only the `tribefire-services` endpoint should be accessible (it is, in fact, the only accessible endpoint in a cloud installation).

To access static content, you need to follow the below URL:

`<tf-services-url>/static/<cartridge-external-id>/<path within the WEB-INF/static folder>`

For example, see the URL below:

[https://tribefire:cortex@documents20-dev-documents.staging.tribefire.cloud/services/static/tribefire.extension.shiro.shiro-cartridge/login-images/google.png](https://tribefire:cortex@documents20-dev-documents.staging.tribefire.cloud/services/static/tribefire.extension.shiro.shiro-cartridge/login-images/google.png)

This example shows the image stored in the Shiro Cartridge under `WEB-INF/static/login-images/google.png`.
