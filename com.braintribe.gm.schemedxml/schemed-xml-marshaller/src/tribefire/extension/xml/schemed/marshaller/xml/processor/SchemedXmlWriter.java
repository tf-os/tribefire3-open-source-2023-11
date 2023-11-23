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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringEscapeUtils;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;

import tribefire.extension.xml.schemed.mapper.api.MapperInfoRegistry;
import tribefire.extension.xml.schemed.mapping.metadata.AnyProcessingTokens;
import tribefire.extension.xml.schemed.mapping.metadata.EntityTypeMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.ModelMappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.Namespace;
import tribefire.extension.xml.schemed.mapping.metadata.PropertyMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.xml.SchemedXmlMarshallingException;
import tribefire.extension.xml.schemed.marshaller.xml.api.HasTokens;
import tribefire.extension.xml.schemed.marshaller.xml.processor.commons.ValueEncoder;

/**
 * an expert to write an assembly to an XML
 * 
 * @author pit
 *
 */
public class SchemedXmlWriter implements HasTokens, AnyProcessingTokens {
	private static Logger log = Logger.getLogger(SchemedXmlWriter.class);	
	private MapperInfoRegistry mappingRegistry;
	private XMLStreamWriter writer;
	private GmSerializationOptions options;
	private String schemaNamespacePrefix = "xsi";
	
	public SchemedXmlWriter(MapperInfoRegistry registry) {
		this.mappingRegistry = registry;		
	}

	/**
	 * write the {@link GenericEntity} passed to the writer
	 * @param writer - the {@link XMLStreamWriter}
	 * @param entity - the {@link GenericEntity} to write
	 * @param options - the {@link SchemedXmlSerializationOptions}
	 * @throws SchemedXmlMarshallingException
	 */
	public void process(XMLStreamWriter writer, GenericEntity entity, GmSerializationOptions options) throws SchemedXmlMarshallingException {
		
		this.writer = writer;
		this.options = options;
		EntityType<GenericEntity> entityType = entity.entityType();

		// we must find out what the proto name of the main type element is...
		String signature = entityType.getTypeSignature();
		String protoName = mappingRegistry.getMappedProtoNameForMainTypeSignature(signature);
		if (protoName == null) {
			if (entity.entityType().getTypeSignature().equalsIgnoreCase(ANY_TYPE_SIGNATURE)) {
				Property nameProperty =entity.entityType().getProperty( TYPE_ANY_NAME);
				protoName = nameProperty.get(entity);
			}
			if (protoName == null) { 
				String msg = "no mapping proto name found for type [" + signature + "]";
				throw new SchemedXmlMarshallingException(msg);
			}
		}
		
		// check if main proto name has a prefix, and if so, remove it
		int colonIndex = protoName.indexOf( ':');
		if (colonIndex >= 0) {
			protoName = protoName.substring( colonIndex+1);
		}
		
		// check if need qualification  
		ModelMappingMetaData modelMappingMetaData = mappingRegistry.getModelMappingMetaData();
		boolean qualifyElementName = modelMappingMetaData.getElementFormIsQualified();
		boolean qualifyAttributeName = modelMappingMetaData.getAttributeFormIsQualified();

		try {
			encode(entityType, entity, protoName, null, false);
		} catch (Exception e) {
			throw new SchemedXmlMarshallingException(e);
		}
	}
	
