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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.xmi.converter.experts.DeclaringModelBinding;
import com.braintribe.model.processing.xmi.converter.registry.entries.EntityTypeEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.EnumTypeEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.GeneralizationEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.MetaDataEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.ModelEntry;
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

/**
 * registry for all relevant elements of a decoding process
 * 
 * @author pit
 *
 */
public class XmiRegistry {

	private Map<String, RegistryEntry> idToEntriesMap = new HashMap<String, RegistryEntry>();
	private Map<String, GmType> nameToSimpleTypeMap = new HashMap<String, GmType>();
	private Map<GmType, Document> typeToDocumentMap = new HashMap<GmType, Document>();
	private Map<Document, DeclaringModelBinding> documentToArtifactBindingMap = new HashMap<Document, DeclaringModelBinding>();
	private Map<String, Document> filenameToDocumentMap = new HashMap<String, Document>();
	private Map<String, GmMetaModel> filenameToModelMap = new HashMap<String, GmMetaModel>();
	private Map<Document, GmEntityType> documentToRoottypeMap = new HashMap<Document, GmEntityType>();
	private Map<Document, String> documentToIdPrefixMap = new HashMap<Document, String>();
	private Map<DeclaringModelBinding, GmMetaModel> artifactToBindingMap = new HashMap<>();
	private Document referenceDocument;
	private BasicModelOracle modelOracle;

	public Document getReferenceDocument() {
		return referenceDocument;
	}

	@Configurable
	public void setReferenceDocument(Document referenceDocument) {
		this.referenceDocument = referenceDocument;
	}
	
	private BasicModelOracle acquireRootModelOracle() {
		if (modelOracle != null) {
			return modelOracle;
		}
		GenericModelTypeReflection reflection = GMF.getTypeReflection();
		Model model = reflection.getModel("com.braintribe.gm:root-model");
		GmMetaModel metaModel = model.getMetaModel();
		
		modelOracle = new BasicModelOracle( metaModel);
		
		return modelOracle;
	}

	public XmiRegistry() {
		// a) set the simple types
		for (SimpleType simpleType : SimpleTypes.TYPES_SIMPLE) {
			GmType gmSimpleType = acquireRootModelOracle().findGmType( simpleType);		
			nameToSimpleTypeMap.put(simpleType.getTypeName(), gmSimpleType);
		}
		// b) the base type
		
		GmBaseType baseType = GmBaseType.T.create();
		baseType.setTypeSignature("object");
		nameToSimpleTypeMap.put("object", baseType);
		
		
	}

	public String getReferenceByDocument(Document document) {
		for (Entry<String, Document> entry : filenameToDocumentMap.entrySet()) {
			if (entry.getValue() == document) {
				return "http://argouml.org/user-profiles/" + entry.getKey();
			}
		}
		return "";
	}

	public GmType getSimpleTypeByName(String name) {
		return nameToSimpleTypeMap.get(name);
	}

	public Collection<GmType> getSimpleTypes() {
		return nameToSimpleTypeMap.values();
	}

	public Map<String, GmType> getSimpleTypeMap() {
		return nameToSimpleTypeMap;
	}

	public Collection<String> getStoredIds() {
		return idToEntriesMap.keySet();
	}

	public ModelEntry addModelById(String id, String name, Element element, Document document) {
		ModelEntry entry = new ModelEntry();
		entry.setXmiId(id);
		entry.setModelName(name);
		entry.setElement(element);
		entry.setDocument(document);
		addToMap(id, entry);
		return entry;
	}

	public SimpleTypeEntry addSimpleTypeById(String name, String id, GmType type, Element element, Document document) {
		SimpleTypeEntry entry = new SimpleTypeEntry();
		entry.setSimpleName(name);
		entry.setXmiId(id);
		entry.setType(type);
		entry.setElement(element);
		entry.setDocument(document);
		addToMap(id, entry);
		return entry;
	}

	public EntityTypeEntry addEntityTypeById(String id, GmType type, Element element, Document document) {
		EntityTypeEntry entry = new EntityTypeEntry();
		entry.setXmiId(id);
		entry.setType(type);
		entry.setElement(element);
		entry.setDocument(document);
		addToMap(id, entry);
		return entry;
	}

	public EnumTypeEntry addEnumTypeById(String id, GmType type, Element element, Document document) {
		EnumTypeEntry entry = new EnumTypeEntry();
		entry.setXmiId(id);
		entry.setType(type);
		entry.setElement(element);
		entry.setDocument(document);
		addToMap(id, entry);
		return entry;
	}
	
	public EnumConstantEntry addEnumConstantById(String id, GmEnumConstant type, Element element, Document document) {
		EnumConstantEntry entry = new EnumConstantEntry();
		entry.setXmiId(id);
		entry.setConstant(type);
		entry.setElement(element);
		entry.setDocument(document);
		addToMap(id, entry);
		return entry;
	}
	
	

