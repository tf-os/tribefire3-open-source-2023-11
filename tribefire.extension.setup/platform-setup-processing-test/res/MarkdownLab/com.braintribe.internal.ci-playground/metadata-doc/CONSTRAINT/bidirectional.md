# Bidirectional

This metadata creates a relationship between two properties, so that when one is defined with a new value, the second property is automatically updated. Bidirectional properties are a pair of properties that are meant to represent the opposing sides of a relation.

Metadata Property Name  | Type Signature  
------- | -----------
`BidirectionalProperty` | `com.braintribe.model.meta.data.constraint.BidirectionalProperty`

## General

Using this metadata you can create relationships between two different properties, either within the same entity type or across different entities. This means that when you define a value for one property, the linked property is also updated. Basically, this metadata tells tribefire that when the configured property has been given a value, this value should also be added to the linked property.

An example of this is a relationship between `Employees` and `Company`. If you correctly configure an `Employee` and define the company they work for, the corresponding `employees` property in `Company` is automatically updated.

{%include note.html content="You must configure this metadata on each property that you wish to be affected. In the above example, you must configure both the `company` property of `Employee` and the `employee` property in `Company` with the bidirectional metadata. If you don't configure this metadata in both `Employee` and `Company`, the relationship works one way only."%}

## Example

To configure this metadata, you define the **Linked Property** field with the appropriate property that should be linked. This means that any value defined in this property is also defined in the linked property.

In this example, there are two entity types: `Attendee` and `Company`. The `Attendee` entity has various properties, including `company`. In the `Company` entity there is a corresponding property `employee`. The `company` property of `Attendee` is configured with the bidirectional metadata and the linked property `employee` is given. When you create an `Attendee` and assign a `Company` the `Company`'s `employee` property is automatically updated with the `Attendee`.

> If you wish this relationship to work in the other direction, you must also define the `employee` property of `Company` with a corresponding bidirectional property.
