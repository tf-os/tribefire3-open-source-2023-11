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
package com.braintribe.model.processing.xmi.converter.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.prompt.Deprecated;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.processing.xmi.converter.Tokens;
import com.braintribe.model.processing.xmi.converter.registry.entries.EntityTypeEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.EnumTypeEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.GeneralizationEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.MetaDataEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.ModelEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.NodeEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.PackagePartEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.PropertyEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.RegistryEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.SimpleTypeEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.StereotypeEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.TagDefinitionEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.TagReferenceEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.TypeEntry;
import com.braintribe.model.processing.xmi.converter.tagdefinition.TagDefinition;
import com.braintribe.model.processing.xmi.converter.tagdefinition.TagReference;
import com.braintribe.utils.xml.dom.DomUtils;

/**
 * generates ids for the XMI models
 * 
 * if data exists of existing xmi snippets, it will reuse them if it can match
 * them, otherwise it will generate new ids
 * 
 * @author pit
 *
 */
public class IdGenerator {

	private static Logger log = Logger.getLogger(IdGenerator.class);

	private Map<Document, String> documentToIdPrefixMap;
	private XmiRegistry xmiRegistry;
	private Map<Document, IdCounter> documentToLastIndexMap = new HashMap<Document, IdCounter>();

	private Document currentDocument;
	private IdCounter defaultCounter = new IdCounter(0L);

	public void setDocumentToIdPrefixMap(Map<Document, String> documentToIdPrefixMap) {
		this.documentToIdPrefixMap = documentToIdPrefixMap;
	}

	public void setXmiRegistry(XmiRegistry xmiRegistry) {
		this.xmiRegistry = xmiRegistry;
	}

	public void setCurrentDocument(Document currentDocument) {
		this.currentDocument = currentDocument;
	}

	public IdGenerator() {
	}

	public void prime(Long id) {
		defaultCounter = new IdCounter(id);
	}

	/**
	 * generate an id for a GmMetaModel entry (model)
	 * 
	 * @param model - the GmMetaModel to get the id for
	 * @return
	 */
	public String generateId(GmMetaModel model) {
		String name = model.getName();

		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(ModelEntry.class);

		for (RegistryEntry entry : entries) {
			ModelEntry modelEntry = (ModelEntry) entry;
			if (modelEntry.getModelName().equalsIgnoreCase(name)) {
				return buildId(modelEntry);
			}
		}

		return generateXmiId();
	}

	/**
	 * @return
	 */
	public Map<String, String> generateSimpleTypeIds() throws CodecException {
		Map<String, String> ids = new HashMap<String, String>(xmiRegistry.getSimpleTypeMap().size());
		for (String simpleType : xmiRegistry.getSimpleTypeMap().keySet()) {
			String id = generateSimpleTypeId(simpleType);
			ids.put(simpleType + "Id", id);
		}
		// now build generalizations
		// get the object type
		GmType objectType = xmiRegistry.getSimpleTypeByName("object");
		for (String simpleType : xmiRegistry.getSimpleTypeMap().keySet()) {
			if (simpleType.equalsIgnoreCase("object"))
				continue;
			GmType simpleTypeByName = xmiRegistry.getSimpleTypeByName(simpleType);
			Map<String, String> genIds = generateId(simpleTypeByName, objectType);
			ids.put(simpleType + "Object", genIds.get("id"));
		}
		return ids;
	}

	/**
	 * @param name
	 * @return
	 */
	private String generateSimpleTypeId(String name) {
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(SimpleTypeEntry.class);
		for (RegistryEntry entry : entries) {
			SimpleTypeEntry simpleTypeEntry = (SimpleTypeEntry) entry;
			if (simpleTypeEntry.getSimpleName().equalsIgnoreCase(name))
				return simpleTypeEntry.getXmiId();
		}
		SimpleTypeEntry entry = new SimpleTypeEntry();
		entry.setSimpleName(name);
		String id = generateXmiId();
		GmType type = xmiRegistry.getSimpleTypeByName(name);
		entry.setType(type);
		entry.setXmiId(id);
		xmiRegistry.addSimpleTypeById(name, id, type, null, null);
		return id;
	}

	public Map<String, String> generateId(GmType type) {
		if (type instanceof GmEntityType) {
			return generateId((GmEntityType) type);
		}
		if (type instanceof GmSimpleType) {
			Map<String, String> ids = new HashMap<String, String>();
			ids.put("id", generateSimpleTypeId(type.getTypeSignature()));
			return ids;
		}
		String msg = "the type [" + type.getTypeSignature() + " ]is not supported";
		log.error(msg, null);
		return null;

	}

