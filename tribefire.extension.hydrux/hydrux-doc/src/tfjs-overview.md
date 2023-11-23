# Introducing tribefire.js

## Core library

`tribefire.js` is a powerful JavaScript library intended to be used as a cornerstone of a `Tribefre server` client application. It allows you to read a write server data, resolve configuration or call services using native tribefire paradigms and APIs like `persistence sessions`, `meta-data resolution` or `DDSA`.

It also comes with great `TypeScript` support in form of TS declaration files (`.d.ts`).

**[read more...](core-tfjs/core-tfjs-intro.md)**

## JS Libraries

Technically, `tribefire.js` is just a simple `JavaScript` file which you can copy into your project and use as is.

However, `Tribefire`'s development mantras are `modularity` and `separation of concerns`, i.e. create narrowly-focused (often reusable) components, build other components on top of them, and let your application only use the components it needs. It thus offers a project structure and tooling to facilitate such approach even for `client-side` development.

**[read more...](js-libs/js-lib-intro.md)**

## Hydrux

`Hydrux` is a `tribefire.js`-based framework for building modular client applications. `Hydrux` application consists of components, which can be configured/parameterized on the server side, and which are implemented in individual `modules`, rather than using the traditional monolithic structure. 

**[read more...](hydrux/hx-intro.md)**