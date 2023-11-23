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
package tribefire.extension.xml.schemed.xsd.analyzer.modelbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.exchange.GenericExchangePayload;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;

import tribefire.extension.xml.schemed.mapping.metadata.MappingMetaData;
import tribefire.extension.xml.schemed.mapping.metadata.ModelMappingMetaData;
import tribefire.extension.xml.schemed.marshaller.commons.HasCommonTokens;
import tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model.SchemedXmlXsdAnalyzerResponse;
import tribefire.extension.xml.schemed.model.xsd.Namespace;
import tribefire.extension.xml.schemed.xsd.analyzer.SchemedXmlXsdAnalyzer;
import tribefire.extension.xml.schemed.xsd.api.analyzer.AnalyzerRegistry;
import tribefire.extension.xml.schemed.xsd.mapper.metadata.BaseTypeInfoMetaData;

/**
 * creates a {@link SchemedXmlXsdAnalyzerResponse} out of the data returned by the {@link SchemedXmlXsdAnalyzer}, builds also the 
 * four models as there are a) skeleton, b) constraint, c) mapping, d) virtual (combined) model.
 *  
 * @author pit
 *
 */
public class ModelBuilder implements HasCommonTokens {
	private static Logger log = Logger.getLogger(ModelBuilder.class);
	private static BasicModelOracle modelOracle;	