	/**
	 * generate ids for a GmEntityType entry (class)
	 * 
	 * @param type
	 * @return
	 */
	public Map<String, String> generateId(GmEntityType type) {
		Map<String, String> retval = new HashMap<String, String>();
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(EntityTypeEntry.class);
		for (RegistryEntry entry : entries) {
			EntityTypeEntry typeEntry = (EntityTypeEntry) entry;
			if (typeEntry.getType().getTypeSignature().equalsIgnoreCase(type.getTypeSignature())) {
				retval.put("id", typeEntry.getXmiId());
			}
		}
		if (retval.containsKey("id") == false) {
			String id = generateXmiId();
			xmiRegistry.addEntityTypeById(id, type, null, currentDocument);
			retval.put("id", id);
		}
		return retval;
	}

	/**
	 * generate ids for a GmEnumType with all its values
	 * 
	 * @param type
	 * @return
	 */
	public Map<String, String> generateId(GmEnumType type) {
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(EnumTypeEntry.class);
		Map<String, String> retval = new HashMap<String, String>();

		//
		// find a pre existing enum type definition
		//
		for (RegistryEntry entry : entries) {
			EnumTypeEntry typeEntry = (EnumTypeEntry) entry;
			if (typeEntry.getType().getTypeSignature().equalsIgnoreCase(type.getTypeSignature())) {

				retval.put("enumId", typeEntry.getXmiId());
				// read values from stored elements
				Element enumLiteralsE = DomUtils.getElementByPath(typeEntry.getElement(), ".*Enumeration\\.literal", false);
				Iterator<Element> literalIterator = DomUtils.getElementIterator(enumLiteralsE, ".*EnumerationLiteral");
				while (literalIterator.hasNext()) {
					Element enumLiteralE = literalIterator.next();
					retval.put(enumLiteralE.getAttribute("name"), enumLiteralE.getAttribute("xmi.id"));
					// what about the id of the tag references? 
				}
				/*
				 * if (Boolean.TRUE.equals( type.getIsMarkedForDiscard())) {
				 * retval.put("stereotypeDiscardId", getStereotypeId(
				 * Tokens.TOKEN_STEREOTYPE_DISCARD, "Enumeration")); }
				 */
				return retval;
			}
		}
		//
		// create a new enum type definition
		//
		String id = generateXmiId();
		EnumTypeEntry typeEntry = xmiRegistry.addEnumTypeById(id, type, null, currentDocument);
		retval.put("enumId", id); // main id

		// ids of enumeration values..
		if ((type.getConstants() != null) && (type.getConstants().size() > 0)) {
			// use the constants
			for (GmEnumConstant constant : type.getConstants()) {
				String value = constant.getName();
				String valueId = generateXmiId();
				retval.put(value, valueId);
				typeEntry.addIdForValue(value, valueId);
			}
		}
		/*
		 * if (Boolean.TRUE.equals( type.getIsMarkedForDiscard())) {
		 * retval.put("stereotypeDiscardId", getStereotypeId(
		 * Tokens.TOKEN_STEREOTYPE_DISCARD, "Enumeration")); }
		 */
		return retval;
	}

	/**
	 * acquire Id function for class references
	 * 
	 * @param type
	 * @return
	 */
	public String acquireId(GmType type) {
		if (type instanceof GmEntityType) {
			return generateId((GmEntityType) type).get("id");
		}
		if (type instanceof GmEnumType) {
			Map<String, String> result = generateId((GmEnumType) type);
			return result.get("enumId");
		}
		return getSympleTypeId(type.getTypeSignature());
	}

	/**
	 * generate ids for a partial package (partial)
	 * 
	 * @param packageName
	 * @return
	 */
	public String generateId(String packageName) {
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(PackagePartEntry.class);
		for (RegistryEntry entry : entries) {
			PackagePartEntry partEntry = (PackagePartEntry) entry;
			if (partEntry.getJavaName().equalsIgnoreCase(packageName))
				return buildId(partEntry);
		}
		return generateXmiId();
	}

