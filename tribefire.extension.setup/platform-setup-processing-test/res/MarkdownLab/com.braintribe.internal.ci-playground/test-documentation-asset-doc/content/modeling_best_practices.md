## General
A deep understanding of the organization's requirements is the first step towards an expressive model and a sustainable solution, followed by neat and effective modeling.

## First Steps and Approach
Top-down, one <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity type</a> at a time.

Always begin with a high-level understanding of the organization's requirements, before moving gradually towards the lower-level details. It's always good to start with a very simple <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">model</a> with basic terminology, for example, one containing just two entity eypes (`Company`, `Product`, and so on) and an association connecting them (products/services, 1-1 or 1-many), then afterwards adding properties and entity types to describe in more detail the requirements.

{%include note.html content="A model at the end of the day is nothing more than a description of a concept – the better and clearer the concept is understood the more expressive a model will be."%}

It's not necessary to model everything imaginable, some entity types, enums or relations don't need to be depicted. Like in the real world, what is more important than knowing everything is to focus on what is relevant to your requirements!

A useful aid to detect these requirements is to ask a number of questions:
* Is there already a model? Some organizations already have a modeled version of their data objects that can be used to create a model tribefire can work with. There's clearly no need to reinvent the wheel in this case. A database schema can also help!
* What entity types need to be drawn?
* What attributes (properties) should each entity type have -> (size, type, expiry date, and so on.)?
* What is the unique identifier for each entity type (ID property)?
* What enumerations can be drawn? What would their constants (fixed values) be? Do the constants need to change periodically? Example: The days of the week don't change, so it makes sense to use an enumeration.
* What entity types are properties of other entity types (complex types)?
* What entity types inherit their properties from others (A bank account is an account; a savings account is a bank account)? Are there any such entity types that require additional local properties?
* What associations/relations need to be drawn? Which are bidirectional (for example, an `Opportunity` entity has a complex property `Customer`, and vice versa. That is, the `Customer` entity also has a property called `Opportunity`. Which relation they both have with each other.)?
* What multiplicity do the associations have (1-1, 0/1-many, etc)?
* How are processes implemented, such as, a value that when changed triggering an action?
* Which properties are mandatory (for example, a ZIP code of an address)? Which properties have range boundaries (for instance, ordinal number of days in a year can be `1-365`)
* Does reverse engineering come into play? If a customer already owns a modeling tool, is it possible to extract a model from it, which can be used with tribefire.
* What naming conventions have to be taken into account (like, `camelCase`)?
* What namespace conventions should be used to prevent naming conflicts?

## Dos and Don'ts

DO  | DON'T
------- | -----------
Be sure to have a knowledge of available data objects you can describe with your model, for example, database tables, document in an ECM system, and so on.  | Don't model in the repository layer (for example, the database schema). Focus on the tribefire modeling layer, where the different sources are integrated. The database schema may not necessarily be identical with the integration model. Still, always keep the repository layer in mind, to know what data objects a <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.smart_access}}">smart access</a> will combine.
If you have to convert a conceptual model drawn on paper using a modeling tool, be as accurate as possible.  | Don't cause update/insertion/deletion anomalies when you modify an entity type that corresponds with a database table. Keep database tables normalized and eliminate redundancy/inconsistency.
Apply the 2-man-rule to ensure the model accurately mirrors the organization's requirements. Discussing with and showing a draft model to a trusted colleague will close reasoning gaps, make the ideation more concrete, bring reflection expressively to the surface, and create new food-for-thought. |
Keep the model open to extensions, time might ask for new versions!  |

## Design Tips
The following are, for the most part, recommendations from our experience, but not a rule that must always be applied, as they depend on the context of each use-case: not following them causes no technical error in tribefire, but can lead to big loss of data, time and space, especially in the long run.