	/**
	 * encode a single entity 
	 * @param entityType - the {@link EntityType}
	 * @param instance - the {@link GenericEntity} instance 
	 * @param protoname - the name as it needs to be in XML
	 * @param parentType - the {@link GmEntityType} of the parent )(if any)
	 * @throws SchemedXmlMarshallingException - if anything goes wrong on processor level  
	 * @throws Exception - if anything goes wrong in the writer writer
	 */
	private void encode(EntityType<? extends GenericEntity> entityType, GenericEntity instance, String protoname, GmEntityType parentType, boolean reference) throws SchemedXmlMarshallingException, Exception {
		if (instance == null)
			return;

		String typeSignature = entityType.getTypeSignature();

		EntityType<?> instanceType = instance.entityType();
		boolean isPolymorph = false;
		if (entityType != instanceType) {
			String instanceTypeSignature = instanceType.getTypeSignature();
			if (log.isInfoEnabled()) {
				log.info("polymorphism detected, replacing base type [" + typeSignature + "] by extended type ["
						+ instanceTypeSignature + "]");
			}
			typeSignature = instanceTypeSignature;
			entityType = instanceType;
			isPolymorph = true;

		}
		GmEntityType gmEntityType = mappingRegistry.getMatchingEntityType(typeSignature);
		EntityTypeMappingMetaData mappingMetaData = mappingRegistry.getEntityTypeMetaData(gmEntityType);
		if (mappingMetaData == null) {
			String msg = "cannot retrieve mapping meta data for type [" + typeSignature + "]";
			log.error(msg, null);
			throw new SchemedXmlMarshallingException(msg);
		}
				
		boolean container = parentType == null ? true : false;
		String namespace;
		if ( container) {
			namespace = getNamespaceOfType(gmEntityType);
		}
		else {
			if (!reference)
				namespace = getNamespaceOfType(parentType);
			else
				namespace = getNamespaceOfType(gmEntityType);
		}
		
		
		if (entityType.getTypeSignature().equalsIgnoreCase( ANY_TYPE_SIGNATURE)) {			
			log.debug("switched to anytype processing");
			encodeAnyType( instance, reference,  parentType == null);		
			log.debug("returning from anytype processing");
			
			return;
		}
		
		

		boolean isSimpleType = mappingMetaData.getIsSimple();
		String nameOfTypeToWrite = generateProtoNameForType(gmEntityType, protoname, namespace, container);
		try {			
			writer.writeStartElement(nameOfTypeToWrite);
		} catch (XMLStreamException e) {
			throw new SchemedXmlMarshallingException(e);
		}
		
		if (isPolymorph) {
			// set the correct xsiNamespace
			String name = mappingMetaData.getXsdName();		
			// TODO: attach xsi prefix 
			writer.writeAttribute("type", name);
		}
		//
		// main container element
		// 
		if (parentType == null) {
			addDocumentLevelAttributes();
			
		}

		// no properties, close it
		List<Property> properties = entityType.getProperties();
	

		List<Property> sortablePropertyList = new ArrayList<Property>(properties.size());
		sortablePropertyList.addAll(properties);
		//
		// must sort the list according the index position in the mapping, depending
		// on the type hierarchy,
		// i.e. the super-types's properties must come before than the sub-type's
		// (XML derivation sequence : first sequence of super type, then sequence of sub
		// type)
		// build a map for the properties from the type in the mapping model
		//
		final Map<String, Integer> nameToPositionMap = new HashMap<String, Integer>();
		Map<Property, PropertyMappingMetaData> propertyToMetaDataMap = new HashMap<Property, PropertyMappingMetaData>();

		Map<EntityType<?>, List<Property>> entityToPropertyMap = extractPropertiesOfHierarchy(entityType);

		for (Property property : sortablePropertyList) {
			String name = property.getName();
			int index = -1;
			PropertyMappingMetaData propertyMappingMetaData = mappingRegistry.getPropertyMappingMetaData(gmEntityType, name);
			if (propertyMappingMetaData != null) { // might be an injected property, such as the synthetic ID property, so no mapping information
				// TODO : adapt to list 
				List<Integer> indices = propertyMappingMetaData.getIndex();
				Integer indexValue = indices.size() > 0 ? indices.get(0) : 0;
				index = indexValue != null ? indexValue : Integer.MAX_VALUE;
			}
			nameToPositionMap.put(name, index);
			propertyToMetaDataMap.put(property, propertyMappingMetaData);
		}

		
		//
		// first do all marked as attributes (need to write them out first)
		//
		//Writer attributeWriter = new StringWriter();		
		Iterator<Property> iterator = sortablePropertyList.iterator();
		while (iterator.hasNext()) {
			Property property = iterator.next();
			PropertyMappingMetaData propertyMappingMetaData = propertyToMetaDataMap.get(property);
			
			//
			if (propertyMappingMetaData == null) {
				if (log.isDebugEnabled())
					log.debug("unknown property [" + property.getName() + "], can't be an attribute");
				continue;
			}
			// do not write any bidirectional properties
			if (propertyMappingMetaData.getIsBacklinkProperty()) {
				if (log.isDebugEnabled()) {
					log.debug("property [" + property.getName()
							+ "] is a backlink (biDirectional) property, not written");
				}
				continue;
			}
			if (Boolean.TRUE.equals(propertyMappingMetaData.getIsAttribute())) {			
				writeAttribute(instance, property, propertyMappingMetaData, namespace);
			}
		}
			
		//
		// secondly do all remaining properties 
		//
		
		// retrieve the types of the hierarchy
		List<EntityType<?>> entityTypesAccSuperstructure = new ArrayList<>(entityToPropertyMap.keySet());
		// reverse so that super types are written first
		Collections.reverse(entityTypesAccSuperstructure);
		
		// iterate over the properties 
		for (EntityType<?> k : entityTypesAccSuperstructure) {				
			processTypesProperties(instance, protoname, gmEntityType, mappingMetaData, nameToPositionMap, propertyToMetaDataMap, entityToPropertyMap, k);
		}
	
		writer.writeEndElement();
					
	}

