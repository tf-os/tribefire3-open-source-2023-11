# JS Library Artifact Structure

## Creating new JS Library

Let's create an example `TypeScript` module called `my-js-lib` in `tribefire.tutorial.js` group with:
```cli
> jinni create-ts-library my-js-lib
```

And we get:

```filesystem
my-js-lib
    vscode/
        settings.json
    src/
    ts-src/
        index.ts
    .gitignore
    build.xml
    js-project.yml
    pom.xml
    tsconfig.json
```
## Adding tribefire.js dependency

As we want to show a little more, let's also add a dependency to `tribefire.js` API, i.e. add this to your `pom.xml`:
```xml
<dependency>
    <groupId>tribefire.js</groupId>
    <artifactId>tf-js-api</artifactId>
    <version>${V.tribefire.js}</version>
</dependency>
```

## Assembling the dependencies

This step **is crucial** before we start developing. It ensures our project sees the relevant dependencies, in this case all the `APIs` and `Models` integrated in `tribefire.js`.

Let's first move inside this new folder:
```cli
> cd my-js-lib
```

And let's prepare our dependencies
```cli
>  jinni assemble-js-deps
```

We should see an **output** like this:
```plaintext
Assembling JS library dependencies for project in ...\tribefire.tutorial.js\my-js-lib
resolved artifacts:  53
resolved libs:  52
linked libs:  52
ignored libs:   0
```

> NOTE that actual numbers might be different.

The filesystem should look something like this:
```filesystem
my-js-lib
    .vscode/
        settings.json
    lib/
        src/        // -> ../src
        ts-src/     // -> ../src
        com.braintribe.common.async-api-2.0~    // -> ~/.devrock/js-libraries/com.braintribe.common.async-api-1.0.66
        ...
    src/
    ts-src/
        index.ts
    sources/        // -> ./lib
    asset.man
    build.xml
    js-projtect.yml
    pom.xml
    tsconfig.json
```
and the `// -> path` parts denote that given folder is a symbolic link to given path.

## Understanding the file structure

We can see there are many symbol links to other folders, which is a trick to ensure relative paths for import in our `.ts` files are correct.

[Details...](./js-lib-file-structure.md)

## Opening the project in VS code

When we open this folder in `VS code`, thanks to `.vscode/settings.json` the complexity is hidden. We should only see the relevant files:
```filesystem
my-js-lib
    .vscode/
        settings.json
    lib/
        com.braintribe.common.async-api-2.0~
        ...
    sources/
        src/
        ts-src/
            index.ts
    pom.xml
    tsconfig.json
```

Now we can start developing our module. 

General notes:
* Typically we only write `TypeScript` files, inside `sources/ts-src/` (we can of course create sub-folders).
* The configuration inside `tsconfig.json` ensures `TypeScript` compiler writes the compilation output into `sources/src/`.
* `sources/src/` is what matters when packaging the artifact. Hence this must be versioned in git, and ensured it's up-to-date (i.e. `TypeScript` must be compiled after every change).
* The compilation output is a pair of `.js` file and a `.d.ts` files, so that whoever depends on our module can also use code-completion.
* In order for `VS Code` to automatically compile `.ts` files press `CTRL + SHIFT + B` and then chose `tsc:watch - tsconfig.json`.
* If we wanted to add a pure `JavaScript` file, we can do so inside the `sources/src` folder.
* There is no cleanup, i.e. if we delete a `.ts` file in `ts-src/`, we also need to manually delete the corresponding `.js` and `.d.ts` files in `src/`.
* The `JavaScript` written by the compiler is very cryptic, to support old browser. To simplify debugging on a newer browser change `target` from `es6` to `esnext` (i.e. latest supported) in `tsconfig.json`.

## Example - Simple Application

As a simple example of code using `tribefire.js` let's write an app that logs in with username and password.

Notes regarding the code:
* The goal is to create `servicesSession` of type `tf.remote.ServicesSession`.
* This `servicesSession` is an API to communicate with the server.
* It is constructed based on a valid `UserSession` entity.
* This `UserSession` is obtained from the server by evaluating `OpenUserSession` request.
* This `UserSession` serves as a token to communicate with the server as the logged in user.
* In real applications there would be a lot of error-handling which we skip for simplicity.
* This code also shows the beauty of `DDSA`, i.e. `Denotation Driven Service Architecute`, i.e. calling a function simply by evaluating an instance of a request (`OpenUserSession` here).

> NOTE This tutorial was written with `tribefire.js` version `3.0`. Please adjust the version if needed.

```typescript
import * as tf from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as securityApiM from "../com.braintribe.gm.security-service-api-model-2.0~/ensure-security-service-api-model.js";

(async () => {

    // This creates a connection, which is just a data structure with the server URL, it can evaluate requests that don't require a logged user
    const connection = tf.remote.connect("http://localhost:8080/tribefire-services");
    const sessionResponse = await openSession();
    const servicesSession = connection.newSession(sessionResponse.userSession);

    // NOW THAT WE ARE LOGGED IN, WE COULD ADD USEFUL CODE HERE

    // we log in with username and password simply by evaluating a `OpenUserSession` request
    async function openSession(): Promise<securityApiM.OpenUserSessionResponse> {
        const userNameIdentification = securityApiM.UserNameIdentification.create();
        userNameIdentification.userName = "admin"

        const userPassword = securityApiM.UserPasswordCredentials.create();
        userPassword.userIdentification = userNameIdentification;
        userPassword.password = "admin123"
        userPassword.passwordIsEncrypted = false;

        const openSession = securityApiM.OpenUserSession.create();
        openSession.credentials = credentials;
        openSession.noExceptionOnFailure = true;

        return openSession.EvalAndGet(connection.evaluator()); // This is DDSA!!!
    }

})().catch(reason => {
    document.body.innerHTML = "Error: " + reason;
})
```

## Example - Simple Library

The above example simply connects to the server and does something. We could also create a helper library, with a function for logging-in.

```typescript
import * as tf from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import * as securityApiM from "../com.braintribe.gm.security-service-api-model-2.0~/ensure-security-service-api-model.js";

export async function login(String url, String username, String password): Promise<tf.remote.ServicesSession> {
    const connection = tf.remote.connect(url);
    const sessionResponse = await openSession();
    return connection.newSession(sessionResponse.userSession);

    async function openSession(): Promise<securityApiM.OpenUserSessionResponse> {
        // ... just like before, but using username and password passed to the "login" function
    }
}
```

## Example - Simple App using Simple Library to login

Now if we assume, that the previous example - `Simple Library` - was a module called `tribefire.tutorial.js:logging-library`.

We could now re-write our `Simple Application` to use this library, by adding it as a dependency to our `pom.xml`, calling `assemble-js-deps` again, and changing the code to:

```typescript
import * as tf from "../tribefire.js.tf-js-api-3.0~/tf-js-api.js";
import { login } from "../tribefire.tutorial.js.logging-library-1.0~/index.js";

(async () => {

    const servicesSession = await login("http://localhost:8080/tribefire-services", "admin", "admin123");

    // DO SOMETHING USEFUL

})().catch(reason => {
    document.body.innerHTML = "Error: " + reason;
})
```