### Association Types
* Single Aggregation

  {%include image.html file="single_agg.png"%}

  The logical association/relation of an Entity Type to another with a 1-1 analogy. For example, the class `Company` has one logo. Note the use of the verb `have` in natural language to describe this. To indicate it on paper we draw a line from one container Entity Type to another container Entity Type with an empty diamond shape on the container Entity Type that will contain the complex property.

* Multiple Aggregation

  {%include image.html file="multiple_agg.png"%}

  The logical association/relation of an Entity Type to another when the cardinality of a class in relation to another is depicted, the analogy here is 1-many. For example, one or more passengers in an airplane, and which can be ordered (list) or unordered (set). A fleet may include multiple airplanes. The notation 0..* in the diagram means “zero to many”. This can theoretically vary (3...15, exactly 4 and so on) but in tribefire the size is unbounded, so for Modeling it's enough to indicate `0..*` or `1..*`. This can be specified with the Metadata `RangeBoundary` in the Control Center.

* Reflexive Aggregation (aka self-reference)

  {%include image.html file="reflexive_agg.png"%}

  An Entity Type that refers to itself may have multiple functions/roles. For instance, a person with the role `Manager` has several employees who are also of the type `Person`! A `Staff` member working in an airport may be a pilot, aviation engineer, a ticket dispatcher, a guard, or a maintenance crew member. If the maintenance crew member manages themselves, it is a reflexive aggregation!

* Inheritance

  {%include image.html file="Inheritance.png"%}

  An Entity Type that is a child of another (parent) entity, it assumes the same properties as its parent. In other words, the child is a specific type of the parent and we use the `is-a` to describe it (A savings account is a bank account, a bird is an animal and an eagle is a bird). To depict inheritance in a UML diagram, a solid line from the child class to the parent class is drawn using an unfilled arrowhead. The child may also have locally-declared properties. In Modeler (unlike Generalizations in UML), the subtype points to the super-type.

### Mixing up Properties and Entity Types
Designers often do not know whether a model element should be shown as a property of an entity type or an entity type in its own right. Let's take the example of a bank that has many accounts, each of which has just one owner. The bank also needs to have information about the owner, like name, address and birthdate.

{% include image.html file="SimpleVsComplex1.png" max-width=600 %}

Modeling `owner` as a property in the example above will soon lead to a dead-end. The model loses in expressiveness of valuable information, and it will not be possible to load any instances of owners.

Do we need a simple or a complex type for a new model element? If it is a simple type(integer, string, decimal, long, etc) then it should be a property. If it is a complex type that has its own Properties, then it has to be an entity type. Or, if it is a complex type with fixed values (constants), it will be an enumeration.

{% include image.html file="newAccountOwner.png" max-width=600 %}

### Mixing up Aggregation and Inheritance
Another common mistake is to mix-up an aggregation with inheritance.

{% include image.html file="Inheritance1.png" max-width=600 %}

This modeling flaw leads to following results: entity types `CheckingAccount` and `SavingsAccount` will not inherit the Properties of `BankAccount`. The properties (`accountId`, `owner`, `balance`), therefore, will have to be declared for each type of account, leading to redundancy – also, changes to these Properties in one type of account will not be reflected in the other accounts, meaning that the model designer has to repeat the same work several times.

Do the entity types share the same Properties with each other? If the answer is yes, then it's an inheritance.

Example 1 (see screenshot above) - The entity type `CheckingAccount` is similar to the `BankAccount` because it also has an `accountId`, an owner and a balance. On top of that, the property `insufficientFund` is declared. The screenshot below shows the proper implementation: the entity type derives from `BankAccount`, and also declares the local property `interestRate`.

{% include image.html file="Inheritance2.png" max-width=600 %}

### Modeling Similar Entity Types
Another tip concerns enumerations: instead of introducing a bunch of similar Entities, the use of enumerations will usually produce a much clearer model without unnecessary entity types! The screenshot below shows an example with four similar entity types all describing kinds of Joins.

{% include image.html file="NewJoins-wrong.png" max-width=600 %}

