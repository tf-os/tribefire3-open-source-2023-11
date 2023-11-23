
=========================================================================================================================
== HIBERNATE MAPPING GENERATOR FOR GENERIC ENTITY MODELS ================================================================
=========================================================================================================================

	1. PURPOSE
	2. USAGES
		2.1. JAVA SERVICE
	3. PARAMETERS
		3.1. input
		3.2. output
		3.3. tablePrefix
		3.4. allUppercase
		3.5. targetDb
		3.6. typeHints
		3.7. typeHintsFile
		3.8. typeHintsOutputFile
	4. TYPE HINTS
		4.1. META DATA HINTS
		4.2. LEGACY FORMAT HINTS
	5. GENERAL KNOWN LIMITATIONS
		5.1. UNDERSTANDING THE HIBERNATE CLASSIFICATION OF ENTITY TYPES.
			5.1.1. LEAF-NODE INSTANTIABLE TYPES
			5.1.2. MEMBER REFERENCED TYPES
			5.1.3. MERGE OF OVERLAPPING HIERARCHIES
		5.2. UNMAPPABLE MODEL SCENARIOS
			5.2.1. INVALID TOP-LEVEL ENTITY (HIBERNATE CLASS)
			5.2.2. NO COMMON SUPER CLASS FOR OVERLAPPING HIERARCHIES
		5.3. ATTRIBUTE QUERY INCOMPATIBILITY IN MERGED HIERARCHY MODELS
		5.4. COVARIANT RETURN TYPES


-------------------------------------------------------------------------------------------------------------------------
-- 1. PURPOSE
-------------------------------------------------------------------------------------------------------------------------

	Provided to generate Hibernate Mapping XML files from Generic Entity Meta Model.
	
	The mapping XML files are generated following the specification for Hibernate version 4.3
	
	Hibernate 4.3 documentation can be found at: https://docs.jboss.org/hibernate/orm/4.3/manual/en-US/html/


-------------------------------------------------------------------------------------------------------------------------
-- 2. USAGES
-------------------------------------------------------------------------------------------------------------------------

	The Hibernate Mapping Generator can be executed through Java code.


---- 2.1. JAVA ----------------------------------------------------------------------------------------------------------

	The HbmXmlGeneratingService class (see artifact com.braintribe.model.processing.deployment:HibernateMappingGenerator) 
	provides a public interface for invoking the generator. 

		e.g.:
	
		//instantiate the service
		HbmXmlGeneratingService generatorService = new HbmXmlGeneratingService();
		
		//set the parameters
		generatorService.setGmMetaModel(...);
		generatorService.setOutputFolder(...);
		generatorService.setTablePrefix(...);
		generatorService.setAllUppercase(...);
		generatorService.setTargetDb(...);
		generatorService.setTypeHints(...);
		generatorService.setTypeHintsFile(...);
		generatorService.setTypeHintsOutputFile(...);
		
		//render the hibernate mapping files
		generatorService.renderMappings();


-------------------------------------------------------------------------------------------------------------------------
-- 3. PARAMETERS
-------------------------------------------------------------------------------------------------------------------------

	The parameters for the generator are the same in either Java or Ant invocation methods:

	1. input (named gmMetaModel in the java service)
	2. output (named outputFolder in the java service)
	3. tablePrefix
	4. allUppercase
	5. targetDb
	6. typeHints
	7. typeHintsFile
	8. typeHintsOutputFile

---- 3.1. PARAMETER input (gmMetaModel) ---------------------------------------------------------------------------------

	Path to the meta model to be mapped.
	
	If the input expects a xml, use the model exposure "condensed".
	

---- 3.2. PARAMETER output (outputFolder) -------------------------------------------------------------------------------

	Output directory of the generated hibernate mapping XML files.
	

---- 3.3. PARAMETER tablePrefix -----------------------------------------------------------------------------------------

	A string to be used as prefix for all tables in the hibernate mappings.
	
	This information will be only used if the table name wasn't provided through other means.
	e.g.: EntityMapping meta data attached to the entities or typeHints parameter.
	

---- 3.4. PARAMETER allUppercase ----------------------------------------------------------------------------------------

	If set to true, the data base (table/column) names values will be all upper-case in the hibernate mapping XML.
	