	/**
	 * @param skeletonModelName - the name of the skeleton model, other model names are derived
	 * @return - the {@link SchemedXmlXsdAnalyzerResponse}
	 */
	public static SchemedXmlXsdAnalyzerResponse buildPrimerResponse(String skeletonModelName, AnalyzerRegistry registry) {
		VersionedArtifactIdentification skeletonArtifact = VersionedArtifactIdentification.parse(skeletonModelName);
		String skeletonName = skeletonArtifact.getGroupId() + ":" + skeletonArtifact.getArtifactId();
		String skeletonVersion = skeletonArtifact.getVersion();
		String constraintsName = skeletonName + "-constraints";
		String mappingName = skeletonName + "-mapping";
		String virtualName = skeletonName + "-virtual";
		
		GenericModelTypeReflection reflection = GMF.getTypeReflection();
		Model model = reflection.getModel("com.braintribe.gm:root-model");
		GmMetaModel rootModel = model.getMetaModel();
		modelOracle = new BasicModelOracle( rootModel);
		
		
		Model mappingMetadataModel = reflection.getModel("com.braintribe.gm.schemedxml:schemed-xml-xsd-mapping-model");
		GmMetaModel gmMappingMetadataModel = mappingMetadataModel.getMetaModel();
		
		//
		// build standard types model
		//
		GmMetaModel standardTypesSkeleton = GmMetaModel.T.create();
		standardTypesSkeleton.getDependencies().add(rootModel);
		standardTypesSkeleton.setName( MODEL_NAME_STANDARD_XML_TYPES);
		standardTypesSkeleton.setGlobalId( "model:" + MODEL_NAME_STANDARD_XML_TYPES);
		standardTypesSkeleton.setVersion( MODEL_VERSION_STANDARD_XML_TYPES);
		
		//
		// build skeleton model
		//
		GmMetaModel skeleton = GmMetaModel.T.create();
		skeleton.getDependencies().add(rootModel);
		skeleton.getDependencies().add( standardTypesSkeleton);
		skeleton.setName( skeletonName);
		skeleton.setGlobalId( "model:"+ skeletonName);
		skeleton.setVersion( skeletonVersion);
				
		List<GmMetaModel> declaringModelsOfSubstitutedTypes = registry.getActualSubstitutionModels();
		Set<GmMetaModel> usedSubstitutionModels = new HashSet<>();
		
		List<GmType> shallowTypes = new ArrayList<GmType>();
		
		// build the models' content	
		for (GmType type : registry.getExtractedTypes().values()) {
			// if declaring model isn't null, it's either a type from the root model or one from the substitution models
			GmMetaModel declaringModel = type.getDeclaringModel();
			
			// if set, then it's a substitute
			if (declaringModel != null) {
				if (declaringModelsOfSubstitutedTypes.contains(declaringModel)) {
					declaringModel.getDependencies().add(rootModel);
					usedSubstitutionModels.add(declaringModel);
				}
				shallowTypes.add(type);
			}
			else {
				
				// might be a standard type 
				if (type.getTypeSignature().startsWith( PACKAGE_STANDARD_XML_TYPES)) {
					type.setDeclaringModel(standardTypesSkeleton);
					shallowTypes.add(type);
					standardTypesSkeleton.getTypes().add(type);					
				}
				else {
				
					// actually created type
					type.setDeclaringModel(skeleton);
					skeleton.getTypes().add(type);
				}
			}
		}
		// add all substitution models of which types were used 
		skeleton.getDependencies().addAll(usedSubstitutionModels);
		
		// 		
		// build constraints model
		//
		GmMetaModel constraints = GmMetaModel.T.create();
		constraints.getDependencies().add(rootModel);
		constraints.getDependencies().add(skeleton);				
		constraints.getDependencies().add(standardTypesSkeleton);
		constraints.setName( constraintsName);
		constraints.setGlobalId( "model:"+ constraintsName);
		constraints.setVersion( skeletonVersion);
		ModelMetaDataEditor constraintsModelEditor = new BasicModelMetaDataEditor(constraints);
		
		//
		// build mapping model
		//
		GmMetaModel mapping = GmMetaModel.T.create();
		mapping.getDependencies().add(rootModel);
		mapping.getDependencies().add( skeleton);	
		mapping.getDependencies().add( gmMappingMetadataModel);
		mapping.setName( mappingName);
		mapping.setGlobalId( "model:"+ mappingName);
		mapping.setVersion( skeletonVersion);
		ModelMetaDataEditor mappingModelEditor = new BasicModelMetaDataEditor(mapping);
		
		// generate model mapping metadata
		ModelMappingMetaData modelMappingMetadata = ModelMappingMetaData.T.create();
		modelMappingMetadata.setModel(mapping);
		modelMappingMetadata.setMainTypeMap( registry.getExtractedTopLevelElements());
		modelMappingMetadata.setTargetNamespace( registry.getTargetNamespace());
		modelMappingMetadata.setElementFormIsQualified( registry.getElementQualified());
		modelMappingMetadata.setAttributeFormIsQualified( registry.getAttributeQualified());
		
		Map<String, Namespace> namespaces = registry.getNamespaces();
		for (Entry<String, Namespace> entry : namespaces.entrySet()){
			tribefire.extension.xml.schemed.mapping.metadata.Namespace namespace = tribefire.extension.xml.schemed.mapping.metadata.Namespace.T.create();
			namespace.setPrefix( entry.getValue().getPrefix());
			namespace.setUri( entry.getValue().getUri());
			namespace.setElementQualification( entry.getValue().getElementQualified());
			namespace.setAttributeQualification( entry.getValue().getAttributeQualified());
			modelMappingMetadata.getNamespaces().put( namespace.getUri(), namespace);
		}
		mapping.getMetaData().add( modelMappingMetadata);
		/*
		for (GmType type : registry.getExtractedTypes().values()) {
			if (type.getTypeSignature().startsWith( PACKAGE_STANDARD_XML_TYPES)) {
				standardTypesSkeleton.getTypes().add(type);
			}
		}
		*/
		
		
		GmEntityType genericEntityGmEntityType = modelOracle.getEntityTypeOracle(GenericEntity.T).asGmEntityType();
		
		// TODO : check what to do with EnumBase?
		// distribute metadata to the different models 
		for (GmType type : registry.getExtractedTypes().values()) {
			switch(type.typeKind()) {
				case ENTITY:				
					// handle entity type 
					MappingMetaData typeMappingMetaData = handleEntityType(skeleton, constraintsModelEditor, mappingModelEditor, (GmEntityType) type, genericEntityGmEntityType);
					if (typeMappingMetaData != null) {
						modelMappingMetadata.getMappingMetaDataMap().put( type.getTypeSignature(), typeMappingMetaData);
					}					
					break;
				case ENUM:
					// handle enum type 
					MappingMetaData enumTypeMappingMetaData =  handleEnumType( skeleton, constraintsModelEditor, mappingModelEditor, (GmEnumType) type);
					modelMappingMetadata.getMappingMetaDataMap().put( type.getTypeSignature(), enumTypeMappingMetaData);
					break;
				default:
					break;			
			}				
		}
	
		//
		// create virtual model that depends all
		//
		GmMetaModel virtual = GmMetaModel.T.create();
		Collections.addAll(virtual.getDependencies(), rootModel, skeleton, constraints, mapping);
		virtual.setName(virtualName);
		virtual.setGlobalId( "model:" + virtualName);
		virtual.setVersion( skeletonVersion);
		
		//
		// build response
		// 
		SchemedXmlXsdAnalyzerResponse response = SchemedXmlXsdAnalyzerResponse.T.create();
		response.setSkeletonModel(skeleton);
		response.setConstraintModel(constraints);
		response.setMappingModel(mapping);
		response.setVirtualModel(virtual);
		response.getShallowTypes().addAll(shallowTypes);
		
		return response;
	}
	
	
	/**
	 * build an {@link ExchangePackage} from the {@link SchemedXmlXsdAnalyzerResponse}
	 * @param primerResponse 
	 * @return
	 */
	public static ExchangePackage buildPrimerResponseAsExchangePackage(SchemedXmlXsdAnalyzerResponse primerResponse) {
			
		ExchangePackage exchangePackage = ExchangePackage.T.create();
		exchangePackage.setExported( new Date());
		exchangePackage.setGlobalId( "primer : " + primerResponse.getSkeletonModel().getName());
		
		GenericExchangePayload payload = GenericExchangePayload.T.create();
		payload.getExternalReferences().addAll( primerResponse.getShallowTypes());
		
		List<GmMetaModel> models = new ArrayList<>();
		models.add( primerResponse.getSkeletonModel());
		models.add( primerResponse.getConstraintModel());
		models.add( primerResponse.getMappingModel());
		models.add( primerResponse.getVirtualModel());
		payload.setAssembly(models);
		
		return exchangePackage;
	}

	