	/**
	 * generate ids for a GmProperty (as attribute)
	 * 
	 * @param property
	 * @return
	 * @throws CodecException
	 */
	public Map<String, String> generateAttributeId(GmProperty property) throws CodecException {

		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(PropertyEntry.class);
		Map<String, String> retval = new HashMap<String, String>();
		for (RegistryEntry entry : entries) {
			PropertyEntry propertyEntry = (PropertyEntry) entry;
			GmProperty suspect = propertyEntry.getProperty();
			if ((suspect.getName().equalsIgnoreCase(property.getName())) && (suspect.getDeclaringType().getTypeSignature()
					.equalsIgnoreCase(property.getDeclaringType().getTypeSignature()))) {
				Element propertyE = propertyEntry.getElement();

				//
				// direct transfer of ids
				//
				retval.put("id", propertyE.getAttribute("xmi.id"));
				String[] elements = new String[] { ".*StructuralFeature.multiplicity/.*Multiplicity",
						".*StructuralFeature.multiplicity/.*Multiplicity/.*Multiplicity.range/.*UML:MultiplicityRange" };
				String[] keys = new String[] { "multiplicityId", "multiplicityRangeId" };
				for (int i = 0; i < elements.length; i++) {
					String element = elements[i];
					Element suspectE = DomUtils.getElementByPath(propertyE, element, false);
					if (suspectE == null) {
						String msg = "cannot find element [" + element + "] in stored data of attribute ["
								+ property.getName() + "] of type [" + property.getDeclaringType().getTypeSignature()
								+ "]";
						log.error(msg, null);
						throw new CodecException(msg);
					}
					retval.put(keys[i], suspectE.getAttribute("xmi.id"));
				}
				retval.put("typeId", getSympleTypeId(property.getType().getTypeSignature()));
				if (property.getName().equals("id")) {
					retval.put("stereotypeId", getStereotypeId("IdProperty", "Attribute"));
				}
				return retval;
			}
		}
		// id
		retval.put("id", generateXmiId());
		// multiplicity
		retval.put("multiplicityId", generateXmiId());
		// multiplicity range
		retval.put("multiplicityRangeId", generateXmiId());
		// id property ?
		// set the stereotype for properties, and DO NOT SET THE STEREOTYPE FOR
		// NONNULLABLE
		retval.put("typeId", getSympleTypeId(property.getType().getTypeSignature()));
		if (property.getName().equals("id")) {
			retval.put("stereotypeId", getStereotypeId(Tokens.TOKEN_STEREOTYPE_IDPROPERTY, "Attribute"));
		} else if (Boolean.TRUE.equals(property.getNullable()) == false) {
			retval.put("nullableId", getStereotypeId(Tokens.TOKEN_STEREOTYPE_NONNULLABLE, "Attribute"));
		}

		return retval;
	}

