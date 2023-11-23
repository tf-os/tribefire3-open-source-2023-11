# Property Lookups

Property lookups are a normalized way to access named string values as you find them in simple property accessors like

* [`System.getenv()`](javadoc:java.lang.System#getenv(java.lang.String))
* [`System.getProperty()`](javadoc:java.lang.System#getProperty(java.lang.String))

Using such lookups directly implies the following cross cutting concerns:

* referential integrity
* mandatory checks
* defaulting
* validation
* type-safe parsing
* decryption

To cover those cross cutting concerns, property lookups provide a generic implementation that enhances the actual lookup by generating [implementations](#usage-example) based on the [Java Proxy Feature](javadoc:java.lang.reflect.Proxy) and a declaration of properties - provided by the user - with the following Java principles:

* [Mandatory custom interfaces](#property-access-interfaces) with method declarations that represent a property each
* Optionally applied predefined [annotations](#supported-annotations) on those methods

## Property Access Interfaces

Property access interfaces define accessible type-safe properties by method declarations.

The signatures of those methods are very restricted to be for property access. The name of the method defines the name of the property, while its [return type](#supported-property-types) defines the type to which an automatic conversion from the given string value happens.

Optionally, a method can be parameterized with a single parameter whose type must be equal to the method's return type. This parameter serves as a user defined default value. Furthermore, the method can be [annotated](#supported-annotations) to control additional features of property access.  

As property access interfaces have no method bodies they serve as inventories of property expectations of some software component as they act as a central place to define properties. You can also get an overview of all the properties and document them in the same place.

The following example shows different cases:

```java
public interface XyzProperties extends PropertyLookupContract {

    // Simplest case of a property with a primitive
    // that implies the default value false
    boolean PARALLEL_PROCESSING();

    // The @Default annotation defines 5 as the default value
    // which is used if there is no explicit value given
    @Default("5")
    int MAX_DB_CONNECTIONS();

    // The accessor has to supply a default value
    // that is used if there is no explicit value given
    String HOST_NAME(String defaultHostName);

    // The property value has to be given explicitly
    @Required
    Duration CHECK_INTERVAL();

    // The @Name annotation defines the actual property name. Use it if a java method identifier would be invalid because of e.g. special characters.
    @Name("CHECK-VALIDITY")
    String checkValidity();

    // An example for a collection property
    List<File> MAPPING_FILES();
}
```

### Supported Property Types

Properties can have one of the supported [base types](#base-types) or [collection types](#collection-types).

#### Base Types

|Type|Implicit Default|String Format|
|---|---|---|
|[Boolean](javadoc:java.lang.Boolean) / `boolean`|`false`|`true` &#124; `false`|
|[Character](javadoc:java.lang.Character) / `char`|`'\u0000'`|A &#124; b &#124; 1 &#124; ...
|[Byte](javadoc:java.lang.Byte) / `byte`|`0`|[255](javadoc:java.lang.Byte#parseByte(java.lang.String))|
|[Short](javadoc:java.lang.Short) / `short`|`0`|[4711](javadoc:java.lang.Short#parseShort(java.lang.String))|
|[Integer](javadoc:java.lang.Integer) / `int`|`0`|[4711](javadoc:java.lang.Integer#parseInt(java.lang.String))|
|[Long](javadoc:java.lang.Long) / `long`|`0`|[4711](javadoc:java.lang.Long#parseLong(java.lang.String))|
|[Float](javadoc:java.lang.Float) / `float`|`0.0`|[3.141592](javadoc:java.lang.Float#parseFloat(java.lang.String))|
|[Double](javadoc:java.lang.Double) / `double`|`0.0`|[3.141592](javadoc:java.lang.Double#parseDouble(java.lang.String))|
|[BigDecimal](javadoc:java.math.BigDecimal)|`0.0`|[3.141592](javadoc:java.math.BigDecimal#BigDecimal(java.lang.String))|
|[String](javadoc:java.lang.String)|`null`|some text|
|[Date](javadoc:java.util.Date)|`null`|<i>yyyy-MM-dd['T'HH[:mm[:ss[.SSS]]]][Z]</i> &#124; <i>yyyyMMdd|['T'HH[mm[ss[SSS]]]][Z]</i>|
|[Duration](javadoc:java.time.Duration)|`null`|<i>P[nD]T[nH][nM][n.nS]</i>|
|[File](javadoc:java.io.File)|`null`|some/path/to/a/file.txt|
|[Path](javadoc:java.nio.file.Path)|`null`|some/path/to/a/file.txt|
|[Class](javadoc:java.lang.Class)|`null`|foo.bar.SomeClass|
|[Enum](javadoc:java.lang.Enum)|`null`|ENUM_CONSTANT|

#### Collection Types

Collection types use delimiters in their patterns to separate elements, keys and values from each other. If you want to use these delimiters, take care of proper [url escaping](javadoc:java.net.URLDecoder#decode(java.lang.String)).

|Type|Pattern|
|---|---|
|[List](javadoc:java.util.List)|value1`,`value2`,`...|
|[Set](javadoc:java.util.Set)|value1`,`value2`,`...|
|[Map](javadoc:java.util.Map)|key1`=`value1`,`key2`=`value2`,`...|


### Supported Annotations

|Type|Description|
|---|---|
|[@Required](javadoc:com.braintribe.cfg.Required)|Defines that a property is mandatory. If the actual lookup has no value given, an [IllegalStateException](javadoc:java.lang.IllegalStateException) is thrown|
|[@Default](javadoc:com.braintribe.wire.api.annotation.Default)|Defines a default value for a property in its string representation in case the actual lookup has no value.|
|[@Name](javadoc:com.braintribe.wire.api.annotation.Name)|Defines the name of the accessed property. This overrides the default behavior that the name of the accessor method defines the property name.|
|[@Decrypt](javadoc:com.braintribe.wire.api.annotation.Decrypt)|Defines that the property value should be decrypted with a parameterized algorithm. This allows to avoid direct exposure of confidential information on screen when editing or viewing property assignments.|

## Usage Example

In order to [create](javadoc:com.braintribe.wire.impl.properties.PropertyLookups#create(java.lang.Class,java.util.function.Function)) and access a lookup implementation with all cross cutting concerns see following example which uses  the [example interface](#property-access-interfaces) and standard java system  environment variables as actual lookup:

```Java
public class Main {

    public static void main(String args[]) {

        // create a custom property access implementation
        XyzProperties properties = PropertyLookups.create(
            // use the property access interface
            XyzProperties.class,
            // use System.getenv() function as actual backend lookup
            System::getenv
        );

        // access one property with referential integrity and type-safety
        if (properties.PARALLEL_PROCESSING())
            System.out.println("Parallel processing is active");
    }

}
```