---- 3.5. PARAMETER targetDb --------------------------------------------------------------------------------------------

	Name of the target data base system. This is used to assist the data base names (column and table names) generation 
	by providing a set of reserved words and maximum name length for the given data base system.
	If this parameter is left unset (or set to an not yet configured target data base system), a most restrictive 
	configuration will be used considering the union of all configured database system's reserved words and the most 
	restrictive name length constraint (currently 30 characters). 
	

---- 3.6. PARAMETER typeHints -------------------------------------------------------------------------------------------

	Some of the hibernate mapping attributes can be explicitly overwritten by this property.
	If set, the typeHints parameter must be either:
	
		1) a string composed by one or more comma-separated type hints;
		2) a string representation fo a JSON map following the expected structure for type hints.
		
	The type hint syntax can be seen in detail in the TYPE HINTS section.
	

---- 3.7. PARAMETER typeHintsFile ---------------------------------------------------------------------------------------

	Alternatively to the typeHints property, the typeHintsFile makes it possible to provide a path to a file containing 
	the type hints.
	
---- 3.8. PARAMETER typeHintsOutputFile ---------------------------------------------------------------------------------

	If informed, the generator will create a hints file describing the database names generated during its processing.
	
	The generated file can then be used as the input for the typeHintsFile parameter in a subsequent run, to ensure that
	the database names will be consistent for the entities and entity properties.

	If previously existent, the target file will be overwritten.
	
	As the generated output uses the legacy hints format, the use of this feature is discouraged.

-------------------------------------------------------------------------------------------------------------------------
-- 4. TYPE HINTS
-------------------------------------------------------------------------------------------------------------------------

	Ideally, the input model is enriched (contains meta data specifically designed to infer ORM mappings), and no further 
	parameter needs to be provided for assisting the mappings generation. 

	But in some cases, the input model is a skeleton (no meta data included) and additional information must be provided to the 
	generator in order to produce the expected mappings.

	Additional information provided to Hibernate Mapping Generator in order to assist the creation of the mapping files
	are what we call type hints.
	
	Currently, type hints can only be provided as JSON objects, and two structures are supported:
	
	Meta data hints, a json map associating entities and properties to meta data which could also been present in the meta model
	
	Legacy hints, a json array of plain objects proving additional information in a way similar (but unrelated) to meta data.


---- 4.1. META DATA HINTS -----------------------------------------------------------------------------------------------

	Available since version 1.3, the meta data hints provide a way of utilizing meta data specifically designed to infer 
	ORM mappings even when the meta model lacks of meta data.
	
	The expected format is a GM JSON of a map where the key is a String denoting the target and the value is the meta data 
	instance itself
	
	Currently, two types of meta data are supported:
	
		com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping
		com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping
	
	NOTE: In order to use the EntityMapping and PropertyMapping meta data, please refer to the official documentation at:
		  https://confluence.braintribe.com/display/TD/Hibernate+Mapping
		  
	For associating a EntityMapping with an entity type, use the type signature as the key, whereas for associating a 
	PropertyMapping with a property, use as the key the type signature plus the property name, separated by the hash 
	character '#'.
	
	Example:
	
    {
      "_type": "map",
      "value": [
        {
          "key": "com.braintribe.model.MyEntity",
          "value": {
            "_type": "com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping",
            "tableName": "MY_ENTITY_TABLE"
          }
        },
        {
          "key": "com.braintribe.model.MyEntity#myStringPropertyA",
          "value": {
            "_type": "com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping",
            "type": "clob",
    		"lazy": "true"
          }
        },
        {
          "key": "com.braintribe.model.MyEntity#myStringPropertyB",
          "value": {
            "_type": "com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping",
            "type": "string",
    		"length": {
    			"value":"1000", 
    			"_type":"long"
    		},
    		"unique": true
          }
        }
      ]
    }

