# Negation Selector

The negation selector allows you to negate the action of metadata.

## General

The `operand` property accepts a further metadata selector, either a discriminator, a [use case selector](use_case_selector.md), or [role selector](role_selector.md). If the selected sector is matched, the metadata is not resolved.

## Example

This selector has only one definable property: `Operand`.

![](../images/NegationSelector02.png)

Double click the `Operand` property to chose a metadata selector. You can choose one of the following options:

* discriminator
* use case selector
* role selector

> You can also choose a further `NegationSelector` instance. However, this negates a negation, meaning it has no effect at all.
