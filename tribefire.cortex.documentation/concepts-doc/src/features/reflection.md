# Reflection

Type reflection allows objects to get information about other object types, as well as its own type.

At its most basic, type reflection provides an API that allows you to get information about the structures of other objects located in tribefire, allowing the implementation of dynamic coding without the need to know beforehand exactly what structure an entity might take.

Reflection also allows you, among other functionality, to check if an entity is an instance or a subclass of another class.

Type reflection can be accessed either by the `GMF.getTypeReflection()` method call:

```java
GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
```

or by accessing it directly on the entity type:

```java
Person.T.create().entityType().getTypeSignature();
// or
Person.T.getTypeSignature();
```

## Reflection and Types

Type reflection is based around the different types available in tribefire. The API is therefore modeled around a type hierarchy, with each class representing a different type, for the supertype `GenericModelType` through its various subtypes. It provides a basic structure for every type described in tribefire.

Remember that each type represents the structure of that type, rather than any particular instance. For example:

```java
EntityType entity = GMF.getTypeReflection().getEntityType(Person.class);
// or
Person.T.create().entityType();
// or
EntityType<Person> person = Person.T;
```

The calls above return an `EntityType` object that can be used to get reflected information on the `Person` entity type returned. This returns basic information about the `Person` entity; what properties it contains, type signature, and so on.

This is the same pattern for each different type. A `GMF.getEnumType()` method call will return a `EnumType` and a `GMF.getGenericModelType()` will return a `GenericModelType` object.

```java
EnumType someEnum =  GMF.getTypeReflection().getEnumType(KeyfactType.class);
// or
GenericModelType someGMT = GMF.getTypeReflection().getType(Person.class);
```


## Reflection and Properties

Once you have the type, you can then investigate it further, to see its properties:

```java
List<Property> someList  = GMF.getTypeReflection().getEntityType((Class<? extends GenericEntity>) Person.class).getProperties();
// or
GenericModelType gender = Person.T.create().entityType().getProperty("gender").getType();
```

The above example will return all properties belonging to the `EntityType` for `Person` or will return the type of the `gender` property.

You can also carry out some functionality, for example cloning an instance, or using the traverse methods to traverse an entity instance, according to a valid traversing criteria. You can then override the relevant interfaces to carry out some custom functionality.

```java
Person person = Person.T.create();
person.setFirstName("Mateusz");
Person person2 = person.clone(new StandardCloningContext());
System.out.println(person.getFirstName());
System.out.println(person2.getFirstName());
```

The above will print the name `Mateusz` twice, because the `person` instance was cloned.

> For more information on traversing criteria see [Traversing Criteria](asset://tribefire.cortex.documentation:concepts-doc/features/traversing_criteria.md) and [Traversing Criteria in Detail](asset://tribefire.cortex.documentation:concepts-doc/features/traversing_criteria_in_detail.md).

<!-- TODO: ADD A LINK TO REFLECTION JAVADOC -->