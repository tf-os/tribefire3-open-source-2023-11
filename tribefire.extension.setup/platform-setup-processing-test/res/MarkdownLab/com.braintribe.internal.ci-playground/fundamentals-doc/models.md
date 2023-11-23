# Models

## What are Models?
Basically, a model declares a number of entity types and enum types which represent something: from low-level integration data to APIs for app development. Therefore, understanding modeling is vital to understanding tribefire itself.

## Model Structure
* model
  * `name`
  * `version`
  * `dependencies`
  * `enumConstantMetaData`
  * `enumTypeMetadata`
  * `metaData`
  * `typeOverrides`
  * `types`

Model Element           | Description
------                  | ----------
`name`                  | Name which identifies the model, for example `car-model` or `api-response-model`.
`version`               | Version of the model.
`dependencies`          | Other models this model uses <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a> from. 
`enumConstantMetaData`  | <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">Metadata</a> to be applied to all enum constants of all enums in this model.
`enumTypeMetaData`      | Metadata to be applied to all enum types in this model.
`metaData`              | Metadata to be applied to this model.
`typeOverrides`         | Overrides allow to apply metadata to an element of a model without changing the original element. Without an override, a metadata conﬁguration on a type from a model's dependency is applied globally. With an override, this can be avoided so the conﬁguration will only apply to your local model.
`types`                 | Entity types this model contains.

{%include tip.html content="For more information, see [Models in-depth](models_in-depth.html). "%}

### Allowed Property Types

{% include allowed_property_types.md %}

## Methods of Modeling
There are a variety of methods that you can use to model for tribefire, although we recommend that you use tribefire Modeler, an easy-to-use graphical modeler inside <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a>.

## Code Representation of Models
In Java, a type is always described by an interface extending one or more other interfaces. By using specific type safe interfaces, the Java representation supports compiler constraints, code completion, hyperlink detection, syntax coloring, refactoring, and debug introspection. This kind of representation supports expressive coding against specific models.

{% include tip.html content="See also: [model-as-API](model-as-API.html)" %}

The interface type hierarchy must always end with `GenericEntity`. The interface can contain a number of setter/getter pairs that use one of the model types as return and parameter type. Additionally a type literal named `T` and pointing to the according `EntityType` is mandatory.
```java
@Abstract
    interface HasName extends GenericEntity {
        final EntityType<HasName> T = EntityTypes.T(HasName.class);
        void setName(String name);
        String getName();
    }
    @Abstract
    interface HasBirthday extends GenericEntity {
        final EntityType<HasBirthday> T = EntityTypes.T(HasBirthday.class);
        Date getBirthday();
        void setBirthday(Date birthday);
    }
    @ImplementAbstractProperties
    interface Person extends HasName, HasBirthday {
        final EntityType<Person> T = EntityTypes.T(Person.class);
        Person getFather();
        void setFather(Person father);
        Person getMother();
        void setMother(Person mother);
    }
```
{% include apidoc.html %}

### Models in Code vs Models in Modeler
While models can be projected to Java after designing them in a UI modeler you also can design them in Java and project them into the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metamodel}}">metamodel</a> representation.

## Depending on Models
You can depend on custom models deployed to <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.tribefire_repository}}">tribefire repository</a> by introducing them in your project's `pom.xml`.

{%include tip.html content="For more information about creating custom projects, see [Cartridge](back_end.html#cartridge) tutorials. "%}