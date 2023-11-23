# OpenAPI

This document is about internal implementation details of our OpenAPI 3.0 endpoint. If you just want to know how to use these endpoints check out the [user documentation](../openapi-export-doc/src/introduction.md).

## Overview
The [OpenAPI 3.0 specification](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md) describes itself as a *standard, language-agnostic interface to RESTful APIs*. At its core there is an OpenAPI document in JSON or YAML format which describes all possible operations of a REST API including the schemas and parameters of their requests and responses. This information can be used for all kinds of meta-programming like creating a generic UI or code stubs for all kinds of REST requests.

In braintribe we have two groups of OpenAPI endpoints. The first one, accessible via `/api/v1/openapi` creates an OpenAPI document from a **service model** (DDSA requests) or **data model** (property- and entity-level CRUD requests). The second one `/openapi/ui/` takes such a document and presents it in a convenient UI, using the official `swagger-ui` javascript library.

Because the tribefire- and OpenAPI-technologies match very well, the generated documents are very rich and a lot of tribefire metadata like descriptions have impact into the generation process.

## Overall Architecture
The OpenAPI UI servlet is implemented as a separate servlet in the `com.braintribe.model.openapi.servlets.OpenapiUiServlet` class. In the same package you will find a Freemarker-template and a self-hosted version of the swagger-ui javascript library. We have [our own fork](https://github.com/braintribehq/swagger-ui/tree/nor) to be able to apply our own bugfixes if necessary because the upstream project is very slow to respond.

The OpenAPI documents are created by standard service processors. All relevant parts of an OpenAPI document are modeled as a tribefire model - the `openapi-v3-model` and can be marshalled into a valid YAML or JSON document by our standard marshallers.

There are three kinds of processors for the three kinds of documents that can be created:
* ApiV1OpenapiProcessor (DDRA)
* EntityOpenapiProcessor (Entity CRUD)
* PropertyOpenapiProcessor (Property CRUD)

### The ApiV1OpenapiProcessor...
...takes a service domain id and fetches the respective service model and DDRA mappings to create a document with both mapped and generic requests.

### The CRUD processors...
...take an access id and fetch the respective data model to create a document with all the possible CRUD requests of our [tribefire CRUD REST api](https://documentation.tribefire.com/tribefire.cortex.documentation/api-doc/REST-v2/rest_v2_rest_v2.html).

## Identity Management
Often you will want to reuse parts of an OpenAPI document for at least two of the following reasons
1. Keep the size of the document small
2. Allow cyclic references

An example for (1.) would be our standard `Failure` responses which every request has in error case and which would increase the document size quite a bit when it would be rendered every time again as a whole. Because a service domain often features hundreds of complex requests, the download- as well as processing speed of the document can be felt considerably.

Cyclic references (2.) are very common, especially in schema definitions. An `OpenapiSchema` describes a `GenericModelType` which is quite often an `EntityType` which again has properties with their own value schemas. A simple example would be an entity of type `Folder` which has a property `parentFolder` of the same `Folder` type. Without the possibility of identity management and reusing of the `OpenapiSchema` it would not be possible to create a finite document to discribe this schema:

```json
{
  "type": "object",
  "properties": {
    "parentFolder": {
      "type": "object",
      "properties": {
        "parentFolder": {
          "type": "object",
          "properties": {
            "parentFolder": {
              "type": "object",
              "properties": {
                "parentFolder": {
                  ...
                }
              }
            }
          }
        }
      }
    }
  }
}
```

While YAML allows referencing previously defined parts of a document via *anchors* JSON doesn't. To support this, there is a specific section in an OpenAPI document where reusable components can be defined via [JSON references](https://json-spec.readthedocs.io/reference.html).

```json
"components": {
  "schemas": {
    "Folder": {
      "type": "object",
      "properties": {
        "parentFolder": {
            "$ref": "#/components/schemas/Folder"
        }
      }
    }
  }
}
```
To be able to support both formats, this implementation uses the standard JSON referencing approach, which also works for the YAML case and seems to be the standard here as well actually when looking at the official OpenAPI reference.

## Reference Recycler
To not have to manually manage the JSON references, there is a convenient API in the `com.braintribe.model.openapi.v3_0.reference` package which abstracts identity management as well as generating the references away from you entirely. Although this is currently only used for this specific usecase it is actually not OpenAPI specific and thus also implemented in a generic way. This document mainly exists to collect my findings in the work with the used algorithms and to explain how and why it works.

### The Interface
From the OpenAPI processors I access the api via the `JsonReferenceBuilder` interface which only consists of two methods:
```java
public interface JsonReferenceBuilder<T extends JsonReferencable, A> {
	JsonReferenceBuilder<T, A> ensure(Function<A, T> factory);
	T getRef();
}
```

The first method `ensure(factory)` gets passed a factory that creates an OpenAPI component. However the factory will only be called if the component does not already exist.

The second method `getRef()` returns a reference to the component which was either just created or did already exist before. Note that the reference is of the same type as the actual component. If you look at the `JsonReferencable` type you will find a `$ref` property of type `String` which can contain a json reference key as introduced above. While a component instance wouldn't have the `$ref` property set, a reference instance would on the contrary have *only* the `$ref` property set. Thus we get a type safe reference that we can use anywhere in our code where we can use the actual component.

As you can see, the interface itself doesn't have an identity management related method so the respective implementations are responsible to determine the identity of the respective component. To give you some context, look at how we could create a request body.

Imagine a custom method `requestBody` that returns a `JsonReferenceBuilder` for an `OpenapiRequestBody` component, which describes the possible content of a REST request body. For tribefire DDRA requests a request body contains a marshalled form of a ServiceRequest entity and thus can be defined by just the entity type of the respective DDSA request. So any request that shares the same entity type (For example the same request type may be mapped with multiple HTTP methods) can reuse the same component. So the entity type is the only thing we need to consider for identity management:

```java
requestBody(requestEntityType)
	.ensure(context -> {
		OpenapiRequestBody body = OpenapiRequestBody.T.create();
		body.setContent(createContent(requestEntityType, context));
		return body;
	})
	.getRef();
```

The first time, the `requestBody` method is called for a certain entity type (e.g. our `Folder` type from before), the factory lambda is called to create an `OpenapiRequestBody` instance for it as well as store the created instance in the respective section of the document (the component pool for request bodies). The `getRef()` method then returns a reference to it that references the just created instance via a JSON reference.

The following times the `requestBody` method is called for the same entity type (for example when creating the schema for the `parentFolder` property of the `Folder` type), the factory lambda is simply ignored and a reference to the previously created instance is returned immediately.

Note, that because all of this happens behind the scenes, another implementation of a `JsonReferenceBuilder` by using the exact same interface might skip the JSON-referencing entirely and just return the original instance if it doesn't deem the overhead necessary.

You may have noticed that the lambda takes a `context` argument. If you look at the type signature of the interface method you can see that this context can be just about anything and its type is only defined by the implementation. How and why this is used will be explained in the next sections.

### The challenges (Part I)
This could already have been it. We have identity management, can reuse components and legally declare cycles and all of this pretty conveniently. But especially `OpenapiSchema`s can be very complex and can also exist in several versions for the same `EntityType`.

Currently we support 3 different mime types:
* application/json (which could be any mime type that supports deeply nested structures)
* application/x-www-form-urlencoded
* multipart/form-data

Because the latter two only support flat values, complex property types can't be mapped. Further multipart/form-data is the only one that supports binary parts, which are determined by the `Resource` entity type.

So trivially there are 3 schemas for any service request that has a `Resource` type property:
* The urlencoded version skips the resource type property entirely
* The multipart version maps the resource type property as a binary part
* The json version reflects the resource type property as the complex entity type it is

There can also be different versions of schemas for the same entity type in cases where request-specific metadata was set. This can be used for example to hide a certain property for a specific request only. (A request specific metadata can be specified with a `UsecaseSelector` with the request path in the usecase string like `openapi:/my/request/path`)

So identity management gets less trivial. We need to put take both in consideration - the REST request path to respect request specific metadata and the mime type to be able to support different mime type schemas. We are not able to reuse schemas between requests and need to define them three times for every single request even when in most cases there is no request specific metadata set and many requests are so simple that the schemas are identical between mime types. This inflates the OpenAPI document in orders of magnitudes as you can imagine.

Thus a further optimization layer was created that creates all these different versions of an `OpenapiSchema` and compares them with each other. If they are identical, only one of them is chosen and referenced by the rest. So the document can stay as small as possible.

To support this new optimization layer, we got the `context` argument which you have already seen in the end of the previous section. This `context` holds the information which version (in our case mime type and REST request path specific metadata) should be created by the factory.

An example implementation for a schema specific implementation of a JsonReferenceBuilder could be used like that:
```java
schema(entityType, context)
  .ensure(currentContext -> {
    OpenapiSchema schema = OpenapiSchema.T.create();
    schema.setTitle(entityType.getTypeSignature());

    switch (currentContext.getMimeType()){
      case MULTIPART_FORMDATA:
        createMultipartSchema(schema, entityType, currentContext);
        break;
      case URLENCODED:
        createUrlencodedSchema(schema, entityType, currentContext);
        break;
      default:
        createGeneralSchema(schema, entityType, currentContext);
    }

    return schema;
  })
  .getRef();
```

Let's imagine we create the schema for `CreateUserRequest` which has two properties:
* `userName` (String)
* `userImage` (Resource)

Because `Resource` is a complex type, it's not supported by the **urlencoded** mime type so the resulting schema only contains the *userName* property.

For the **multipart** mime type a `Resource` type entity is interpreted as a binary part which contains the actual binary of the user image.

For the general or **JSON** mime type a `Resource` is used as the complex type it is including referenced complex properties like `resourceSource` or `resourceSpecification` which corresponding types may have further complex properties.

The factory lambda is run tree times with tree different contexts and all three versions of the document are created and then compared. Because they all differ, we need to create all three different versions of the schema.

However much more often we will have something like the hypothetical `GetUserRequest` with a single property that returns some information about a certain user:
* `userId` (String)

Because there is no complex property involved, there are identical `OpenapiSchema` instances created for all three mime types. After they are compared, one of them is chosen and reused for each of the three mime types. So we can keep our document small.

Note, that the context (or at least parts of it i.e. the mime type and request path) needs now also to be considered as part of the identity management because when we encounter an entityType the second time we need to know which version of the three we want to reference: the version with the same mime type we are currently resolving.

### The Algorithm (Basics)
Well, that escalated quickly. So how would an algorithm work to support all that?

To help us imagine this algorithm let's invent a simple usecase to showcase the problems:

Let's first assume we want to create `OpenapiSchema` instances for `EntityType`s with 0, 1 or more properties. Every property references another schema instance. This could be a simplified example code to create OpenapiSchema instance:

```java
public OpenapiSchema createSchema(entityType, context) {
  return schema(entityType, context)
      .ensure(currentContext -> {
          OpenapiSchema schema = OpenapiSchema.T.create();
          schema.setTitle(entityType.getTypeSignature());
          schema.setDescription(getDescription(entityType, currentContext))

          for (PropertyInfo p : getProperties(entityType, currentContext)) {
            OpenapiSchema propertySchema = createSchema(p.getType(), currentContext);
            schema.getProperties().put(p.getName(), propertySchema);
          }

          return schema;
      })
      .getRef();
}
```

* The `createSchema` method contains the custom logic how to create an OpenapiSchema instance from an entity type in a specific context
* The `schema` method returns a `JsonReferenceBuilder` which is responsible for identity management and contains the logic of the optimization algorithm we are currently discussing
* The `getDescription` method returns a description string that is taken from an eventual `Description` metadata that could be set for the respective EntityType. This could be request path specific metadata and thus depend on the used context.
* The `getProperties` method returns the list of properties of an entity type in a certain context. Remember, that the number and type of properties can depend on the context's mime type.

Please look now at a few examples. For sake of simplicity we assume there is only one context for now! We will look later at how that would work with multiple mime types and request path specific metadata.

#### Example 1
A simple case would be EntityType `A` with one property of type `B` which again doesn't have a single property. To imagine the issue better I invented a simple syntax: We could write that like so:

```
A -> B
```

When this entity `A` would be processed by our example code it would execute in the following order.
1. `createSchema(A, context)` is called
2. `schema(A, context)` is called and returns a `JsonReferenceBuilder`
3. `ensure(...)` is called. Because `A` was never processed before the lambda is executed.
4. A new `OpenapiSchema` is created
5. `getProperties(A, context)` is called and returns exactly one property of type `B`
6. `createSchema(B, context)` is called
7. `schema(B, context)` is called and returns a `JsonReferenceBuilder`
8. `ensure(...)` is called. Because `B` was never processed before the lambda is executed.
9. A new `OpenapiSchema` is created
10. `getProperties(B, context)` is called and an empty list is returned
11. The factory lambda of the ensure method returns the finished `OpenapiSchema` component for EntityType `B`
12. The component is stored in the respective section of the OpenAPI document.
13. `getRef()` is called and returns a reference to the created component (An `OpenapiSchema` entity with only the `$ref` property set).
14. The `schema` method returns that reference
15. That reference is put as a property into the `OpenapiSchema` for `B`
16. The factory lambda of the ensure method returns the finished `OpenapiSchema` component for EntityType `A`
17. `getRef()` is called and returns a reference to the created component
18. The `schema` method returns that reference

As a result we have two `OpenapiSchema` components for the EntityTypes `A` and `B` in the schema components section of the OpenAPI document and a reference to schema `A` which could be used at one or multiple parts of the document, for example to describe the possible contents of a request body.

You will notice that the algorithm is depth-first: While a component is processed, references to new components are followed immediately and only after all references are processed the original component returns.

#### Example 2
When EntityType `A` has one property of type `B` which again has a property of type `C` with again a single `D` type property this would look like so

```
A -> B -> C -> D
```

If you try to walk through the creation of the components like above you will notice that the algorithm walks again from the first to the last back to the first component in the following order:

```
A B C D C B A
```

To better see what happens I will prefix the EntityType with a `-` when it is visited because we return from processing a reference. Unprefixed means that we met it the first time and start to process it.

```
A B C D -C -B -A
```

And again you will notice that the actual components are not directly referenced anywhere. A component only holds a reference to another component and the actual components are stored in the schema components section.

#### Example 3

Now a bit more complex example

In the following example, `A` has two properties of type `B` and `D`, where `B` references the `C` schema with another property

```
A -> B -> C
A -> D
```

If you walk again through the creation of the components like above you will find the following processing order

```
A B C -B -A D -A
```

#### Example 4

Let's look now at cycles. The simplest cycle is a self-reference like in our `Folder` example:

```
Folder -> Folder

or

A -> A
```

If we process again the example code for that self-referencing schema the first seven steps are identical.

1. `createSchema(A, context)` is called
2. `schema(A, context)` is called and returns a `JsonReferenceBuilder`
3. `ensure(...)` is called. Because `A` was never processed before the lambda is executed.
4. A new `OpenapiSchema` is created
5. `getProperties(A, context)` is called and returns exactly one property of type `A`
6. `createSchema(A, context)` is called
7. `schema(A, context)` is called and returns a `JsonReferenceBuilder`

But then there is something new

8. Because there already exists a schema for `A`, the factory method of `ensure(...)` is **not** called and a reference to the `OpenapiSchema` of `A` is returned (remember: that's an entity of type `OpenapiSchema` with only the `$ref` property set).

From there on we skip steps 9. to 14. and continue with step 15. until the end.

Note, that in step 8. a reference to the `A` schema was returned, **before** it was actually created! Because the reference is basically just a type safe String, we can use references of components that don't exist yet but will exist in the future. This technique breaks the cycles.

If we would want to write down that walk in a similar manner as above we could notate it as follows:

```
A (A)
```

I introduced the new notation `(A)` for the case when only a reference is returned but the EntityType isn't processed again.

#### Example 5
Now a bigger cycle example:

```
A -> B -> C -> D -> B
B -> E
D -> F
```

The only entity types with two properties are `B` and `D` and only `B` is referenced two times.

You can imagine these examples as a [graph](https://en.wikipedia.org/wiki/Graph_theory), where the `OpenapiSchema` components are the **nodes** connected by **edges**, which are the property references. Please remember this terms as from now on we will use the words *nodes* and *edges* instead of *OpenapiSchema* and *properties*.

If you would draw such a graph, for this example it would look somehow like:

(TODO: draw graph and insert image)

If you do the walk you will get

```
A B C D (B) F -D -C -B E -B -A
```

Another interesting task is to count the cycles. There is exactly one: `BCDB`. Three nodes are not part of any cycle: `A`, `E` and `F`.


#### Example 6
Maybe you can do the following example already by yourself:

```
A -> B -> C -> A
C -> D -> E -> D
```

The only entity type with two properties is `C`
`D` and `A` are however both referenced two times

If you do the walk you should get

```
A B C (A) D E (D) -D -C -B -A
```

Let's again count the cycles. There are exactly two:

```
ABCA
DED
```

Also there is not a single node which is not part of a circle, but both circles don't share a node. This is one of the simplest possible multi-circle examples. Later we will see how complex this can get, why this is relevant and why the algorithm still works there. And yes, all these simple and complex circles occur in practice!

### The Algorithm (Multiple Contexts)
Now as we have the basics we can look at how this can work with multiple contexts.

To get some order into the chaos, contexts can be organized in hierarchies. There is a general context with the three mime type specific contexts as child contexts which again have all the REST request path specific contexts as child contexts. This structure allows that one context always only needs to compare its result with its parent context. If the results are identical, the parent context compares its result with its grandparent context and so on until the first context aka root context is reached.

Let's start again with the first example. This time we have 2 contexts. A general one (the root context) and one more for request path specific metadata which has the root context as a parent.

#### Example 1
You remember:

```
A -> B
```

We run the example code from above with the child context (the one with specific metadata)

We need to create the components with both contexts and compare them to see if we can reuse them. The rule here is, that the component is created with the parent context first and then with the child context. The walk here is as follows:

1. (child):
2. (parent): A B -A
3. (child): A B

at this point the first entity was created by both child and parent and they can be compared against each other. If they differ (There is a request path specific metadata, i.e. `Description`) then a reference to the child component is returned, created and used.
If they are identical, the child component is not even stored in the schema components and won't appear anywhere in the OpenAPI document. In this case a reference to the parent component is returned.

4. (child): -A

The second component was created by both child and parent and can be compared as well. Note that when B was already different, A is different as well because it references another component as the parent does. It is also possible, that in both cases the B component is identical to the parent one but the A component differs (has a different description). In this case there is an A component created and in the child context and also used and returned, but this A component references the B component of the parent context.

One more time more slowly:

I) Both A and B are identical in the child and the parent context. Then the parent context's components are used for both

II) A differs but B is identical to the parent context. Then the child's A is returned which however references the parent context's B. The child context's B does not appear at any part of the document but both versions of A might exist in different parts.

III) A and B both differ from the parent's. Then the child's A is returned which references the child's B as well.

IV) B differs, but A is otherwise identical to the parent's. This still results in both being different because we need an A that references the child's B. The parent's A references of course the parent's B so it can't be reused. This shows that IV) is nothing but another case of III)

