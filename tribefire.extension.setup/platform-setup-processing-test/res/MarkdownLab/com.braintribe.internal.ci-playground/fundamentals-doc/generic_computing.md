# Generic Computing

## Rationale Behind GmCore
The reason why we came up with that paradigm is that usual high-level programming languages can't cope with the diversity of today's IT landscapes. Java is a good basis, but if you have to deal with different abstraction layers, different technologies, different languages and different data, you need a more comprehensive way to approach those domains. Yes, there is the paradigm of genericity, but first of all it's hard to read such code and secondly, it's not consistent enough.

We call our generic computing paradigm GmCore, which stands for **Generic Model Core**.

## Principles of GmCore

### Normalization
Austrian mathematician Kurt Gödel introduced his normalization principle so he could treat operands and operators the same way by assigning natural numbers to each component of a mathematical expression.

We extended this "gödelization" by mapping all kinds of concepts (data, functionality, operations, configuration, etc.) to model types and further mapping those <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a> to <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_instance}}">entity instances</a>.

This way, a consistent normalization is assured, which leads to a generic way of dealing with concepts from all domains.

### Separation of Concerns
By keeping definition (entity types) and executables (entity instances) separated, a solid separation of concerns is provided. Instances are interchangeable as the entity types assure the contract - similar to the interface-principle in object-oriented programming, but brought to a generic level.

### Expressive Genericness
The generic entity types are exposed as their own API which allows you to deal with them in an expressive way. A generic model defined via <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.gmcore}}">GmCore</a> describes a concrete element (e.g. a service implementation), so this model is concrete by itself. The GmCore framework allows to access the properties of the modeled types by automatically provided methods. Having this principle, genericity and expressiveness is no contradiction anymore. This leads to sustainable code that allows to implement generic patterns that can be used by any model.
