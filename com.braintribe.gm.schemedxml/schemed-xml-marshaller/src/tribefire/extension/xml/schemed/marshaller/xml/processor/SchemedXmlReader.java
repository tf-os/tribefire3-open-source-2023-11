// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.xml.schemed.marshaller.xml.processor;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.sql.rowset.spi.XmlReader;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.SAXException;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;

import tribefire.extension.xml.schemed.mapper.api.MapperInfoRegistry;
import tribefire.extension.xml.schemed.mapper.structure.MapperInfoForProperty;
import tribefire.extension.xml.schemed.mapping.metadata.AnyProcessingTokens;
import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.commons.QNameExpert;
import tribefire.extension.xml.schemed.marshaller.xml.SchemedXmlMarshallingException;
import tribefire.extension.xml.schemed.marshaller.xml.api.HasTokens;
import tribefire.extension.xml.schemed.marshaller.xml.processor.commons.QNameWrapperCodec;
import tribefire.extension.xml.schemed.marshaller.xml.processor.commons.ValueDecoder;
import tribefire.extension.xml.schemed.marshaller.xml.processor.stack.ParsingStackEntry;
import tribefire.extension.xml.schemed.marshaller.xml.processor.stack.PropertyMappingMetaDataCacheElement;


/**
 * the actual expert that reads the XML and extracts the assembly from it 
 * @author pit 
 *
 */
public class SchemedXmlReader implements HasTokens, AnyProcessingTokens{
	
	private static Logger log = Logger.getLogger(SchemedXmlReader.class);
	private Stack<ParsingStackEntry> stack = new Stack<>();
	private MapperInfoRegistry mappingRegistry;
	private String prefixForXsdSchema;	
	private Map<String, String> prefixToNamespaceMap;
	private Map<String, PropertyMappingMetaDataCacheElement> propertyMappingMetaDataCache = new HashMap<>();	
	private GenericEntity root;
	private GmDeserializationOptions options;
	
	
	public SchemedXmlReader(MapperInfoRegistry registry) {
		this.mappingRegistry = registry;	
	}
	
	/**
	 * return the top container {@link GenericEntity}
	 * @return
	 */
	public GenericEntity getRoot() {
		return root;
	}
	
	/**
	 * process a start element 
	 * @param reader - the {@link XmlReader}
	 * @param options - the {@link SchemedXmlDeserializationOptions}
	 */
	public void processStartElement( XMLStreamReader reader, GmDeserializationOptions options) {
		
		this.options = options;
		
		if (stack.isEmpty()) {
			
			processTopLevelElement( reader);
		}
		else {
			processElement( reader);
		}			
	}

	
	/**
	 * identify top level element, and process from there 
	 * @param reader
	 * @return
	 */
	private void processTopLevelElement(XMLStreamReader reader) {
		// read all attributes 
		Map<QName, String> attributes = readAttributes(reader);
		
		// find the standard xsd namespace/prefix
		prefixForXsdSchema = determineXsdNamespacePrefix( reader);
		
		prefixToNamespaceMap = getImportedNamespaces( reader);
		
		
		
		// check if a polymorph container type is required
		QName tag = null;
		tag = determinePolymorphType( prefixForXsdSchema, attributes);
		if (tag == null) {
			tag = reader.getName();
		}
		
		String typeSignature = determineContainerTypeSignature(tag);
		
		GmEntityType gmEntityType = mappingRegistry.getMatchingEntityType(typeSignature);
		
		String typeSignature2 = gmEntityType.getTypeSignature();
		GenericEntity instance = create( typeSignature2);
		root = instance;
		
		EntityType<GenericEntity> entityType = instance.entityType();
		
		if (typeSignature.equalsIgnoreCase( ANY_TYPE_SIGNATURE)) {
			assignAnyAttributes(gmEntityType, instance, attributes, tag);		
		}
		else {
			parseAndAssignAttributes(gmEntityType, instance, attributes);
			
		}
		
		ParsingStackEntry entry = new ParsingStackEntry();
		entry.setGmEntityType( gmEntityType);
		
		entry.setEntityType( entityType);
		entry.setQname(tag);
		entry.setGenericEntity(instance);
		
		// is there a value property declared? 
		MapperInfoForProperty simpleContentPropertyInfoForType = mappingRegistry.getSimpleContentPropertyInfoForType(gmEntityType);
		if (simpleContentPropertyInfoForType != null) { 
			PropertyMappingMetaData md = simpleContentPropertyInfoForType.getMetaData();
			if (md != null) {
				entry.setPropertyMapping( new PropertyMappingMetaDataCacheElement(md));
			}
		}		
		stack.push(entry);						
	}

