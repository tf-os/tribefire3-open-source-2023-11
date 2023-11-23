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
package com.braintribe.model.processing.xmi.converter;

import java.io.File;
import java.io.InputStream;
import java.security.ProviderException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.xmi.converter.coding.MetaModelDependencyHandler;
import com.braintribe.model.processing.xmi.converter.coding.XmiDecoder;
import com.braintribe.model.processing.xmi.converter.coding.XmiEncoder;
import com.braintribe.model.processing.xmi.converter.coding.differentiator.ModelDifferentiator;
import com.braintribe.model.processing.xmi.converter.coding.differentiator.ModelDifferentiatorContext;
import com.braintribe.model.processing.xmi.converter.experts.PackageExpert;
import com.braintribe.model.processing.xmi.converter.registry.IdGenerator;
import com.braintribe.model.processing.xmi.converter.registry.XmiRegistry;
import com.braintribe.model.processing.xmi.converter.registry.entries.ModelEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.RegistryEntry;
import com.braintribe.model.processing.xmi.converter.registry.entries.TagReferenceEntry;
import com.braintribe.model.processing.xmi.converter.tagdefinition.TagReference;
import com.braintribe.utils.xml.dom.DomUtils;
import com.braintribe.utils.xml.parser.DomParser;
import com.braintribe.utils.xml.parser.DomParserException;
import com.braintribe.web.velocity.renderer.VelocityTemplateRenderer;

/**
 * an XMI to Metamodel converter<br/>
 * <br/>
 * 
 * currently supports XMI version 1.2, as written by Argo UML.<br/>
 * <br/>
 * over layed properties:<br/>
 * encoding extracts all {@link GmProperty} that are not to be written to the
 * XMI. The list of skipped {@link GmProperty} are stored in a {@link List}, and
 * can be supplied to the decoding process, where they are reattached.
 * 
 * @author Pit
 * 
 */
public class XmiToMetaModelCodec implements Codec<GmMetaModel, Document> {

	private static Logger log = Logger.getLogger(XmiToMetaModelCodec.class);

	private File referenceDirectory;

	private VelocityTemplateRenderer renderer;
	private XmiRegistry xmiRegistry;
	private XmiDecoder xmiDecoder;
	private XmiEncoder xmiEncoder;
	private PackageExpert packageExpert;
	private IdGenerator idGenerator;
	private ModelDifferentiatorContext differentiatorContext = new ModelDifferentiatorContext();

	private List<GmProperty> overlayedProperties;

	private Supplier<String> baseDocumentProvider;
	private InputStream recycleInputStream;

	
	@Configurable
	public void setReferenceDirectory(File referenceDirectory) {
		this.referenceDirectory = referenceDirectory;
	}
	
	@Configurable @Required
	public void setRenderer(VelocityTemplateRenderer renderer) {
		this.renderer = renderer;
	}

	@Configurable
	public void setRecycleInputStream(InputStream recycleInputStream) {
		this.recycleInputStream = recycleInputStream;
	}

	@Override
	public Class<GmMetaModel> getValueClass() {
		return GmMetaModel.class;
	}
	
	@Configurable
	public void setBaseDocumentProvider(Supplier<String> baseProvider) {
		this.baseDocumentProvider = baseProvider;
	}

	@Configurable
	public void setOverlayedProperties(List<GmProperty> overlayedProperties) {
		this.overlayedProperties = overlayedProperties;
	}

	public List<GmProperty> getOverlayedProperties() {
		return overlayedProperties;
	}
	
	/**
	 * constructor
	 */
	public XmiToMetaModelCodec() {
		// setup internal types
		xmiRegistry = new XmiRegistry();
	}

	@Override
	public Document encode(GmMetaModel metaModel) throws CodecException {

		//
		// prep the generator
		if (idGenerator == null) {
			idGenerator = new IdGenerator();
			idGenerator.setXmiRegistry(xmiRegistry);
		}

		//
		// prep the encoder
		if (xmiEncoder == null) {
			xmiEncoder = new XmiEncoder();
			xmiEncoder.setXmiRegistry(xmiRegistry);
			xmiEncoder.setIdGenerator(idGenerator);
			xmiEncoder.setRenderer(renderer);
		}

		try {
			//
			// load base document
			//
			if (baseDocumentProvider != null) {
				String base = baseDocumentProvider.get();
				Document baseDocument = DomParser.load().from(base);
				decode(baseDocument);

			}

			//
			// load old version if present to prime the registry
			if (recycleInputStream != null) {
				Document modelDocument = DomParser.load().setNamespaceAware().from(recycleInputStream);
				GmMetaModel oldModel = decode(modelDocument);
				
				// check differences between old and new model
				ModelDifferentiator.differentiate(differentiatorContext, metaModel, oldModel);			

				xmiEncoder.setDocumentPrefixToUse(xmiRegistry.getDocumentPrefixByDocument(modelDocument));
				xmiRegistry.addDocumentByFilename("recycle", modelDocument);
				xmiRegistry.addMetaModelByFilename("recycle", oldModel);
				xmiRegistry.setReferenceDocument(modelDocument);

				// set the save range for new ids..
				long maxId = 0;
				for (String fullId : xmiRegistry.getStoredIds()) {
					int p = fullId.lastIndexOf(":");
					String id = fullId.substring(p + 1);
					long value = Long.parseLong(id, 16);
					if (value > maxId)
						maxId = value;
				}
				idGenerator.setCurrentDocument(modelDocument);
				idGenerator.prime(maxId);

				// reset the packageName data storages
				xmiEncoder.resetPackageNameCache();
			} else {
				idGenerator.prime(0L);
			}

			//
			// encode model
			xmiEncoder.setDifferentiatorContext(differentiatorContext);
			
			Document document = xmiEncoder.encode(metaModel);
			
			// grab the overlayed properties as determined by the encoder
			overlayedProperties = xmiEncoder.getOverlayedProperties();
			return document;
		} catch (DomParserException e) {
			String msg = "Cannot load model document as " + e;
			log.error(msg, e);
			throw new CodecException(msg, e);
		} catch (ProviderException e) {
			String msg = "Cannot load base document as " + e;
			log.error(msg, e);
			throw new CodecException(msg, e);
		}

	}

