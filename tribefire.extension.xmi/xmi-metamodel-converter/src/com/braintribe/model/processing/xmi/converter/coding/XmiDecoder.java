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


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.prompt.Deprecated;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.processing.xmi.converter.Tokens;
import com.braintribe.model.processing.xmi.converter.experts.PackageExpert;
import com.braintribe.model.processing.xmi.converter.registry.XmiRegistry;
import com.braintribe.model.processing.xmi.converter.registry.entries.RegistryEntry;
import com.braintribe.model.processing.xmi.converter.tagdefinition.TagDefinition;
import com.braintribe.model.processing.xmi.converter.tagdefinition.TagReference;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.dom.iterator.FilteringElementIterator;
import com.braintribe.utils.xml.dom.iterator.filters.RegExpTagNameFilter;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;

/**
 * @author pit
 *
 */
public class XmiDecoder {

	private static Logger log = Logger.getLogger(XmiDecoder.class);

	private File referenceDirectory;
	private XmiRegistry xmiRegistry;
	private Codec<GmMetaModel, Document> codec;
	private PackageExpert packageExpert;
	private String decodedDifferentiation;

	public void setReferenceDirectory(File referenceDirectory) {
		this.referenceDirectory = referenceDirectory;
	}

	@Required
	public void setXmiRegistry(XmiRegistry xmiRegistry) {
		this.xmiRegistry = xmiRegistry;
	}

	@Required
	public void setCodec(Codec<GmMetaModel, Document> codec) {
		this.codec = codec;
	}

	@Required
	public void setPackageExpert(PackageExpert packageExpert) {
		this.packageExpert = packageExpert;
	}
	
	public String getDecodedDifferentiation() {
		return decodedDifferentiation;
	}
	
	/**
	 * @param id
	 * @return
	 */
	private GmType checkIfTypeIsAlreadyAcquired(String id) {
		if (id.contains("#")) {
			String[] parts = id.split("#");
			String reference = parts[0];
			String xmiRefId = parts[1];
			log.info("Checking on type of [" + reference + "], id [" + xmiRefId + "]");
			return xmiRegistry.getTypeById(xmiRefId);
		} else {
			return xmiRegistry.getTypeById(id);
		}
	}

	public Map<String, String> acquireModel(Element modelE, Document document) throws CodecException {
		// tag references are added to the class
		RegistryEntry registryEntry = xmiRegistry.addModelById(modelE.getAttribute("xmi.id"),
				modelE.getAttribute("name"), modelE, document);
		Map<String, String> taggedValues = acquireTagReferences(modelE, registryEntry);
		return taggedValues;
	}

	/**
	 * @param classE   : dom element that represents the class
	 * @param document : the document to use as lookup
	 * @return - the acquired GmType or null
	 * @throws CodecException
	 */
	public GmType acquireClass(Element classE, Document document) throws CodecException {
		String xmiId = classE.getAttribute("xmi.id");
		String name = classE.getAttribute("name");

		// check if already processed
		// GmType gmType = xmiIdToEntityTypeMap.get( xmiId);
		GmType gmType = checkIfTypeIsAlreadyAcquired(xmiId);
		if (gmType != null) {
			return gmType;
		}

		// check if internal simple type
		gmType = xmiRegistry.getSimpleTypeByName(name);

		if (gmType != null) {
			xmiRegistry.addSimpleTypeById(name, xmiId, gmType, classE, document);
			log.info("Adding simple type [" + name + "] for id [" + xmiId + "]");
			return gmType;
		}

		// create type
		String packageName = packageExpert.getPackageNameOfClass(classE);

		GmEntityType gmEntityType = GmEntityType.T.create();
		if ((packageName != null) && (packageName.length() > 0))
			gmEntityType.setTypeSignature(packageName + "." + name);
		else
			gmEntityType.setTypeSignature(name);

		// store type
		RegistryEntry registryEntry = xmiRegistry.addEntityTypeById(xmiId, gmEntityType, classE, document);
		log.info("Adding complex type [" + gmEntityType.getTypeSignature() + "] for id [" + xmiId + "]");

		// check abstract flag
		String isAbstract = classE.getAttribute("isAbstract");
		if (isAbstract.equalsIgnoreCase("true")) {
			gmEntityType.setIsAbstract(true);
		} else {
			gmEntityType.setIsAbstract(false);
		}

		// attributes
		Element classifierE = DomUtils.getElementByPath(classE, ".*Classifier.feature", false);
		if (classifierE != null) {
			FilteringElementIterator iterator = new FilteringElementIterator(classifierE, new RegExpTagNameFilter(".*Attribute"));
			while (iterator.hasNext()) {
				Element attributeE = iterator.next();
				acquireAttribute(attributeE, gmEntityType, document);
			}
		}
		/*
		 * // generalizations Element generalizationRefE =
		 * DomUtils.getElementByPath(classE,
		 * ".*GeneralizableElement.generalization/.*Generalization", false); if
		 * (generalizationRefE != null) { String refId =
		 * generalizationRefE.getAttribute("xmi.idref"); Element generalizationE =
		 * acquireElement(refId, "Generalization", document); if (generalizationE ==
		 * null) { String msg = "Generalization [" + refId + "] cannot be found ";
		 * log.error(msg, null); throw new CodecException(msg); }
		 * acquireGeneralisation(generalizationE, document); }
		 */
		//
		// caution : several stereotypes are possible
		//

		NodeList nodes = classE.getElementsByTagNameNS("*", "Stereotype");
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Element stereotypeRefE = (Element) nodes.item(i);
			String stereotypeId = getReferenceId(stereotypeRefE);
			Element stereotypeTypeE = acquireElement(stereotypeId, "Stereotype", document);
			if (stereotypeTypeE == null) {
				String msg = "cannot find GM stereotype for xmi id [" + stereotypeId + "] of attribute [" + name + "("
						+ classE.getAttribute("xmi.id") + ")]";
				log.error(msg, null);
				throw new CodecException(msg);
			}
			String stereotypeName = stereotypeTypeE.getAttribute("name");
			if (stereotypeName.equalsIgnoreCase(Tokens.TOKEN_STEREOTYPE_ROOT_TYPE)) {
				xmiRegistry.addRoottypeByDocument(document, gmEntityType);
			}		
		}
		// gmEntityType.setIsPlain(isPlain);