This shows that the following steps are needed to determine whether to use a child's or a parent's component
1. The component needs to be fully created in the child context - that automatically means that it's created in the parent and that all its referenced components are fully created in the child and thus again also in the parent.
2. If at least a single referenced component differs from the parent, the current component differs from the parent as well because of this reference. This rule can be applied transitively for any transitively referenced component (reference of a reference of a...).

To be aware of this is crucial when trying to resolve cycles.

With this knowledge try to walk through example 2 and 3 again.

#### Example 4
This was the self reference, the most trivial cycle:

```
A -> A
```

Because there is no other node to consider, the walk could still be trivial

1. (child):
2. (parent): A (A)
3. (child): A (A)

Following the rules we found in the previous example, this is the point where we know that the entity and all referenced nodes (itself) are fully created and we can compare them. Because the only referenced node is the node itself, we can in this case still safely ignore the edge because it wouldn't change the result: If the child's `A` differs from the parent's `A` we will need to return the child's `A` with a self-reference to the child's `A`. Otherwise we will return the parent's `A` with a self-reference to the parent's `A`. This would be the same result, even when `A` wouldn't have a self-reference.

If this sounds complicated then this is only because it's actually so trivial and obvious. In other words the problem and thus the walk can be simplified to the one-node and zero-edges graph