	/**
	 * generate ids for a GmProperty (as association)
	 * 
	 * @param property
	 * @return
	 * @throws CodecException
	 */
	public Map<String, String> generateAssociationId(GmProperty property) throws CodecException {
		Map<String, String> result = new HashMap<String, String>();
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(PropertyEntry.class);
		for (RegistryEntry entry : entries) {
			PropertyEntry propertyEntry = (PropertyEntry) entry;
			GmProperty suspect = propertyEntry.getProperty();
			if ((suspect.getName().equalsIgnoreCase(property.getName())) && (suspect.getDeclaringType().getTypeSignature()
					.equalsIgnoreCase(property.getDeclaringType().getTypeSignature()))) {
				Element propertyE = propertyEntry.getElement();
				result.put("id", propertyE.getAttribute("xmi.id"));

				NodeList nodes = propertyE.getElementsByTagNameNS("*", "AssociationEnd");
				//
				// end 1 :
				//
				Element end1E = (Element) nodes.item(0);
				result.put("end1Id", end1E.getAttribute("xmi.id"));

				//
				// end 2 :
				//
				Element end2E = (Element) nodes.item(1);
				result.put("end2Id", end2E.getAttribute("xmi.id"));

				// multiplicity (is optional in XMI, be we'll always write it, see below)
				Element multiplicityE = DomUtils.getElementByPath(end2E, ".*AssociationEnd.multiplicity/.*Multiplicity",
						false);
				if (multiplicityE != null) {
					result.put("multiplicityId", multiplicityE.getAttribute("xmi.id"));
					Element multiplicityRangeE = DomUtils.getElementByPath(multiplicityE,
							".*Multiplicity.range/.*MultiplicityRange", false);
					result.put("multiplicityRangeId", multiplicityRangeE.getAttribute("xmi.id"));
				}

				// might be a association class entry, so there might be a key id in the
				// element..
				Element attributeIdE = DomUtils.getElementByPath(propertyE, ".*Classifier\\.feature/.*Attribute",
						false);
				if (attributeIdE != null) {
					result.put("keyId", attributeIdE.getAttribute("xmi.id"));
				}
				Element keyTypeE = DomUtils.getElementByPath(propertyE,
						".*Classifier\\.feature/.*Attribute/.*StructuralFeature\\.type/.*Class", false);
				if (keyTypeE != null) {
					result.put("typeRefId", keyTypeE.getAttribute("xmi.idref"));
				}

				break;
			}
		}

		if (result.containsKey("id") == false) {
			result.put("id", generateXmiId());
		}
		if (result.containsKey("end1Id") == false) {
			result.put("end1Id", generateXmiId());
		}
		if (result.containsKey("end2Id") == false) {
			result.put("end2Id", generateXmiId());
		}
		if (result.containsKey("multiplicityId") == false) {
			result.put("multiplicityId", generateXmiId());
		}
		if (result.containsKey("multiplicityRangeId") == false) {
			result.put("multiplicityRangeId", generateXmiId());
		}

		// end1 type
		GmEntityType endType1 = property.getDeclaringType();
		EntityTypeEntry entityTypeEntry = (EntityTypeEntry) xmiRegistry.getTypeEntry(endType1);
		if (entityTypeEntry != null) {
			result.put("class1IdRef", buildId(entityTypeEntry));
		} else {
			String msg = "type [" + endType1.getTypeSignature() + "] of property [" + property.getName()
					+ "] is unknown at this time - pre-generating";
			log.warn(msg);
			// throw new CodecException(msg);
			String id = generateId(endType1).get("id");
			result.put("class1IdRef", id);
		}

		// end2 type
		GmType endType2 = property.getType();
		GmType actualEndType2 = null;
		if (endType2 instanceof GmCollectionType) {
			GmCollectionType collectionType = (GmCollectionType) endType2;
			if (collectionType instanceof GmLinearCollectionType) {
				GmLinearCollectionType linearCollectionType = (GmLinearCollectionType) collectionType;
				actualEndType2 = linearCollectionType.getElementType();
				result.put("multiplicity", "-1");
				//
				// currently : List -> ordered, others unordered
				if (linearCollectionType instanceof GmListType) {
					result.put("order", "ordered");
				} else {
					result.put("order", "unordered");
				}
			} else {
				GmMapType mapType = (GmMapType) endType2;
				GmType gmKeyType = mapType.getKeyType();
				GmType gmValueType = mapType.getValueType();
				actualEndType2 = gmValueType;
				TypeEntry keyEntry = xmiRegistry.getTypeEntry(gmKeyType.getTypeSignature());
				if (keyEntry != null) {
					result.put("keyClassRef", buildId(keyEntry));
				} else {
					String msg = "map key type [" + gmKeyType.getTypeSignature() + "] of property ["
							+ property.getName() + "] is unknown at this time.. pre-generating";
					log.warn(msg);
					// throw new CodecException(msg);
					String id = acquireId(gmKeyType);
					result.put("keyClassRef", id);
				}
				//
				if (result.containsKey("keyId") == false)
					result.put("keyId", generateXmiId());

				result.put("typeId", getSympleTypeId(gmKeyType.getTypeSignature()));
				// reuse others from the association code above

			}

		} else {
			actualEndType2 = endType2;
			result.put("multiplicity", "1");
			result.put("order", "unordered");
		}
		//
		// actual type of end2
		TypeEntry typeEntry = xmiRegistry.getTypeEntry(actualEndType2.getTypeSignature());
		if (typeEntry != null) {
			result.put("class2IdRef", buildId(typeEntry));
		} else {
			String msg = "type [" + actualEndType2.getTypeSignature() + "] of property [" + property.getName()
					+ "] is unknown at this time.. pre-generating";
			log.warn(msg);
			// throw new CodecException(msg);
			String id = acquireId(actualEndType2);
			result.put("class2IdRef", id);
		}

		return result;
	}

	/**
	 * generates ids for a generalization
	 * 
	 * @param subtype
	 * @param supertype
	 * @return
	 * @throws CodecException
	 */
	public Map<String, String> generateId(GmType subtype, GmType supertype) throws CodecException {
		Map<String, String> result = new HashMap<String, String>();
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(GeneralizationEntry.class);
		for (RegistryEntry entry : entries) {
			GeneralizationEntry generalizationEntry = (GeneralizationEntry) entry;
			if ((generalizationEntry.getSubType().getTypeSignature().equalsIgnoreCase(subtype.getTypeSignature()))
					&& (generalizationEntry.getSuperType().getTypeSignature()
							.equalsIgnoreCase(supertype.getTypeSignature()))

			) {
				result.put("id", generalizationEntry.getXmiId());
			}
		}
		// no generalization entry found
		if (result.containsKey("id") == false) {
			String id = generateXmiId();
			result.put("id", id);
			xmiRegistry.addGeneralizationById(id, supertype, subtype, null);
		}

		TypeEntry subTypeEntry = xmiRegistry.getTypeEntry(subtype);
		if (subTypeEntry == null) {
			String msg = "generalization child [" + subtype.getTypeSignature() + "] (parent ["
					+ supertype.getTypeSignature() + "]) is unknown at this time.. pre-generating";
			log.warn(msg);
			String id = generateId(subtype).get("id");
			result.put("class1IdRef", id);
			// throw new CodecException(msg);
		} else {
			result.put("subtype", buildId(subTypeEntry));
		}

		TypeEntry superTypeEntry = xmiRegistry.getTypeEntry(supertype);
		if (superTypeEntry == null) {
			String msg = "generalization parent [" + supertype.getTypeSignature() + "] (child ["
					+ subtype.getTypeSignature() + "]) is unknown at this time";
			log.warn(msg);
			// throw new CodecException(msg);
			String id = generateId(supertype).get("id");
			result.put("supertype", id);
		} else {
			result.put("supertype", buildId(superTypeEntry));
		}
		return result;
	}

