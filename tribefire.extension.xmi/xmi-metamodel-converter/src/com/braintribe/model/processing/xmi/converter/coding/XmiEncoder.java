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
package com.braintribe.model.processing.xmi.converter.coding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.prompt.Deprecated;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.processing.xmi.converter.Tokens;
import com.braintribe.model.processing.xmi.converter.coding.differentiator.ModelDifferentiatorContext;
import com.braintribe.model.processing.xmi.converter.experts.DeclaringModelBinding;
import com.braintribe.model.processing.xmi.converter.experts.PropertyFilteringUtils;
import com.braintribe.model.processing.xmi.converter.registry.IdGenerator;
import com.braintribe.model.processing.xmi.converter.registry.XmiRegistry;
import com.braintribe.model.processing.xmi.converter.tagdefinition.TagDefinition;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.dom.iterator.FilteringElementIterator;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;
import com.braintribe.web.velocity.renderer.VelocityTemplateRenderer;
import com.braintribe.web.velocity.renderer.VelocityTemplateRendererException;

/**
 * 
 * encodes a GmMetaModel to a XMI document
 * 
 * @author pit
 */

public class XmiEncoder {

	private static Logger log = Logger.getLogger(XmiEncoder.class);
	private XmiRegistry xmiRegistry;
	private VelocityTemplateRenderer renderer;
	private IdGenerator idGenerator;
	private Map<String, Element> generatedPackages = new HashMap<String, Element>();
	private String documentPrefixToUse;
	private PropertyFilteringUtils propertyFilteringUtils = new PropertyFilteringUtils();
	private List<GmProperty> overlayedProperties;
	private ModelDifferentiatorContext differentiatorContext;

	@Configurable @Required
	public void setXmiRegistry(XmiRegistry xmiRegistry) {
		this.xmiRegistry = xmiRegistry;
	}

	@Configurable @Required
	public void setRenderer(VelocityTemplateRenderer renderer) {
		this.renderer = renderer;
	}