	@Override
	public GmMetaModel decode(Document document) throws CodecException {

		if (packageExpert == null) {
			packageExpert = new PackageExpert();
			packageExpert.setXmiRegistry(xmiRegistry);
		}

		if (xmiDecoder == null) {
			xmiDecoder = new XmiDecoder();
			xmiDecoder.setCodec(this);
			xmiDecoder.setReferenceDirectory(referenceDirectory);
			xmiDecoder.setXmiRegistry(xmiRegistry);
			xmiDecoder.setPackageExpert(packageExpert);
		}

		String version = document.getDocumentElement().getAttribute("xmi.version");
		if (version.equalsIgnoreCase(Tokens.TOKEN_SUPPORTEDVERSION) == false) {
			String msg = "Version of supplied xmi [" + version + "] is not the expected version [" + Tokens.TOKEN_SUPPORTEDVERSION + "]";
			log.warn(msg);
		}

		Element modelE = DomUtils.getElementByPath(document.getDocumentElement(), Tokens.TOKEN_PATH_MODEL, false);
		if (modelE == null) {
			String msg = "Cannot find entry point [" + Tokens.TOKEN_PATH_MODEL + "] for model data";
			log.error(msg, null);
			throw new CodecException(msg);
		}

		//
		// extract the document's prefix
		//
		String modelId = modelE.getAttribute("xmi.id");
		String documentId = modelId.substring(0, modelId.indexOf(":"));
		xmiRegistry.addDocumentIdPrefix(document, documentId);

		//
		// READ THE XMI FILE
		//

		//
		// tag definitions
		
		NodeList nodes = document.getElementsByTagNameNS("*", "TagDefinition");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element suspectE = (Element) nodes.item(i);
		
			if (!suspectE.getTagName().equals("UML:TagDefinition")) { 
				continue;
			}
			String ref = suspectE.getAttribute("xmi.id");
			if ((ref == null) || (ref.length() == 0))
				continue;
			xmiDecoder.acquireTagDefinition(suspectE, document);
		}
		
		// model itself
		Map<String, String> map = xmiDecoder.acquireModel(modelE, document);
		String dependencyData = map.get(Tokens.TOKEN_TAG_MODEL_DEPENDENCIES);
		
		Map<String, GmMetaModel> modelAndDependencies = MetaModelDependencyHandler.decodeModelAndDependencies(dependencyData);
		xmiRegistry.primeArtifactToBindingMap(modelAndDependencies, document);
		
		String mainModelName = map.get( Tokens.TOKEN_TAG_ARTIFACTBINDING);		
		differentiatorContext.setAccumulatedOldDifferentiations( map.get( Tokens.TOKEN_TAG_DOCUMENTATION_SEE));	
		
		