	/**
	 * filters tag references by their owning type 
	 * @param entries - a {@link Collection} of {@link RegistryEntry} 
	 * @param type - the {@link GmType}
	 * @return - all tag references attached to the {@link GmType}
	 */
	private Collection<RegistryEntry> filterTagReferenceRegistryEntriesByParent( Collection<RegistryEntry> entries, GmType type) {
		return entries.stream().filter( e -> {
			TagReferenceEntry referenceEntry = (TagReferenceEntry) e;
			// parent must be an TypeEntry 
			RegistryEntry parentEntry = referenceEntry.getParentEntry();
			if (parentEntry instanceof TypeEntry == false)
				return false;
			// and its type signature must match
			TypeEntry typeEntry = (TypeEntry) parentEntry;
			if (typeEntry.getType().getTypeSignature().equalsIgnoreCase(type.getTypeSignature()) == false)
				return false;
			return true;
		}).collect( Collectors.toCollection(ArrayList::new));		
	}
	/**
	 * filters tag references by their owning model 
	 * @param entries - a {@link Collection} of {@link RegistryEntry} 
	 * @param type - the {@link GmMetaModel}
	 * @return - all tag references attached to the {@link GmMetaModel}
	 */	
	private Collection<RegistryEntry> filterTagReferenceRegistryEntriesByParent( Collection<RegistryEntry> entries, GmEnumConstant constant) {
		return entries.stream().filter( e -> {
			TagReferenceEntry referenceEntry = (TagReferenceEntry) e;
			// parent must be an TypeEntry 
			RegistryEntry parentEntry = referenceEntry.getParentEntry();
			if (parentEntry instanceof MetaDataEntry == false)
				return false;
			EnumConstantEntry typeEntry = (EnumConstantEntry) parentEntry;
			// and its declaring type's signature must match
			if (typeEntry.getConstant().getDeclaringType().getTypeSignature().equalsIgnoreCase( constant.getDeclaringType().getTypeSignature()) == false)
				return false;
			// and its value must match
			if (typeEntry.getConstant().getName().equalsIgnoreCase(constant.getName()) == false)
				return false;
			return true;
		}).collect( Collectors.toCollection(ArrayList::new));		
	}
	/**
	 * filters tag references by their owning type 
	 * @param entries - a {@link Collection} of {@link RegistryEntry} 
	 * @param property - the {@link GmType}
	 * @return - all tag references attached to the {@link GmType}
	 */
	private Collection<RegistryEntry> filterTagReferenceRegistryEntriesByParent( Collection<RegistryEntry> entries, GmProperty property) {
		return entries.stream().filter( e -> {
			TagReferenceEntry referenceEntry = (TagReferenceEntry) e;
			// parent must be an TypeEntry 
			RegistryEntry parentEntry = referenceEntry.getParentEntry();
			if (parentEntry instanceof PropertyEntry == false)
				return false;
			// and its type signature must match
			PropertyEntry typeEntry = (PropertyEntry) parentEntry;
			if (typeEntry.getProperty().getGlobalId().equals(property.getGlobalId()) == false)
				return false;
			return true;
		}).collect( Collectors.toCollection(ArrayList::new));		
	}
	/**
	 * filters tag references by their owning model 
	 * @param entries - a {@link Collection} of {@link RegistryEntry} 
	 * @param type - the {@link GmMetaModel}
	 * @return - all tag references attached to the {@link GmMetaModel}
	 */	
	private Collection<RegistryEntry> filterTagReferenceRegistryEntriesByParent( Collection<RegistryEntry> entries, GmMetaModel model) {
		return entries.stream().filter( e -> {
			TagReferenceEntry referenceEntry = (TagReferenceEntry) e;
			// parent must be an TypeEntry 
			RegistryEntry parentEntry = referenceEntry.getParentEntry();
			if (parentEntry instanceof ModelEntry == false)
				return false;
			// and its type signature must match
			ModelEntry typeEntry = (ModelEntry) parentEntry;
			if (typeEntry.getModelName().equalsIgnoreCase(model.getName()) == false)
				return false;
			return true;
		}).collect( Collectors.toCollection(ArrayList::new));		
	}
	/**
	 * filters tag references by their owning model 
	 * @param entries - a {@link Collection} of {@link RegistryEntry} 
	 * @param type - the {@link GmMetaModel}
	 * @return - all tag references attached to the {@link GmMetaModel}
	 */	
	private Collection<RegistryEntry> filterTagReferenceRegistryEntriesByParent( Collection<RegistryEntry> entries, MetaData metadata) {
		return entries.stream().filter( e -> {
			TagReferenceEntry referenceEntry = (TagReferenceEntry) e;
			// parent must be an TypeEntry 
			RegistryEntry parentEntry = referenceEntry.getParentEntry();
			if (parentEntry instanceof MetaDataEntry == false)
				return false;
			// and its type signature must match
			MetaDataEntry typeEntry = (MetaDataEntry) parentEntry;
			if (typeEntry.getMetaData().entityType().getShortName().equalsIgnoreCase(metadata.entityType().getShortName()) == false)
				return false;
			return true;
		}).collect( Collectors.toCollection(ArrayList::new));		
	}
	