		// a) Root type

		/*
		 * Element stereotypeRefE = DomUtils.getElementByPath( classE,
		 * ".*ModelElement.stereotype/.*Stereotype", false); if (stereotypeRefE != null)
		 * { String stereotypeId = getReferenceId(stereotypeRefE); Element
		 * stereotypeTypeE = acquireElement(stereotypeId, "Stereotype", document); if
		 * (stereotypeTypeE == null) { String msg =
		 * "cannot find GM stereotype for xmi id [" + stereotypeId + "] of attribute ["
		 * + name + "(" + classE.getAttribute("xmi.id") + ")]"; log.error(msg, null);
		 * throw new CodecException(msg); } String stereotypeName =
		 * stereotypeTypeE.getAttribute("name"); if
		 * (stereotypeName.equalsIgnoreCase(Tokens.TOKEN_STEREOTYPE_ROOT_TYPE)) {
		 * xmiRegistry.addRoottypeByDocument(document, gmEntityType); } }
		 * 
		 * // b) interface/class marker
		 */
		//
		xmiRegistry.addTypeToDocumentMap(gmEntityType, document);

		// tag references are added to the class
		Map<String, String> tagMap = acquireTagReferences(classE, registryEntry);
		String declaringModelName = tagMap.get(Tokens.TOKEN_TAG_ARTIFACTBINDING);
		if (declaringModelName != null) {
			gmEntityType.setDeclaringModel(xmiRegistry.getDeclaringModelForArtifactBinding(declaringModelName));
		}
		String globalId = tagMap.get( Tokens.TOKEN_TAG_GLOBALID);
		if (globalId != null) {
			gmEntityType.setGlobalId(globalId);
		}
		// deprecated
		String deprecationValue = tagMap.get( Tokens.TOKEN_TAG_DEPRECATED);
		if (deprecationValue != null && deprecationValue.equalsIgnoreCase("true")) {
			Deprecated dep = Deprecated.T.create();
			gmEntityType.getMetaData().add( dep);
		}
		// description 
		String descriptionValue = tagMap.get( Tokens.TOKEN_TAG_DOCUMENTATION);
		if (descriptionValue != null) {
			Description dep = Description.T.create();
			LocalizedString ls = LocalizedString.create(descriptionValue);
			dep.setDescription( ls);
			gmEntityType.getMetaData().add( dep);
		}
		return gmEntityType;
	}

	/**
	 * @param enumerationE
	 * @param document
	 * @return
	 * @throws CodecException
	 */
	public GmType acquireEnumeration(Element enumerationE, Document document) throws CodecException {
		String xmiId = enumerationE.getAttribute("xmi.id");
		String name = enumerationE.getAttribute("name");

		// check if already processed
		GmType gmType = xmiRegistry.getTypeById(xmiId);
		if (gmType != null) {
			return gmType;
		}

		// check if internal simple type
		gmType = xmiRegistry.getSimpleTypeByName(name);
		if (gmType != null) {
			xmiRegistry.addSimpleTypeById(name, xmiId, gmType, enumerationE, document);
			log.info("Adding simple type [" + name + "] for id [" + xmiId + "]");
			return gmType;
		}

		// create type
		String packageName = packageExpert.getPackageNameOfClass(enumerationE);

		GmEnumType gmEnumType = GmEnumType.T.create();
		if ((packageName != null) && (packageName.length() > 0))
			gmEnumType.setTypeSignature(packageName + "." + name);
		else
			gmEnumType.setTypeSignature(name);

		// store type
		RegistryEntry registryEntry = xmiRegistry.addEnumTypeById(xmiId, gmEnumType, enumerationE, document);
		log.info("Adding enum type [" + name + "] for id [" + xmiId + "]");

		// values..

		List<GmEnumConstant> constants = new ArrayList<GmEnumConstant>();
		Element classifierE = DomUtils.getElementByPath(enumerationE, ".*Enumeration.literal", false);
		if (classifierE != null) {
			FilteringElementIterator iterator = new FilteringElementIterator(classifierE, new RegExpTagNameFilter(".*EnumerationLiteral"));
			while (iterator.hasNext()) {

				Element literalE = iterator.next();
				GmEnumConstant gmConstant = GmEnumConstant.T.create();
				RegistryEntry constantEntry = xmiRegistry.addEnumConstantById( xmiId, gmConstant, literalE, document);

				String value = literalE.getAttribute("name");
				// add new style enum constant
				// back link
				gmConstant.setDeclaringType(gmEnumType);
				gmConstant.setName(value);
				constants.add(gmConstant);
				
				// grab tag references of the GmEnumConstant here..  
				Map<String, String> tagMap = acquireTagReferences(literalE, constantEntry);
				// deprecated
				String deprecationValue = tagMap.get( Tokens.TOKEN_TAG_DEPRECATED);
				if (deprecationValue != null && deprecationValue.equalsIgnoreCase("true")) {
					Deprecated dep = Deprecated.T.create();
					gmConstant.getMetaData().add( dep);
				}
				// description 
				String descriptionValue = tagMap.get( Tokens.TOKEN_TAG_DOCUMENTATION);
				if (descriptionValue != null) {
					Description dep = Description.T.create();
					LocalizedString ls = LocalizedString.create(descriptionValue);
					dep.setDescription( ls);
					gmConstant.getMetaData().add( dep);
				}
			}
		}
		gmEnumType.setConstants(constants);

		Map<String, String> tagMap = acquireTagReferences(enumerationE, registryEntry);
		String declaringModelName = tagMap.get(Tokens.TOKEN_TAG_ARTIFACTBINDING);
		if (declaringModelName != null) {
			gmEnumType.setDeclaringModel(xmiRegistry.getDeclaringModelForArtifactBinding(declaringModelName));
		}
		
		String globalId = tagMap.get( Tokens.TOKEN_TAG_GLOBALID);
		if (globalId != null) {
			gmEnumType.setGlobalId(globalId);
		}
		// deprecated
		String deprecationValue = tagMap.get( Tokens.TOKEN_TAG_DEPRECATED);
		if (deprecationValue != null && deprecationValue.equalsIgnoreCase("true")) {
			Deprecated dep = Deprecated.T.create();
			gmEnumType.getMetaData().add( dep);
		}
		// description 
		String descriptionValue = tagMap.get( Tokens.TOKEN_TAG_DOCUMENTATION);
		if (descriptionValue != null) {
			Description dep = Description.T.create();
			LocalizedString ls = LocalizedString.create(descriptionValue);
			dep.setDescription( ls);
			gmEnumType.getMetaData().add( dep);
		}
		

		NodeList nodes = enumerationE.getElementsByTagNameNS("*", "Stereotype");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element stereotypeRefE = (Element) nodes.item(i);
			String stereotypeId = getReferenceId(stereotypeRefE);
			Element stereotypeTypeE = acquireElement(stereotypeId, "Stereotype", document);
			if (stereotypeTypeE == null) {
				String msg = "cannot find GM stereotype for xmi id [" + stereotypeId + "] of attribute [" + name + "("
						+ enumerationE.getAttribute("xmi.id") + ")]";
				log.error(msg, null);
				throw new CodecException(msg);
			}
		}

		xmiRegistry.addTypeToDocumentMap(gmEnumType, document);
		return gmEnumType;
	}

	/**
	 * @param xmiId
	 * @param document
	 * @return
	 * @throws CodecException
	 */
	private GmType acquireClass(String xmiId, Document document) throws CodecException {
		// GmType gmType = xmiIdToEntityTypeMap.get(xmiId);
		GmType gmType = checkIfTypeIsAlreadyAcquired(xmiId);
		if (gmType != null)
			return gmType;

		Element element = acquireElement(xmiId, "Class", document);

		if (element != null)
			return acquireClass(element, document);
		return null;
	}

	/**
	 * @param xmiId
	 * @param document
	 * @return
	 * @throws CodecException
	 */
	private GmType acquireEnum(String xmiId, Document document) throws CodecException {
		GmType gmType = xmiRegistry.getTypeById(xmiId);
		if (gmType != null)
			return gmType;

		Element element = acquireElement(xmiId, "Enumeration", document);

		if (element != null)
			return acquireClass(element, document);
		return null;
	}
	
	private GmType acquireClassOrEnum( String xmiId, Document document) {
		GmType gmType = xmiRegistry.getTypeById(xmiId);
		if (gmType != null)
			return gmType;
		Element element = acquireElement(xmiId, "Class", document);
		if (element != null) {
			return acquireClass(element, document);
		}
		else {
			element = acquireElement(xmiId, "Enumeration", document);
			if (element != null) {
				return acquireEnum(xmiId, document);
			}
			throw new IllegalStateException("document doesn't contain neither a Class nor an Enumeration with the id [" + xmiId + "]");			
		}
	}

	/**
	 * extracts an element either from the local document or from a cross reference
	 * document
	 * 
	 * @param xmiRefId      - the idref or href of the element
	 * @param tag           - the tag namne
	 * @param localDocument - the local document (may be switched for child
	 *                      processes)
	 * @return
	 * @throws CodecException
	 */
	public Element acquireElement(String xmiRefId, String tag, Document localDocument) throws CodecException {

		String id = xmiRefId;
		Document scanDocument = localDocument;

		// check if cross reference
		if (id.contains("#")) {
			String[] parts = id.split("#");
			String reference = parts[0];
			xmiRefId = parts[1];
			String referenceFileName = reference.substring(reference.lastIndexOf("/") + 1);
			Document suspect = xmiRegistry.getDocumentByFilename(referenceFileName);
			if (suspect == null) {
				if (referenceDirectory == null) {
					String msg = "cannot load reference file [" + referenceFileName
							+ "] as not reference directory has been specified";
					log.error(msg, null);
					throw new CodecException(msg);
				}
				try {
					File referenceFile = new File(referenceDirectory, referenceFileName);
					suspect = DomParser.load().from(referenceFile);
					xmiRegistry.addDocumentByFilename(referenceFileName, suspect);

					GmMetaModel importedMetaModel = codec.decode(suspect);
					xmiRegistry.addMetaModelByFilename(referenceFileName, importedMetaModel);
				} catch (DomParserException e) {
					String msg = "cannot load reference file [" + referenceFileName + "] as " + e;
					log.error(msg, e);
					throw new CodecException(msg, e);
				}
			}
			scanDocument = suspect;

		}

		// scan document for element by id
		NodeList nodes = scanDocument.getElementsByTagNameNS("*", tag);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element node = (Element) nodes.item(i);
			String suspectId = node.getAttribute("xmi.id");
			if (suspectId.equalsIgnoreCase(xmiRefId)) {
				return node;
			}
		}

		return null;
	}

	/**
	 * a local reference is stored as an attribute "xmi.idref", a cross reference
	 * however is stored as "href"
	 * 
	 * @param element - element to extract the id from
	 * @return
	 */
	private String getReferenceId(Element element) {
		String refId = element.getAttribute("xmi.idref");
		if (refId.length() == 0)
			return element.getAttribute("href");
		return refId;
	}

	/**
	 * acquire an attribute
	 * 
	 * @param attributeE
	 * @param entityType
	 * @param scanParentE
	 * @throws CodecException
	 */
	private void acquireAttribute(Element attributeE, GmEntityType entityType, Document document) throws CodecException {
		String name = attributeE.getAttribute("name");
		Element typeRefE = DomUtils.getElementByPath(attributeE, ".*StructuralFeature.type/.*Class", false);

		boolean isEnum = false;
		if (typeRefE == null) {
			typeRefE = DomUtils.getElementByPath(attributeE, ".*StructuralFeature.type/.*Enumeration", false);
			if (typeRefE == null) {
				String msg = "cannot find type element for attribute [" + name + "] of type ["
						+ entityType.getTypeSignature() + "]";
				log.error(msg, null);
				throw new CodecException(msg);
			}
			isEnum = true;
		}
		String xmiId = getReferenceId(typeRefE);
		GmType gmType = acquireClassOrEnum(xmiId, document);
		if (gmType == null) {
			String msg = "cannot find type for xmi id [" + xmiId + "] of attribute [" + name + "("
					+ attributeE.getAttribute("xmi.id") + ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}

		//
		// check stereotypes : id property & nullable property
		//
		boolean isIdProperty = false;
		boolean isNullableProperty = true;
		Element stereotypeParentE = DomUtils.getElementByPath(attributeE, ".*ModelElement.stereotype", false);
		if (stereotypeParentE != null) {
			Iterator<Element> iterator = DomUtils.getElementIterator(stereotypeParentE, ".*Stereotype");
			while (iterator.hasNext()) {
				Element stereotypeRefE = iterator.next();
				String stereotypeId = getReferenceId(stereotypeRefE);
				Element stereotypeTypeE = acquireElement(stereotypeId, "Stereotype", document);
				if (stereotypeTypeE == null) {
					String msg = "cannot find GM stereotype for xmi id [" + stereotypeId + "] of attribute [" + name
							+ "(" + attributeE.getAttribute("xmi.id") + ")]";
					log.error(msg, null);
					throw new CodecException(msg);
				}
				String stereotypeName = stereotypeTypeE.getAttribute("name");
				if (stereotypeName.equalsIgnoreCase(Tokens.TOKEN_STEREOTYPE_IDPROPERTY)) {
					isIdProperty = true;
				} else if (stereotypeName.equalsIgnoreCase(Tokens.TOKEN_STEREOTYPE_NONNULLABLE)) {
					isNullableProperty = false;
				}
			}
		}
		// id property : may not be non-nullable
		if (isIdProperty) {
			isNullableProperty = true;
		}

		/*
		 * boolean isIdProperty = false; Element stereotypeRefE =
		 * DomUtils.getElementByPath(attributeE,
		 * ".*ModelElement.stereotype/.*Stereotype", false); if (stereotypeRefE != null)
		 * { String stereotypeId = getReferenceId(stereotypeRefE); Element
		 * stereotypeTypeE = acquireElement(stereotypeId, "Stereotype", document); if
		 * (stereotypeTypeE == null) { String msg =
		 * "cannot find GM stereotype for xmi id [" + stereotypeId + "] of attribute ["
		 * + name + "(" + attributeE.getAttribute("xmi.id") + ")]"; log.error(msg,
		 * null); throw new CodecException(msg); } String stereotypeName =
		 * stereotypeTypeE.getAttribute("name"); if
		 * (stereotypeName.equalsIgnoreCase(Tokens.TOKEN_STEREOTYPE_IDPROPERTY)) {
		 * isIdProperty = true; } }
		 */
		GmProperty property = GmProperty.T.create();
		property.setDeclaringType(entityType);
		property.setName(name);
		property.setType(gmType);
		property.setNullable(isNullableProperty);
		List<GmProperty> properties = entityType.getProperties();
		if (properties == null) {
			properties = new ArrayList<GmProperty>(1);
			entityType.setProperties(properties);
		}
		properties.add(property);
		/*
		Map<String, String> tagMap = acquireTagReferences( property, registryEntry);
		String globalId = tagMap.get( Tokens.TOKEN_TAG_GLOBALID);
		if (globalId != null) {
			gmEntityType.setGlobalId(globalId);
		}
		// deprecated
		String deprecationValue = tagMap.get( Tokens.TOKEN_TAG_DEPRECATED);
		if (deprecationValue != null && deprecationValue.equalsIgnoreCase("true")) {
			Deprecated dep = Deprecated.T.create();
			gmEntityType.getMetaData().add( dep);
		}
		// description 
		String descriptionValue = tagMap.get( Tokens.TOKEN_TAG_DOCUMENTATION);
		if (descriptionValue != null) {
			Description dep = Description.T.create();
			LocalizedString ls = LocalizedString.create(descriptionValue);
			dep.setDescription( ls);
			gmEntityType.getMetaData().add( dep);
		}
		*/

		// add to registry
		RegistryEntry registryEntry = xmiRegistry.addPropertyById(attributeE.getAttribute("xmi.id"), property, attributeE);
		// tag references are added to the class
		Map<String, String> tagReferences = acquireTagReferences(attributeE, registryEntry);
		String globalId = tagReferences.get( Tokens.TOKEN_TAG_GLOBALID);
		property.setGlobalId(globalId);
		
		// deprecated
		String deprecationValue = tagReferences.get( Tokens.TOKEN_TAG_DEPRECATED);
		if (deprecationValue != null && deprecationValue.equalsIgnoreCase("true")) {
			Deprecated dep = Deprecated.T.create();
			property.getMetaData().add( dep);
		}
		// description 
		String descriptionValue = tagReferences.get( Tokens.TOKEN_TAG_DOCUMENTATION);
		if (descriptionValue != null) {
			Description dep = Description.T.create();
			LocalizedString ls = LocalizedString.create(descriptionValue);
			dep.setDescription( ls);
			property.getMetaData().add( dep);
		}
		
		

		log.info("acquired attribute [" + name + "] of property type [" + gmType + "] for entity ["
				+ entityType.getTypeSignature() + "] ");
	}

	/**
	 * @param associationE
	 * @param scanParentE
	 * @throws CodecException
	 */
	public void acquireAssociation(Element associationE, Document document) throws CodecException {
		String name = associationE.getAttribute("name");

		// two ends..
		Element associationConnectionE = DomUtils.getElementByPath(associationE, ".*Association.connection", false);
		if (associationConnectionE == null) {
			String msg = "cannot find connection element of association [" + name + "("
					+ associationE.getAttribute("xmi.id") + ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}
		FilteringElementIterator iterator = new FilteringElementIterator(associationConnectionE,
				new RegExpTagNameFilter(".*AssociationEnd"));
		if (iterator.hasNext() == false) {
			String msg = "no associationEnd found for association [" + name + "(" + associationE.getAttribute("xmi.id")
					+ ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}
		// From Association : first associationEnd
		Element associationFromE = iterator.next();
		Element associationEndFromClassE = DomUtils.getElementByPath(associationFromE,
				".*AssociationEnd.participant/.*Class", false);
		if (associationEndFromClassE == null) {
			String msg = "no association participient found for first end of association [" + name + "("
					+ associationE.getAttribute("xmi.id") + ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}

		boolean fromIsOrdered = associationFromE.getAttribute("ordering").equals("ordered");

		String xmiIdFromClass = getReferenceId(associationEndFromClassE);
		GmType typeFrom = acquireClass(xmiIdFromClass, document);
		if (typeFrom == null) {
			String msg = "cannot find type for xmi id [" + xmiIdFromClass + "] of attribute [" + name + "("
					+ associationEndFromClassE.getAttribute("xmi.idref") + ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}

		// To Association : second association end
		Element associationToE = iterator.next();
		boolean toIsOrdered = associationToE.getAttribute("ordering").equalsIgnoreCase("ordered");
		Element associationEndToClassE = DomUtils.getElementByPath(associationToE, ".*AssociationEnd.participant/.*Class", false);
	
		boolean isEnum = false;
		if (associationEndToClassE == null) {
			associationEndToClassE = DomUtils.getElementByPath(associationToE, ".*AssociationEnd.participant/.*Enumeration", false);
			if (associationEndToClassE == null) {
				String msg = "no association participient found for second end of association [" + name + "(" + associationE.getAttribute("xmi.id") + ")]";
				log.error(msg, null);
				throw new CodecException(msg);
			}
			isEnum = true;

		}
		String xmiIdToClass = getReferenceId(associationEndToClassE);
		
		

		GmType typeTo = acquireClassOrEnum(xmiIdToClass, document);
		if (typeTo == null) {
			String msg = "cannot find type for xmi id [" + xmiIdToClass + "] of attribute [" + name + "(" + associationEndFromClassE.getAttribute("xmi.idref") + ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}

		Element multiplicityRangeE = DomUtils.getElementByPath(associationToE, ".*AssociationEnd.multiplicity/.*Multiplicity/.*Multiplicity.range/.*MultiplicityRange", false);
		if (multiplicityRangeE != null) {

			// String lower = multiplicityRangeE.getAttribute( "lower");
			String upper = multiplicityRangeE.getAttribute("upper");
			int upperValue = Integer.parseInt(upper);
			if (upperValue < 0) {
				// TODO: acquire for types.. reuse for already used element types
				if (fromIsOrdered || toIsOrdered) {
					// if ordered -> GmListType
					GmListType gmListType = GmListType.T.create();
					gmListType.setElementType(typeTo);
					gmListType.setTypeSignature("list<" + typeTo.getTypeSignature() + ">");
					typeTo = gmListType;

					//
				} else {
					// else unordered -> GmSetType
					GmSetType gmSetType = GmSetType.T.create();
					gmSetType.setElementType(typeTo);
					gmSetType.setTypeSignature("set<" + typeTo.getTypeSignature() + ">");
					typeTo = gmSetType;

					//
				}
			}
		}

		GmEntityType entityType = (GmEntityType) typeFrom;
		GmProperty gmProperty = GmProperty.T.create();
		gmProperty.setName(name);
		gmProperty.setDeclaringType(entityType);
		gmProperty.setType(typeTo);
		
				

		List<GmProperty> properties = entityType.getProperties();
		if (properties == null) {
			properties = new ArrayList<GmProperty>(1);
			entityType.setProperties(properties);
		}
		properties.add(gmProperty);
		// add to registry
		RegistryEntry registryEntry = xmiRegistry.addPropertyById(associationE.getAttribute("xmi.id"), gmProperty, associationE);

		// tag references are added to the class
		Map<String, String> tagReferences = acquireTagReferences(associationE, registryEntry);
		String globalId = tagReferences.get( Tokens.TOKEN_TAG_GLOBALID);
		gmProperty.setGlobalId(globalId);
		log.info("acquired association [" + name + "] of property type [" + typeTo + "] (" + globalId + ") for entity [" + entityType.getTypeSignature() + "] ");
		
		// deprecated
		String deprecationValue = tagReferences.get( Tokens.TOKEN_TAG_DEPRECATED);
		if (deprecationValue != null && deprecationValue.equalsIgnoreCase("true")) {
			Deprecated dep = Deprecated.T.create();
			gmProperty.getMetaData().add( dep);
		}
		// description 
		String descriptionValue = tagReferences.get( Tokens.TOKEN_TAG_DOCUMENTATION);
		if (descriptionValue != null) {
			Description dep = Description.T.create();
			LocalizedString ls = LocalizedString.create(descriptionValue);
			dep.setDescription( ls);
			gmProperty.getMetaData().add( dep);
		}

	}

	/**
	 * @param associationClassE
	 * @param scanParentE
	 * @throws CodecException
	 */
	public void acquireAssociationClass(Element associationClassE, Document document) throws CodecException {
		String name = associationClassE.getAttribute("name");

		// two ends..
		Element associationConnectionE = DomUtils.getElementByPath(associationClassE, ".*Association.connection",
				false);
		if (associationConnectionE == null) {
			String msg = "cannot find connection element of association [" + name + "("
					+ associationClassE.getAttribute("xmi.id") + ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}
		FilteringElementIterator iterator = new FilteringElementIterator(associationConnectionE,
				new RegExpTagNameFilter(".*AssociationEnd"));
		if (iterator.hasNext() == false) {
			String msg = "no associationEnd found for association [" + name + "("
					+ associationClassE.getAttribute("xmi.id") + ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}
		// From Association : first associationEnd
		Element associationFromE = iterator.next();
		Element associationEndFromClassE = DomUtils.getElementByPath(associationFromE,
				".*AssociationEnd.participant/.*Class", false);
		if (associationEndFromClassE == null) {
			String msg = "no association participient found for first end of association [" + name + "("
					+ associationClassE.getAttribute("xmi.id") + ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}

		String xmiIdFromClass = getReferenceId(associationEndFromClassE);
		GmType typeFrom = acquireClass(xmiIdFromClass, document);
		if (typeFrom == null) {
			String msg = "cannot find type for xmi id [" + xmiIdFromClass + "] of attribute [" + name + "("
					+ associationEndFromClassE.getAttribute("xmi.idref") + ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}

		// To Association : second association end
		Element associationToE = iterator.next();
		Element associationEndToClassE = DomUtils.getElementByPath(associationToE,
				".*AssociationEnd.participant/.*Class", false);
		boolean isEnum = false;
		if (associationEndToClassE == null) {
			associationEndToClassE = DomUtils.getElementByPath(associationToE,
					".*AssociationEnd.participant/.*Enumeration", false);
			if (associationEndToClassE == null) {
				String msg = "no association participient found for second end of association [" + name + "("
						+ associationClassE.getAttribute("xmi.id") + ")]";
				log.error(msg, null);
				throw new CodecException(msg);
			}
			isEnum = true;

		}
		String xmiIdToClass = getReferenceId(associationEndToClassE);

		GmType typeTo = acquireClassOrEnum(xmiIdToClass, document);
		if (typeTo == null) {
			String msg = "cannot find type for xmi id [" + xmiIdToClass + "] of attribute [" + name + "("
					+ associationEndFromClassE.getAttribute("xmi.idref") + ")]";
			log.error(msg, null);
			throw new CodecException(msg);
		}

		GmMapType mapType = GmMapType.T.create();
		Element keyTypeE = DomUtils.getElementByPath(associationClassE,
				".*Classifier\\.feature/.*Attribute/.*StructuralFeature\\.type/.*Class", false);
		if (keyTypeE == null) {
			keyTypeE = DomUtils.getElementByPath(associationClassE,
					".*Classifier\\.feature/.*Attribute/.*StructuralFeature\\.type/.*Enumeration", false);
		}
		if (keyTypeE == null) {
			String msg = "cannot find association type in association class";
			log.error(msg);
			throw new CodecException(msg);
		}
		GmType keyType = xmiRegistry.getTypeById(keyTypeE.getAttribute("xmi.idref"));
		mapType.setKeyType(keyType);
		mapType.setValueType(typeTo);
		mapType.setTypeSignature("map<" + keyType.getTypeSignature() + "," + typeTo.getTypeSignature() + ">");
		typeTo = mapType;

		/*
		 * Element multiplicityRangeE = DomUtils.getElementByPath(associationToE,
		 * ".*AssociationEnd.multiplicity/.*Multiplicity/.*Multiplicity.range/.*MultiplicityRange",
		 * false); if (multiplicityRangeE != null) {
		 * 
		 * // String lower = multiplicityRangeE.getAttribute( "lower"); String upper =
		 * multiplicityRangeE.getAttribute("upper"); int upperValue =
		 * Integer.parseInt(upper); if (upperValue < 0) { // TODO: acquire for types..
		 * reuse for already used element types if (ordered.equalsIgnoreCase("ordered"))
		 * { // if ordered -> GmListType GmListType gmListType = GmListType.T.create();
		 * gmListType.setElementType(typeTo); typeTo = gmListType; } else { // else
		 * unordered -> GmSetType GmSetType gmSetType = GmSetType.T.create();
		 * gmSetType.setElementType(typeTo); typeTo = gmSetType; } } }
		 */
		GmEntityType entityType = (GmEntityType) typeFrom;
		GmProperty gmProperty = GmProperty.T.create();
		gmProperty.setName(name);
		gmProperty.setDeclaringType(entityType);
		gmProperty.setType(typeTo);

		List<GmProperty> properties = entityType.getProperties();
		if (properties == null) {
			properties = new ArrayList<GmProperty>(1);
			entityType.setProperties(properties);
		}
		properties.add(gmProperty);
		// add to registry
		RegistryEntry registryEntry = xmiRegistry.addPropertyById(associationClassE.getAttribute("xmi.id"), gmProperty,
				associationClassE);

		// tag references are added to the class
		Map<String, String> tagReferences = acquireTagReferences(associationClassE, registryEntry);
		String globalId = tagReferences.get( Tokens.TOKEN_TAG_GLOBALID);
		gmProperty.setGlobalId(globalId);
		log.info("acquired association [" + name + "] of property type [" + typeTo + "] (" + globalId +") for entity [" + entityType.getTypeSignature() + "] ");

	}

	/**
	 * @param generalisationE
	 * @param scanParentE
	 * @throws CodecException
	 */
	public void acquireGeneralisation(Element generalisationE, Document document) throws CodecException {
		String name = generalisationE.getAttribute("name");
		log.info("Acquiring generalization [" + name + "]");
		// deriving class : sub type
		Element generalisationChildE = DomUtils.getElementByPath(generalisationE, ".*Generalization.child/.*Class",
				false);
		if (generalisationChildE == null) {
			String msg = "no generalisation child found for generalisation [" + generalisationE.getAttribute("xmi.id");
			log.error(msg, null);
			throw new CodecException(msg);
		}
		String subId = getReferenceId(generalisationChildE);

		GmType subType = acquireClass(subId, document);
		if (subType == null) {
			String msg = "cannot resolve sub type of generalisation";
			log.error(msg, null);
			throw new CodecException(msg);
		}
		// if it's THE GenericEntity type, we don't care about any derivation
		// (it's derived from object for completeness)
		if (subType.getTypeSignature().equalsIgnoreCase(Tokens.TOKEN_GE_TYPESIGNATURE))
			return;

		// derived class : super type
		Element generalisationParentE = DomUtils.getElementByPath(generalisationE, ".*Generalization.parent/.*Class",
				false);
		if (generalisationParentE == null) {
			String msg = "no generalisation parent found for generalisation [" + generalisationE.getAttribute("xmi.id");
			log.error(msg, null);
			throw new CodecException(msg);
		}
		String superId = getReferenceId(generalisationParentE);

		GmType superType = acquireClass(superId, document);
		if (superType == null) {
			String msg = "super type of generalisation [" + generalisationE.getAttribute("xmi.id") + "] with id ["
					+ subId + "] is not a GmEntityType";
			log.error(msg, null);
			throw new CodecException(msg);
		}

		// if it's a simple type, it has been injected by us for completeness
		if ((superType instanceof GmSimpleType) || (subType instanceof GmSimpleType)) {
			// create a registry entry for it anyhow
			xmiRegistry.addGeneralizationById(generalisationE.getAttribute("xmi.id"), superType, subType,
					generalisationE);
			return;
		}

		GmEntityType subEntityType = (GmEntityType) subType;
		GmEntityType superEntityType = (GmEntityType) superType;
		/*
		 * Set<GmEntityType> subs = superEntityType.getSubTypes(); if (subs == null) {
		 * subs = new HashSet<GmEntityType>(); superEntityType.setSubTypes(subs); } if
		 * (subs.contains( subEntityType) == false) { subs.add(subEntityType); } else {
		 * log.warn("subtype check: second generalization [" + name + "] found for type"
		 * + subEntityType.getTypeSignature() + "] as [" +
		 * superEntityType.getTypeSignature() + "]"); }
		 */
		List<GmEntityType> supers = subEntityType.getSuperTypes();
		if (supers == null) {
			supers = new ArrayList<GmEntityType>(1);
			subEntityType.setSuperTypes(supers);
		}
		if (supers.contains(superEntityType) == false) {
			supers.add(superEntityType);
		} else {
			log.warn("super type check: second generalization [" + name + "] found for type"
					+ subEntityType.getTypeSignature() + "] as [" + superEntityType.getTypeSignature() + "]");
		}
		// add to registry
		RegistryEntry registryEntry = xmiRegistry.addGeneralizationById(generalisationE.getAttribute("xmi.id"),
				superEntityType, subEntityType, generalisationE);

		// tag references are added to the class
		acquireTagReferences(generalisationE, registryEntry);
		log.info("acquired generalization [" + name + "] of type [" + subEntityType.getTypeSignature() + "] as ["
				+ superEntityType.getTypeSignature() + "]");
	}

	/**
	 * @param id
	 * @return
	 */
	private TagDefinition checkIfTagIsAlreadyAcquired(String id) {
		if (id.contains("#")) {
			String[] parts = id.split("#");
			String reference = parts[0];
			String xmiRefId = parts[1];
			log.info("Checking on tag definition of [" + reference + "], id [" + xmiRefId + "]");
			return xmiRegistry.getTagDefinitionById(xmiRefId);
		} else {
			return xmiRegistry.getTagDefinitionById(id);
		}
	}

	/**
	 * @param tagDefinitionE
	 * @param document
	 * @throws CodecException
	 */
	public void acquireTagDefinition(Element tagDefinitionE, Document document) throws CodecException {
		String xmiId = tagDefinitionE.getAttribute("xmi.id");
		String name = tagDefinitionE.getAttribute("name");

		TagDefinition tagDefinition = checkIfTagIsAlreadyAcquired(xmiId);
		if (tagDefinition != null)
			return;

		tagDefinition = new TagDefinition();
		tagDefinition.setXmiId(xmiId);
		tagDefinition.setName(name);
		RegistryEntry registryEntry = xmiRegistry.addTagDefinitionById(xmiId, tagDefinition, tagDefinitionE, document);

		acquireTagReference(tagDefinitionE, registryEntry, name);

	}

	public String acquireTagReference(Element parent, RegistryEntry parentEntry, String tagDefinitionName) {
		Iterator<Element> modelElementTaggedValueIterator = DomUtils.getElementIterator(parent, ".*ModelElement.taggedValue");
		while (modelElementTaggedValueIterator.hasNext()) {
			Element taggedValueParentE = modelElementTaggedValueIterator.next();
			if (taggedValueParentE == null)
				continue;
			Iterator<Element> iterator = DomUtils.getElementIterator(taggedValueParentE, ".*TaggedValue");
			while (iterator.hasNext()) {
				Element taggedValueE = iterator.next();
				String value = DomUtils.getElementValueByPath(taggedValueE, ".*TaggedValue.dataValue", false);
				Element typeE = DomUtils.getElementByPath(taggedValueE, ".*TaggedValue.type/.*TagDefinition", false);
				String refId = getReferenceId(typeE);
				TagDefinition tagDefinition = checkIfTagIsAlreadyAcquired(refId);
				if (!tagDefinition.getName().equals(tagDefinitionName))
					continue;
				TagReference tagReference = new TagReference();
				tagReference.setReferencedTagDefinition(tagDefinition);
				tagReference.setValue(value);
				xmiRegistry.addTagReferenceById(taggedValueE.getAttribute("xmi.id"), tagReference, taggedValueE, parent.getOwnerDocument(), parentEntry);
				return value;
			}
		}
		return null;
	}

	public Map<String, String> acquireTagReferences(Element parent, RegistryEntry parentEntry) {
		Map<String, String> result = new HashMap<>();
		Iterator<Element> modelElementTaggedValueIterator = DomUtils.getElementIterator(parent, ".*ModelElement.taggedValue");
		while (modelElementTaggedValueIterator.hasNext()) {
			Element taggedValueParentE = modelElementTaggedValueIterator.next();
			if (taggedValueParentE == null)
				continue;
			Iterator<Element> iterator = DomUtils.getElementIterator(taggedValueParentE, ".*TaggedValue");
			while (iterator.hasNext()) {
				Element taggedValueE = iterator.next();
				String value = DomUtils.getElementValueByPath(taggedValueE, ".*TaggedValue.dataValue", false);
				Element typeE = DomUtils.getElementByPath(taggedValueE, ".*TaggedValue.type/.*TagDefinition", false);
				String refId = getReferenceId(typeE);
				TagDefinition tagDefinition = checkIfTagIsAlreadyAcquired(refId);
				//
				if (tagDefinition == null) {
					log.warn("no tag definition found for id [" + refId + "]");
					continue;
				}
				TagReference tagReference = new TagReference();
				tagReference.setReferencedTagDefinition(tagDefinition);
				tagReference.setValue(value);
				String iDattribute = taggedValueE.getAttribute("xmi.id");
				xmiRegistry.addTagReferenceById(iDattribute, tagReference, taggedValueE, parent.getOwnerDocument(), parentEntry);
				result.put(tagDefinition.getName(), value);
			}
		}
		return result;
	}
	
	

}