	private void addDocumentLevelAttributes() throws XMLStreamException {
		// if we're the main signature, we have to add the xsi namespace
		writer.writeAttribute("xmlns:" + schemaNamespacePrefix, NAMESPACE_SCHEMA);
		
		//ModelMappingMetaData modelMappingMetaData = mappingRegistry.getModelMappingMetaData();
		
		
		int i = 1;
		for (Entry<String,Namespace> entry  :mappingRegistry.getTargetNamespaces().entrySet()) {
			Namespace subNamespace = entry.getValue();
			
			String prefix = subNamespace.getPrefix();
			if (prefix == null) {
				prefix = "ns" + i++;
			}
			writer.writeAttribute( "xmlns:" + prefix, subNamespace.getUri());								
		}
	}

	/**
	 * specialized writer for anytype instances 
	 * @param instance	 
	 * @param reference - reference to outside namespace 
	 * @param toplevel  - first entity 
	 */
	private void encodeAnyType(GenericEntity instance, boolean reference, boolean toplevel) {
		try {
					
			GmEntityType entityType = mappingRegistry.getMatchingEntityType( instance.entityType().getTypeSignature());
			EntityTypeMappingMetaData md = mappingRegistry.getEntityTypeMetaData(entityType);
			String namespace = md.getNamespace();
			
			
			Property nameProperty = instance.entityType().getProperty( TYPE_ANY_NAME);
			String name = nameProperty.get(instance);
				
			int ns = name.indexOf(':');
							
			if (ns <= 0 ) {
				if (namespace != null) {
					String prefix = mappingRegistry.getPrefixOfNamespace(namespace);
					writer.writeStartElement(prefix, namespace, name);
				}
				else {
					writer.writeStartElement( name.substring(ns+1));
				}
			} 
			else {
				String namespaceInName = name.substring(0, ns);
				String prefix = mappingRegistry.getPrefixOfNamespace(namespaceInName);
				writer.writeStartElement( prefix + ":" + name.substring( ns+1));					
			}
			
			if (toplevel) {
				// merge the attributes .. 
				addDocumentLevelAttributes();
			}
							
				// attributes 
				Property attributeProperty = instance.entityType().getProperty( TYPE_ANY_ATTRIBUTES);
				List<GenericEntity> anyAttributeInstances = attributeProperty.get(instance);
				for (GenericEntity ge : anyAttributeInstances) {
					if (ge.entityType().getTypeSignature().equalsIgnoreCase(COM_BRAINTRIBE_XML +"." + TYPE_ANY_ATTRIBUTE_TYPE)) {
						Property attributeNameProperty = ge.entityType().getProperty( TYPE_ANY_NAME);
						String attributeName= attributeNameProperty.get(ge);						
						String clearedAttributeName = convertNamespacedValue(attributeName);
						
						Property attributeValzeProperty = ge.entityType().getProperty( TYPE_ANY_VALUE);
						String attributeValue = attributeValzeProperty.get(ge);
						String clearedAttributeValue = convertNamespacedValue(attributeValue);
						
						writer.writeAttribute(clearedAttributeName, clearedAttributeValue);
					}
				}
				
			// body value 
			Property valueProperty = instance.entityType().getProperty( TYPE_ANY_VALUE);
			String value = valueProperty.get(instance);
			if (value != null) {
				writer.writeCharacters(value);
			}
								
			// children 
			Property propertiesProperty = instance.entityType().getProperty( TYPE_ANY_PROPERTIES);
			List<GenericEntity> anyTypeInstances = propertiesProperty.get(instance);
			for (GenericEntity ge : anyTypeInstances) {
				if (ge.entityType().getTypeSignature().equalsIgnoreCase(COM_BRAINTRIBE_XML +"." + TYPE_ANY_TYPE)) {
					encodeAnyType( ge, reference, false);
				}
			}
			
			writer.writeEndElement();
			
		} catch (XMLStreamException e) {
			throw new SchemedXmlMarshallingException(e);
		}
	}
	