	/**
	 * filters tag defintions by their name 
	 * @param entries - - a {@link Collection} of {@link RegistryEntry} 
	 * @param definitionName - the name of the {@link TagDefinition}
	 * @return - all tag definitions
	 */
	@SuppressWarnings("unused")
	private RegistryEntry filterTagDefinitionRegistryEntriesByTagDefinitionType(Collection<RegistryEntry> entries, String definitionName) {
		return entries.stream().filter( t -> {			
				TagDefinitionEntry definitionEntry = (TagDefinitionEntry) t;
				TagDefinition td = definitionEntry.getTagDefinition();
				if (td.getName().equals( definitionName))
					return true;
				return false;
		}).findFirst().orElse(null);
	}

	
	/**
	 * filter tag references by the name of the tag definition they point to 
	 * @param entries - the {@link Collection} of {@link RegistryEntry}
	 * @param definition - the name of the tag definition
	 * @return - all tag references pointing the tag definition with the given name
	 */
	private Collection<RegistryEntry> filterTagReferenceEntriesRegistryEntryByTagDefinition(Collection<RegistryEntry> entries, String definition) {		
		return entries.stream().filter( e -> {
			TagReferenceEntry referenceEntry = (TagReferenceEntry) e;
			TagReference tagReference = referenceEntry.getTagReference();
			TagDefinition tagDefinition = tagReference.getReferencedTagDefinition();
			if (tagDefinition.getName().equals( definition))
				return true;
			return false;			
		}).collect( Collectors.toCollection(ArrayList::new));
	}
	
	/**
	 * filters all tag definitions entries from the REGISTRY with a certain name
	 * @param definition
	 * @return
	 */
	private TagDefinitionEntry filterTagDefinitionEntry( String definition) {
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(TagDefinitionEntry.class);	
		TagDefinitionEntry regEntry = entries.stream().map(e -> (TagDefinitionEntry) e).filter( e -> {
			return e.getTagDefinition().getName().equalsIgnoreCase(definition);
		}).findFirst().orElse( null);
		
		if (regEntry == null) {
			return null;
		}
		return regEntry;		
	}
	
	/**
	 * get the reference ids for a tag ref
	 * @param tagDefinition -
	 * @return
	 */
	private Map<String,String> getTagDefinitionReferenceId( String tagDefinition) {
		Map<String,String> result = new HashMap<>();
		TagDefinitionEntry definitionEntry = filterTagDefinitionEntry( tagDefinition);		
		String refid = buildId(definitionEntry);
		if (isExternalReference(refid)) {
			result.put("refIdType", "href");
		} else {
			result.put("refIdType", "xmi.id");
		}
		result.put("refId", refid);
		
		return result;
		
	}
	