	private String determineContainerTypeSignature(QName tag) {
		String typeSignature = null;
		// prefix from name
		String key = tag.getLocalPart();
		String prefix = tag.getPrefix();
		if (prefix != null && prefix.length() > 0) {
			key = prefix + ":" + tag.getLocalPart();
		}
		typeSignature = mappingRegistry.getMappedMainTypeSignature( key);
		if (typeSignature != null) {
			return typeSignature;
		}
		
		// prefix from any namespace 
		for (String pref : prefixToNamespaceMap.keySet()) {
			typeSignature = mappingRegistry.getMappedMainTypeSignature( pref + ":" + tag.getLocalPart());
			if (typeSignature != null) {
				return typeSignature;
			}
		}
						
		
		// no prefix 
		typeSignature = mappingRegistry.getMappedMainTypeSignature( tag.getLocalPart());
		
		if (typeSignature == null) {
			return ANY_TYPE_SIGNATURE;
		}
				 
		return typeSignature;
	}
	
	/**
	 * recursively process an element
	 * @param reader
	 * @return
	 */
	private void processElement(XMLStreamReader reader) {

		Map<QName, String> attributes = readAttributes(reader);
						
		String extendedTypeSignature = null;
		QName tag = reader.getName();
		tag = determinePolymorphType( prefixForXsdSchema, attributes);
		if (tag == null) {
			tag = reader.getName();
		}
		ParsingStackEntry parentEntry = stack.peek();
		
		ParsingStackEntry entry = new ParsingStackEntry();
		GmEntityType parentType = parentEntry.getGmEntityType();															
		
		PropertyMappingMetaDataCacheElement	mappingMd = null;
		String parentTypeSignature = null;
		// we are within a type, so we need to find the property with this name
		if (parentType == null) {
			GenericModelType x = parentEntry.getPropertyMapping().elementType;
			parentTypeSignature = x.getTypeSignature();
			parentType = mappingRegistry.getMatchingEntityType(parentTypeSignature);					
		}
		if (parentType != null) {
			parentTypeSignature = parentType.getTypeSignature();
			String cacheKey = parentTypeSignature + ":" + tag;			
			mappingMd = getPropertyMapping( tag, parentType, cacheKey, false);
		}
		else  {
			log.error(" no parenttype found");
		}
		String typeSignature = ANY_TYPE_SIGNATURE;
		boolean overrideType = false;
		if (extendedTypeSignature == null) {
			typeSignature = mappingRegistry.getTypeSignatureOfPropertyType( parentTypeSignature, QNameExpert.parse(tag), false);
			// if we have no direct hit, it probably is a converted entity such as a collection 
			if (typeSignature == null) {
				// need to look it up from somewhere..
				typeSignature = mappingRegistry.getTypeSignatureOfPropertyElementType( parentTypeSignature, QNameExpert.parse(tag));
			}			
			if (typeSignature == null) {
				if (parentEntry.isUndefined() == false) {
					// 
					// check if this is an any type property
					//
					tribefire.extension.xml.schemed.model.xsd.QName anyTuple = QNameExpert.parse( "any");
					typeSignature = mappingRegistry.getTypeSignatureOfPropertyType( parentTypeSignature, anyTuple, false);
					// no any type, might be a nested anytype 
					if (typeSignature == null) {
						if (parentTypeSignature.equalsIgnoreCase( ANY_TYPE_SIGNATURE)) {
							typeSignature = ANY_TYPE_SIGNATURE;
						}
					}
					// still none? abort
					if (typeSignature == null) {
						String msg = "no stored type found for property [" + tag + "] of parent [" + parentType.getTypeSignature() + "]";
						log.error( msg);
						throw new SchemedXmlMarshallingException( msg);	
					} 
					
					if (log.isDebugEnabled()) {
						log.debug("any type declaration induced for ["+ tag + "]");							
					}
					entry.setAnyType(true);
					if( parentTypeSignature.equalsIgnoreCase( ANY_TYPE_SIGNATURE)) {
						String cacheKey = parentType.getTypeSignature() + ":" + MD_ANY_PROPERTIES;
						mappingMd = getPropertyMapping(new QName( MD_ANY_PROPERTIES), parentType, cacheKey, false);
					}
					else {					
						String cacheKey = parentType.getTypeSignature() + ":any";
						mappingMd = getPropertyMapping(new QName("any"), parentType, cacheKey, false);
					}
					
					
				}
				else {
					entry.setUndefined(true);
					mappingMd = parentEntry.getPropertyMapping();
				}				
			}
		}	
		
		// check for simple type
		GmEntityType gmEntityType = null;
		GenericEntity instance = null;
		EntityType<GenericEntity> entityType = null;
		// behave differently if a defined or a 'any' type is encountered 
		if (
				entry.isAnyType() == false &&
				parentEntry.isAnyType() == false &&		
				mappingMd.isUndefined == false &&
				!typeSignature.equalsIgnoreCase(ANY_TYPE_SIGNATURE)
			) {
			// standard entity processing 
			if (
					mappingMd.gmPropertyType instanceof GmSimpleType == false &&
					mappingMd.gmPropertyType instanceof GmEnumType == false &&
					mappingMd.gmPropertyType instanceof GmCollectionType == false
				) {
				gmEntityType = mappingRegistry.getMatchingEntityType(typeSignature);			
				instance = create( typeSignature);
				entityType = instance.entityType();
				parseAndAssignAttributes( gmEntityType, instance, attributes);	
			} 
			else if (mappingMd.gmPropertyType instanceof GmCollectionType) {
				GenericModelType eType = mappingMd.elementType;
				if (eType instanceof SimpleType == false) {
					if (!overrideType) {
						typeSignature = eType.getTypeSignature();
					}
					gmEntityType = mappingRegistry.getMatchingEntityType(typeSignature);			
					instance = create( typeSignature);
					entityType = instance.entityType();				
					parseAndAssignAttributes( gmEntityType, instance, attributes);
				}
			} 
			else {
				gmEntityType = parentEntry.getGmEntityType();
				instance = parentEntry.getGenericEntity();	
			}
		}
		else {
			// any processing			
			
			instance = create(ANY_TYPE_SIGNATURE);			
			entry.setPropertyMapping(mappingMd);			
			entry.setAnyType( true);
			gmEntityType = mappingRegistry.getMatchingEntityType( ANY_TYPE_SIGNATURE);
			assignAnyAttributes( gmEntityType, instance, attributes, tag);
			
		}
						 			
		// create new ParsingStackEntry and push to stack			
		entry.setGmEntityType( gmEntityType);
		entry.setQname( tag);
		if (entityType == null && instance != null) {
			entry.setEntityType(instance.entityType());
		}
		else {
			entry.setEntityType(entityType);
		}
		entry.setGenericEntity(instance);
		entry.setPropertyMapping(mappingMd);	
		entry.setParent(parentEntry);										
		stack.push(entry);								
			
	}
	
	 
	
