# JS Libraries Introduction

Tribefire `JS Library` is a an artifact which contains `JavaScript` components, usually compiled from `TypeScript` and built on `tribefire-js`.

> NOTE: This documentation will focus on `JS Libraries` written in `TypeScript`. Plain `JavaScript` modules are much simpler, we just write JS directly into the `src` folder.

> NOTE: The tooling and documentation assumes IDE used is `Visual Studio`.

## Basics

Tribefire `JS Libraries`

* are `Platform Assets` with `JsLibrary` nature.
* consist of `JavaScript` files which are `ES6` modules, i.e. from `html` they are imported as *type=module* and they can import each other. 
* support development workflow where the server serves the live source (or live output of the `TypeScript` compiler)

## How To Create

`JS Library` is a simple artifact which we can create with `Jinni`. Typically, it contains `TypeScript` code and the `JavaScript` generated from it by the compiler.

[Details...](./js-lib-how-to-create.md)

## File Structure

The file structure of a `JS Library` is a little convoluted, as it needs a trick to ensure relative paths for import in our `.ts` files are correct. But with proper `VS Code` settings the complexity is hidden in the IDE.

[Details...](./js-lib-file-structure.md)

## Live Deployment

Tribefire supports **live deployment** of `JS Libraries`, i.e. with proper configuration **the server** is able to **serve the code** directly **from your codebase**. This means you can **re-deploy** your app **by refreshing** the browser **without server restart**.

[Details...](./js-lib-live-deployment.md)