---- 4.2. LEGACY FORMAT HINTS -------------------------------------------------------------------------------------------

	NOTE: This format is deprecated since 1.3, prefer the meta data hints over the legacy formated hints.

	Available since version 1.1, the following JSON format can be used to explicitly determine Hibernate mapping hints
	for generic model entities and properties:
	
		{ 
			"com.braintribe.model.MyEntity": {
				"table": "MY_ENTITY_TABLE",
				"properties": {
					"textProp":               { "type": "string", "length": 1000 },
					"clobProp":               { "type": "clob" },
					"collectionProp":         { "type": "string", "length": 2000, "table": "MY_ENTITY_LIST_TABLE" },
					"otherProp":              { "column": "MY_CUSTOM_COL_NAME" },
				}
			}
		}
		
	All attributes are optional and can be omitted from the json structure.
	
	Multiple entities can be denoted in the following fashion:
	
	
		{ 
			"com.braintribe.model.MyEntityA": {
				"table": "MY_ENTITY_A",
				"properties": {
					"myStringProperty":       { "type": "string", "length": 1000 }
				}
			},
			"com.braintribe.model.MyEntityB": {
				"table": "MY_ENTITY_B",
				"properties": {
					"myLongStringProperty":    { "type": "clob" }
				}
			},
			"com.braintribe.model.MyEntityC": {
				"table": "MY_ENTITY_C",
				"properties": {
					"someCollectionProperty":  { "table": "MY_ENTITY_C_LIST" }
				}
			}
		}
	
	
---- 4.2.1. ENTITY LEVEL ATTRIBUTES -------------------------------------------------------------------------------------


	At entity level, there are two available properties: "table" and "discColumn".
	
	1) "table": 
		
		Defines the table name. This attribute will only be used if the entity is elected as a top-level entity, and 
		therefore represented in hibernate mapping as a <class/>.
		
		Example:
		
		The following json hint:
		
		{ 
			"com.braintribe.model.MyEntityA": {
				"table": "MY_ENTITY_A_TABLE"
			}
		}
		
		Will result in the following "table" attribute for the <class/> node in the hibernate mapping xml file:
		
			<class name="com.braintribe.model.MyEntityA" table="MY_ENTITY_A_TABLE" ... >
			
			
	2) "discColumn":
	
		Defines the name of the discriminator column. This attribute will only be used if:
						 
		a) the entity is elected as a top-level entity, and therefore represented in hibernate mapping as a <class/>
		b) there are <subclass/> referencing the hinted entity in the "extends" attribute.

	
	
