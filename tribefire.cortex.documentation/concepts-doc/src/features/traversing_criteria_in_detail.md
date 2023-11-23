# Traversing Criteria in Detail

Even though traversing criteria are most commonly used when controlling the eager loading of data returned by a query, it is a more general concept which can be used in different situations.

This document describes traversing and traversing criteria in general and from scratch, with some querying-specific (and other) remarks later on.

## Traversing and TraversingCriteria

_Traversing_ in our context informally means _visiting_ (and checking/processing) an entity (or a collection of entities) and it's/their properties. If the property is again an entity, or a collection of them, we continue the traversing recursively.

Speaking more formally, traversing is a [graph traversal](https://en.wikipedia.org/wiki/Graph_traversal) of a [directed graph](https://en.wikipedia.org/wiki/Directed_graph) whose nodes are GM values (entity, enum, simple or a collection of those) and node `n1` is connected by an edge to `n2` if `n1` is an entity and `n2` is a value of some of it's properties, or `n1` is a collection and `n2` is one of it's elements.

`TraversingCriteria` is a parameter given to the traversing algorithm which controls whether or not the traversing process should pass a certain edge. For example, it can describe that a collection property should not be traversed.

`TraversingCriteria` is actually an instance of some `TraversingCriterion` sub-type (so from type perspective a singular form of the word would seem more fitting), but we call it `TraversingCriteria` when we use it as a parameter that controls the traversing.

### Implementation Notes

Traversing implementation is part of the very core of GM, in fact it is implemented directly on the various `GenericModelType`s. The traversing process can be influenced from the outside by a parameter called `TraversingContext`. This object controls for example whether we pass a given edge, but also listens on the various _visit_ events. `TraversingCriteria` is just one part of the standard implementation of the `TraversingContext` - the one that influences which edges to pass. In many cases (like querying) it is the most prominent part, because it's the only traversing parameter exposed to the user.

One important aspect of the standard implementation (when it comes to core, also the only implementation) of this `TraversingContext` is the fact that it never allows for the same node to be visited multiple times. This has one significant side effect, which is discussed at the end of this document.

There is also a second, more simple method for traversing which takes a `Matcher` as a parameter, rather than `TraversingContext`. But the same applies here - the standard implementation is based on `TraversingCriteria`.

## `TraversingCriterion` Stack

Before we look at concrete `TraversingCriteria`, it is helpful to understand how they are applied. As the traversing happens, the algorithm is maintaining an information about the path it passed from the "root" node to a currently visited node. This information is a list of `TraversionCriterion` instances which describes the passed nodes and edges, although the individual items do not always directly correspond to the nodes and edges of the graph described above (see map-related `TraversingCriteria`, for example). We call this list the `Traversing Stack`.

The following table shows all the possible types of a `TraversingCriterion` which can be used in such a stack. We call these `BasicCriteria` and every single one of them carries also a `typeSignature` information, which  is taken by the algorithm from the corresponding `GenericModelType` instance.

`BasicCriterion` type | Description | Note
-----  | -----  | -----
`root` | marks the beginning of the TC list | `root` is always the first element on the stack
`entity` | denotes an entity | -
`property` | denotes a property; is always preceded by an `entity` criterion; It is also the only criterion which carries an extra value - the name of the property | `typeSignature` is that of the property, not the actual value, with **ONE EXCEPTION** - the `id` property, where we take the signature of the value's type.
`listElement` | means the previous element is a list and the following one is it's element | `typeSignature` is taken from the list element type, so it might be a super-type of the actual element's type
`setElement` | see listElement | -
`map` | denotes a map | -
`mapEntry` | means the previous element is a `map` and the following is either `map-key` or `map-value` | `typeSignature` is taken from the corresponding map
`mapKey` | means the previous element is a `map-entry` and the following is the value of this entry's key | see `listElement` remark regarding `typeSignature`
`mapValue` | see mapKey | see `listElement` remark regarding `typeSignature`

For easier reference we now also present the remaining `TraversingCriteria`, which are explained later. If you are reading this for the first time, read the next section (about their application) first.

`Criterion` type | Description | Note
----- | ----- | -------
`joker*` | matches every `BasicCriterion` | -on the stack
`propertyType*`| matches a `PropertyCriterion` if the specified type matches | **Deprecated** in favor of `TypeConditionCriterion`
`typeCondition*`| matches a `BasicCriterion` if the specified type matches | -
`valueContition*`| matches a `BasicCriterion` if the resolved value matches given value | resolved value means we take the value corresponding to the `BasicCriterion` and resolve the property path specified via the `ValueConditionCriterion`
`conjunction`| matches if all nested criteria match the current stack | see examples
`disjunction`| analogous to conjunction  | -
`negation`| matches if the nested criteria doesn't match  | -
`pattern`| matches multiple elements of the stack in order | see details below
`recursion`| matches if the nested criterion can be matched as a pattern a number of times, and this number is within the bounds specified | `RecursionCriterion` comes with a nested criterion and two numbers - `minRecursion` and `maxRecursion`

`*` This is an `ElementStackCriterion`, which means when being evaluated, it is compared with exactly one element in the `Traversing Stack`. The others can potentially be compared to multiple elements of the stack.


## `TraversingCriteria` Application 

Now that we understand the `TC stack`, it should be quite simple to explain what `TraversingCriteria` is. It is just an expression that either does or does not match a given `TC` stack.

We might also add right away that when the criteria matches a given stack, the traversing stops and we take some other action instead. For example for querying this means we do cut the query result at that point, i.e. we do not include the data behind that edge.

### `TraversingCriteriaModel`

So far we have only discussed the `BasicCriteria`, which correspond to a concrete position in the traversed graph, but there are other criteria which allow us to combine these basic ones into more complex expressions.

From modeling perspective `BasicCriterion` is a sub type of `TraversingCriterion`, but there are other types which are used for matching. (This is very similar to regular expressions, where a text consists of say letters and number, and the matching pattern consists of them plus special expressions with various semantics). In fact, the whole `TC` expression is expressed as instance of this `TraversingCriterion` type.

### Building `TraversingCriteria` with Java API

The java API for building `TCs` is called `CriterionBuilder` and is accessible in static way like this:

```java
TraversingCriterion someCriterion = TC.create()
      // your criteria
    .done();
```
We will not go into details about all the methods of this builder, as it is very intuitive once you are familiar with the `TC` model. See for yourself, as all the examples that follow for `TC` will use this builder.

Let's now have a look at the `TC` other than basic. We'll also skip the logical ones (conjunction, disjunction, negation) as they are self explanatory.

### `Joker`

This is a `TC` that matches everything. The most common usage is in the form:
```java
final TraversingCriterion matchNothingTc = TC.create()
    .negation()
        .joker()
.done();
```
If it was just the `joker`, it would match everything and thus nothing would be traversed. Adding a negation in front of that inverts the logic, thus this `TC` never matches, which leads to a full traversal of the graph.

### `Pattern`

`Pattern` makes it possible to match multiple elements on the `TC stack`. So far we have only seen criteria which are compared to the element on the top of the stack - i.e. the currently visited node. But if we want to also consider the previous nodes on the traversing path, `Pattern` is the way to go. It consists of a list of `TCs`, where the last one is matched against the last element on the stack, the second to last is matched against the second to last element on the stack and so on.

For example, if we wanted to match the property _value_ of the type _BigValueHolder_, but only that type, we'd say:
```java
final TraversingCriterion matchBigValueTc = TC.create()
    .pattern()
        .entity("BigValueHolder")
        .property("value")
    .close()
.done()
```

### `Root`

As mentioned above, `root` is the marker for the beginning of traversing, thus it is always the first element in the `TC stack` and does not show up anywhere else. This means if you want to use it in your `TraversingCriteria`, it needs to be the first element of a `Pattern` for it to make sense.

For example, imagine we are traversing a _Folder_ (a single one) and want to traverse all it's _subFolders_, but nothing deeper. The corresponding `TC` could be:

```java
final TraversingCriterion matchSubFoldersOneLevelDeepTc = TC.create()
    .conjunction()
        .pattern()
            .entity("Folder")
            .property("subFolders")
        .close()
        .negation()
            .pattern()
                .root()
                .entity("Folder")
                .property("subFolders")
            .close()
.done()
```

The example above could be simplified if we knew that _subFolders_ is not a property of anything else other than a _Folder_ in our assembly. The simplified `TC` would be:

```java
final TraversingCriterion matchSubFoldersOneLevelDeepTc = TC.create()
    .conjunction()
        .property("subFolders")
        .negation()
            .pattern()
                .root()
                .entity("Folder")
                .property("subFolders")
            .close()
.done()
```

### `Recursion`

`Recusion` is not commonly used, but it's here, so let's talk about it. It comes with three parameters - `min`, `max` and a nested `TC`. When determining whether it matches given stack, it tests how many times the nested `TC` can be matched against the stack, and if this number is between `min` and `max` (both included), the `recursion` matches.

### Matching Types
For matching node types, we use another expression API called [`TypeCondition`](javadoc:com.braintribe.model.generic.typecondition.TypeConditions). 

For example `TC` that matches entities and collections would look like this:
```java
final TraversingCriterion entityOrCollectionTc = TC.create()
    .typeCondition(
        TypeConditions.or(
            TypeConditions.isKind(TypeKind.entityType),
            TypeConditions.isKind(TypeKind.collectionType)
        )
    )
.done();
```

## Advanced Traversing Aspects

### Matching Inside the Traversing Algorithm - Not Everything is Being Checked

As we have said in the beginning, the traversing algorithm is building a `TC` stack reflecting the passed nodes and edges, and this is then matched against the `TC` expression given to the algorithm. However, of the 9 possible `BasicCriteria` only for 4 of them the matching check is being made. This is because in the other situations it doesn't make sense to do the check. 

The following table shows which `BasicCriteria` are not checked, with explanation why that is the case

TC | Reason
----- | ------
`root` | at this point, no information from the traversed assembly would be considered; doing the check here would be an expensive check on whether we even want to traverse at all, which is not needed
`entity` | this would be obsolete - if we don't want to traverse an entity, we can decide so on the predecessor of the `entity` node, e.g. `property` or `listElement`.
`map` | This is a weird `TC` to begin with, as there is no `list` or `set`. Generally speaking, if we want to skip the entire `collection`, we should do the match on the predecessor of the `collection` node, and if we want to skip collection elements, we can match on `mapEntry`, `listElement` and `setElement`.
`mapKey` | We cannot skip just the key, but we can skip `mapEntry`.
`mapValue` | We cannot skip just the value, but we can skip `mapEntry`.

### Query Troubleshooting - Only Properties are Being Checked

We just said that only 4 of the `BasicCriteria` are checked against the given `TC`, but in case of queries we actually only check one, namely `property`. This means you can decide which properties are loaded when doing a query, but if that property is a collection, you can either load all of it's elements or none. It is not possible to apply `TC` on the collection elements and thus load only a sub-set of the contained values.

This mean one has to be careful when building the `TC` attached to a query. Only a `TC` that can be matched against a `property` makes sense.

As a real-life example, imagine you are querying for a model, and you want to omit meta-data. One might think that using this `TC` would work, but this `TC` doesn't match anything in the context of query evaluation:

```java
final TraversingCriterion matchMetaDataTc = TC.create()
    .typeCondition(
        TypeConditions.isType(MetaData.T)
    )
.done();
```

For completion, since all the metadata on the various model elements are of type `Set<MetaData>`, the relevant meta data properties could be matched with:

```java
final TraversingCriterion matchMetaDataTc = TC.create()
    .typeCondition(
        TypeConditions.hasCollectionElement(
            TypeConditions.isType(MetaData.T)
        )
    )
.done();
```

### Entities are Not Revisited 
As mentioned in a side node at the beginning of this document, standard implementation of the `TraversingContext` never allows for the same node to be visited multiple times. This may lead to somewhat surprising results when the same entity is show up at different depths in the returned assembly. 

As an example, let's get back to the `Folders`, where each `Folder` has a property called `children`.

 Let's have this simple data set:

```
parent
    child
        grandChild
```

and make the following query:
```sql
select f from Folder f
```

with the `TC` from the `root` path above that says we want to load the `subFolders` on top level, but not on a deeper level:
```java
final TraversingCriterion matchSubFoldersOneLevelDeepTc = TC.create()
    .conjunction()
        .property("subFolders")
        .negation()
            .pattern()
                .root()
                .entity("Folder")
                .property("subFolders")
            .close()
.done()
```

When executing the query, we first obtain a list of all the `Folders`, and then we apply the traversing criteria. If we however think about the data, it is not clear at all what should be returned back. We say we want the top-level `Folders` to have their `subFolders` loaded, but not anything deeper. However, since we are doing a query for all the `Folders`, every `Folder` from our result's perspective is a top level query, and what will be loaded and what absent depends purely on the order of the `Folders` in our list.

Imagine the list we traverse would look like this:
```java
[Folder("parent"), Folder("child"), Folder("grandChild")]
```

Since the traversing algorithm uses a [depth-first](https://en.wikipedia.org/wiki/Depth-first_search) order, it starts by visiting the `parent`, followed by visiting `child` along the `subFolders` property edge. There it examines all the properties of the `child` `Folder`, and when it gets to `subFolders`, the stack looks like this:

```java
root
entity("Folder") // value is Folder("parent")
property("subFolders")
entity("Folder") // value is Folder("child")
property("subFolders")
```

This stack matches our `TC`, thus the `children` property is marked not to be loaded.

Then the traversing continues back to the `parent` node, cannot continue with it's sibling `child`, because that was already visited, and ends the traversing with visiting `grandChild`. This means that our top-level entity `child` doesn't have it's property `subFolders` set.

This would not happen, if the order of the initial data was for example:

```java
[Folder("grandChild"), Folder("child"), Folder("parent")]
```


## Examples

### Depth Parameter in REST

The `depth` parameter (if you are not familiar), is an integer which specifies how many levels deep the returned result should be loaded.

```java
// First let's define a TC for only loading the simple and enum properties
final TraversingCriterion shallowTc = TC.create()
    .pattern()
        .entity()
		.typeCondition(
			TypeConditions.or(
				TypeConditions.isKind(TypeKind.entityType),
				TypeConditions.isKind(TypeKind.collectionType)
			)
		)
    .close()
.done();

final TraversingCriterion tcForGivenDepth = TC.create()
    .pattern() // pattern 1
        .recursion(depth, depth)
            .pattern() // pattern 2
                .entity()
                .disjunction() // disjunction 1
                    .property()
                    .pattern() // pattern 3
                        .property()
                        .disjunction() // disjunction 2
                            .listElement()
                            .setElement()
                            .pattern().map().mapKey().close()
                            .pattern().map().mapValue().close()
                        .close() // disjunction 2
                    .close() // pattern 3
                .close() // disjunction 1
            .close() // pattern 2
        .criterion(shallowTc
    .close() // pattern 1
.done();
```

## Other Examples 
In these examples, we are going to query the `auth` access to get information about the `cortex` user. We  assume you have a local tribefire installation running at port `8080`. 

>For information on how to set up your development environment so that you have all the necessary dependencies, see [Setting Up IDE for Cartridge Development](asset://tribefire.cortex.documentation:tutorials-doc/cartridge/setting_up_ide.md).

To start querying, we must first create a valid session. Let's use a helper method for that:

```java
public static PersistenceGmSession getSession(String accessId) throws GmSessionException, GmSessionFactoryBuilderException {
    PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote("http://localhost:8080/tribefire-services")
		.authentication("cortex", "cortex").done();
	PersistenceGmSession session = sessionFactory.newSession(accessId);
	return session;
	}
```

With that helper method in place, we can now establish a session to the `auth` access:

```java
PersistenceGmSession session = getSession("auth");
```

As traversing criteria don't query on their own but influence a query object, let's create a query:

```java
SelectQuery query = new SelectQueryBuilder()
				.from(User.T, "u")
					.where().property(User.name).eq("cortex")
				.done();

User user = session.query().select(query).first();

System.out.println(GMCoreTools.getDescription(user));
```

<!--{% include apidoc_url.md className="SelectQueryBuilder" link="classcom_1_1braintribe_1_1model_1_1processing_1_1query_1_1fluent_1_1_select_query_builder.md" %}-->

The query above returns an instance of the type `User` where the value of the `name` property is equal to `cortex`. Running the query without any traversing criteria and printing out what's returned results in the following:

```java
User[
  description = null
  email = null
  firstName = 'C.'
  globalId = '85b67e99-e4db-4052-9621-55c81ad3458f'
  groups = ?
  id = 'e835fc5f-094e-43cb-88b3-8390d0ff35ae'
  lastLogin = null
  lastName = 'Cortex'
  name = 'cortex'
  partition = 'auth'
  password = '*****'
  picture = ?
  roles = ?
]
```

Let's use the traversing criteria now to return absent information for everything but the value of the `partition` parameter:

```java
TraversingCriterion traversingCriterion = TC.create()
	.negation()
		.property("partition")
	.done();

query.setTraversingCriterion(traversingCriterion);
user = session.query().select(query).first();

System.out.println(GMCoreTools.getDescription(user));
```

Running the query above results in the following:

```java
User[
  description = null
  email = null
  firstName = null
  globalId = null
  groups = [empty set]
  id = null
  lastLogin = null
  lastName = null
  name = null
  partition = 'auth'
  password = null
  picture = null
  roles = [empty set]
]

```

Let's say we want to see what roles the `cortex` user has. In the first call, only the `?` was returned. You could always use the `[...] .negation().joker().done();` traversing criterion, but that returns everything. To return the `roles` property, use the following:

```java
TraversingCriterion traversingCriterion2 = TC.create()
			    .negation()
			        .disjunction()
			            .pattern().entity(Role.T).joker().close()   
			            .pattern().entity(User.T).property(User.roles).close()
			        .close()
			    .done();
```

In the traversing criterion above we first specify that we want to return all information for the `Role` entity. Next, we specify that we want to return the property `roles` from the `User` entity. 

You might wonder why we're using a disjunction instead of a conjunction here. The reason is because of the negation. The negation of `A or B` is the statement `Not A and not B` and that is exactly what we want to achieve. We want to return the entire `Role` entity and the value of the `roles` property.

This traversing criterion returns:

```java
User[
  description = null
  email = null
  firstName = null
  globalId = null
  groups = [empty set]
  id = null
  lastLogin = null
  lastName = null
  name = null
  partition = null
  password = null
  picture = null
  roles = 1 element:
    element: Role[
      description = LocalizedString[
        globalId = null
        id = null
        localizedValues = [empty map]
        partition = null
      ]
      globalId = '576f3b33-0364-4127-9478-e6f21fdc9ff6'
      id = 'd14c4b72-c343-4d16-b049-2261215d75ec'
      localizedName = LocalizedString[
        globalId = null
        id = null
        localizedValues = [empty map]
        partition = null
      ]
      name = 'tf-admin'
      partition = 'auth'
    ]
]
``` 

