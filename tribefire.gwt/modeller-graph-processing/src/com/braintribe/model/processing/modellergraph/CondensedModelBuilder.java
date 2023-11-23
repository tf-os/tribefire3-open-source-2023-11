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
package com.braintribe.model.processing.modellergraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.modellergraph.condensed.CondensedModel;
import com.braintribe.model.modellergraph.condensed.CondensedRelationship;
import com.braintribe.model.modellergraph.condensed.CondensedType;
//import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
//import com.braintribe.model.processing.meta.oracle.BasicModelOracle;

public class CondensedModelBuilder {
	
	private final GmMetaModel model;
	private final Map<GmType, CondensedType> condensedTypeIndex = new HashMap<GmType, CondensedType>(); 
	private final Map<String, CondensedRelationship> condensedRelationshipIndex = new HashMap<String, CondensedRelationship>();
	private CondensedModel condensedModel;
//	private CmdResolverImpl ccmd;
	private boolean ensureTypesPerProperties = true;
	private Set<String> modelCache;
	//private BasicModelOracle oracle;
	
	private CondensedModelBuilder(GmMetaModel model) {
		this.model = model;
		modelCache = new HashSet<>();
		//this.oracle = new BasicModelOracle(model);
	}
	
//	public void setCcmd(CmdResolverImpl ccmd) {
//		this.ccmd = ccmd;
//	}
	
	public void setEnsureTypesPerProperties(boolean ensureTypesPerProperties) {
		this.ensureTypesPerProperties = ensureTypesPerProperties;
	}
	
	protected CondensedModel build() {
		condensedModel = CondensedModel.T.create();
		condensedModel.setModel(model);
		condensedModel.setTypes(new HashSet<CondensedType>());
		
		//Set<GmType> types = new HashSet<GmType>(oracle.getTypes().asGmTypes().collect(Collectors.toList()));		
		Set<GmType> types = new HashSet<GmType>(getDependentTypes(model));
		
		if(ensureTypesPerProperties){
			for(GmEntityType gmEntityType : model.entityTypeSet()){
				if(gmEntityType.getProperties() != null) {
					for(GmProperty gmProperty : gmEntityType.getProperties()){
						GmType gmPropertyType = gmProperty.getType();
						if(gmPropertyType != null && (gmPropertyType.isGmEntity() || gmPropertyType.isGmEnum()))
							types.add(gmPropertyType);
					}
				}
			}
		}
		
		for (GmType type: types) {
			CondensedType condensedType = acquireCondensedType(type);
			condensedModel.getTypes().add(condensedType);
		}
		
		return condensedModel;
	}
	
	protected CondensedType acquireCondensedType(GmType type) {
		CondensedType condensedType = condensedTypeIndex.get(type);
		
		if (condensedType == null) {
			condensedType = CondensedType.T.create();
			condensedType.setModel(condensedModel);
			Set<CondensedRelationship> relationships = new HashSet<CondensedRelationship>();
			condensedType.setRelationships(relationships);
			
			condensedTypeIndex.put(type, condensedType);
			condensedType.setGmType(type);
			
			if(type instanceof GmEntityType){
				GmEntityType entityType = (GmEntityType)type;
				List<GmEntityType> superTypes = entityType.getSuperTypes();
				
				if (superTypes != null) {
					for (GmEntityType superType: superTypes) {
						CondensedType condensedSuperType = acquireCondensedType(superType);
						CondensedRelationship condensedRelationship = acquireCondensedRelationship(condensedType, condensedSuperType);
						condensedRelationship.setGeneralization(true);
	
						// add also inverse relationship
						CondensedRelationship inverseCondensedRelationship = acquireCondensedRelationship(condensedSuperType, condensedType);
						inverseCondensedRelationship.setSpecialization(true);
					}
				}			
				
				List<GmProperty> properties = getProperties(entityType, new HashSet<String>());
				
				if (properties != null) {
					for (GmProperty property: properties) {
						GmType propertyType = property.getType();
						
						Set<GmType> referencedType = getReferencedType(propertyType);
						
						for (GmType referencedEntityType: referencedType) {
							CondensedType associatedType = acquireCondensedType(referencedEntityType);
							CondensedRelationship condensedRelationship = acquireCondensedRelationship(condensedType, associatedType);
							condensedRelationship.getAggregations().add(property);
							
							// add also inverse relationship
							CondensedRelationship inverseCondensedRelationship = acquireCondensedRelationship(associatedType, condensedType);
							inverseCondensedRelationship.getInverseAggregations().add(property);
						}
					}
				}
				
				//relationship for mapped types
//				QualifiedEntityAssignment entityAssignment = (QualifiedEntityAssignment) (ccmd != null ? ccmd.getMetaData().entity(entityType).meta(QualifiedEntityAssignment.T).exclusive() : null);
				
				/*
				if(entityAssignment == null){
					if(entityType.getMetaData() != null && !entityType.getMetaData().isEmpty()){
						for(MetaData metaData : entityType.getMetaData()){
							if(metaData instanceof QualifiedEntityAssignment){
								entityAssignment = (QualifiedEntityAssignment) metaData;
								break;
							}
						}
					}
				}
				
				if(entityAssignment != null){
					CondensedType mappedType = acquireCondensedType(entityAssignment.getEntityType());
					
					CondensedRelationship condensedRelationship = acquireCondensedRelationship(condensedType, mappedType);
					condensedRelationship.setMapping(true);

					// add also inverse relationship
					CondensedRelationship inverseCondensedRelationship = acquireCondensedRelationship(mappedType, condensedType);
					inverseCondensedRelationship.setMapping(true);
				} */
			}		
		}
		
		return condensedType;
	}
	