	public StereotypeEntry addStereotypeById(String id, String name, String target, Element element, Document document) {
		StereotypeEntry entry = new StereotypeEntry();
		entry.setXmiId(id);
		entry.setName(name);
		entry.setElement(element);
		entry.setDocument(document);
		entry.setTarget(target);
		Collection<RegistryEntry> stereotypes = getMatchingEntries(StereotypeEntry.class);
		for (RegistryEntry stereotypeEntry : stereotypes) {
			StereotypeEntry stereotype = (StereotypeEntry) stereotypeEntry;
			if ((stereotype.getName().equalsIgnoreCase(name) == true)
					&& (stereotype.getTarget().equalsIgnoreCase(target))) {
				return entry;
			}
		}
		addToMap(id, entry);
		return entry;
	}
	
	public MetaDataEntry addMetadataById( String id, MetaData metadata, Element element, Document document) {
		MetaDataEntry entry = new MetaDataEntry();
		entry.setXmiId( id);
		entry.setMetaData(metadata);
		entry.setElement(element);
		entry.setDocument(document);
		addToMap(id, entry);
		return entry;
	}

	private void addToMap(String id, RegistryEntry entry) {
		RegistryEntry suspect = idToEntriesMap.get(id);
		if ((suspect != null) && (suspect.isEqual(entry) == false)

		) {
			System.err.println("Redefining id [" + id + "] from [" + suspect.getClass().getName() + "] to ["
					+ entry.getClass().getName() + "]");
		}
		idToEntriesMap.put(id, entry);
	}

	public GmType getTypeById(String id) {
		RegistryEntry entry = idToEntriesMap.get(id);
		if (entry != null) {
			if (entry instanceof TypeEntry) {
				return ((TypeEntry) entry).getType();
			}
		}
		return null;
	}

	public Collection<GmType> getTypesPerDocument(Document document) {
		Collection<GmType> result = new HashSet<GmType>();
		for (RegistryEntry entry : idToEntriesMap.values()) {
			if ((entry instanceof TypeEntry) && (entry.getDocument() == document)) {
				result.add(((TypeEntry) entry).getType());
				continue;
			}
		}
		return result;
	}

	public TagDefinition getTagDefinitionById(String id) {
		RegistryEntry entry = idToEntriesMap.get(id);
		if (entry != null) {
			if (entry instanceof TagDefinitionEntry)
				return ((TagDefinitionEntry) entry).getTagDefinition();
		}
		return null;
	}

	public TagDefinitionEntry addTagDefinitionById(String id, TagDefinition tagDefinition, Element element, Document document) {
		TagDefinitionEntry entry = new TagDefinitionEntry();
		entry.setXmiId(id);
		entry.setTagDefinition(tagDefinition);
		entry.setElement(element);
		entry.setDocument(document);
		addToMap(id, entry);
		return entry;
	}

	public TagReference getTagReferenceById(String id) {
		RegistryEntry entry = idToEntriesMap.get(id);
		if (entry != null) {
			if (entry instanceof TagReferenceEntry)
				return ((TagReferenceEntry) entry).getTagReference();
		}
		return null;
	}

	public TagReferenceEntry addTagReferenceById(String id, TagReference tagReference, Element element, Document document, RegistryEntry parentEntry) {
		TagReferenceEntry entry = new TagReferenceEntry();
		entry.setXmiId(id);
		entry.setTagReference(tagReference);
		entry.setElement(element);
		entry.setDocument(document);
		entry.setParentEntry(parentEntry);
		addToMap(id, entry);
		return entry;
	}

	public Collection<TagReference> getTagReferences() {
		Collection<TagReference> result = new HashSet<TagReference>();
		for (RegistryEntry entry : idToEntriesMap.values()) {
			if (entry instanceof TagReferenceEntry) {
				result.add(((TagReferenceEntry) entry).getTagReference());
			}
		}
		return result;
	}

	public Collection<RegistryEntry> getMatchingEntries(Class<? extends RegistryEntry> entryClass) {
		Collection<RegistryEntry> result = new HashSet<RegistryEntry>();
		for (RegistryEntry entry : idToEntriesMap.values()) {
			if (entry.getClass().equals(entryClass)) {
				result.add(entry);
			}
		}
		return result;
	}

	public Collection<RegistryEntry> getMatchingEntries(Class<? extends RegistryEntry>[] entryClasses) {
		Collection<RegistryEntry> result = new HashSet<RegistryEntry>();
		for (RegistryEntry entry : idToEntriesMap.values()) {
			for (Class<? extends RegistryEntry> suspect : entryClasses) {
				if (entry.getClass().equals(suspect)) {
					result.add(entry);
				}
			}
		}
		return result;
	}

	public Document getDocumentByFilename(String name) {
		return filenameToDocumentMap.get(name);
	}

	public void addDocumentByFilename(String name, Document document) {
		filenameToDocumentMap.put(name, document);
	}

	public PropertyEntry addPropertyById(String id, GmProperty property, Element element) {
		PropertyEntry entry = new PropertyEntry();
		entry.setXmiId(id);
		entry.setProperty(property);
		entry.setElement(element);
		entry.setDocument(element.getOwnerDocument());
		addToMap(id, entry);
		return entry;
	}