---- 4.2.1. PROPERTY LEVEL ATTRIBUTES -----------------------------------------------------------------------------------


	At property level, it is possible to define the following properties:


	1) "type":
	
		Defines the type of the property. If used in set/list/maps, defines the type of the elements.
		If used in maps, this property designates the type of the map values.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "type": "clob" }
				}
			}
		}
		
		If myProperty is a direct property:
		
		    <property name="myProperty" type="clob" ... >
		    
		
		If myProperty is a List, Set or Map, the "type" is applied to the <element/>:
		
 			<[list|set|map] name="myProperty" ... >
	 			...
 				<element type="clob" ... />
			</[list|set|map]>
		
		    
	2) "length":
	
		Defines the length of the property. If valued to 0, the property is ignored.
		
		This attribute is only applied if the "type" is also given.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "length": 400, "type": "string" }
				}
			}
		}
		
		If myProperty is a direct property:
		
 			<property name="myProperty" type="string">
			  <column length="400" ... />
			</property>
		    
		
		If myProperty is a List, Set or Map, the "length" and "type" are applied to the <element/>:
		
 			<[list|set|map] name="myProperty" ... >
	 			...
 				<element length="400" type="string" ... />
			</[list|set|map]>
			
			
		Note: 
		
		As per Hibernate 3.6 documentation, the "length" attribute applies only if a string-valued column is used.
		For defining the length of numeric columns, use "precision" and "scale".
			
		
	3) "precision":
	
		Defines the decimal precision of the property.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myNumericProperty": { "precision": 20 }
				}
			}
		}
		
		Will result in the following "precision" attribute for the <column/> element within the Hibernate mapping xml file:
		
 			<property name="myNumericProperty">
				<column precision="20" ...  />
			</property>
			
		
	4) "scale":
	
		Defines the decimal scale of the property.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myNumericProperty": { "scale": 2 }
				}
			}
		}
		
		Will result in the following "precision" attribute for the <column/> element within the Hibernate mapping xml file:
		
 			<property name="myNumericProperty">
				<column scale="2" ...  />
			</property>
			
		
	5) "keyType":
	
		Defines the type of the key of a map property. 
		This property is only applied if the target property is a Map.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "keyType": "string" }
				}
			}
		}
		
		If and only myProperty is a Map, the "type" is applied to the <map-key/>:
		
 			<map name="myProperty" ... >
	 			...
				<map-key type="string" ... />
			</map>
		
		
	6) "keyLength":
	
		Defines the length of the key of a map property. If valued to 0, the property is ignored.
		This property is only applied if the target property is a Map.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "keyLength": 500 }
				}
			}
		}
		
		If and only myProperty is a Map, the "length" is applied to the <map-key/>:
		
 			<map name="myProperty" ... >
	 			...
				<map-key length="500" ... />
			</map>

		
	7) "table":
	
		Many-to-many table name for collection/map properties.
		This property is only applied if the target property is either a Set, List or Map.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "table": "MY_MANY_TO_MANY_TABLE" }
				}
			}
		}
		
		If and only myProperty is a List, Set or Map, the "table" is applied to the <list/>, <set/> or <map/> node:
		
 			<[list|set|map] name="myProperty" table="MY_MANY_TO_MANY_TABLE" ... >
				...
			</[list|set|map]>
		
		
	8) "column":
	
		Defines the column name for the property.
		This property is ignored if the target property is either a Set, List or Map.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "column": "MY_CUSTOM_COL_NAME" }
				}
			}
		}
		
		Will result in the following "name" attribute in the <column/> node for the property:
		
 			<property name="myProperty" ... >
 				...
			  	<column name="MY_CUSTOM_COL_NAME" ... />
			</property>
			
			
	9) "oneToMany":
	
		If set to "true", the entity element of this collection will be mapped as <one-to-many/> instead of the default <many-to-many/>.
		
		This property is only applied if the target property is a Collection (Set, List or Map) of entities.

		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "oneToMany": true }
				}
			}
		}
		
		If and only myProperty is a List, Set or Map of entities, the <one-to-many/> is used instead of <many-to-many/> as <list/>, <set/> or <map/> node child:
		
 			<[list|set|map] name="myProperty" ... >
				...
				<one-to-many class="com.test.SomeEntityType" />
			</[list|set|map]>
			


	10) "keyColumn":
	
		Defines the column name for key property of a many-to-many table.
		This property is only applied if the target property is either a Set, List or Map.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "keyColumn": "MY_CUSTOM_KEY_COLUMN_NAME" }
				}
			}
		}
		
		If and only myProperty is a List, Set or Map, the "column" is applied to the <key/> element in the resulting <list/>, <set/> or <map/> node:
		
 			<[list|set|map] name="myProperty" ... >
				...
				<key column="MY_CUSTOM_KEY_COLUMN_NAME" />
			</[list|set|map]>
			
			
	11) "keyPropertyRef":
	
		Defines the property-ref attribute for a collection <key/> element of a one-to-many or many-to-many association.
		This property is only applied if the target property is either a Set, List or Map.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "keyPropertyRef": "myForeignKeyProperty" }
				}
			}
		}
		
		If and only myProperty is a List, Set or Map, the "property-ref" is applied to the <key/> element in the resulting <list/>, <set/> or <map/> node:
		
 			<[list|set|map] name="myProperty" ... >
				...
				<key property-ref="myForeignKeyProperty" />
			</[list|set|map]>
			

	12) "indexColumn":
	
		Defines the column name for index property of a many-to-many table.
		This property is only applied if the target property is a List.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "indexColumn": "MY_CUSTOM_IDX_COLUMN_NAME" }
				}
			}
		}
		
		If and only myProperty is a List, the "column" attribute is applied to the <list-index/> node:
		
 			<list name="myProperty" ... >
				...
				<list-index column="MY_CUSTOM_IDX_COLUMN_NAME" />
			</list>
	
		
	13) "mapKeyColumn":

		Defines the column name for map key property of a many-to-many table.
		This property is only applied if the target property is a Map.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "mapKeyColumn": "MY_CUSTOM_MAPKEY_COLUMN" }
				}
			}
		}
		
		If and only myProperty is a Map, having simple types as the key, then the "column" attribute is applied to 
		the <map-key/> node:
		
 			<map name="myProperty" ... >
	 			...
				<map-key column="MY_CUSTOM_MAPKEY_COLUMN" type="string" />
			</map>
		
		If and only myProperty is a Map, having an entity type as the key, then the "column" attribute is applied to 
		the <map-key-many-to-many/>  node:	
		
 			<map name="myProperty" ... >
	 			...
				<map-key-many-to-many column="MY_CUSTOM_MAPKEY_COLUMN" type="string" />
			</map>

		
	14) "elementColumn":
	
		Defines the column name for element property of a many-to-many table.
		This property is only applied if the target property is either a Set, List or Map.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myProperty": { "elementColumn": "MY_CUSTOM_ELEMENT_COLUMN" }
				}
			}
		}
		
		Will result in the following "column" attribute for the <element/> node child to the collection (<list/>, 
		<set/> or <map/>) definition node:
		
 			<[list|set|map] name="myProperty" ... >
	 			...
 				<element column="MY_CUSTOM_ELEMENT_COLUMN" ... />
			</[list|set|map]>
			
			
	15) "unique":
	
		Defines the uniqueness of the property. If used in set/list/maps, defines the uniqueness of the elements.
		
		Use "true" to enable an unique constraint on the property, and "false" to explicitly disable a preset unique constraint on the property.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myUniqueProperty": { "unique": true },
					"myNonUniqueProperty": { "unique": false }
				}
			}
		}
		
		Will result in the following mappings for this property:
		
 			<property name="myUniqueProperty" unique="true" />
 			<property name="myNonUniqueProperty" unique="false" />

		
	16) "uniqueKey":
	
		Groups multiple properties in a single unique key constraint.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"propertyA": { "uniqueKey": "MY_UNIQUE_INDEX" },
					"propertyB": { "uniqueKey": "MY_UNIQUE_INDEX" }
				}
			}
		}
		
		
		Will result in the following "unique-key" attributes for the <column/> child elements for these properties within the Hibernate mapping xml file:
		
 			<property name="propertyA">
				<column unique-key="MY_UNIQUE_INDEX" ...  />
			</property>
 			<property name="propertyB">
				<column unique-key="MY_UNIQUE_INDEX" ...  />
			</property>

		
		NOTE: As per Hibernate 3.6 documentation, the specified value of the unique-key attribute is not used to name 
		      the constraint in the generated DDL. It is only used to group the columns in the mapping file.

		
	17) "index":
	
		Defines the name of the index to be created for a property. 
		
		Multiple columns can be grouped into the same index by simply specifying the same index name.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"propertyA": { "index": "MY_INDEX" },
					"propertyB": { "index": "MY_INDEX" }
				}
			}
		}
		
		
		Will result in the following "index" attributes for the <column/> child elements for these properties within the Hibernate mapping xml file:
		
 			<property name="propertyA">
				<column unique-key="MY_INDEX" ...  />
			</property>
 			<property name="propertyB">
				<column unique-key="MY_INDEX" ...  />
			</property>

		
		NOTE: As per Hibernate 3.6 documentation, when using multi-column indexes, the order of 
		      the columns within the index cannot be explicitly specified through XML mappings.
		
 			
	18) "notNull":
	
		Defines if a property can be null or not. If used in set/list/maps, defines if the elements can be null.
		
		Use "true" to enable an not-null constraint on the property, and "false" to explicitly disable a preset not-null constraint on the property.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myNonNullableProperty": { "notNull": true },
					"myNullableProperty": { "notNull": false }
				}
			}
		}
		
		Will result in the following mappings for this property:
		
 			<property name="myNonNullableProperty" not-null="true" />
 			<property name="myNullableProperty" not-null="false" />

	
	19) "idGeneration":
	
		Defines a id generation class on an id property.
		
		By default, numeric ids will generate <generator class="native" /> 
		whereas String ids will generate <generator class="assigned" />.
		This property can be used to override this default behavior.
		
		The "idGeneration" property is ignored if the hinted target is not an id property.
		
		Examples:
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"myNumericIdProperty": { "idGeneration": "assigned" }
				}
			}
		}
		
		If and only "myNumericIdProperty" is an id property, the following mapping will the generated:
		
			<id name="myNumericIdProperty">
				<generator class="assigned" />
			</id>


	20) "lazy":
	
		Defines the value (as string, not boolean) of the "lazy" attribute for properties nodes (<property/>, <many-to-one/>, <set/>, <list/>, <map/>)
		
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"simpleProperty": { "lazy": "true" },
					"entityProperty": { "lazy": "false" },
					"collectionProperty": { "lazy": "extra" }
				}
			}
		}
		
		Will result in the following mappings for these properties:
		
			<property name="simpleProperty" ... lazy="true" />
			<many-to-one name="entityProperty" ... lazy="false" />
			<list name="collectionProperty" ... lazy="extra">
				...
			</list>
	
	
	21) "fetch":
	
		Defines the value of the "fetch" attribute for <many-to-one/>, <set/>, <list/> and <map/> nodes.
			
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"entityProperty": { "fetch": "join" },
					"collectionProperty": { "fetch": "subselect" }
				}
			}
		}
		
		Will result in the following mappings for these properties:
		
			<many-to-one name="entityProperty" ... fetch="join" />
			<list name="collectionProperty" ... fetch="subselect">
				...
			</list>
	
	
	22) "manyToManyFetch":
	
		Defines the value of the "fetch" attribute for <many-to-many/> note of collections.
			
		Given the following json hint:
		
		{ 
			"com.braintribe.model.MyEntity": {
				"properties": {
					"collectionOfEntities": { "manyToManyFetch": "select" }
				}
			}
		}
		
		Will result in the following mappings for these properties:
		
			<list name="collectionOfEntities" ... >
				...
				<many-to-many class="com.braintribe.model.OtherEntity" ... fetch="select" />
			</list>
	
	

