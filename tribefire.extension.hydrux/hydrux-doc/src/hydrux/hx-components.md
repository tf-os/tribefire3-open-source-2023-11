# Custom Components

Let's look at how exactly we can develop a `Hydrux` module with custom components.

## General Structure

As stated in the [Hydrux Introduction](./hx-intro.md), each custom component has a denotation type and an implementation. However, each custom denotation type extends some type from the `hydrux-deployment-model` (e.g. `HxView`) and the implementation then follows the corresponding specification (interface) from `hydrux-api` (e.g. `IHxView`).

For simplicity, all basic denotation types use `HxXyz` naming pattern, with the corresponding interface being called `IHxXyz`.

There are other interfaces in `hydrux-api`, which do not correspond to any component, but all those also have a name starting with `IHx` like `IHxModuleContract`.

[Full list of Component Types and their corresponding TypeScript interfaces](./hx-deployment-model.md#components)

## Simple Example

Let's define a **simple `Hydrux` module**  with a **single visual component** called `DemoView`. This view simply displays a configured static text.

Let's use `tribefire.demo.hx` as a group for our artifacts.

NOTE that **we do not care what this view is doing**, can be a simple button, which when clicked calls the server to retrieve a random image of a cat and then displays it. Or anything else. The **point is** to **show how to package** such a component **as a `Hydrux` module**.

###  The Model

We start with a **model**, say `demo-hx-deployment-model`, which has an asset dependency on `hydrux-deployment-model`.

We create the model artifact with:
```
jinni create-model demo-hx-deployment-model
```

We add a dependency to it's `pom.xml`:
```xml
<dependencies>
    <dependency>
        <groupId>tribefire.extension.hydrux</groupId>
        <artifactId>hydrux-deployment-model</artifactId>
        <version>${V.tribefire.extension.hydrux}</version>
        <?tag asset?>
    </dependency>
</dependencies>
```

And we create a denotation type for our view, with a single property - `dark` - which determines rendering colors (light/dark).

```java
public interface DemoView extends HxView {
	EntityType<DemoView> T = EntityTypes.T(DemoView.class);

	boolean getDark();
	void setDark(boolean dark);
}
```

// TODO figure out how to do the jinni commands. One by one, or all at once?

### The Implementation

For the implementation, **we need a** `JsLibrary` **artifact**, called `demo-hx-module`, which has a `JS` **dependency on** `hydrux-api` and an **asset dependency on** our model (`demo-hx-deployment-model`).

We create the library with:
```
jinni create-ts-library demo-hx-module
```

We add these dependencies to it's `pom.xml`:
```xml
<dependencies>
    <dependency>
        <groupId>tribefire.extension.hydrux</groupId>
        <artifactId>hydrux-api</artifactId>
        <version>${V.tribefire.extension.hydrux}</version>
        <?tag js?>
    </dependency>
    <dependency>
        <groupId>tribefire.demo.hx</groupId>
        <artifactId>demo-hx-deployment-model</artifactId>
        <version>${V.tribefire.demo.hx}</version>
        <?tag asset?>
    </dependency>
</dependencies>
```

And all that is left is to provide the correct code inside `index.ts` so that it is a valid `Hydrux` module - i.e. exporting a `contract` variable of type `IHxModuleContract`. Like this:

```typescript
import * as hx from "../tribefire.extension.hydrux.hydrux-api-2.1~/hydrux-api.js";
import { DemoView } from "../tribefire.demo.hx.demo-hx-deployment-model-1.0~/ensure-demo-hx-deployment-model.js";

export const contract: hx.IHxModuleContract = {
    bind(context: hx.IHxModuleBindingContext): void {
        const componentBinder = context.componentBinder();
        componentBinder.bindView(DemoView, createDemoView);
    }
}

async function createDemoView(denotation: DemoView, context: hx.IHxComponentCreationContext): Promise<hx.IHxView> {
    const application = context.application(); // hx.IHxApplication
    //...

    const div = document.createElement("div");
    div.textContent = "Hello World!"
    div.classList.add("demo-main-div");

    if (denotation.dark) {
        document.body.style.color = "white";
        document.body.backgroundColor = "black";
    }

    // We must return Promise<hx.IHxView>
    // hx.IHxView in an interface with a single method: htmlElement(): HTMLElement;
    // For this we have a convenient way how to create an IHxView based on a function that returns an HTMLElement.

    return hx.IHxViews.create(() => div);
}
```

**NOTE** that 


And that's it. Now if a `Hydrux` application uses our module, and there is an instance of `DemoView` somewhere, the `Hydrux Runtime` can resolve it to an instance of `IHxView` using the code above. QED

[See how an application with our DemoView component can be configured](./hx-app-config.md)

### Notes on the file/folder structure

Just for clarity, our project has the following file-system structure:

```filesystem
lib/
    src/        // -> ../src
    ts-src/     // -> ../src
    com.braintribe.common.async-api-1.0~    // -> ~/.devrock/js-libraries/com.braintribe.common.async-api-1.0.66
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

Note the `// -> path` parts denote that given folder is a symbolic link to given path.

With proper `VS-code` settings the structure in `VS-code` looks like this:

```plaintext
lib/
    com.braintribe.common.async-api-1.0~
    ...
sources/
    src/
    ts-src/
        index.ts
pom.xml
tsconfig.json
```

This just shows how we have conveniently hidden everything that we don't need.

Now we will only make changes inside of the `sources/ts-src/` folder. The reason why we use the `sources` symbolic link instead of opening the files directly under top-level `ts-src` [is explained here](../js-lib-asset/js-lib-intro.md).