	private String convertNamespacedValue( String orgValue) {
		
		// check for namespace uri
		int curlyEnd = orgValue.indexOf( '}');
		if (curlyEnd > 0) {
			String uri = orgValue.substring(1, curlyEnd);
			String value = orgValue.substring( curlyEnd + 1);
			String prefix;
			if (uri.equalsIgnoreCase( NAMESPACE_SCHEMA)) {
				prefix = schemaNamespacePrefix;
			}
			else {
				prefix = mappingRegistry.getPrefixOfNamespace(uri);
			}
			return prefix + ":" + value;
		}
		return orgValue;
		
	}
	private String getNamespaceOfType(GmEntityType gmEntityType) {
		String parentNamespace = null;
		if (gmEntityType != null) {
			EntityTypeMappingMetaData parentMappingMetaData = mappingRegistry.getEntityTypeMetaData( gmEntityType);
			if (parentMappingMetaData != null) {
				parentNamespace = parentMappingMetaData.getNamespace();
			}
		}
		return parentNamespace;
	}
	

	/**
	 * @param gmEntityType
	 * @param protoName
	 * @param container
	 * @return
	 */
	private String generateProtoNameForType(GmEntityType gmEntityType, String protoName, String parentalNamespace, boolean container) {
		
		if (parentalNamespace == null)
			return protoName;
		
		if (mappingRegistry.getElementQualification(parentalNamespace) || container) {
			String prefixOfNamespace = mappingRegistry.getPrefixOfNamespace(parentalNamespace);
			//String prefixOfNamespace = prefixToNamespaceMap.get(namespace);
			if (prefixOfNamespace == null)
				return protoName;
			
			return prefixOfNamespace + ":" + protoName;			
		}
		return protoName;
		
	
	}
	