	@Configurable @Required
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}
	@Configurable @Required
	public void setDifferentiatorContext(ModelDifferentiatorContext differentiatorContext) {
		this.differentiatorContext = differentiatorContext;
	}
	@Configurable
	public void setDocumentPrefixToUse(String documentPrefixToUse) {
		this.documentPrefixToUse = documentPrefixToUse;
	}
	
	public List<GmProperty> getOverlayedProperties() {
		return overlayedProperties;
	}

	public void resetPackageNameCache() {
		generatedPackages.clear();
	}

	/**
	 * encodes a {@link GmMetaModel} to XMI (xml) document
	 * @param model
	 * @return
	 * @throws CodecException
	 */

	public Document encode(GmMetaModel model) throws CodecException {

		Document document = null;
		try {
			document = DomParser.create().makeItSo();
		} catch (DomParserException e) {
			String msg = "cannot render as " + e;
			log.error(msg, e);
			throw new CodecException(msg, e);
		}

		overlayedProperties = new ArrayList<GmProperty>();
		idGenerator.setCurrentDocument(document);

		DeclaringModelBinding mainArtifactBinding = new DeclaringModelBinding(model);

		// header
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MM dd HH:mm:ss zzz");
		renderer.setContextValue("base", "timestamp", simpleDateFormat.format(new Date()));
		if (documentPrefixToUse == null) {
			documentPrefixToUse = idGenerator.generateDocumentPrefix(document);
		}

		xmiRegistry.addDocumentIdPrefix(document, documentPrefixToUse);

		Map<Document, String> prefixMap = new HashMap<Document, String>();
		prefixMap.put(document, documentPrefixToUse);
		idGenerator.setDocumentToIdPrefixMap(prefixMap);

		Element headerE = render("main", "header");
		Node node = document.importNode(headerE, true);
		Element headerParentE = (Element) document.appendChild(node);
		Element concatenationE = DomUtils.getElementByPath(headerParentE, "XMI.content", false);

		// model

		//
		// check id of model if present..
		//
		String id = idGenerator.generateId(model);
		renderer.setContextValue("model", "id", id);
		renderer.setContextValue("model", "name", model.getName());
		Element modelE = render("model", "model");
		node = document.importNode(modelE, true);

		Element modelParentE = (Element) concatenationE.appendChild(node);
		concatenationE = DomUtils.getElementByPath(modelParentE, ".*Namespace.ownedElement", false);

		//
		// auto injections
		//
		

		
		//
		// TagDefinitions
		//
		Element nodeE = encodeNode( document, "intrinsics");		
		Node importedNodeNode = document.importNode(nodeE, true);
		concatenationE.appendChild(importedNodeNode);
		
		Element namespaceOwnedE = encodeNamespaceOwned();
		Node importedNamespaceNode = document.importNode(namespaceOwnedE, true);
		importedNodeNode.appendChild(importedNamespaceNode);
		

		// GeneralizationPriorityTagDefinition : $name=GeneralizationPriority
		Element generalizationPriorityTagDefinitionE = encodeTagDefinition(Tokens.TOKEN_TAG_GENERALIZATION_PRIORITY, document, mainArtifactBinding);
		Node importedNode = document.importNode(generalizationPriorityTagDefinitionE, true);	
		importedNamespaceNode.appendChild(importedNode);

		// DeclaringModelTagDefinition : $name=ModelDeclaration
		Element artifactBindingTagDefinitionE = encodeTagDefinition(Tokens.TOKEN_TAG_ARTIFACTBINDING, document, mainArtifactBinding);
		importedNode = document.importNode(artifactBindingTagDefinitionE, true);
		importedNamespaceNode.appendChild(importedNode);

		// global id $name=GlobalId
		Element globalTagDefinitionIdE = encodeTagDefinition(Tokens.TOKEN_TAG_GLOBALID, document, mainArtifactBinding);
		importedNode = document.importNode(globalTagDefinitionIdE, true);
		importedNamespaceNode.appendChild(importedNode);
		
		// model dependencies 
		Element modelDependenciesTagDefinitionE = encodeTagDefinition(Tokens.TOKEN_TAG_MODEL_DEPENDENCIES, document, mainArtifactBinding);
		importedNode = document.importNode(modelDependenciesTagDefinitionE, true);		
		importedNamespaceNode.appendChild(importedNode);
		
		// argo/model documentation - tag see
		Element modelDocumentationTagDefinitionE = encodeTagDefinition(Tokens.TOKEN_TAG_DOCUMENTATION_SEE, document, mainArtifactBinding);
		importedNode = document.importNode(modelDocumentationTagDefinitionE, true);
		importedNamespaceNode.appendChild(importedNode);
		
		// model documentation time stamp
		Element dateTagDefinitionE = encodeTagDefinition(Tokens.TOKEN_TAG_DOCUMENTATION_SINCE, document, mainArtifactBinding);
		importedNode = document.importNode(dateTagDefinitionE, true);
		importedNamespaceNode.appendChild(importedNode);


		// documentation - entity (model, class, enum, property) documentation 
		Element documentationTagDefinitionE = encodeTagDefinition(Tokens.TOKEN_TAG_DOCUMENTATION, document, mainArtifactBinding);
		importedNode = document.importNode(documentationTagDefinitionE, true);
		importedNamespaceNode.appendChild(importedNode);

		// deprecated  $tag=true/false
		Element deprecatedTagDefinitionE = encodeTagDefinition(Tokens.TOKEN_TAG_DEPRECATED, document, mainArtifactBinding);
		importedNode = document.importNode(deprecatedTagDefinitionE, true);
		importedNamespaceNode.appendChild(importedNode);
		
		
		//
		// stereotypes
		//
		
		// nullable stereotype : $name=nullable, $class=Attribute
		Element nullableE = encodeStereotype(Tokens.TOKEN_STEREOTYPE_NONNULLABLE, "Attribute", document);
		importedNode = document.importNode(nullableE, true);
		//importedNode = encodeWrapped( document, importedNode);
		importedNamespaceNode.appendChild(importedNode);
		
		
		
		//
		// simple types
		//
		Element simpleTypesE = encodeSimpleTypes(document);
		importedNode = document.importNode(simpleTypesE, true);

		
		FilteringElementIterator iterator = new FilteringElementIterator((Element) importedNode, e -> e.getTagName().equals("UML:Class") || e.getTagName().equals( "UML:Generalization"));  				

		while (iterator.hasNext()) {

			Element suspectE = iterator.next();
			concatenationE.appendChild(suspectE);

			if (suspectE.getTagName().equalsIgnoreCase("UML:Class")) {
				String typeName = suspectE.getAttribute("name");
				// if the type's not yet stored (with its xml representation), add it
				if (xmiRegistry.getTypeEntry(typeName) == null) {
					xmiRegistry.addSimpleTypeById(typeName, suspectE.getAttribute("xmi.id"), xmiRegistry.getSimpleTypeByName(typeName), suspectE, document);
				}

			} else {
				// we don't need the generalizations of the simple types in our registry
			}
		}

		//
		// model properties
		//
		
		// artifact binding for model	
		Element artifactBindingE = encodeArtifactBinding(model);
		importedNode = document.importNode(artifactBindingE, true);
		modelParentE.appendChild(importedNode);
		
		// model dependencies
		Element modelDependenciesE = encodeModelDependencies(model);
		importedNode = document.importNode(modelDependenciesE, true);
		modelParentE.appendChild(importedNode);
		
		// model documentation : time stamp
		Element modelExportDateE = encodeModelStringPayloads(model, "since", "created at " + differentiatorContext.date());
		importedNode = document.importNode(modelExportDateE, true);
		modelParentE.appendChild(importedNode);
		
		// differentiator context in 'see' documentation	
		Element modelSeeE = encodeModelStringPayloads(model, "see", differentiatorContext.asString());
		importedNode = document.importNode(modelSeeE, true);
		modelParentE.appendChild(importedNode);
		
		// 

		/*
		// artifact's model revision
		Element artifactBindingRevisionE = encodeArtifactBindingRevision(model);
		if (artifactBindingRevisionE != null) {
			importedNode = document.importNode(artifactBindingRevisionE, true);
			modelParentE.appendChild(importedNode);
		}
		*/

		Collection<GmProperty> associatedProperties = new TreeSet<GmProperty>(new Comparator<GmProperty>() {
			@Override
			public int compare(GmProperty property1, GmProperty property2) {
				int i = property1.getDeclaringType().getTypeSignature().compareTo(property2.getDeclaringType().getTypeSignature());
				if (i != 0)
					return i;
				return property1.getName().compareTo(property2.getName());
			}

		});

		Collection<GmEntityType> relevantEntityTypes = new TreeSet<GmEntityType>(new Comparator<GmEntityType>() {
			@Override
			public int compare(GmEntityType type1, GmEntityType type2) {
				return type1.getTypeSignature().compareTo(type2.getTypeSignature());
			}
		});

		Collection<GmEntityType> modelEntityTypes = new TreeSet<GmEntityType>(new Comparator<GmEntityType>() {
			@Override
			public int compare(GmEntityType type1, GmEntityType type2) {
				return type1.getTypeSignature().compareTo(type2.getTypeSignature());
			}
		});
				
		modelEntityTypes.addAll( recursivelyExtractAllEntitytypes(model));
		
		// TODO : extract all attached data that UML can't handle

		Map<String, Element> packages = new HashMap<>();
		Map<GmType, Element> typeToDocumentElementMap = new HashMap<>();

		//
		// entity types
		//

		for (GmEntityType entityType : modelEntityTypes) {
			
			relevantEntityTypes.add(entityType);

			String typeSignature = entityType.getTypeSignature();
			log.info("Handling type [" + typeSignature + "]");

			// determine (and ev create) the correct parent element for the entity type -> depending on package name extracted from type signature
			Element parentE = concatenationE;
			parentE = determinePackageParentForType(concatenationE, packages, typeSignature, parentE);

			// 
			Element typeE = encodeEntityType(entityType, associatedProperties);
			importedNode = document.importNode(typeE, true);
			parentE.appendChild(importedNode);
			
			typeToDocumentElementMap.put( entityType, (Element) importedNode);

			// artifact binding for GmEntityType -> create one and store if not main model	
			artifactBindingE = encodeArtifactBinding(entityType);
			Node importedBindingNode = document.importNode(artifactBindingE, true);
			importedNode.appendChild(importedBindingNode);
		
			
			// global id
			Element globalIdE = encodeGlobalId(entityType);
			Node importedGlobalIdNode = document.importNode(globalIdE, true);
			importedNode.appendChild(importedGlobalIdNode);
			
			// meta data 
			List<Element> metaDataE = encodeRelevantMetaData(entityType);
			if (metaDataE != null && !metaDataE.isEmpty()) {
				final Document d = document;
				final Node i = importedNode;
				metaDataE.stream().forEach( e -> {
					Node importedMetaDateNode = d.importNode(e, true);
					i.appendChild(importedMetaDateNode);
				});
			}			
		}

		//
		// enum types
		//

		Collection<GmEnumType> modelEnumTypes = new TreeSet<GmEnumType>(new Comparator<GmEnumType>() {

			@Override
			public int compare(GmEnumType type1, GmEnumType type2) {
				return type1.getTypeSignature().compareTo(type2.getTypeSignature());
			}
		});

		modelEnumTypes.addAll( recursivelyExtractAllEnumtypes(model));

		for (GmEnumType enumType : modelEnumTypes) {

			String typeSignature = enumType.getTypeSignature();
			log.info("Handling type [" + typeSignature + "]");

			Element parentE = concatenationE;
			parentE = determinePackageParentForType(concatenationE, packages, typeSignature, parentE);

			Element typeE = encodeEnumType(enumType);
			importedNode = document.importNode(typeE, true);
			parentE.appendChild(importedNode);

			typeToDocumentElementMap.put( enumType, (Element) importedNode);

			// artifact binding for GmEnumType
			DeclaringModelBinding typeBinding = new DeclaringModelBinding( enumType.getDeclaringModel());			
			artifactBindingE = encodeArtifactBinding(enumType);
			Node importedBindingNode = document.importNode(artifactBindingE, true);
			importedNode.appendChild(importedBindingNode);
			
			// global id
			Element globalIdE = encodeGlobalId(enumType);
			Node importedGlobalIdNode = document.importNode(globalIdE, true);
			importedNode.appendChild(importedGlobalIdNode);				
			
			// meta data 
			List<Element> metaDataE = encodeRelevantMetaData(enumType);
			if (metaDataE != null && !metaDataE.isEmpty()) {
				final Document d = document;
				final Node i = importedNode;
				metaDataE.stream().forEach( e -> {
					Node importedMetaDateNode = d.importNode(e, true);
					i.appendChild(importedMetaDateNode);
				});
			}
		}

		//
		// associations
		//

		for (GmProperty property : associatedProperties) {
			Element associationE = encodeAssociation(property);

			log.info("Handling association [" + property.getName() + "] of ["
					+ property.getDeclaringType().getTypeSignature() + "] ([" + property.getType().getTypeSignature()
					+ "])");

			importedNode = document.importNode(associationE, true);

			// global id		
			Element globalIdE = encodeGlobalId(property);
			Node importedGlobalIdNode = concatenationE.getOwnerDocument().importNode(globalIdE, true);
			importedNode.appendChild(importedGlobalIdNode);
			
			// meta data 
			List<Element> metaDataE = encodeRelevantMetaData( property);
			if (metaDataE != null && !metaDataE.isEmpty()) {
				final Document d = document;
				final Node i = importedNode;
				metaDataE.stream().forEach( e -> {
					Node importedMetaDateNode = d.importNode(e, true);
					i.appendChild(importedMetaDateNode);
				});
			}

			// must be last in sequence because of the wrapper !

			// find what package the entity type belongs to and associate the association with it
			GmEntityType entityType = property.getDeclaringType();			
			Element associationParentE = typeToDocumentElementMap.get( entityType);
			if (associationParentE != null) {
				importedNode = encodeWrapped( document, importedNode);				
				associationParentE.appendChild(importedNode);
			}
			else {
				concatenationE.appendChild(importedNode);
			}			
				
		}

		//
		// generalizations..		
		//

		// a) get all generalizations
		for (GmEntityType entityType : relevantEntityTypes) {
			List<GmEntityType> supers = entityType.getSuperTypes();

			if ( supers != null && supers.size() > 0) {

				for (GmEntityType superType : supers) {
					log.info("Handling generalization [" + entityType.getTypeSignature() + "] -> ["
							+ superType.getTypeSignature() + "]");

					// only do generalizations that are not linked to a root type
					String superTypeSignature = superType.getTypeSignature();

					GmEntityType rootType = xmiRegistry.getRoottypeByDocument(document);
					if (
							rootType == null ||
							superTypeSignature.equalsIgnoreCase(rootType.getTypeSignature()) == false
						){

						Element generalizationE = encodeGeneralization(entityType, superType);
						importedNode = document.importNode(generalizationE, true);
						
						Element associationParentE = typeToDocumentElementMap.get( entityType);
						if (associationParentE != null) { 
							// wrap
							importedNode = encodeWrapped(document, importedNode);
							associationParentE.appendChild(importedNode);													
						}
						else {
							concatenationE.appendChild(importedNode);
						}						
					}
				}
			}
		}
		//
		return document;
	}

	Predicate<MetaData> filter = m -> {
		if (m instanceof Deprecated)
			return true;
		if (m instanceof Description)
			return true;
		return false;
	};
	
	private Element renderMetaData(GenericEntity ge, MetaData m) {
		Map<String, String> ids;
		String value;
		if (m instanceof Deprecated) {
			if (ge instanceof GmEntityType || ge instanceof GmEnumType) {
				ids = idGenerator.generateIdForDeprecatedTagReference((GmType) ge);
			}			
			else if (ge instanceof GmProperty) {
				ids = idGenerator.generateIdForDeprecatedTagReference((GmProperty) ge);
			}
			else if (ge instanceof GmEnumConstant) {
				ids = idGenerator.generateIdForDeprecatedTagReference((GmEnumConstant) ge);
			}
			else {
				throw new IllegalStateException("type [ " + ge.entityType().getTypeName() + "] is not a valid anchor for rendered 'deprecated' metadata");
			}
			value = "true";
		}
		else if (m instanceof Description) {
			if (ge instanceof GmEntityType || ge instanceof GmEnumType) {
				ids = idGenerator.generateIdForDocumentationTagReference((GmType) ge);
			}			
			else if (ge instanceof GmProperty) {
				ids = idGenerator.generateIdForDocumentationTagReference((GmProperty) ge);
			}
			else if (ge instanceof GmEnumConstant) {
				ids = idGenerator.generateIdForDocumentationTagReference((GmEnumConstant) ge);
			}
			else {
				throw new IllegalStateException("type [ " + ge.entityType().getTypeName() + "] is not a valid anchor for rendered 'description' metadata");
			}
			Description description = (Description) m;
			value = description.getDescription().value();
		}
		else {
			return null;
		}
		
		renderer.setContextValue("metadata", "id", ids.get("id"));
		renderer.setContextValue("metadata", "refIdType", "xmi.idref");
		renderer.setContextValue("metadata", "refId", ids.get("refId"));
		renderer.setContextValue("metadata", "value", StringEscapeUtils.escapeXml(value));
		return render("tagReference", "metadata");
	}

	/**
	 * @param entityType - the {@link GmEntityType} of whose {@link MetaData} should be transformed
	 * @return - the {@link Element} that represents the selected {@link MetaData}
	 */
	private List<Element> encodeRelevantMetaData(GmEntityType entityType) {
		Set<MetaData> attachedMetadata = entityType.getMetaData();
		if (attachedMetadata == null || attachedMetadata.isEmpty()) 
			return null;
		return attachedMetadata.stream().filter( filter).map( m -> {
			Element e = renderMetaData( entityType, m);
			return e;
		}).collect(Collectors.toList());		
	}
	/**
	 * @param enumType - the {@link GmEnumType} of whose {@link MetaData} should be transformed
	 * @return - the {@link Element} that represents the selected {@link MetaData}
	 */
	private List<Element> encodeRelevantMetaData(GmEnumType enumType) {
		Set<MetaData> attachedMetadata = enumType.getMetaData();
		if (attachedMetadata == null || attachedMetadata.isEmpty()) 
			return null;
		return attachedMetadata.stream().filter( filter).map( m -> {
			Element e = renderMetaData( enumType, m);
			return e;
		}).collect(Collectors.toList());		
	}

	/**
	 * @param enumConstant - the {@link GmEnumConstant} of whose {@link MetaData} should be transformed
	 * @return - the {@link Element} that represents the selected {@link MetaData}
	 */
	private List<Element> encodeRelevantMetaData(GmEnumConstant enumConstant) {
		Set<MetaData> attachedMetadata = enumConstant.getMetaData();
		if (attachedMetadata == null || attachedMetadata.isEmpty()) 
			return null;
		return attachedMetadata.stream().filter( filter).map( m -> {
			Element e = renderMetaData( enumConstant, m);
			return e;
		}).collect(Collectors.toList());		
	}
	

	

	/**
	 * @param property - the {@link GmProperty} of whose {@link MetaData} should be transformed
	 * @return - {@link Element} that represents the selected {@link MetaData}
	 */
	private List<Element> encodeRelevantMetaData(GmProperty property) {
		Set<MetaData> attachedMetadata = property.getMetaData();
		if (attachedMetadata == null || attachedMetadata.isEmpty()) 
			return null;
		return attachedMetadata.stream().filter( filter).map( m -> {
			Element e = renderMetaData( property, m);
			return e;
		}).collect(Collectors.toList());		
	}
	

	private Node encodeWrapped(Document document, Node importedNode) {		
		Element namespaceOwnedE = encodeNamespaceOwned();
		Node importedNamespaceOwnedNode = document.importNode(namespaceOwnedE, true);
		importedNamespaceOwnedNode.appendChild(importedNode);
		return importedNamespaceOwnedNode;
	}

	private Element determinePackageParentForType(Element concatenationE, Map<String, Element> packages, String typeSignature, Element parentE) {
		int p = typeSignature.lastIndexOf(".");
		if (p > 0) {
			String packageName = typeSignature.substring(0, p);
			if (packages.containsKey(packageName) == false) {
				parentE = encodePackage(concatenationE, packageName);
				packages.put(packageName, parentE);
			} else {
				parentE = packages.get(packageName);
			}
		}
		return parentE;
	}
	
	private Element encodeNode(Document document, String name) {
		String id = idGenerator.generateNodeId(name);
		renderer.setContextValue( "node", "id", id);
		renderer.setContextValue("node", "name", name);
		return render("node", "node");
	}

	public Element encodeSimpleTypes(Document document) throws CodecException {
		String documentId = xmiRegistry.getDocumentPrefixByDocument(document);
		Map<String, String> ids = idGenerator.generateSimpleTypeIds();
		for (Entry<String, String> entry : ids.entrySet()) {
			renderer.setContextValue("context", entry.getKey(), entry.getValue());
		}
		renderer.setContextValue("context", "documentId", documentId);
		return render("simpletypes", "context");

	}

	public Element encodeTagDefinition(String name, Document document, DeclaringModelBinding artifactBinding) throws CodecException {

		Map<String, String> ids = idGenerator.generateTagDefinitionId(name);
		String id = ids.get("id");

		renderer.setContextValue("context", "id", id);
		renderer.setContextValue("context", "name", name);
		renderer.setContextValue("context", "multiplicityId", ids.get("multiplicity"));
		renderer.setContextValue("context", "multiplicityRangeId", ids.get("multiplicityRange"));
		
		Element elementE = render("tagDefinition", "context");
		TagDefinition tagDefinition = new TagDefinition();
		tagDefinition.setName(name);
		tagDefinition.setXmiId(id);
		tagDefinition.setArtifactBinding(artifactBinding);
		xmiRegistry.addTagDefinitionById(id, tagDefinition, elementE, document);

		return elementE;

	}

	public Element encodeStereotype(String name, String target, Document document) throws CodecException {

		String id = idGenerator.generateStereotypeId(name, target, null, document);
		renderer.setContextValue("context", "id", id);
		renderer.setContextValue("context", "name", name);
		renderer.setContextValue("context", "class", target);
		
		Element elementE = render("stereotype", "context");
		xmiRegistry.addStereotypeById(id, name, target, elementE, document);

		return elementE;

	}

	/**
	 * encodes a package (by reusing already existing subpaths)
	 * @param modelE - the {@link Element} 
	 * @param packageName - the name of the package
	 * @return
	 * @throws CodecException
	 */

	public Element encodePackage(Element modelE, String packageName) throws CodecException {

		String[] parts = packageName.split("\\.");

		Element lastE = null;
		Element packagePartE = null;
		String partToTest = "";

		for (String part : parts) {

			//
			partToTest += partToTest.length() > 0 ? "." + part : part;
			packagePartE = generatedPackages.get(partToTest);
			if (packagePartE != null) {
				lastE = DomUtils.getElementByPath(packagePartE, ".*Namespace.ownedElement", false);
				continue;
			}
			renderer.setContextValue("package", "name", part);

			String id = idGenerator.generateId(partToTest);
			renderer.setContextValue("package", "id", id);

			packagePartE = render("package", "package");
			packagePartE = (Element) modelE.getOwnerDocument().importNode(packagePartE, true);

			generatedPackages.put(partToTest, packagePartE);

			if (lastE == null) {
				Element attachedE = (Element) modelE.appendChild(packagePartE);
				lastE = DomUtils.getElementByPath(attachedE, ".*Namespace.ownedElement", false);
			} else {
				lastE.appendChild(packagePartE);
				lastE = DomUtils.getElementByPath(packagePartE, ".*Namespace.ownedElement", false);
			}
		}
		return (Element) lastE;

	}

	/**
	 * @param type
	 * @param associatedProperties
	 * @return
	 * @throws CodecException
	 * 
	 */

	public Element encodeEntityType(GmEntityType type, Collection<GmProperty> associatedProperties) throws CodecException {

		// class
		Map<String, String> idMap = idGenerator.generateId(type);
		renderer.setContextValue("class", "id", idMap.get("id"));
		
		String signature = type.getTypeSignature();
		int p = signature.lastIndexOf(".");
		String name = signature.substring(p + 1);
		renderer.setContextValue("class", "name", name);

		// set the abstract flag
		String abstractFlag = "false";

		if (Boolean.TRUE.equals(type.getIsAbstract())) {
			abstractFlag = "true";
		}

		renderer.setContextValue("class", "abstract", abstractFlag);

		// stereotype for plain

		String stereoTypeId = idMap.get("stereotypePlainId");
		if (stereoTypeId != null) {
			renderer.setContextValue("class", "stereotypePlainId", stereoTypeId);
			if (idGenerator.isExternalReference(stereoTypeId)) {
				renderer.setContextValue("class", "stereotypePlainRefIdKey", "href");
			} else {
				renderer.setContextValue("class", "stereotypePlainRefIdKey", "xmi.idref");
			}

		} else {
			renderer.setContextValue("class", "stereotypePlainId", null);
		}

		String stereoTypeDiscardId = idMap.get("stereotypeDiscardId");

		if (stereoTypeDiscardId != null) {
			renderer.setContextValue("class", "stereotypeDiscardId", stereoTypeDiscardId);
			if (idGenerator.isExternalReference(stereoTypeDiscardId)) {
				renderer.setContextValue("class", "stereotypeDiscardRefIdKey", "href");
			} else {
				renderer.setContextValue("class", "stereotypeDiscardRefIdKey", "xmi.idref");
			}
		} else {
			renderer.setContextValue("class", "stereotypeDiscardId", null);
		}

		Element classE = render("class", "class");

		Element concatenationE = DomUtils.getElementByPath(classE, ".*Classifier.feature", false);

		// attributes

		List<GmProperty> properties = type.getProperties();

		if (properties != null) {
			for (GmProperty property : properties) {
				// check if we need to implement the property on this entityType..
				if (propertyFilteringUtils.propertyNeedsToBeImplemented(type, property) == false) {
					overlayedProperties.add(property);
					continue;
				}

				GmType propertyType = property.getType();

				// complex properties are expressed as associations and not as attributes
				if ( 
						(propertyType instanceof GmEntityType) ||
						(propertyType instanceof GmEnumType) ||
						(propertyType instanceof GmCollectionType)
				) {
					associatedProperties.add(property);
					continue;
				}

				//

				log.info("Handling attribute [" + property.getName() + "] of ["
						+ property.getDeclaringType().getTypeSignature() + "] ([" + property.getType().getTypeSignature()
						+ "])");

				Element attributeE = encodeAttribute(property);
				Node importedNode = concatenationE.getOwnerDocument().importNode(attributeE, true);
				concatenationE.appendChild(importedNode);
				
				// global id			
				Element globalIdE = encodeGlobalId(property);
				Node importedGlobalIdNode = concatenationE.getOwnerDocument().importNode(globalIdE, true);
				importedNode.appendChild(importedGlobalIdNode);
				
				//
				List<Element> metaDataE = encodeRelevantMetaData( property);
				if (metaDataE != null && !metaDataE.isEmpty()) {
					final Document d = concatenationE.getOwnerDocument();
					final Node i = importedNode;
					metaDataE.stream().forEach( e -> {
						Node importedMetaDateNode = d.importNode(e, true);
						i.appendChild(importedMetaDateNode);
					});
				}
				
			
			}

		}

		return classE;

	}

	/**
	 * encode a {@link GmEnumType}
	 * @param type 
	 * @return
	 * @throws CodecException
	 * 
	 */

	public Element encodeEnumType(GmEnumType type) throws CodecException {
		Map<String, String> valueToIdMap = idGenerator.generateId(type);
		renderer.setContextValue("enum", "id", valueToIdMap.get("enumId"));
		valueToIdMap.remove("enumId");
		renderer.setContextValue("enum", "map", valueToIdMap);
		Map<String,String> mdMap = new HashMap<>();
		// 
		renderer.setContextValue( "enum", "mdMap", mdMap);

		String signature = type.getTypeSignature();
		int p = signature.lastIndexOf(".");
		String name = signature.substring(p + 1);

		renderer.setContextValue("enum", "name", name);
		Element classE = render("enum", "enum");
		
		Map<String, GmEnumConstant> nameToConstant = new HashMap<>();
		type.getConstants().stream().filter( c -> c.getMetaData() != null && !c.getMetaData().isEmpty()).forEach( c -> nameToConstant.put( c.getName(), c));		
		
		Element enumLiteralsE = DomUtils.getElementByPath( classE, ".*Enumeration\\.literal", false);
		Iterator<Element> literalIterator = DomUtils.getElementIterator(enumLiteralsE, ".*EnumerationLiteral");
		while (literalIterator.hasNext()) {
			Element enumLiteralE = literalIterator.next();
			String constantName = enumLiteralE.getAttribute("name");
			GmEnumConstant constant = nameToConstant.get( constantName);
			// constant not in map --> no 
			if (constant == null)
				continue;
			
			List<Element> metaDataE = encodeRelevantMetaData( constant);
			if (metaDataE != null && !metaDataE.isEmpty()) {
				final Document d = classE.getOwnerDocument();
				final Node i = enumLiteralE;
				metaDataE.stream().forEach( e -> {
					Node importedMetaDateNode = d.importNode(e, true);
					i.appendChild(importedMetaDateNode);
				});
			}
		}
		
		return classE;

	}

	/**
	 * encode a {@link GmProperty} as attribute
	 * @param property - the {@link GmProperty}
	 * @return - the proper {@link Element}
	 * 
	 * @throws CodecException
	 */

	public Element encodeAttribute(GmProperty property) throws CodecException {

		Map<String, String> idMap = idGenerator.generateAttributeId(property);

		renderer.setContextValue("attribute", "id", idMap.get("id"));
		renderer.setContextValue("attribute", "multiplicityId", idMap.get("multiplicityId"));
		renderer.setContextValue("attribute", "multiplicityRangeId", idMap.get("multiplicityRangeId"));

		String typeRefId = idMap.get("typeId");
		if (typeRefId == null) {
			String msg = "no id found for typeId [" + property.getType().getTypeSignature() + "] of ["
					+ property.getDeclaringType().getTypeSignature() + "]'s property [" + property.getName() + "]";
			log.error(msg, null);
			throw new CodecException(msg);
		}

		if (idGenerator.isExternalReference(typeRefId)) {
			renderer.setContextValue("attribute", "typeRefIdKey", "href");
		} else {
			renderer.setContextValue("attribute", "typeRefIdKey", "xmi.idref");
		}

		renderer.setContextValue("attribute", "typeRefId", typeRefId);
		renderer.setContextValue("attribute", "name", property.getName());

		// id property stereotype
		String stereoTypeId = idMap.get("stereotypeId");

		if (stereoTypeId != null) {
			renderer.setContextValue("attribute", "stereotypeId", stereoTypeId);
			if (idGenerator.isExternalReference(stereoTypeId)) {
				renderer.setContextValue("attribute", "stereotypeRefIdKey", "href");
			} else {
				renderer.setContextValue("attribute", "stereotypeRefIdKey", "xmi.idref");
			}
		} else {
			renderer.setContextValue("attribute", "stereotypeId", null);
		}
		// nullable stereotype

		String nullableId = idMap.get("nullableId");

		if (nullableId != null) {
			renderer.setContextValue("attribute", "nullableId", nullableId);
			if (idGenerator.isExternalReference(nullableId)) {
				renderer.setContextValue("attribute", "nullableRefIdKey", "href");
			} else {
				renderer.setContextValue("attribute", "nullableRefIdKey", "xmi.idref");
			}
		} else {
			renderer.setContextValue("attribute", "nullableId", null);
		}

		// multiplicity
		renderer.setContextValue("attribute", "lowerMultiplicity", "1");
		renderer.setContextValue("attribute", "upperMultiplicity", "1");
		return render("attribute", "attribute");
	}
	
	public Element encodeNamespaceOwned() {
		renderer.setContextValue("context", "nothing", "important");
		return render("namespaceOwned", "context");
	}

	/**
	 * encode a {@link GmProperty} as an association
	 * @param property - the {@link GmProperty}
	 * @return - the proper {@link Element}
	 * 
	 * @throws CodecException
	 */

	public Element encodeAssociation(GmProperty property) throws CodecException {

		Map<String, String> ids = idGenerator.generateAssociationId(property);
		renderer.setContextValue("association", "id", ids.get("id"));
		renderer.setContextValue("association", "name", property.getName());
		renderer.setContextValue("association", "end1Id", ids.get("end1Id"));
		renderer.setContextValue("association", "end2Id", ids.get("end2Id"));

		String class1IdRef = ids.get("class1IdRef");

		if (idGenerator.isExternalReference(class1IdRef)) {
			renderer.setContextValue("association", "class1IdRefType", "href");
		} else {
			renderer.setContextValue("association", "class1IdRefType", "xmi.idref");
		}

		renderer.setContextValue("association", "class1IdRef", class1IdRef);

		String class2IdRef = ids.get("class2IdRef");

		if (idGenerator.isExternalReference(class2IdRef)) {
			renderer.setContextValue("association", "class2IdRefType", "href");
		} else {
			renderer.setContextValue("association", "class2IdRefType", "xmi.idref");
		}

		renderer.setContextValue("association", "class2IdRef", class2IdRef);
		renderer.setContextValue("association", "multiplicityId", ids.get("multiplicityId"));
		renderer.setContextValue("association", "multiplicityRangeId", ids.get("multiplicityRangeId"));
		renderer.setContextValue("association", "multiplicity", ids.get("multiplicity"));
		renderer.setContextValue("association", "order", ids.get("order"));

		// special for maps..

		String keyId = ids.get("keyId");

		if (keyId != null) {
			renderer.setContextValue("association", "keyId", ids.get("keyId"));
			renderer.setContextValue("association", "typeRef", ids.get("keyClassRef"));
			renderer.setContextValue("association", "multiplicity2Id", ids.get("multiplicity2Id"));
			renderer.setContextValue("association", "multiplicity2RangeId", ids.get("multiplicity2RangeId"));
			return render("associationClass", "association");

		} else {
			return render("association", "association");
		}
	}

	public Element encodeAssociationClass(GmProperty property) throws CodecException {

		return null;

	}

	/**
	 * encodes a inheritance relation as a generalization
	 * @param subType
	 * @param superType
	 * @return - the encoded result as {@link Element}
	 * @throws CodecException
	 */

	public Element encodeGeneralization(GmEntityType subType, GmEntityType superType) throws CodecException {

		Map<String, String> ids = idGenerator.generateId(subType, superType);

		renderer.setContextValue("generalization", "id", ids.get("id"));

		String subtypeIdRef = ids.get("subtype");

		if (idGenerator.isExternalReference(subtypeIdRef)) {
			renderer.setContextValue("generalization", "subtypeIdRefType", "href");
		} else {
			renderer.setContextValue("generalization", "subtypeIdRefType", "xmi.idref");
		}

		renderer.setContextValue("generalization", "subtype", subtypeIdRef);

		String supertypeIdRef = ids.get("supertype");

		if (idGenerator.isExternalReference(supertypeIdRef)) {
			renderer.setContextValue("generalization", "supertypeIdRefType", "href");
		} else {
			renderer.setContextValue("generalization", "supertypeIdRefType", "xmi.idref");
		}

		renderer.setContextValue("generalization", "supertype", supertypeIdRef);

		// name it
		String subName = subType.getTypeSignature().substring(subType.getTypeSignature().lastIndexOf(".") + 1);
		String superName = superType.getTypeSignature().substring(superType.getTypeSignature().lastIndexOf(".") + 1);
		renderer.setContextValue("generalization", "name", subName + "->" + superName);

		return render("generalization", "generalization");

	}

	/**
	 * render a GmMetaModel as an artifactbinding reference 
	 * @param model - the {@link GmMetaModel}
	 * @return - the encoded result as {@link Element} 
	 * 
	 * @throws CodecException
	 * 
	 */
	public Element encodeArtifactBinding(GmMetaModel model) throws CodecException {

		Map<String, String> ids = idGenerator.generateIdForArtifactBindingTagReference(model);

		renderer.setContextValue("artifactBinding", "id", ids.get("id"));
		renderer.setContextValue("artifactBinding", "refIdType", "xmi.idref");
		renderer.setContextValue("artifactBinding", "refId", ids.get("refId"));
		renderer.setContextValue("artifactBinding", "value", model.getName());
		return render("tagReference", "artifactBinding");
	}
	
	

	/**
	 * render a GmMetaModel as an artifactbinding reference 
	 * @param model - the {@link GmMetaModel}
	 * @return - the encoded result as {@link Element} 
	 * 
	 * @throws CodecException
	 * 
	 */
	public Element encodeModelDependencies(GmMetaModel model) throws CodecException {
		
		String data = MetaModelDependencyHandler.collectEncodedModelDependencies(model);

		Map<String, String> ids = idGenerator.generateIdForModelDependencyTagReference(model);

		renderer.setContextValue("modelDependencies", "id", ids.get("id"));
		renderer.setContextValue("modelDependencies", "refIdType", "xmi.idref");
		renderer.setContextValue("modelDependencies", "refId", ids.get("refId"));
		renderer.setContextValue("modelDependencies", "value", data);
		return render("tagReference", "modelDependencies");
	}	
	
	public Element encodeModelStringPayloads( GmMetaModel model, String tagDefinition, String payload) {
		Map<String, String> ids = idGenerator.generateIdForModelDocumentationTagReference( model, tagDefinition);

		renderer.setContextValue("payload", "id", ids.get("id"));
		renderer.setContextValue("payload", "refIdType", "xmi.idref");
		renderer.setContextValue("payload", "refId", ids.get("refId"));
		renderer.setContextValue("payload", "value", payload);
		return render("tagReference", "payload");
		
	}
	
	/**
	 * encodes the declaring model of the type 
	 * @param type - the {@link GmType} in question
	 * @return - the newly generated {@link Element}
	 * @throws CodecException
	 */
	public Element encodeArtifactBinding(GmType type) throws CodecException {

		Map<String, String> ids = idGenerator.generateIdForArtifactBindingTagReference(type);

		renderer.setContextValue("artifactBinding", "id", ids.get("id"));
		renderer.setContextValue("artifactBinding", "refIdType", ids.get("refIdType"));
		renderer.setContextValue("artifactBinding", "refId", ids.get("refId"));

		GmMetaModel model = null;
		if (type instanceof GmEntityType) {
			GmEntityType entityType = (GmEntityType) type;
			model = entityType.getDeclaringModel();
		} else {
			GmEnumType enumType = (GmEnumType) type;
			model = enumType.getDeclaringModel();
		}

		renderer.setContextValue("artifactBinding", "value", new DeclaringModelBinding(model).toString());
		return render("tagReference", "artifactBinding");
	}
	
	/**
	 * encodes the global id of the type 
	 * @param type - the {@link GmType} in question
	 * @return - the newly generated {@link Element}
	 * @throws CodecException
	 */
	public Element encodeGlobalId( GmType type) throws CodecException {
		Map<String, String> ids = idGenerator.generateIdForGlobalIdTagReference(type);

		renderer.setContextValue("globalId", "id", ids.get("id"));
		renderer.setContextValue("globalId", "refIdType", ids.get("refIdType"));
		renderer.setContextValue("globalId", "refId", ids.get("refId"));
		
		renderer.setContextValue("globalId", "value", type.getGlobalId());
		return render("tagReference", "globalId");
		
	}
	/**
	 * encodes the global id of the type 
	 * @param property - the {@link GmType} in question
	 * @return - the newly generated {@link Element}
	 * @throws CodecException
	 */
	public Element encodeGlobalId( GmProperty property) throws CodecException {
		Map<String, String> ids = idGenerator.generateIdForGlobalIdTagReference(property);

		renderer.setContextValue("globalId", "id", ids.get("id"));
		renderer.setContextValue("globalId", "refIdType", ids.get("refIdType"));
		renderer.setContextValue("globalId", "refId", ids.get("refId"));
		
		renderer.setContextValue("globalId", "value", property.getGlobalId());
		return render("tagReference", "globalId");
		
	}


	/**
	 * render a template 
	 * @param key - the key for the template
	 * @param context - the context to be used 
	 * @return - the {@link Element} resulting (needs to imported !!)
	 * @throws CodecException
	 * 
	 */
	private Element render(String key, String context) throws CodecException {

		try {
			String value = renderer.renderTemplate(key, context);
			Document document = DomParser.load().from(value);
			return document.getDocumentElement();
		} catch (VelocityTemplateRendererException e) {

			String msg = "cannot render as " + e;
			log.error(msg, e);
			throw new CodecException(msg, e);

		} catch (DomParserException e) {
			String msg = "cannot load rendered string as " + e;
			log.error(msg, e);
			throw new CodecException(msg, e);
		}

	}

	private boolean compareBindingArtifact(DeclaringModelBinding one, DeclaringModelBinding two) {
		if (one.getModel() == two.getModel())
			return true;
		if (one.getName().equals( two.getName()))
			return true;
		return false;
	}
	
	private Collection<GmEntityType> recursivelyExtractAllEntitytypes (GmMetaModel model) {
		List<GmEntityType> result = new ArrayList<>();
		result.addAll( model.getTypes().stream().filter( t -> t.isGmEntity()).map( t -> (GmEntityType) t).collect(Collectors.toList()));
		for (GmMetaModel dependency : model.getDependencies()) {
			result.addAll( recursivelyExtractAllEntitytypes(dependency));
		}
		return result;
	}
	private Collection<GmEnumType> recursivelyExtractAllEnumtypes (GmMetaModel model) {
		List<GmEnumType> result = new ArrayList<>();
		result.addAll( model.getTypes().stream().filter( t -> t.isGmEnum()).map( t -> (GmEnumType) t).collect(Collectors.toList()));
		for (GmMetaModel dependency : model.getDependencies()) {
			result.addAll( recursivelyExtractAllEnumtypes(dependency));
		}
		return result;
	}

}