	/**
	 * transfer an undefined number of attributes of undefined type - any processing
	 * @param gmEntityType - the {@link GmEntityType}
	 * @param instance - the {@link GenericEntity} 
	 * @param attributes - a map 
	 */
	private void assignAnyAttributes(GmEntityType gmEntityType, GenericEntity instance, Map<QName, String> attributes, QName tag) {
		// 
		Property nameProperty = instance.entityType().findProperty( TYPE_ANY_NAME);
		String namespace = tag.getNamespaceURI();
		if (namespace != null) {
			nameProperty.set(instance,  namespace + ":" + tag.getLocalPart());
		}
		else {
			nameProperty.set(instance, tag.getLocalPart());
		}
		
		Property attributesProperty = instance.entityType().findProperty( TYPE_ANY_ATTRIBUTES);
		if (attributesProperty == null) {
			throw new IllegalStateException("any attributes can only be assigned to a property called [" + TYPE_ANY_ATTRIBUTES + "] and [" + gmEntityType.getTypeSignature() + "] hasn't");
		}
		List<Object> attributeList = attributesProperty.get(instance);
					
		for (Entry<QName, String> entry : attributes.entrySet()) {
			GenericEntity anyAttributeInstance = create(COM_BRAINTRIBE_XML + "." + TYPE_ANY_ATTRIBUTE_TYPE);	
			
			Property attrNameProperty = anyAttributeInstance.entityType().getProperty( TYPE_ANY_NAME);					
			String attributeName = entry.getKey().toString();
			attrNameProperty.set(anyAttributeInstance, attributeName);
			
			Property attrValueProperty = anyAttributeInstance.entityType().getProperty( TYPE_ANY_VALUE);

			String attrValue = entry.getValue().toString();
			int ns = attrValue.indexOf(':');
			if (ns <= 0) {				
				attrValueProperty.set(anyAttributeInstance, attrValue.substring( ns+1));
			}
			else {
				String prefix = attrValue.substring(0, ns);
				String name = attrValue.substring( ns+1);
				String uri = prefixToNamespaceMap.get(prefix);
				attrValueProperty.set( anyAttributeInstance, "{" + uri + "}" + name);
			}									
			
			attributeList.add( anyAttributeInstance);
			
		}
				
	}

