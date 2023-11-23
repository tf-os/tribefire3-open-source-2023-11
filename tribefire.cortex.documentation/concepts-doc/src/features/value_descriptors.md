# Value Descriptors
A Value Descriptor (represented by the `ValueDescriptor` type in Tribefire) is a normalisation of how different values are represented and persisted, regardless if they represent entities or other values within Tribefire.

## Usage
When configuring `ChangeValueManipulation` scripts for [templates](Templates/template.md), Value Descriptors are normally assigned to variables as their `defaultValue`, when you don't want to set a fixed value (which is normally the case in templates). The value is then evaluated upon the execution of the template-based action (for example as the name of the current user) and assigned to the prototype (beware of type mismatching!). The following descriptors are available in Tribefire:

## Cast Value Descriptors
These descriptors represent cast operations.

Value Descriptor | Function
--- | ---
Cast | A  `ValueDescriptor` that represents a cast from a `Number` type to another `Number` type. Allowed types are `Integer`, `Double`, `Float`, `Long`, `BigDecimal`.
DecimalCast | A `Cast` that will cast any number to `Decimal`.
DoubleCast | A `Cast` that will cast any number to `Double`.
FloatCast | A `Cast` that will cast any number to `Float`.
IntegerCast | A `Cast` that will cast any number to `Integer`.
LongCast | A `Cast` that will cast any number to `Long`.

## Context Value Descriptors
These descriptors relate the current state of a Tribefire instance.

Value Descriptor | Function
--- | ---
CurrentLocale | A `StringDescriptor` that represents the current Locale.
FocusedEntity | A `ValueDescriptor` for an entity, assuming the context of our resolution is given by a single entity.
UserName | A `StringDescriptor` that represents the logged-in user name.

## Conversion Descriptors
These descriptors represent conversion from one data type to another.

Value Descriptor | Function
--- | ---
Convert | A `ValueDescriptor` that represents a conversion from one data type to another.
FormattedConvert | A `Convert` that has a format that can be applied for the conversion.
ToBoleean | A `FormattedConvert` that converts to `Boolean`.
ToDate | A `FormattedConvert` that converts to `Date`.
ToDecimal | A `FormattedConvert` that converts to `Decimal`.
ToDouble | A `FormattedConvert` that converts to `Double`.
ToEnum | A `Convert` that converts to `Enum`.
ToFloat | A `FormattedConvert` that converts to `Float`.
ToInteger | A `FormattedConvert` that converts to `Integer`.
ToLong | A `FormattedConvert` that converts to `Long`.
ToString | A `FormattedConvert` that converts to `String`.

## Logic Descriptors

Value Descriptor | Function
--- | ---
Conjunction | A `Junction` that performs conjunction operation.
Disjunction | A `Junction` that performs disjunction operation.
Junction | A `BooleanDescriptor` that performs logical operations on list of boolean operands.
Negation | A `BooleanDescriptor` that negates boolean operand.

## Math Descriptors
These descriptors perform arithmetic operations.

Value Descriptor | Function
--- | ---
Add | An `ArithmeticOperation` that performs addition.
ApproximateOperation | An `ImplicitlyTypedDescriptor` that performs approximate operation on a value with respect to a precision. Allowed types are `numeric` ( int, long, float, double, decimal) and `date`.
ArithmeticOperation | An `ImplicitlyTypedDescriptor` that performs arithmetic operation on a list of operands.
Avg | An `ArithmeticOperation` that finds the average among a list of operands. The operands must be numeric. The returned type will be either a double or a decimal
Ceil | An `ApproximateOperation` that computes the ceiling of the value with respect to the precision.
Divide | An `ArithmeticOperation` that performs division.
Floor | An `ApproximateOperation` that computes the floor of the value with respect to the precision.
Max | An `ArithmeticOperation` that finds the largest value among a list of operands. The operands must be numeric.
Min | An `ArithmeticOperation` that finds the smallest value among a list of operands. The operands must be numeric.
Multiply | An `ArithmeticOperation` that performs multiplication.
Round | An `ApproximateOperation` that computes the rounding of the value with respect to the precision.
Subtract | An `ArithmeticOperation` that performs subtraction.

## Navigation Descriptors
These descriptors relate to navigation within Tribefire elements.

Value Descriptor | Function
--- | ---
PropertyPath | An `ImplicitlyTypedDescriptor` that represents a property path with respect to an entity.
ModelPath | An instance of `GmModelPath` which contains elements reflecting the navigation path shown in the GME.

## Predicate Descriptors
These descriptors resolve predicates.

Value Descriptor | Function
--- | ---
Assignable | A `BinaryPredicate` that represents an **assignable** operation.
BinaryPredicate | A `BooleanDescriptor` that represents a binary operation on two operands.
Equal | A `BinaryPredicate` that represents an **equal** operation.
Greater | A `BinaryPredicate` that represents a **greater** operation.
GreaterOrEqual | A `BinaryPredicate` that represents a **greater or equal** operation.
Ilike | A `BinaryPredicate` that represents an **Ilike** operation.
In | A `BinaryPredicate` that represents an **In** operation.
InstanceOf | A `BinaryPredicate` that represents an **InstanceOf** operation.
Less | A `BinaryPredicate` that represents a **Less** operation.
LessOrEqual | A `BinaryPredicate` that represents a **LessOrEqual** operation.
Like | A `BinaryPredicate` that represents a **Like** operation.
NotEqual | A `BinaryPredicate` that represents a **NotEqual** operation.

## String Descriptors
These descriptors represent string manipulation operations.

Value Descriptor | Function
--- | ---
Concatenation | A `StringDescriptor` that concatenates a list of string operands.
Localize | A `StringDescriptor` that represents the localisation of a string with respect to a certain locale.
Lower | A `StringDescriptor` that converts all uppercase character data to lowercase.
StringOperation | A `StringDescriptor` that represents a string manipulation operation on a string.
SubString | A `StringOperation` that extracts a substring given a mandatory `startIndex` and optional `endIndex`.
Upper | A `StringDescriptor` that converts all lowercase character data to uppercase.

## Time Descriptors
These descriptors represent time-related data.

Value Descriptor | Function
--- | ---
Now | A `DateDescriptor` that represents an instant of date, i.e. now.

# What's Next?
For information on how to use Value Descriptors, see [Assigning Value Descriptors to Templates](asset://tribefire.cortex.documentation:tutorials-doc/template/assigning_value_descriptors.md)