```
A
```

So maybe that example was boring and too specific so let's move on to a *real* circle:

#### Example 5
```
A -> B -> C -> D -> B
B -> E
D -> F
```

1. (child):
2. (parent): A B C D (B) F -D -C -B E -B -A
3. (child): A B C D (B) F

This is the point where the first node `F` was fully created. Because `F` doesn't have any outgoing edges (properties), trivially all its referenced nodes are created.

If the child's `F` differs from the parent's one (let's coin the term it's **changed**), all nodes from the graph are *changed*, because all of them transitively reference `F`. But if it would be **unchanged** the walk needs to continue.

4. (child): -D

Now that's an interesting point! We finished creating `D`, but we can not compare it yet because it references `B` which was not finished yet (The referenced node `E` was not yet processed). Remember, we have the reference to `B`, even when it was not finished yet, because it occurred already the second time and the identity management system can provide us with a preliminary reference. Welcome to the world of problems we have in cycles!

So even when the node `D` was fully created, we can't decide yet whether it's *changed* or not. For that reason we must store it somewhere so that we can decide this on a later point.

5. (child): -C

Same issue here - C transitively references the unfinished `B`. We must store it away as well to decide about its state later.

6. (child): -B E

Finally we finish `E`.

Because `E` doesn't have any references as well, the same rules apply as for `F` after step 3.