	/**
	 * the <xsd-prefix>:type attribute can specify the real type to use, if the actual type in the xsd was
	 * a base type 
	 * @param xsdPrefix - the xsd prefix as declared in the header attributes
	 * @param attributes - the map of {@link QName} to {@link String} attributes (coding map) 
	 * @return - the {@link QName}
	 */
	private QName determinePolymorphType(String xsdPrefix, Map<QName, String> attributes) {
		// no use of the schema in this xml, ergo no type attributes 
		if (xsdPrefix == null) {
			return null;
		}
		QName polymorphType = new QName(xsdPrefix, "type");
		String polymorphTypeName = attributes.get(polymorphType);
		if (polymorphTypeName != null)
			return new QName( polymorphTypeName);
		return null;
	}

	/**
	 * retrieves the xsd prefix to be used within the XML file 
	 * @param attributes - the map of {@link QName} to {@link String} attributes (coding map)
	 * @return - the prefix used for XSD 
	 */
	private String determineXsdNamespacePrefix(XMLStreamReader reader) {				 
		int namespaceCount = reader.getNamespaceCount();
		for (int i = 0; i < namespaceCount; i++) {							
			String namespacePrefix = reader.getNamespacePrefix(i);
			String namespaceURI = reader.getNamespaceURI(i);
			if (namespaceURI.equalsIgnoreCase(NAMESPACE_SCHEMA)) {
				return namespacePrefix;			
			}
		}		
		return null;
	}
	
	private Map<String, String> getImportedNamespaces( XMLStreamReader reader) {		
		Map<String, String> result = new HashMap<>();		 
		int namespaceCount = reader.getNamespaceCount();
		for (int i = 0; i < namespaceCount; i++) {							
			String namespacePrefix = reader.getNamespacePrefix(i);
			String namespaceURI = reader.getNamespaceURI(i);
			if (namespaceURI.equalsIgnoreCase(NAMESPACE_SCHEMA))
				continue;
			result.put( namespacePrefix, namespaceURI);
			if (!mappingRegistry.getUsedNamespaces().contains(namespaceURI)) {
				String msg = "unknown namespace [" + namespacePrefix + ":" + namespaceURI + "] encountered";
				log.warn(msg);
			}
			else {
				String msg = "namespace [" + namespacePrefix + ":" + namespaceURI + "] encountered";
				log.warn(msg);
			}
		}
		return result;							
	}

	/**
	 * reads the attributes at the current position of the {@link XMLStreamReader}
	 * @param reader - the {@link XMLStreamReader}
	 * @return - a {@link CodingMap} of {@link QName} to {@link String}
	 */
	protected static Map<QName,String> readAttributes( XMLStreamReader reader) {
		Map<QName, String> attributes = CodingMap.createHashMapBased( new QNameWrapperCodec());
		int attributeCount = reader.getAttributeCount();
		for (int i = 0; i < attributeCount; i++) {			
			QName name = reader.getAttributeName(i);
			String value = reader.getAttributeValue(i);
			attributes.put( name, value);
		}
		return attributes;
	}
	 