	/**
	 * generates id for a TagReference, either a new one or with a recycled id 
	 * @param genericEntity - the owning {@link GmType}
	 * @param tagDefinition - the tag definition's name 
	 * @return - a {@link Map} with 'id' and 'refId' for the renderer 
	 * @throws CodecException
	 */
	public Map<String, String> generateIdForTagReference(GenericEntity genericEntity, String tagDefinition) throws CodecException {
		Map<String, String> result = new HashMap<String, String>();
		Collection<RegistryEntry> tagReferences = xmiRegistry.getMatchingEntries(TagReferenceEntry.class);
		Collection<RegistryEntry> referencesToArtifactBindingTagDefinitions = filterTagReferenceEntriesRegistryEntryByTagDefinition(tagReferences, tagDefinition);
		Collection<RegistryEntry> referencesWithinType = null;
		if (genericEntity instanceof GmType) {
			referencesWithinType = filterTagReferenceRegistryEntriesByParent(referencesToArtifactBindingTagDefinitions, (GmType) genericEntity);
		}
		else if (genericEntity instanceof GmProperty) {
			referencesWithinType = filterTagReferenceRegistryEntriesByParent(referencesToArtifactBindingTagDefinitions, (GmProperty) genericEntity);
		}
		else if (genericEntity instanceof GmMetaModel){
			referencesWithinType = filterTagReferenceRegistryEntriesByParent(referencesToArtifactBindingTagDefinitions, (GmMetaModel) genericEntity);
		}
		else if (genericEntity instanceof GmEnumConstant) {
			referencesWithinType = filterTagReferenceRegistryEntriesByParent(referencesToArtifactBindingTagDefinitions, (GmEnumConstant) genericEntity);
		}
		else {
			throw new IllegalStateException("an owner type of [" + genericEntity.entityType().getTypeSignature() +"] is not supported");
		}
		/*
		else if (genericEntity instanceof Deprecated) {
			referencesWithinType = filterTagReferenceRegistryEntriesByParent(referencesToArtifactBindingTagDefinitions, (Deprecated) genericEntity);
		}
		else if (genericEntity instanceof Description) {
			referencesWithinType = filterTagReferenceRegistryEntriesByParent(referencesToArtifactBindingTagDefinitions, (Description) genericEntity);
		}
		*/
		
		RegistryEntry regEntry = referencesWithinType.stream().findFirst().orElse( null);
		if (regEntry != null) { // pre existing tag reference (after decode)
			TagReferenceEntry refEntry = (TagReferenceEntry) regEntry;
			result.put( "id", buildId( refEntry));
			result.putAll( getTagDefinitionReferenceId(tagDefinition));
		}
		else { // new tag reference (at encode BEFORE decode)
			result.put("id", generateXmiId());
			result.putAll( getTagDefinitionReferenceId( tagDefinition));
		}		
		return result;
	}
	
	public Map<String, String> generateIdForArtifactBindingTagReference(GmType type) throws CodecException {
		return generateIdForTagReference(type, Tokens.TOKEN_TAG_ARTIFACTBINDING);
	}
	public Map<String, String> generateIdForGlobalIdTagReference(GmType type) throws CodecException {
		return generateIdForTagReference(type, Tokens.TOKEN_TAG_GLOBALID);		
	}
	public Map<String, String> generateIdForDeprecatedTagReference(GmType type) throws CodecException {
		return generateIdForTagReference(type, Tokens.TOKEN_TAG_DEPRECATED);
	}
	public Map<String, String> generateIdForDocumentationTagReference(GmType type) throws CodecException {
		return generateIdForTagReference(type, Tokens.TOKEN_TAG_DOCUMENTATION);		
	}
	public Map<String, String> generateIdForDeprecatedTagReference(GmEnumConstant constant) throws CodecException {
		return generateIdForTagReference(constant, Tokens.TOKEN_TAG_DEPRECATED);
	}
	public Map<String, String> generateIdForDocumentationTagReference(GmEnumConstant constant) throws CodecException {
		return generateIdForTagReference(constant, Tokens.TOKEN_TAG_DOCUMENTATION);		
	}
	
	public Map<String, String> generateIdForGlobalIdTagReference(GmProperty property) throws CodecException {
		return generateIdForTagReference(property, Tokens.TOKEN_TAG_GLOBALID);		
	}	
	public Map<String, String> generateIdForDeprecatedTagReference(GmProperty property) throws CodecException {
		return generateIdForTagReference(property, Tokens.TOKEN_TAG_DEPRECATED);
	}
	public Map<String, String> generateIdForDocumentationTagReference(GmProperty property) throws CodecException {
		return generateIdForTagReference(property, Tokens.TOKEN_TAG_DOCUMENTATION);		
	}
	
	public Map<String, String> generateIdForArtifactBindingTagReference(GmMetaModel model) throws CodecException {
		return generateIdForTagReference(model, Tokens.TOKEN_TAG_ARTIFACTBINDING);
	}
	public Map<String, String> generateIdForModelDependencyTagReference(GmMetaModel model) throws CodecException {
		return generateIdForTagReference(model, Tokens.TOKEN_TAG_MODEL_DEPENDENCIES);
	}
	


	public Map<String, String> generateIdForModelDocumentationTagReference(GmMetaModel model, String tagDefinitonName) throws CodecException {
		return generateIdForTagReference(model, tagDefinitonName);
	}
	
	
	

	/**
	 * returns the id of a simple type (as defined in the base model)
	 * 
	 * @param name
	 * @return
	 */
	private String getSympleTypeId(String name) {

		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(SimpleTypeEntry.class);
		for (RegistryEntry entry : entries) {
			SimpleTypeEntry simpleTypeEntry = (SimpleTypeEntry) entry;
			if (simpleTypeEntry.getSimpleName().equalsIgnoreCase(name)) {

				return buildId(simpleTypeEntry);
			}
		}
		return null;
	}