	public GeneralizationEntry addGeneralizationById(String id, GmType superType, GmType subType, Element element) {
		GeneralizationEntry entry = new GeneralizationEntry();
		entry.setXmiId(id);
		entry.setSuperType(superType);
		entry.setSubType(subType);
		entry.setElement(element);
		if (element != null)
			entry.setDocument(element.getOwnerDocument());
		addToMap(id, entry);
		return entry;
	}

	public PackagePartEntry addPackagePartEntryById(String id, String name, Element element) {
		PackagePartEntry entry = new PackagePartEntry();
		entry.setXmiId(id);
		entry.setName(name);
		entry.setElement(element);
		entry.setDocument(element.getOwnerDocument());
		addToMap(id, entry);
		return entry;
	}

	public void addMetaModelByFilename(String name, GmMetaModel model) {
		filenameToModelMap.put(name, model);
	}

	public void addRoottypeByDocument(Document document, GmEntityType root) {
		documentToRoottypeMap.put(document, root);
	}

	public GmEntityType getRoottypeByDocument(Document document) {
		return documentToRoottypeMap.get(document);
	}

	public void addDocumentIdPrefix(Document document, String id) {
		if (documentToIdPrefixMap.containsKey(document))
			return;
		String[] parts = id.split(":");
		if (parts[0].contains("#"))
			return;
		documentToIdPrefixMap.put(document, parts[0]);
	}

	public String getDocumentPrefixByDocument(Document document) {
		return documentToIdPrefixMap.get(document);
	}

	public void addTypeToDocumentMap(GmType type, Document document) {
		typeToDocumentMap.put(type, document);
	}

	public void addDocumentToArtifactBindingMap(Document document, String artifact) {
		for (DeclaringModelBinding dmb : artifactToBindingMap.keySet()) {
			if (dmb.matchesArtifact(artifact)) {
				documentToArtifactBindingMap.put(document, dmb);
				return;
			}
		}
		GmMetaModel mm = GmMetaModel.T.create();
		mm.setName(artifact);
		
		DeclaringModelBinding binding = new DeclaringModelBinding( mm);
		artifactToBindingMap.put( binding, mm);
		documentToArtifactBindingMap.put(document, binding);
	}
	
	public void primeArtifactToBindingMap( Map<String, GmMetaModel> toAdd, Document document) {
		for (Map.Entry<String, GmMetaModel> entry : toAdd.entrySet()) {
			DeclaringModelBinding binding = new DeclaringModelBinding( entry.getValue());
			artifactToBindingMap.put( binding, entry.getValue());			
			documentToArtifactBindingMap.put(document, binding);
		}				
	}

	public DeclaringModelBinding getArtifactBindingForDocument(Document document) {
		return documentToArtifactBindingMap.get(document);
	}

	public GmMetaModel getMetaModelByName(String modelName) {
		for (GmMetaModel model : filenameToModelMap.values()) {
			if (model.getName().equalsIgnoreCase(modelName))
				return model;
		}
		return null;
	}

	public TypeEntry getTypeEntry(GmType entityType) {
		@SuppressWarnings("unchecked")
		Collection<RegistryEntry> entries = getMatchingEntries( new Class[] { EntityTypeEntry.class, SimpleTypeEntry.class });
		for (RegistryEntry entry : entries) {
			TypeEntry typeEntry = (TypeEntry) entry;
			if (typeEntry.getType().getTypeSignature().equalsIgnoreCase(entityType.getTypeSignature()))
				return typeEntry;
		}
		return null;
	}

	public TypeEntry getTypeEntry(String signature) {
		Collection<RegistryEntry> entries = getMatchingEntries(EntityTypeEntry.class);
		entries.addAll(getMatchingEntries(EnumTypeEntry.class));
		entries.addAll(getMatchingEntries(SimpleTypeEntry.class));
		for (RegistryEntry entry : entries) {
			TypeEntry typeEntry = (TypeEntry) entry;
			if (typeEntry.getType().getTypeSignature().equalsIgnoreCase(signature))
				return typeEntry;
			if (typeEntry instanceof SimpleTypeEntry) {
				SimpleTypeEntry simpleEntry = (SimpleTypeEntry) typeEntry;
				if (simpleEntry.getSimpleName().equalsIgnoreCase(signature))
					return simpleEntry;
			}
		}
		return null;
	}

	public GmMetaModel getDeclaringModelForArtifactBinding(DeclaringModelBinding binding) {
		for (Entry<DeclaringModelBinding, GmMetaModel> entry : artifactToBindingMap.entrySet()) {
			if (binding.matchesArtifact(entry.getKey()))
				return entry.getValue();
		}
		GmMetaModel model = binding.getModel();
		artifactToBindingMap.put(binding, model);
		return model;
	}
	
	public GmMetaModel getDeclaringModelForArtifactBinding(String modelName) {
		for (Entry<DeclaringModelBinding, GmMetaModel> entry : artifactToBindingMap.entrySet()) {
			if (modelName.equals(entry.getKey().getName()))
				return entry.getValue();
		}
		return null;
	}


}