	/**
	 * @param gmEntityType
	 * @param instance
	 * @param attributes
	 * @throws SchemedXmlMarshallingException
	 */
	private void parseAndAssignAttributes(GmEntityType gmEntityType, GenericEntity instance, Map<QName,String> attributes) throws SchemedXmlMarshallingException {
		// parse attributes and assign them as properties.					
		for (Entry<QName, String> entry : attributes.entrySet()) {
			String name = entry.getKey().getLocalPart();
			if (Arrays.asList(ATTRIBUTES_TO_IGNORE).contains( name)) {
					log.debug("attribute [" + name + "] is noted as to be ignored");
					continue;
			}
			String cacheKey = instance.entityType().getTypeSignature() + ":" + "attr_" + name;
			PropertyMappingMetaDataCacheElement pe = getPropertyMapping( entry.getKey(), gmEntityType, cacheKey, true);
			
			if (pe == null) {
				if (options.getDecodingLenience() != null && options.getDecodingLenience().isLenient() == false) {
					log.warn( "No property mapping meta data found for property [" + name + "] exists in type [" + instance.entityType().getTypeSignature() + "]");
				}
			} else {					  
				String projectedName = pe.gmPropertyName;
							
				try {
					// very ugly - make sure that we get a kind of valid property name here 
					projectedName = projectedName.substring(0, 1).toLowerCase() + projectedName.substring(1);
					Property property = instance.entityType().getProperty(projectedName);
					Object value = null;
					String attrvalue = entry.getValue();
					GenericModelType propertyType = property.getType();
					if (propertyType instanceof EnumType) {
						value = ValueDecoder.decodeEnum(mappingRegistry, prefixToNamespaceMap, pe.apparentXsdType, propertyType, attrvalue);
					} 
					else if (propertyType instanceof EntityType) {
						GmEntityType matchingEntityType = mappingRegistry.getMatchingEntityType(propertyType.getTypeSignature());
						if (matchingEntityType == null) {
							throw new IllegalStateException( "no matching mapped type found for [" + propertyType.getTypeSignature());
						}
						EntityTypeMappingMetaData entityTypeMetaData = mappingRegistry.getEntityTypeMetaData(matchingEntityType);
						if (entityTypeMetaData.getIsSimple()) {
							GenericEntity matchingInstance = create( matchingEntityType.getTypeSignature());
							Property matchingProperty = matchingInstance.entityType().getProperty("value");
							value = ValueDecoder.decode( matchingProperty.getType(), attrvalue, pe.actualXsdType != null ? pe.actualXsdType : pe.apparentXsdType);
							matchingProperty.set(matchingInstance, value);
							//property.set(instance, matchingInstance);
							value = matchingInstance;
							
						}
						
					}
					else {
						value = ValueDecoder.decode( propertyType, attrvalue, pe.actualXsdType != null ? pe.actualXsdType : pe.apparentXsdType);						
					}
					property.set(instance, value);
				} catch (GenericModelException e) {
					//
					log.warn( "No property with the name [" + projectedName + "] exists in type [" + instance.entityType().getTypeSignature() + "]");
				}
			}
		}
	}
	
	/**
	 * helper to get or create a property mapping  
	 * @param qName - the XML tag name 
	 * @param parentType - the {@link GmEntityType} of the parent
	 * @param cacheKey - the key for the cache element 
	 * @return - either the found or newly created {@link PropertyMappingMetaDataCacheElement}
	 * @throws SchemedXmlMarshallingException
	 */
	private PropertyMappingMetaDataCacheElement getPropertyMapping(QName qName, GmEntityType parentType, String cacheKey, boolean attribute) throws SchemedXmlMarshallingException {
		PropertyMappingMetaDataCacheElement mappingMd = this.propertyMappingMetaDataCache.get(cacheKey);
		if (mappingMd == null) {
			// is it a collection ? 		
			PropertyMappingMetaData propertyMappingMetaData = mappingRegistry.getPropertyMappingMetaData(parentType, qName, attribute);
			if (propertyMappingMetaData == null) {
				return null;
			}		
			mappingMd = new PropertyMappingMetaDataCacheElement(propertyMappingMetaData);
			this.propertyMappingMetaDataCache.put(cacheKey, mappingMd);
		}
		return mappingMd;
	}
	
	/**
	 * @param gmEntityType
	 * @return
	 * @throws SchemedXmlMarshallingException
	 */
	@SuppressWarnings("unused")
	private GenericEntity create( GmEntityType gmEntityType) throws SchemedXmlMarshallingException {
		EntityType<GenericEntity> entityType = gmEntityType.entityType();
		return create( entityType);
	}
	
	/**
	 * @param signature
	 * @return
	 * @throws SchemedXmlMarshallingException
	 */
	private GenericEntity create( String signature) throws SchemedXmlMarshallingException {
		EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(signature);
		return create( entityType);		
	}