-------------------------------------------------------------------------------------------------------------------------
-- 5. GENERAL KNOWN LIMITATIONS
-------------------------------------------------------------------------------------------------------------------------

---- 5.1. UNDERSTANDING THE HIBERNATE CLASSIFICATION OF ENTITY TYPES ----------------------------------------------------

	To better comprehend the origin of possible upcoming issues, it might come in handy to understand how the generator 
	elects the generic entity types to be represented in a hibernate's <class> counterpart.


________ 5.1.1. LEAF-NODE INSTANTIABLE TYPES ____________________________________________________________________________

	At first, the generator will elect the the leaf-node instantiable types as top-level hibernate entities. 
	"Leaf-node instantiable types" being generic entity types that are instantiable and have no subclass.
	
	So in the given hierarchy:
	
		A
	   / \
	  B   C
	
	Knowing that B and C are instantiable, distinct <class> definitions will be rendered for representing both B and C. 
	A will not be represented explicitly in the hibernate mappings.

	
________ 5.1.2. MEMBER REFERENCED TYPES _________________________________________________________________________________

	Secondly, the generator will elect as top-level hibernate entities the generic entity types referenced in members of 
	other entities. 

	So in the given hierarchy:

      X<>---A
           / \
          B   C
  
	Where X has a property of type A, A <class> definition will be rendered for generic entity type A. 
	Leaving its subclasses B and C to be represented as <subclass> definitions.
	
	
		|== NOTE =======================================================================================================|
		|                                                                                                               |
		|	For Hibernate, types referenced in members of mapped entities must be mapped types.                         |
		|		                                                                                                        |
		|	If a member of a mapped type referenced an unmapped class, hibernate throws:                                |
		|	                                                                                                            |
		|	org.hibernate.MappingException:                                                                             |
		|	  An association from the table <TABLE_NAME> refers to an unmapped class: <UNMAPPED_TYPE>                   |
		|	                                                                                                            |
		|	e.g.:                                                                                                       |
		|	                                                                                                            |
		|	If the following mapping is used:                                                                           |
		|	                                                                                                            |
		|	<hibernate-mapping>                                                                                         |
		|	  <class name="com.test.Auto" table="AUTO" abstract="false">                                                |
		|	    <id name="id" column="ID">                                                                              |
		|		  <generator class="native" />                                                                          |
		|		</id>                                                                                                   |
		|		<many-to-one name="manufacturer" class="com.test.Manufacturer" column="MANUFACTURER" />                 |
		|	  </class>                                                                                                  |
		|	</hibernate-mapping>                                                                                        |
		|	                                                                                                            |
		|	com.test.Manufacturer must be a mapped type, otherwise Hibernate will throw:                                |
		|	                                                                                                            |
		|	org.hibernate.MappingException:                                                                             |
		|	  An association from the table AUTO refers to an unmapped class: com.test.Manufacturer                     |
		|	                                                                                                            |
		|===============================================================================================================|


