# Model Violations

When you trigger a model validation check, your model is checked for the following:

> Note that some of these checks will never fail if you use our Modeler to create your model, as Modeler makes sure not to assign null values. For more information about Modeler, see [Using Modeler](asset://tribefire.cortex.documentation:tutorials-doc/control-center/using_modeler.md).


Violation | Description
-----     | -----------
BASIC_METAMODEL_NULL | The model must not be null.
BASIC_METAMODEL_NAME_INVALID | The name of the model must not be null and must be longer than 0 characters. What's more, the name must contain a colon (`:`) and a dot (`.`) separated identifier sequence followed by a valid model name. The identifier sequence must not contain any Java-reserved words and must only consist of chars a-z (uppercase and lowercase), numbers, an underscore (`_`) and a dollar sign (`$`). The model name can contain Java-reserved words. An example of a valid model name is: `com.model.braintribe.yourmodel:your-model-name`. 
BASIC_METAMODEL_VERSION_INVALID | The version of the model must not be null or empty and must only contain numbers between 0-9 separated by a dot (`.`)  
BASIC_DECLARINGMODEL_NULL | Every enum or entity type must have a declaring model.
BASIC_TYPES_CONTAIN_NULL | None of the types in a model can be null. Every instance of a `GmCustomType` (entity or an enum type) must have a declaring model which must not be null. Entity types can have several supertypes but none of them can be null.
BASIC_TYPESIGNATURE_INVALID | The type signature must contain a dot (`.`) separated identifier sequence and must only consist of chars a-z (uppercase and lowercase), numbers, an underscore (`_`) and a dollar sign (`$`) with no Java keywords. An entity or enum type signature must contain at least one dot (`.`) and after the last dot a  simple class name (which must start with an uppercase letter) must follow.
BASIC_TYPESIGNATURE_NOT_UNIQUE | The type signature must be unique. 
BASIC_ENTITYTYPE_SUPERTYPES_CONTAIN_NULL | All supertypes an entity type may have must not be null.
BASIC_ENTITYTYPE_PROPERTIES_CONTAIN_NULL | All properties an entity type may have must not be null.
BASIC_ENTITYTYPE_PROPERTY_NAME_INVALID | All properties in an entity type must have a name which must not contain any Java-reserved words and must only consist of chars a-z (uppercase and lowercase), numbers, an underscore (`_`) and a dollar sign (`$`). What's more, the first character must not be a number, and the first and second characters must not be uppercase.
BASIC_ENTITYTYPE_PROPERTY_NAME_NOT_UNIQUE | In the same entity type properties must not have the same name.
BASIC_ENTITYTYPE_PROPERTY_BACKREFERENCE_NULL | All properties in an entity type must have a declaring type which must not be null.
BASIC_ENTITYTYPE_PROPERTY_BACKREFERENCE_NO_MATCH | All properties in an entity type must refer back to the entity type where they were declared in.
BASIC_ENUMTYPES_CONTAIN_NULL | All constants in an enum type must not be null.
BASIC_ENUMTYPE_CONSTANT_NAME_INVALID | All constants in an enum type must have a name which must only consist of chars a-z (uppercase and lowercase), numbers, an underscore (`_`) and a dollar sign (`$`). The first character must not be a number.
BASIC_ENUMTYPE_CONSTANT_NAME_NOT_UNIQUE | In the same enum type constants must not have the same name.
BASIC_ENUMTYPE_CONSTANTS_CONTAIN_NULL | All constants in an enum type must not be null.
BASIC_ENUMTYPE_CONSTANT_BACKREFERENCE_NULL | All constants in an enum type must must have a declaring type which must not be null.
BASIC_ENUMTYPE_CONSTANT_BACKREFERENCE_NO_MATCH | All constants in an enum type must refer back to the enum type where they were declared in.