	/**
	 * create an new instance of the type passed, if the {@link SchemedXmlDeserializationOptions} contain a session, this is used
	 * @param entityType - the {@link EntityType}
	 * @return - the instantiated {@link GmEntityType}
	 */
	private GenericEntity create(EntityType<GenericEntity> entityType) {
		if (options.getSession() != null) {
			try {
				return options.getSession().create(entityType);
			} catch (RuntimeException e) {
				String msg ="instance provider cannot provide new instance of type [" + entityType.getTypeSignature() + "]";
				log.error( msg, e);
				throw new SchemedXmlMarshallingException(msg, e);
			}
		} 
		else {
			return entityType.create();
		}
	}

	/**
	 * finalize the element 
	 * @param reader - the {@link XMLStreamReader} 
	 * @param options - the {@link SchemedXmlDeserializationOptions}
	 * @throws SchemedXmlMarshallingException - thrown if anything goes wrong
	 */
	public void processEndElement(XMLStreamReader reader, GmDeserializationOptions options) throws SchemedXmlMarshallingException {
		QName qName = reader.getName();
		// pop from stack
		ParsingStackEntry currentEntry = stack.pop();
		if (currentEntry.isToBeIgnored()) {
			if (log.isDebugEnabled()) {
				log.debug( "Popping ignored element [" + qName + "]");
			}
			return;
		}		
		
		PropertyMappingMetaDataCacheElement md = currentEntry.getPropertyMapping();
		
		
		if (
				currentEntry.isAnyType() ||
				currentEntry.isUndefined()
			) {			
			ParsingStackEntry entry = stack.peek();
			// see whether we need to attach any text content
			String stringValue = currentEntry.getStringBuffer().toString().trim();
			if (stringValue != null && stringValue.length() > 0) {			
				if (currentEntry.isAnyType()) {
					Property valueProperty = currentEntry.getGenericEntity().entityType().getProperty( "value");
					valueProperty.set( currentEntry.getGenericEntity(), stringValue);
				}							
			}
			// must fillout 

			entry.attachChild( currentEntry);	
			return;
		}		
		
		if (md == null) {
			// honk - no mapping information .. must be the root
			return;
		}
		String tag = md.gmPropertyName;
		try {												
			// if it's a simple type, just set it 
			GenericModelType modelType = md.propertyType;
			String childValue = currentEntry.getStringBuffer().toString().trim();			
			Object value = handleValuedElement(currentEntry, modelType, childValue, md.actualXsdType, md.apparentXsdType);			
			currentEntry.setValue(value);
		} catch (Exception e) {
			String msg = "cannot end node [" + tag +"]";
			log.error( msg, e);
			throw new SchemedXmlMarshallingException( msg, e);	
		}
	 				
		// not root, then there is a parent, so we must do some wiring here..						
		if (stack.isEmpty() == false) {
			ParsingStackEntry entry = stack.peek();
			entry.attachChild( currentEntry);		
			
			if (!md.propertyType.isSimple()) {
				EntityTypeMappingMetaData emappingData = mappingRegistry.getEntityTypeMetaData(currentEntry.getGmEntityType());
				// might not be mapped (virtual property, such as collections)
				if (emappingData != null) {
					GmProperty gmBacklinkProperty = emappingData.getBacklinkProperty();
					// handle auto bidirectionals	
				
					if (gmBacklinkProperty != null) {
						// require name of the property to set the parent to 
						String name = gmBacklinkProperty.getName(); 
						try {
							Property backlinkProperty = currentEntry.getEntityType().getProperty( name);
						
							
							// need to find what the actual parent is..				
							if (entry.getPropertyMapping() == null || !entry.getPropertyMapping().isMultiple) {
								backlinkProperty.set( currentEntry.getGenericEntity(), entry.getGenericEntity());
							}
							else {
								backlinkProperty.set( currentEntry.getGenericEntity(), entry.getParent().getGenericEntity());
							}
																
						} catch (GenericModelException e) {
							String msg="cannot set backlink property [" + name +"] of [" + currentEntry.getGmEntityType().getTypeSignature() + "] to [" + entry.getGmEntityType().getTypeSignature() + "]";
							log.error( msg, e);					
						}							
					}
				}
			}
			
		} else {
			currentEntry.attachToSelf();
		}
		
	}