		//
		// classes
		nodes = document.getElementsByTagNameNS("*", "Class");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element suspectE = (Element) nodes.item(i);
			String ref = suspectE.getAttribute("xmi.id");
			if ((ref == null) || (ref.length() == 0))
				continue;
			xmiDecoder.acquireClass(suspectE, document);
		}

		//
		// enumerations
		nodes = document.getElementsByTagNameNS("*", "Enumeration");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element suspectE = (Element) nodes.item(i);
			String ref = suspectE.getAttribute("xmi.id");
			if ((ref == null) || (ref.length() == 0))
				continue;
			xmiDecoder.acquireEnumeration(suspectE, document);
		}

		//
		// associations
		nodes = document.getElementsByTagNameNS("*", "Association");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element suspectE = (Element) nodes.item(i);
			String ref = suspectE.getAttribute("xmi.id");
			if ((ref == null) || (ref.length() == 0))
				continue;
			xmiDecoder.acquireAssociation(suspectE, document);
		}
		//
		// associationClass
		nodes = document.getElementsByTagNameNS("*", "AssociationClass");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element suspectE = (Element) nodes.item(i);
			String ref = suspectE.getAttribute("xmi.id");
			if ((ref == null) || (ref.length() == 0))
				continue;
			xmiDecoder.acquireAssociationClass(suspectE, document);
		}

		//
		// stereotypes : only add the definitions to the registry
		nodes = document.getElementsByTagNameNS("*", "Stereotype");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element suspectE = (Element) nodes.item(i);
			String ref = suspectE.getAttribute("xmi.id");
			if ((ref == null) || (ref.length() == 0))
				continue;
			String name = suspectE.getAttribute("name");
			String target = DomUtils.getElementValueByPath(suspectE, ".*Stereotype.baseClass", false);
			xmiRegistry.addStereotypeById(ref, name, target, suspectE, document);
		}

		// generalizations..
		nodes = document.getElementsByTagNameNS("*", "Generalization");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element generalizationE = (Element) nodes.item(i);

			String refId = generalizationE.getAttribute("xmi.idref");

			// check if it's a reference only..
			if (refId.length() > 0) {
				generalizationE = xmiDecoder.acquireElement(refId, "Generalization", document);
				if (generalizationE == null) {
					String msg = "Generalization [" + refId + "] cannot be found ";
					log.error(msg, null);
					throw new CodecException(msg);
				}
			}

			xmiDecoder.acquireGeneralisation(generalizationE, document);
		}
		//
		// find name

		//
		// a) per model name - cheap and wrong
		String modelName = modelE.getAttribute("name");
		//
		// b) per tagDefinition "ArtifactBinding"
		Collection<RegistryEntry> entries = xmiRegistry.getMatchingEntries(TagReferenceEntry.class);
		for (RegistryEntry entry : entries) {
			TagReferenceEntry referenceEntry = (TagReferenceEntry) entry;
			RegistryEntry parentEntry = referenceEntry.getParentEntry();
			if (parentEntry instanceof ModelEntry == false)
				continue;

			if (parentEntry.getDocument() != document)
				continue;
			TagReference tagReference = referenceEntry.getTagReference();
			
			if (tagReference.getReferencedTagDefinition().getName().equalsIgnoreCase(Tokens.TOKEN_TAG_ARTIFACTBINDING)) {
				modelName = tagReference.getValue();
				break;
			}

		}

		xmiRegistry.addDocumentToArtifactBindingMap(document, modelName);
		
	

		//
		// WRITE THE METAMODEL
		//
		
		GmMetaModel mainModel = xmiRegistry.getDeclaringModelForArtifactBinding(mainModelName);
				
		
		// transfer found complex types to model.
		Set<GmEntityType> entityTypes = new HashSet<GmEntityType>();
		Set<GmEnumType> enumTypes = new HashSet<GmEnumType>();
		
		for (GmType gmType : xmiRegistry.getTypesPerDocument(document)) {

			// transfer entity types
			if (gmType instanceof GmEntityType) {
				GmEntityType gmEntityType = (GmEntityType) gmType;
				//				
				// attach overlayed properties here..
				attachOverlayedProperties(gmEntityType, overlayedProperties);
				
				GmMetaModel declaringModel = gmEntityType.getDeclaringModel();
				declaringModel.getTypes().add( gmEntityType);
												
				entityTypes.add(gmEntityType);				
			}
			
			// transfer enum types
			if (gmType instanceof GmEnumType) {
				GmEnumType gmEnumType = (GmEnumType) gmType;
				
				GmMetaModel declaringModel = gmEnumType.getDeclaringModel();
				declaringModel.getTypes().add( gmEnumType);
				enumTypes.add(gmEnumType);								
			}
		}

		

		log.info("acquired [" + entityTypes.size() + "] entity types and [" + enumTypes.size() + "] enum types for model [" + modelName + "]");
	
		return mainModel;
		
	
		
	}

	

	

	/**
	 * look up any overlayed properties of a GmEntityType and restore them <br/>
	 * the list has been generated during the encoding and survived the round trip,
	 * and now needs to be reattached.
	 * 
	 * @param entityType          - the {@link GmEntityType} that is the target
	 * @param overlayedProperties - the stored {@link List} of {@link GmProperty}
	 */
	private void attachOverlayedProperties(GmEntityType entityType, List<GmProperty> overlayedProperties) {
		/*
		if (overlayedProperties == null || overlayedProperties.size() == 0)
			return;
		String currentSignature = entityType.getTypeSignature();
		for (GmProperty property : overlayedProperties) {
			GmEntityType propertyEntityType = property.getEntityType();
			if (currentSignature.equalsIgnoreCase(propertyEntityType.getTypeSignature()) == false)
				continue;
			property.setEntityType(entityType);
			// make sure that the overlay properties is set.
			property.setIsOverlay(true);
			// attach it
			List<GmProperty> properties = entityType.getProperties();
			if (properties == null) {
				properties = new ArrayList<GmProperty>();
				entityType.setProperties(properties);
			}
			properties.add(property);
		}
		*/
	}

}