________ 5.1.3. MERGE OF OVERLAPPING HIERARCHIES ________________________________________________________________________

	After electing the member referenced types as top-level hibernate entities, there may be a situation where multiple 
	hierarchies overlap, that is, some <subclass> definition participates in more than one hierarchy. 

	So given the following hierarchy:

              A
             / \
      X<>---B   C---<>Y
             \ /
              D
			
	B and C will be elected as top-level hibernate entities since they're referenced as member types of X and Y. 
	This will leave the <subclass> definition of D participating in two hibernate <class> hierarchies: B and C.
	To fix the overlapping, the B and C must be merged into one single hierarchy. This is achieved by electing one B and 
	C common super type as top-level hibernate entity.
	In the presented scenario, A will be elected as top-level, since it is the common super type for B and C. 
	By electing A as hibernate's <class>, B, C and D will be left represented as hibernate's <subclass> mappings.
	
	It may occur that there are multiple common super types for a set of overlapping hierarchies. In this case, the 
	generator will elect as top level the common super type that introduces the least number of new subclasses to the 
	hierarchy.


---- 5.2. UNMAPPABLE MODEL SCENARIOS ------------------------------------------------------------------------------------

________ 5.2.1. INVALID TOP-LEVEL ENTITY (HIBERNATE CLASS) ______________________________________________________________

	Regardless of the criterion used to elect a generic entity type as a hibernate top-level entity (<class>), it may 
	occur that the generic entity type elected to be represented as a hibernate class do not fulfil the basic 
	requirements for it.

	For instance, if the elected top-level generic entity type do not define or inherit an id property (@IdPropery), 
	there will be no source of information provided to generate the <id> definition for the hibernate's <class>.

	Knowing that a <class> definition without <id> is an unexpected situation, the mapping generator will throw an 
	UnmappableModelException, aborting the mapping rendering.

	