	/*protected CondensedType getAssociatedType(CondensedType from, CondensedRelationship relationship) {
		return from == relationship.getFromType()? 
			relationship.getToType():
			relationship.getFromType();
	}*/
	
	private Set<GmType> getReferencedType(GmType type) {
		Set<GmType> types = new HashSet<GmType>();
		getReferencedTypes(type, types);
		return types;
	}
	
	private void getReferencedTypes(GmType type, Set<GmType> types) {
		
		if(type != null){
			switch (type.typeKind()) {
			case ENTITY:case ENUM:
				types.add(type);
				break;
			case LIST:
			case SET:
				getReferencedTypes(((GmLinearCollectionType)type).getElementType(), types);
				break;
			case MAP:
				GmMapType mapType = (GmMapType)type;
				getReferencedTypes(mapType.getKeyType(), types);
				getReferencedTypes(mapType.getValueType(), types);
				break;
			default:
				break;
			}
		}
	}

	protected CondensedRelationship acquireCondensedRelationship(CondensedType fromType, CondensedType toType) {
		String key = fromType.getGmType().getTypeSignature() + ":" + toType.getGmType().getTypeSignature();
		CondensedRelationship condensedRelationship = condensedRelationshipIndex.get(key); 
		
		if (condensedRelationship == null){
			condensedRelationship = CondensedRelationship.T.create();
			condensedRelationship.setFromType(fromType);
			condensedRelationship.setToType(toType);
			
			fromType.getRelationships().add(condensedRelationship);
			
			condensedRelationship.setAggregations(new HashSet<GmProperty>());
			condensedRelationship.setInverseAggregations(new HashSet<GmProperty>());
			condensedRelationshipIndex.put(key, condensedRelationship);
		}
		return condensedRelationship;
	}
	
	/*protected void addGeneralization(CondensedRelationship condensedRelationship, CondensedType fromType) {
		boolean inverse = condensedRelationship.getFromType() != fromType;
		
		condensedRelationship.setGeneralizationDirection(inverse? GeneralizationDirection.inverse: GeneralizationDirection.normal);
	}

	protected void addAggregation(CondensedRelationship condensedRelationship, CondensedType fromType, GmProperty property) {
		boolean inverse = condensedRelationship.getFromType() != fromType;
		
		if (inverse) {
			condensedRelationship.getInverseAggregations().add(property);
		}
		else {
			condensedRelationship.getAggregations().add(property);
		}
	}*/

	public static CondensedModel build(GmMetaModel model) {
		return new CondensedModelBuilder(model).build();
	}
	
	private List<GmProperty> getProperties(GmEntityType gmEntityType, Set<String> nameBuffer){
		List<GmProperty> gmProperties = new ArrayList<GmProperty>();
		if(gmEntityType.getProperties() != null){
			for(GmProperty gmProperty : gmEntityType.getProperties()){
				if(!nameBuffer.contains(gmProperty.getName())){
					gmProperties.add(gmProperty);
					nameBuffer.add(gmProperty.getName());
				}
			}
		}
		if(gmEntityType.getSuperTypes() != null){
			for(GmEntityType superType : gmEntityType.getSuperTypes()){
				gmProperties.addAll(getProperties(superType, nameBuffer));
			}
		}
		return gmProperties;
	}
	
	private Set<GmType> getDependentTypes(GmMetaModel model){
		Set<GmType> types = new HashSet<GmType>();
		if(!modelCache.contains(model.getGlobalId())){
			modelCache.add(model.getGlobalId());
			if(model.getDependencies() != null){
				for(GmMetaModel dep : model.getDependencies()){
					types.addAll(getDependentTypes(dep));				
				}
				types.addAll(model.getTypes());
			}
		}
		return types;
	}
}
