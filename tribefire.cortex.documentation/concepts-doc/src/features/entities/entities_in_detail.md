# Entities in Detail

Every entity type in tribefire extends the `GenericEntity` type. Because of that hierarchy, entities share certain functionalities.

## General

To start discussing entities, we have to note the distinction between entity types and entity instances.

Component | Description  
------- | -----------
entity type | A generic blueprint which defines the data structure contained in its instances. We have a number of entity types available, but let us consider the entity type `Person`. The model defines the data structure of every single instance of `Person` but has no real data on its own.
entity instance | An instantiated entity type populated with actual data. Let us consider the `Person` entity again. Each instance of the entity type `Person` represents a different person, with different names, social security numbers, etc.

## GenericEntity

The `com.braintribe.model.generic.GenericEntity` class is the base type for all entity types.  

We distinguish two basic entity types:

* plain entities
* enhanced entities

## Plain Entities

Plain entities have no potential for `PropertyAccessInterceptors` and their cross-cutting concerns. Therefore, property access is much faster and they remain reflectable. However, because of the simple nature of plain entities, certain features are not available:

* session attachment
* manipulation tracking
* collection properties of plain entities can be null and must be checked for this state

> In most cases you should not work with plain entities. Plain entities are useful for fast algorithms that frequently access properties without the need to address the cross-cutting concerns.

To create a plain entity with initialized properties you use the `createPlain()` method of the type literal `T`: `MyEntity instance = MyEntity.T.createPlain();`

To create plain entities without initialized properties (raw plain entities) you use the `createPlainRaw()` method of the type literal `T`: `MyEntity instance = MyEntity.T.createPlainRaw()`

If you want to transform enhanced entities into plain entities, you can use the `CloningContext.supplyRawClone()` method.

## Enhanced Entities

Enhanced entities have property access interceptors and thus support cross-cutting concerns. One standard cross-cutting concern is ensuring non-null collections. Enhanced entities can be attached to a session and by this use the session’s property access interceptor configuration. When attached to a session with a configured `ManipulationTrackingPropertyAccessInterceptor`, this interceptor notifies the tracked manipulations to the session which may record them or propagate them to further consumers.

Due to the interceptor pattern, access to the properties of enhanced entities is slower than access to the properties of plain entities. As opposed to plain properties, you gain a lot of important features that normally would also slow down your processes a bit.

To create plain entities with initialized properties you use the `create()` method of the type literal `T`: `MyEntity instance = MyEntity.T.create();`

To create plain entities without initialized properties (raw entities) you use the `createRaw()` method of the type literal `T`: `MyEntity instance = MyEntity.T.createRaw();`

As enhanced entities can be attached to a session, you can create an entity and assign it to a session in a single call.

To conveniently create enhanced session-bound entities with initialized properties you use the `create()` method of your session object: `MyEntity instance = session.create(MyEntity.T);`

To conveniently create enhanced session-bound entities without initialized properties you use the `createRaw()` method of your session object: `MyEntity instance = session.createRaw(MyEntity.T);`

## Type Literal `T`

Every entity in tribefire has a static field `T`, which is known as the type literal. Essentially, `Person.T` returns an object which represents the `Person` entity.

> You have to define the `T` literal for every custom entity. Otherwise, the supertype entity is returned. If your supertype is abstract, this method fails.

### Defining the Type Literal

The type literal is defined in the following way:

```java
EntityType<ENCLOSING_TYPE> T = EntityTypes.T(ENCLOSING_TYPE.class);
```

For example, the declaration of the type literal for the `Person` entity looks as follows:

```java
EntityType<Person> T = EntityTypes.T(Person.class);
```