________ 5.2.2. NO COMMON SUPER CLASS FOR OVERLAPPING HIERARCHIES _______________________________________________________

	A model will be considered unmappable if no common super class is found for a set of overlapping hierarchies.
	In fact, GenericEntity will always be an existent common super type, but it will never be considered a valid 
	hibernate <class>.
	
	
---- 5.3. ATTRIBUTE QUERY INCOMPATIBILITY IN MERGED HIERARCHY MODELS ----------------------------------------------------

	In Java, it is possible to simulate multiple inheritance with the use of interfaces.
	For instance, the hierarchy used as example in the item 5.1.3. is only possible if either B or C is a interface:

              A
             / \
      X<>---B   C---<>Y
             \ /
              D
			  
	As seen on item 5.1.3., the above hierarchy will result in A as <class> and B, C and D as <subclass>
	
	Although in Java we could easily defined "class D implements B, C", as per a hibernate limitation, there is no way 
	to define multiple super types of a type mapped as <subclass>, since the "extends" attribute of <subclass> must be 
	set and contain only one super type definition.
	
	Although for Hibernate, D is not of type C, the following association is valid and will be saved successfully:
	
	C d = new D();
	Y y = new Y();
	y.setC(d);
	
	Selecting the saved entities as a whole (without specific properties in "SELECT" clause), will 
	return the expected D instance:
	
	session.createQuery("FROM A").list(); //d will be returned as expected.
	session.createQuery("FROM B").list(); //d will be returned as expected.
	session.createQuery("FROM D").list(); //d will be returned as expected.
	session.createQuery("FROM C").list(); //d will be returned even though C is not a supertype of D for hibernate.

	But, selecting the "c" attribute of Y with "SELECT o.c from Y o" will fail. The discriminator of the retrieved instance "D" 
	is not a known subtype of "C" for Hiberante:
	
	org.hibernate.WrongClassException:  
		Object with id: 1 was not of the specified subclass: com.test.model.C (Discriminator: com.test.model.D)
	
   
---- 5.4. COVARIANT RETURN TYPES ----------------------------------------------------------------------------------------

	While covariant return types are perfectly supported by Java since 5.0, determining more specialized return type 
	in overwritten methods may result in Hibernate errors such as: 
	
	javassist.bytecode.DuplicateMemberException: 
		duplicate method: <METHOD_NAME> in org.hibernate.proxy.HibernateProxy_$$_javassist_42