	/**
	 * process the character event, i.e. read the text content
	 * @param reader - the {@link XMLStreamReader}
	 * @param options - the {@link SchemedXmlDeserializationOptions}
	 * @throws SchemedXmlMarshallingException - thrown if anything goes wrong
	 */
	public void processCharacters(XMLStreamReader reader, GmDeserializationOptions options) throws SchemedXmlMarshallingException {
		ParsingStackEntry entry = stack.peek();
		if (entry == null) {
			String msg = "the stack may not be empty at this point of decoding";
			throw new SchemedXmlMarshallingException(msg);
		}
		entry.getStringBuffer().append( reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());		
	}
	
	/**
	 * attaches a string - any - to an element 
	 * @param currentEntry - the {@link ParsingStackEntry} that contains the parent entity of this any 
	 * @param childValue - the any's value 
	 */
	private void attachSingleValueToAny(ParsingStackEntry currentEntry, String childValue) {	
		// assign value to its any data holder
		// either "_any" or "_any*
		List<Property> properties = currentEntry.getEntityType().getProperties();
		for (Property property : properties) {
			if (property.getName().startsWith( "_any")) {
				Object valueToAssign = childValue;
				if (property.getType() instanceof CollectionType) {				
					Collection<Object> collection = property.get(currentEntry.getGenericEntity());					
					collection.add( valueToAssign);
					property.set( currentEntry.getGenericEntity(), collection);
				}
				else {
					property.set(currentEntry.getGenericEntity(), childValue);
				}
				break;
			}
		}
	}
	
	/**
	 * create the appropriate object from the simple-type value
	 * @param currentEntry - the {@link ParsingStackEntry} that contains the parent 
	 * @param modelType - the {@link GenericModelType} that reflects the type 
	 * @param childValue - the value as {@link String}
	 * @param drill - the drilled-down type (final type in derivation chain) as a {@link String}
	 * @param xsd - the name of the XSD type as a {@link String}
	 * @return - the {@link Object} reflecting the value's representation
	 * @throws SAXException
	 */
	private Object handleValuedElement(ParsingStackEntry currentEntry, GenericModelType modelType, String childValue, String drill, String xsd) throws SchemedXmlMarshallingException {
		Object value;
		if (modelType instanceof SimpleType) {					
			value = ValueDecoder.decode( modelType, childValue, drill != null ? drill : xsd);					
		} 
		else if (modelType instanceof EnumType) {
				//
				value = ValueDecoder.decodeEnum( mappingRegistry, prefixToNamespaceMap, xsd, modelType, childValue);					
		}
		else if (modelType instanceof CollectionType == false) {
				// otherwise just attach the instance to the parent
				MapperInfoForProperty mapperInfoForProperty = mappingRegistry.getSimpleContentPropertyInfoForType( currentEntry.getGmEntityType());
				PropertyMappingMetaData pmSimple = null;
				if (mapperInfoForProperty != null) {
					pmSimple =  mapperInfoForProperty.getMetaData();		
					GmProperty gmProperty = pmSimple.getProperty();
					Property property = currentEntry.getEntityType().getProperty( gmProperty.getName());
					GenericModelType propertyType = property.getType();
					value = handleValuedElement(currentEntry, propertyType, childValue, drill, xsd);
					//value = ValueDecoder.decode( propertyType, childValue, pmSimple.getActualXsdType() != null ? pmSimple.getActualXsdType() : pmSimple.getApparentXsdType());
					property.set( currentEntry.getGenericEntity(), value);
					value = currentEntry.getGenericEntity();				
				}		
				else {
					// CLUDGE : needs to be reworked for mixed=true types
					// if this is a non simple any type, but still has text content, so it's mixed
					// .. look for "_any*" property? 
					EntityTypeMappingMetaData typeMd = mappingRegistry.getEntityTypeMetaData( currentEntry.getGmEntityType());
					// only add it if there's any text there.. in this case, add it to the _anyList..  
					// TODO : typeMd == null means that it's most probably an injected type (com.braintribe.mxl).. needs to be managed clearly
					if (typeMd == null || Boolean.TRUE.equals(typeMd.getHasAnyType()) && childValue.trim().length() > 0) {
						attachSingleValueToAny(currentEntry, childValue);
					}
					return  currentEntry.getGenericEntity();
				}				
		} 
		else {
			CollectionType collectionType = (CollectionType) modelType;			
			value = handleValuedElement(currentEntry, collectionType.getCollectionElementType(), childValue, drill, xsd);												
		}
		return value;
	}

	public void setPrefixToNamespacesMap(Map<String, String> namespaces) {
		// TODO Auto-generated method stub
		
	}

	
	
}