	/**
	 * process an enum type as created by the processor : assign it to the skeleton, distribute its (and its constants) metadata accross the models 
	 * @param skeleton - the {@link GmMetaModel} which is the model 
	 * @param constraintsModelEditor - the {@link ModelMetaDataEditor} which stands for the constraints model 
	 * @param mappingModelEditor - the {@link ModelMetaDataEditor} which stands for the mapping model
	 * @param type - the {@link GmEnumType} to handle 
	 * @return - the single {@link MappingMetaData} attached to the {@link GmType}
	 */
	private static MappingMetaData handleEnumType(GmMetaModel skeleton, ModelMetaDataEditor constraintsModelEditor, ModelMetaDataEditor mappingModelEditor, GmEnumType type) {
		MappingMetaData enumTypeMappingMetadata = null;
		
		skeleton.enumTypeSet().add(type);			
		for (MetaData metaData : type.getMetaData()) {
			if (metaData instanceof MappingMetaData) {
				enumTypeMappingMetadata = (MappingMetaData) metaData;
				mappingModelEditor.onEnumType( type.getTypeSignature()).addMetaData( metaData);
			}
			else {
				constraintsModelEditor.onEnumType( type.getTypeSignature()).addMetaData( metaData);
			}
		}
		type.getMetaData().clear();
		for (GmEnumConstant constant : type.getConstants()) {
			for (MetaData metaData : constant.getMetaData()) {
				if (metaData instanceof MappingMetaData) {
					mappingModelEditor.onEnumType( type.getTypeSignature()).addConstantMetaData(constant,  metaData);
				}
				else {
					constraintsModelEditor.onEnumType( type.getTypeSignature()).addConstantMetaData( metaData);
				}	
			}
			constant.getMetaData().clear();
		}				
		return enumTypeMappingMetadata;
	}


	/**
	 * process an entity type as created by the processor : 
	 * assign it to the skeleton, distribute the meta data to the other models 
	 * @param skeleton - the {@link GmMetaModel} standing for the skeleton model 
	 * @param constraintsModelEditor - the {@link ModelMetaDataEditor} accessing the constraints model 
	 * @param mappingModelEditor - the {@link ModelMetaDataEditor} accessing the mapping model 
	 * @param type - the {@link GmEntityType} in question 
	 * @param genericEntityGmEntityType - the {@link GenericEntity} as {@link GmType}
	 * @return - the single {@link MappingMetaData} attached to the {@link GmType}
	 */
	private static MappingMetaData handleEntityType(GmMetaModel skeleton, ModelMetaDataEditor constraintsModelEditor, ModelMetaDataEditor mappingModelEditor, GmEntityType type, GmEntityType genericEntityGmEntityType) {
	
		skeleton.entityTypeSet().add( type);

		if (type.getTypeSignature().startsWith( PACKAGE_STANDARD_XML_TYPES)) {
			// add to skeleton type
			// shallowize this type
		}
		
		// attach to GenericEntity 
		if (type.getSuperTypes().size() == 0) {
			type.getSuperTypes().add(genericEntityGmEntityType);
		}
		
		MappingMetaData typeMappingMetadata = null;
	
		Set<MetaData> entityTypeMetadata = type.getMetaData();
		boolean typeMappingMetaDataFound = false;
		for (MetaData metaData : entityTypeMetadata) {
			if (metaData instanceof MappingMetaData) {			
				typeMappingMetadata = (MappingMetaData) metaData;
				mappingModelEditor.onEntityType( type.getTypeSignature()).addMetaData(metaData);
				typeMappingMetaDataFound = true;
			}
			else if (metaData instanceof BaseTypeInfoMetaData) {				
				log.debug("filtering base type info meta data on [" + type.getTypeSignature() + "]");
			}
			else {
				constraintsModelEditor.onEntityType( type.getTypeSignature()).addMetaData( metaData);																								
			}
		}
		entityTypeMetadata.clear();
		// add property meta data
		for (GmProperty property : type.getProperties()) {
			boolean propertyMappingMetaDataFound = false;
			Set<MetaData> propertyMetaData = property.getMetaData();			
			for (MetaData metaData : propertyMetaData) {
				if (metaData instanceof MappingMetaData) {
					mappingModelEditor.onEntityType( type.getTypeSignature()).addPropertyMetaData(property, metaData);
					propertyMappingMetaDataFound = true;
				}
				else {
					constraintsModelEditor.onEntityType( type.getTypeSignature()).addPropertyMetaData(property, metaData);
				}				
			}			
			propertyMetaData.clear();
			if (!propertyMappingMetaDataFound) {
				log.warn("No mapping meta data attached to property [" + property.getName() + "] of [" + type.getTypeSignature() + "]");
			}
		}
		if (!typeMappingMetaDataFound) {
			log.warn("No mapping meta data attached to type [" + type.getTypeSignature() + "]");
		}
		
		return typeMappingMetadata;
	}

}