7. (child): -B

At this point we know that there are no more nodes referenced by B and that all entities of the cycle are finished.

Now let's think about what a *changed* or *unchanged* `E` would mean for the other nodes. Because it is referenced by one node of the circle (`B`), it's transitively referenced by **all** nodes of the circle. So if it is *changed*, all nodes of the circle are *changed*. At this point we can go through the collection of the undecided cycle nodes (`D`, `C`) and set their status.

Also all nodes that reference the cycle are *changed* because they again transitively reference `E`. In our example that is a single node: `A`. Note also, that this does not influence the result for `F`. So it is possible that all nodes are *changed* except `F` which could still be *unchanged*.

8. -A

At this point we finished all nodes and can decide about the status (*changed* or *unchanged*) of `A`. Note that the status of all other nodes is already decided and the result for `A` doesn't influence the other nodes any more.

So we found that cycle nodes need to be treated differently than non-cycle nodes (let's call them **branches**). Our graph has a 3-node cycle and 3 1-node branches.

* A branch node's status can be decided immediately after it is finished.
* A cycle node's status can only be decided after all other cycle nodes are finished and thus may have to be collected and temporarily stored separately.
* All nodes of a cycle have the same status: If one cycle node is *changed*, all are *changed*. Otherwise they are all *unchanged*.

Finally the nodes of the graph could have the following possible combinations of statuses (**c** is *changed*, **u** is *unchanged*):

| A | B | C | D | E | F |
|:- |:-:|:-:|:-:|:-:| -:|
| c | c | c | c | c | c |
| c | c | c | c | c | u |
| c | c | c | c | u | c |
| c | c | c | c | u | u |
| c | u | u | u | u | u |
| u | u | u | u | u | u |

This list of possible combinations is exhaustive: There are no other possible combinations. Please make sure that you understand why, before you continue. You can also draw the graph to be better able to visualize.

#### Example 6

Two independent cycles

```
A -> B -> C -> A
C -> D -> E -> D
```

1. (child):
2. (parent): A B C (A) D E (D) -D -C -B -A
3. (child): A B C (A)

At this point we know that we are in a cycle. But that doesn't change anything yet...

4. D E (D)

At this point we detected a second cycle.

Only at this point the first node (`E`) is finished. Because it's in a cycle with `D` we need to store it away and decide later about its status

5. (child): -D

Now all nodes of the second cycle are finished and we can decide about its status.

6. (child): -C

The first node of the first cycle is finished but again needs to be stored away for later

7. (child): -B

Also finished and going to be stored away

8. (child): -A

Only now all nodes of the first cycle are finished and we can decide about its status. This also concludes the walk.

These are again all possible combinations of node statuses:

| A | B | C | D | E |
|:- |:-:|:-:|:-:| -:|
| c | c | c | c | c |
| c | c | c | u | u |
| u | u | u | u | u |

#### Conclusion

Of course there are much more complicated examples. For example there could be intertwined cycles that share several nodes. There could be nodes or even cycles in one context that are missing in another. But the goal of that chapter was to give an introduction in how to debug the algorithm and give you tools to prove that it actually works in your (and hopefully all possible) cases.

### The challenges (Part II)
In retrospective the following part of the algorithm might be obsolete as there are different ways to solve this problem, or it might not happen so often that it was worth to implement this optimization. Nevertheless it still makes up a large portion of the code so I will cover it here:

Let me first introduce a few more aspects of OpenAPI document resolution I did not mention before to make more clear why this optimization was introduced:

1. An OpenAPI document is actually resolved from more than one tribefire model. The generic endpoints are reflected by the `tribefire.cortex:ddra-endpoints-model`, error results are represented by the `Failure` EntityType from `tribefire.cortex:service-api-model` and both may not be dependencies of the service- or data model the OpenAPI document is supposed to reflect. That means that the context also holds and determines the model that is used to build `OpenapiSchema`s from.
2. Many models and entities are shared between all service and data models, like the `com.braintribe.gm:root-model` in general or the `tribefire.cortex:service-api-model` between service models and thus they could in theory be generated once, cached in a central place and then simply be reused. In practice however even these general entities may be configured with request path specific metadata or property overrides in the reflected model and can only be reused conditionally.
3. For debugging reasons it can be interesting to understand, why a certain component of the OpenAPI document turned out the way it is. Earlier it was always possible to see from the key of a component from which context it came. If you saw, that it came from a specific child context, you would know that at that specific point there was a change that brought that version of the component in existence.
So if it came from the *multipart* mime type context, you would know that there was a mime type specific change for that component. Now there are further optimizations that generate short and pretty keys and hide the source context in non-ambiguous situations. But still again when you find a long ugly key the context id is again added to it and you can use that information to find out why this was necessary.

For example we could want to reflect upon CRUD request for a custom data model, which depends on the `tribefire.cortex:service-api-model`, e.g. because the access is used to persist service request templates. As we just leaned, the `Failure` type will be processed by two different contexts - the one responsible for generic components to generate schemas for error responses, which always exist regardless of the access's data model and then again by the data model specific one to be able to reflect CRUD operations on that entity type. In most cases they will be identical so we want to use only a single `OpenapiSchema` component for the `Failure` type. To support this, we must allow that a child context operates on a different model that its parent context - i.e. is backed by a `ModelAccessory` and a `ModelOracle` for that model to resolve types and metadata.

In our case the parent context would have the `tribefire.cortex:service-api-model` and the child context the reflected data model. Given that the data model includes the `Failure` type as well, above algorithm works again. It generates a component for that type with both contexts, compares them and reuses the parent one if they are identical.

However this raises one more aspect: An entity might exist in the model of one context, but not in the other. In our case we might have a custom entity `Address` in our data model of the child context, which is not part of `tribefire.cortex:service-api-model` of the parent context. If we run into this node with our algorithm it will crash while processing the parent context because it won't be able to reflect upon that `EntityType` with its `ModelAccessory` or its `ModelOracle`. This means that some components may exist in the child context and not in the parent one or vice versa.

Thus a new status is introduced besides *changed* and *unchanged*: **notPresent**. If a component has this status, it is not even started to be processed and thus also won't be created or compared in the respective context. If the parent context returns *notPresent* for a component, the child context will treat it as if it would be *changed*: It creates it and uses it for all its references.

### The Algorithm (Mixed Models)
Let's go through this again with our examples:

#### Example 1
```
A -> B
```

1. (child):
2. (parent): A

If `A` doesn't exist in the parent's model we stop here and return with step 5.

3. (parent): B

If `B` doesn't exist in the parent's model we stop here and return with step 5. If `B` doesn't exist, it can't be referenced and consequently `A` can't be created as well, so both get the *notPresent* status. This is a rare case but happened to me already with incorrectly defined models.

4. (parent): -A
5. (child): A

If `A` was resolved as *notPresent* in the **parent**, we don't have anything to compare against and automatically assign it the *changed* status. Otherwise the algorithm continues as we already know from the previous chapters.

If `A` is resolved as *notPresent* in the **child**, the algorithm is aborted and we take the parent result if it is present there.

6. (child): B

It's possible that `A` is *notPresent* in the parent but the referenced `B` exists there. The same is possible in the child context. That's why the same considerations apply as in the previous point.

7. (child): -A

And the algorithm terminates.

You see, that the *notPresent* status is very similar to the *changed* status, because a single *notPresent* node affects **all referencing** nodes and changes their status as well to *notPresent*. Again this doesn't have any effect on **referenced** nodes.


### Conclusion
Because of time limitations I have to finish to write this document at this point. This was just a general overview of the classes of problems with an attempt to give you tools to further investigate and understand on your own how it works in many different situations.