	/**
	 * @param name
	 * @return
	 */
	public String generateStereotypeId(String name, String target, Element element, Document document) {
		String id = getStereotypeId(name, target);
		if (id != null)
			return id;
		id = generateXmiId();
		return id;
	}

	/**
	 * returns the id of a stereotype
	 * 
	 * @param name
	 * @return
	 */
	private String getStereotypeId(String name, String target) {
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(StereotypeEntry.class);
		for (RegistryEntry entry : entries) {
			StereotypeEntry stereotypeEntry = (StereotypeEntry) entry;
			if ((stereotypeEntry.getName().equalsIgnoreCase(name))
					&& (stereotypeEntry.getTarget().equalsIgnoreCase(target))) {
				return stereotypeEntry.getXmiId();
			}
		}
		return null;
	}
	
	/**
	 * generates an ID for a node
	 * @param name - the name of the node
	 * @return
	 */
	public String generateNodeId( String name) {
		String nodeId = getNodeId(name);
		if (nodeId != null) {
			return nodeId;			
		}
		nodeId = generateXmiId();
		return nodeId;
	}
	
	private String getNodeId(String name) {
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(NodeEntry.class);
		for (RegistryEntry entry : entries) {
			NodeEntry stereotypeEntry = (NodeEntry) entry;
			if (stereotypeEntry.getNodeName().equalsIgnoreCase(name)) {					
				return stereotypeEntry.getXmiId();
			}
		}
		return null;
	}
	
	

	/**
	 * @param name
	 * @return
	 */
	public Map<String, String> generateTagDefinitionId(String name) {
		Map<String, String> result = new HashMap<String, String>();
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(TagDefinitionEntry.class);
		for (RegistryEntry entry : entries) {
			TagDefinitionEntry tagDefinitionEntry = (TagDefinitionEntry) entry;
			if (tagDefinitionEntry.getTagDefinition().getName().equalsIgnoreCase(name)) {
				result.put("id", tagDefinitionEntry.getXmiId());
				Element definitionE = tagDefinitionEntry.getElement();
				Element multiplicityE = DomUtils.getElementByPath(definitionE,
						".*TagDefinition.multiplicity/.*Multiplicity", false);
				result.put("multiplicity", multiplicityE.getAttribute("xmi.id"));
				Element multiplicityRangeE = DomUtils.getElementByPath(multiplicityE,
						".*Multiplicity.range/.*MultiplicityRange", false);
				result.put("multiplicityRange", multiplicityRangeE.getAttribute("xmi.id"));
				return result;
			}
		}

		result.put("id", generateXmiId());
		result.put("multiplicity", generateXmiId());
		result.put("multiplicityRange", generateXmiId());
		return result;
	}

	/**
	 * depending on whether the entry's a local entry (or from the reference
	 * document) it generates either a local or a remote id
	 * 
	 * @param entry
	 * @return
	 */

	private String buildId(RegistryEntry entry) {
		if ((entry.getDocument() == currentDocument) || (entry.getDocument() == xmiRegistry.getReferenceDocument()))
			return entry.getXmiId();
		//
		String id = entry.getXmiId();
		// return xmiRegistry.getReferenceByDocument( entry.getDocument()) + "#" + id;
		return id;
	}

	/**
	 * checks whether the id's local or remote (external)
	 * 
	 * @param id
	 * @return
	 */
	public boolean isExternalReference(String id) {
		if (id.contains("#"))
			return true;
		return false;
	}

	/**
	 * generates a new xmi id
	 * 
	 * @return
	 */
	public String generateXmiId() {
		String prefix = null;
		if (documentToIdPrefixMap != null) {
			prefix = documentToIdPrefixMap.get(currentDocument);
		}
		if (prefix == null) {
			prefix = generateDocumentPrefix(currentDocument);

		}
		String id = generateId();

		return prefix + ":" + id;
	}

	/**
	 * generates a UUID for a document prefix
	 * 
	 * @return
	 */
	public String generateDocumentPrefix(Document document) {
		String prefix = UUID.randomUUID().toString();
		if (documentToIdPrefixMap == null)
			documentToIdPrefixMap = new HashMap<Document, String>();
		documentToIdPrefixMap.put(document, prefix);
		return prefix;
	}

	/**
	 * generates a new id (running number)
	 * 
	 * @return
	 */
	private String generateId() {

		IdCounter counter = documentToLastIndexMap.get(currentDocument);

		long i = 1;
		if (counter != null) {
			i = counter.getCount() + 1;
		} else {
			counter = defaultCounter;
			documentToLastIndexMap.put(currentDocument, counter);
			i = counter.getCount();
		}
		counter.setCount(i);

		StringBuffer id = new StringBuffer("" + i);
		while (id.length() < 16) {
			id.insert(0, "0");
		}

		return id.toString();
	}

	

}