An enumeration `JoinType` (with 4 respective constants) specifying the type of a Join resolves this issue and makes it simple to grasp and implement. This way tribefire reaches the necessary information in a shorter time and with higher efficiency.

{% include image.html file="JoinType-right.png" max-width=600 %}

### Mixing up List and Map
The differentiation between a `List` (or `Set`) and a `Map` often puzzles model designers. Many fall into the trap of not making use of the Map at all (making a model less expressive by leaving out valuable information). Others make a different mistake: they depict it as a aggregation property. In the screenshot below `Subsidiary` has an `Address` property, rather than `Company` having a relationship with `Subsidiary`, defined by the key-value `Address`.

{% include image.html file="Wrong-new5.png" max-width=600 %}

The result here is both inconsistency and redundancy that causes the reader confusion.

{% include image.html file="Map-new-right.png" max-width=600 %}

The solution is a Map that assigns a subsidiary to a company according to an address – each `Subsidiary` corresponds to just one `Address`. This enriches the model's information regarding the association between a company and its subsidiaries, while the model's consistency remains intact and performance increases.

### Modeling Descriptor / Lookup Entity Types
Another common pitfall is using a descriptor/lookup entity type containing data about instances. It might be something like a register of codes for medicine or the name of a local doctor. The concept often confuses a modeler's decision-making.

{% include image.html file="wrong-7.png" max-width=600 %}

entity types such as `DoctorList` are not practical and should not be modeled, because they don't have a meaningful purpose.

There are several other places we could have lookup tables: one to show the doctors, one for the drug treatments and even one for selecting patients. However, the model would be more complex and look very cluttered. Besides, this information can be found by simple queries on the associated entity type (Doctor, in this case). But beware: we cannot exclude their use altogether! If usage dictates that a look-up entity type is utilized regularly, then it's okay to implement it to avoid costly query calls.

How often is a lookup entity type used? Again the answer depends on the use-case. If a prescription book is consulted by a doctor to prescribe medication and its absence makes the scenario problematic, then model it by all means! Otherwise, drop it.

## Considerations for More Consistent and Expressive Models
These naming conventions create a common language for designers, so that they can understand each other better. Their usage has no technical impact on tribefire and therefore not applying them causes no errors. One exception: first item on this list must be followed, as it derives from Java naming requirements.

* Do the names of entity types, Enumerations, Properties and constants comply with Java requirements?
tribefire is based on Java naming conventions, and so these requirements must be adhered to! Therefore use only characters (`A...Z,a...z`), digits (`0..9`) and `_`. Do NOT use space, `;` , `?` , `/` and so on.

* Is the name of the entity type/Enumeration in singular?
Using the plural for entity types (`Doctors`, `Patients`) leads to confusion. Even worse, modelers create a new entity type (`Doctor`, `Patient`) and connect it to the pluralized version with a set/list aggregation!

* Is the name of the entity type/enumeration describing it in a unique way?
Leaving space for confusion can lead to redundant entity types (for instance, `Staff` can be interpreted in many ways). It should have one clear role (such as, `Developer`, `Pre-Sales_Professional`, `Trainer` and so on). If it can't be named easily, it may be a bad idea to include it altogether.

* Is the name of the entity type/enumeration a noun?
Using verbs leads the reader to think of an action (`eat`, `sleep`, and so on). Using noun phrases (like, `Revenues of high energy-prices`, `People in the station`) is also confusing, therefore prefer nouns.

* Is the name of the multiple aggregation (Set, List, Map) in plural?
Calling an aggregation of lions as `Lion` will most certainly confuse.

* Do composite names use the CamelCase convention?
For better readability use concatenated names with no separating characters: make sure new words begin with uppercase letters, for example, `SavingsAccount`, `HousingCredit` (even for constants like `atRisk`, `highChance`) and so on.

* Do property names start with the lowercase? entity types and enums with the uppercase?
For consistency use such names: `projects`, `employees`, `customer`, and so on. Whether properties are written in the singular or the plural depends on the multiplicity.