	/**
	 * extracts all properties from the type hierarchy 
	 * @param entityType - the {@link EntityType} to scan 
	 * @return - a {@link Map} of {@link EntityType} to {@link List} of {@link Property}
	 */
	private Map<EntityType<?>, List<Property>> extractPropertiesOfHierarchy( EntityType<? extends GenericEntity> entityType) {
		Map<EntityType<?>, List<Property>> result = new LinkedHashMap<>();
		EntityType<?> currentType = entityType;
		for (Property property : entityType.getDeclaredProperties()) {
			result.computeIfAbsent(entityType, e -> new ArrayList<>()).add(property);
		}
		for (EntityType<?> supertype : currentType.getSuperTypes()) {
			result.putAll(extractPropertiesOfHierarchy(supertype));
		}

		return result;
	}
	/**
	 * write an attribute 
	 * @param instance
	 * @param property
	 * @param propertyMappingMetaData
	 * @throws IOException
	 */
	private void writeAttribute(GenericEntity instance, Property property, PropertyMappingMetaData propertyMappingMetaData, String parentNamespace) throws IOException {
		// anyAttributesWritten = true;
		GenericModelType genericModelType = property.getType();
		GmProperty gmProperty = propertyMappingMetaData.getProperty();
		Object propertyInstance = property.get(instance); 
		if (propertyInstance == null) {
			return;
		}
		GmType propertyType = gmProperty.getType();
		String propertyProtoName = propertyMappingMetaData.getXsdName();
		if (propertyProtoName.startsWith( "attr_")) {
			propertyProtoName = propertyProtoName.substring( 5);
		}
		if (mappingRegistry.getAttributeQualification( parentNamespace)) {			
			String prefix = mappingRegistry.getPrefixOfNamespace(parentNamespace);
			propertyProtoName = prefix + ":" + propertyProtoName;
		}
		// TODO : MARKER 'XML-CLUDGE'
		if (propertyProtoName.equals("xml_lang")) {
			propertyProtoName = "xml:lang";
		}
		String propertyXsdType = propertyMappingMetaData.getApparentXsdType();
		
		String propertyDrilledDownXsdType = propertyMappingMetaData.getActualXsdType();
		String fixedValue = propertyMappingMetaData.getFixedValue();

		if (genericModelType instanceof SimpleType) {
			SimpleType simpleType = (SimpleType) genericModelType;
			String propertyValue = ValueEncoder.encode(gmProperty, simpleType, property.get(instance), propertyDrilledDownXsdType != null ? propertyDrilledDownXsdType : propertyXsdType, fixedValue);
			// if the value's null or empty, check for default value
			if (propertyValue == null || propertyValue.length() == 0) {
				if (propertyMappingMetaData.getDefaultValue() != null)
					propertyValue = propertyMappingMetaData.getDefaultValue();
			}
			// only write a property if the string representation isn't empty
			if (propertyValue != null && propertyValue.length() > 0) {
								
				try {
					writer.writeAttribute(propertyProtoName, propertyValue);
				} catch (XMLStreamException e) {
					throw new SchemedXmlMarshallingException(e);
				}
			}
		} else if (genericModelType instanceof EnumType) {
			// enum's also valid here..
			String propertyValue = ValueEncoder.encodeEnumValue(mappingRegistry, (GmEnumType) propertyType, property, instance);
			if (propertyValue != null && propertyValue.length() > 0) {
				//String namespace = propertyMappingMetaData.getNamespace();
				try {
					writer.writeAttribute(propertyProtoName, propertyValue);
				} catch (XMLStreamException e) {
					throw new SchemedXmlMarshallingException(e);
				}				
			}
		} 
		else if (genericModelType instanceof EntityType) {
			Property valueProperty = ((EntityType) genericModelType).findProperty("value");
			if (valueProperty == null) {
				String msg = String.format("Type [%s] is an unsupported type for an attribute (only value-typed GmEntityTypes are allowed to stand in for attributes)", genericModelType.getTypeSignature());
				throw new SchemedXmlMarshallingException(msg);
			}
			SimpleType simpleType = (SimpleType) valueProperty.getType();
			// instance is not the value type				
			Object valueAsObject = valueProperty.get( (GenericEntity) propertyInstance);
			String propertyValue = ValueEncoder.encode(gmProperty, simpleType, valueAsObject, propertyDrilledDownXsdType != null ? propertyDrilledDownXsdType : propertyXsdType, fixedValue);		
			try {
				writer.writeAttribute(propertyProtoName, propertyValue);
			} catch (XMLStreamException e) {
				throw new SchemedXmlMarshallingException(e);
			}
		}
		else {			
			String msg = String.format("Type [%s] is an unsupported type for an attribute", genericModelType.getTypeSignature());
			throw new SchemedXmlMarshallingException(msg);
		}

	}
	
	/**
	 * @param instance
	 * @param protoname
	 * @param gmEntityType
	 * @param mappingMetaData
	 * @param nameToPositionMap
	 * @param propertyToMetaDataMap
	 * @param entityToPropertyMap
	 * @param k
	 */
	private void processTypesProperties(GenericEntity instance, String protoname, GmEntityType gmEntityType, EntityTypeMappingMetaData mappingMetaData, final Map<String, Integer> nameToPositionMap, Map<Property, PropertyMappingMetaData> propertyToMetaDataMap, Map<EntityType<?>, List<Property>> entityToPropertyMap, EntityType<?> k) {
		
		List<Property> s = new ArrayList<>(entityToPropertyMap.get(k));

		// sort the properties of the type skeleton model according the info
		// from the mapping model
		Collections.sort(s, new Comparator<Property>() {

			@Override
			public int compare(Property o1, Property o2) {
				Integer i1 = nameToPositionMap.get(o1.getName());
				Integer i2 = nameToPositionMap.get(o2.getName());
				if (i1 != null && i2 != null)
					return i1.compareTo(i2);
				return 0;
			}
		});

		//
		// iterate over properties in the correct order
		//
		for (Property property : s) {
			try {
				PropertyMappingMetaData propertyMappingMetaData = propertyToMetaDataMap.get(property);
				// no mapping, won't get written
				if (propertyMappingMetaData == null) {
					if (log.isDebugEnabled()) {
						log.debug("property [" + property.getName() + "] has no mapping data, not written");
					}
					continue;
				}
				// directional
				if (propertyMappingMetaData.getIsBacklinkProperty()) {
					if (log.isDebugEnabled()) {
						log.debug("property [" + property.getName() + "] is a backlink (biDirectional) property, not written");
					}
					continue;
				}

				// don't write attributes
				if (Boolean.TRUE.equals(propertyMappingMetaData.getIsAttribute())) {
					continue;
				}
				writeProperty(property, gmEntityType, protoname, instance, mappingMetaData, propertyToMetaDataMap);
				
			} catch (Exception e) {			
				e.printStackTrace();
			}
		}		
	}

	/**
	 * @param property
	 * @param gmEntityType
	 * @param protoname
	 * @param instance
	 * @param mappingMetaData
	 * @param propertyToMetaDataMap
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void writeProperty(Property property, GmEntityType gmEntityType, String protoname, GenericEntity instance, EntityTypeMappingMetaData mappingMetaData, Map<Property, PropertyMappingMetaData> propertyToMetaDataMap) throws Exception {
		String propertyName = property.getName();
		PropertyMappingMetaData propertyMappingMetaData = propertyToMetaDataMap.get(property);
		if (propertyMappingMetaData == null) {
			String msg = "no mapping meta data for [" + gmEntityType.getTypeSignature() + "]'s property [" + propertyName + "] - will not be written";			
				if (property.isIdentifier()) {
					log.debug(msg, null);
			}
			return;
		}
		// do not write any bidirectional properties
		if (propertyMappingMetaData.getIsBacklinkProperty()) {
			if (log.isDebugEnabled()) {
				log.debug("property [" + property.getName() + "] is a backlink (biDirectional) property, not written");
			}
			return;
		}
		String fixedValue = propertyMappingMetaData.getFixedValue();
		GmProperty gmProperty = propertyMappingMetaData.getProperty();

		GmType propertyType = gmProperty.getType();
		String propertyProtoName = propertyMappingMetaData.getXsdName();
		String propertyXsdType = propertyMappingMetaData.getApparentXsdType();
		String propertyDrilledDownXsdType = propertyMappingMetaData.getActualXsdType();
		String parentalNamespace = getNamespaceOfType(gmEntityType);
		boolean overridesParentNamespace = propertyMappingMetaData.getNamespaceOverrides();
		// is it a value
		if (Boolean.TRUE.equals(propertyMappingMetaData.getIsValue())) {
			GenericModelType type = property.getType();
			if (type.isSimple()) {
			
				SimpleType simpleType = (SimpleType) type;
				String actualXsdType = propertyDrilledDownXsdType != null ? propertyDrilledDownXsdType : propertyXsdType;
				if (actualXsdType == null)
					actualXsdType = "xs:string";
				String propertyValue = ValueEncoder.encode(gmProperty, simpleType, property.get(instance), actualXsdType, fixedValue);
				String nameToWrite = generateProtoNameForType(gmProperty.getDeclaringType(), protoname, parentalNamespace, false);	
				
				//writer.writeStartElement(nameToWrite);
				writer.writeCharacters(propertyValue);
				//writer.writeEndElement();
			}
			else if (type.isEnum()) {
				String propertyValue = ValueEncoder.encodeEnumValue(mappingRegistry, (GmEnumType) gmProperty.getType(), property, instance);
				writer.writeCharacters(propertyValue);
			}			
			else {
				throw new IllegalStateException("unknown type [" + type.getTypeSignature() + "] here");
			}
		
			
			
			return;
		} else {
			// do collection
			if (Boolean.TRUE.equals(propertyMappingMetaData.getIsMultiple())) { // is a list..
				GmLinearCollectionType collectionType = (GmLinearCollectionType) propertyType;
				GmType collectionElementType = collectionType.getElementType();

				Collection<Object> propertyCollectionValue = property.get(instance);
				if (propertyCollectionValue != null) {
					for (Object object : propertyCollectionValue) {
						if (collectionElementType instanceof GmSimpleType) { // simple type
						
							SimpleType simpleType = (SimpleType) GMF.getTypeReflection().getType(collectionElementType.getTypeSignature());
							String propertyValue = ValueEncoder.encode(gmProperty, simpleType, object, propertyDrilledDownXsdType != null ? propertyDrilledDownXsdType : propertyXsdType, fixedValue);
							if ((propertyProtoName.equalsIgnoreCase("any") && Boolean.TRUE.equals(mappingMetaData.getHasAnyType())) || (propertyMappingMetaData.getIsUndefined())) {
								//writer.write(offset(indent + 1) + propertyValue);
							} else {
								String nameToWrite = generateProtoNameForType(gmProperty.getDeclaringType(), propertyProtoName, parentalNamespace, false);
								writer.writeStartElement( nameToWrite);
								writer.writeCharacters(StringEscapeUtils.escapeXml(propertyValue));
								writer.writeEndElement();								
							}
							
						} else {
							if (propertyType instanceof GmEnumType) {
								writeEnumType(property, instance, gmProperty, (GmEnumType) propertyType, propertyProtoName);
							} else { // complex type							
								EntityType<GenericEntity> propertyEntityType = (EntityType<GenericEntity>) GMF.getTypeReflection().getType( collectionElementType.getTypeSignature());
								writeEntityType( (GenericEntity) object, propertyProtoName, gmProperty.getDeclaringType(), propertyEntityType, overridesParentNamespace);
								/*
								EntityType<GenericEntity> propertyEntityType = (EntityType<GenericEntity>) GMF.getTypeReflection().getType(collectionElementType.getTypeSignature());
								encode(propertyEntityType, (GenericEntity) object, propertyProtoName, gmProperty.getDeclaringType());
								*/
							}
						}
					}
				}
			} else {
				if (propertyType instanceof GmSimpleType) { // simple type
					SimpleType simpleType = (SimpleType) property.getType();
					Object value = property.get(instance);
					if (value != null || fixedValue != null) {
						String propertyValue = ValueEncoder.encode(gmProperty, simpleType, value, propertyDrilledDownXsdType != null ? propertyDrilledDownXsdType : propertyXsdType, fixedValue);
						if (propertyValue != null) {
							if ((propertyProtoName.equalsIgnoreCase("any") && Boolean.TRUE.equals(mappingMetaData.getHasAnyType())) || (propertyMappingMetaData.getIsUndefined())) {
								//writer.write(offset(indent + 1) + propertyValue);
							} else {
								String nameToWrite = generateProtoNameForType(gmProperty.getDeclaringType(), propertyProtoName, parentalNamespace, false);
								writer.writeStartElement( nameToWrite);
								writer.writeCharacters(StringEscapeUtils.escapeXml(propertyValue));
								writer.writeEndElement();
							}
						}
					}
				} else {
					if (propertyType instanceof GmEnumType) { // enum type
						writeEnumType(property, instance, gmProperty, (GmEnumType) propertyType, propertyProtoName);						
					} else {						
						EntityType<GenericEntity> propertyEntityType = (EntityType<GenericEntity>) property.getType();
						/*
						encode(propertyEntityType, (GenericEntity) property.get(instance), propertyProtoName, gmProperty.getDeclaringType());
						*/
						writeEntityType( (GenericEntity) property.get(instance), propertyProtoName, gmProperty.getDeclaringType(), propertyEntityType, overridesParentNamespace);
					}
				}
			}
		}
	}
	
	/**
	 * @param instance
	 * @param xsdName
	 * @param declaringType
	 * @param propertyEntityType
	 * @throws Exception
	 */
	private void writeEntityType(GenericEntity instance, String xsdName, GmEntityType declaringType, EntityType<GenericEntity> propertyEntityType, boolean referenced) throws Exception {		
		encode(propertyEntityType, instance, xsdName, declaringType, referenced);
	}

	/**
	 * write the value of an enum 
	 * @param property - the {@link Property}
	 * @param instance - the {@link GenericEntity}
	 * @param gmProperty - the {@link GmProperty}
	 * @param enumType - 
	 * @param propertyProtoName
	 * @throws XMLStreamException
	 */
	private void writeEnumType(Property property, GenericEntity instance, GmProperty gmProperty, GmEnumType enumType, String propertyProtoName) throws XMLStreamException {
		
		GmEntityType parentType = gmProperty.getDeclaringType();
		String parentalNamespace = getNamespaceOfType(parentType);
		
		String propertyValue = ValueEncoder.encodeEnumValue(mappingRegistry, enumType, property, instance);
		if (propertyValue != null) {
			String nameToWrite = generateProtoNameForType(gmProperty.getDeclaringType(), propertyProtoName, parentalNamespace, false);
			writer.writeStartElement( nameToWrite);
			writer.writeCharacters(propertyValue);
			writer.writeEndElement();
		}
	}
	
}
