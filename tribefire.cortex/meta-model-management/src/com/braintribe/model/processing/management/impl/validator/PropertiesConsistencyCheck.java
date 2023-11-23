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
package com.braintribe.model.processing.management.impl.validator;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.management.MetaModelValidationViolation;
import com.braintribe.model.management.MetaModelValidationViolationType;
import com.braintribe.model.meta.GmCollectionType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

public class PropertiesConsistencyCheck implements ValidatorCheck {
	
	@Override
	public boolean check(GmMetaModel metaModel, List<MetaModelValidationViolation> validationErrors) {
		metaModel.entityTypes().forEach(t -> checkPropertiesConsistency(t, newSet(), validationErrors));
		return true;
	}
	
	private Map<String, GmProperty> checkPropertiesConsistency(GmEntityType entityType, Set<String> visitedTypeSignatures, 
			List<MetaModelValidationViolation> validationErrors) {
		
		Map<String, GmProperty> res = Collections.emptyMap();
		if (entityType != null) {
			visitedTypeSignatures.add(entityType.getTypeSignature());
			Map<String, GmProperty> allSuperpropertiesBySignature = new HashMap<String, GmProperty> ();

			if (entityType.getSuperTypes() != null) {
				for (GmEntityType st : entityType.getSuperTypes()) {
					//hierarchy loop protection needed
					if ((st != null) && (!visitedTypeSignatures.contains(st.getTypeSignature()))) {
						Map<String, GmProperty> stProps = checkPropertiesConsistency(st, visitedTypeSignatures, validationErrors);
						allSuperpropertiesBySignature.putAll(stProps);
					}
				}
			}
			
			
			checkConsistencyAcrossDifferrentInheritanceLines(allSuperpropertiesBySignature, entityType, validationErrors);
			
			
			Map<String, GmProperty> thisEntityPropertiesBySignature = new HashMap<String, GmProperty>();
			if (entityType.getProperties() != null) {
				for (GmProperty p : entityType.getProperties()) {
					if (p != null) {
						String propSignature = p.getName() + ":" + (p.getType() == null ? null : p.getType().getTypeSignature());
							
						thisEntityPropertiesBySignature.put(propSignature, p);
						
						checkForNestedCollectionsOrCollectionsWithNullElementTypes(p, entityType, validationErrors);
						
//						if (p.getIsOverlay() && !allSuperpropertiesBySignature.containsKey(propSignature)) {
//							ViolationBuilder.to(validationErrors).withProperty(entityType, p)
//								.add(MetaModelValidationViolationType.PROPERTY_OVERLAY_NO_SUPERPROPERTY, 
//									"Type '" + entityType.getTypeSignature() + "' contains GmProperty with name '" + p.getName() + 
//									"' with isOverlay true, but no supertype has a property with the same name and type");
//						} else {
//						
//							boolean foundMatchByName = false;
//							for (String key : allSuperpropertiesBySignature.keySet()) {
//								if (key.startsWith(p.getName() + ":")) {
//									foundMatchByName = true;
//									break;
//								}
//							}
//							
//							if (foundMatchByName && !p.getIsOverlay()) {
//								ViolationBuilder.to(validationErrors).withProperty(entityType, p)
//									.add(MetaModelValidationViolationType.PROPERTY_CONFLICT_SUPERPROPERTY, 
//										"Type '" + entityType.getTypeSignature() + 
//										"' contains GmProperty with name '" + p.getName() + 
//										"' that has isOverlay set to false, but it's supertype '" + 
//										allSuperpropertiesBySignature.get(propSignature).getEntityType().getTypeSignature() + 
//										"' has a property with the same name");
//							}
//						}
						
					}					
				}
			}

			allSuperpropertiesBySignature.putAll(thisEntityPropertiesBySignature);
			res = Collections.unmodifiableMap(allSuperpropertiesBySignature);
		}
		return res;
	}

	private void checkForNestedCollectionsOrCollectionsWithNullElementTypes(GmProperty p, GmEntityType entityType,
			List<MetaModelValidationViolation> validationErrors) {
		
		if (p.getType() != null && p.getType() instanceof GmLinearCollectionType) {
			GmType elementType = ((GmLinearCollectionType) p.getType()).getElementType();
			if (elementType == null) {
				ViolationBuilder.to(validationErrors).withProperty(entityType, p)
					.add(MetaModelValidationViolationType.PROPERTY_TYPE_COLLECTION_ELEMENT_TYPE_NULL, 
						"Type '" + entityType.getTypeSignature() + "' contains property '" + p.getName() + 
						"' of linear collection type, which element type is null");
			} else if (elementType instanceof GmCollectionType) {
				ViolationBuilder.to(validationErrors).withProperty(entityType, p)
					.add(MetaModelValidationViolationType.PROPERTY_TYPE_COLLECTION_ELEMENT_TYPE_COLLECTION, 
						"Type '" + entityType.getTypeSignature() + "' contains property '" + p.getName() + 
						"' of linear collection type, which element type is also a collection");
			}
		}
		
		if (p.getType() != null && p.getType() instanceof GmMapType) {
			GmType keyType = ((GmMapType) p.getType()).getKeyType();
			if (keyType == null) {
				ViolationBuilder.to(validationErrors).withProperty(entityType, p)
					.add(MetaModelValidationViolationType.PROPERTY_TYPE_COLLECTION_ELEMENT_TYPE_NULL, 
						"Type '" + entityType.getTypeSignature() + "' contains property '" + p.getName() + 
						"' of map type, which key type is null");
			} else if (keyType instanceof GmCollectionType) {
				ViolationBuilder.to(validationErrors).withProperty(entityType, p)
					.add(MetaModelValidationViolationType.PROPERTY_TYPE_COLLECTION_ELEMENT_TYPE_COLLECTION, 
						"Type '" + entityType.getTypeSignature() + "' contains property '" + p.getName() + 
						"' of map type, which key type is also a collection");
			}
			
			GmType valueType = ((GmMapType) p.getType()).getValueType();
			if (valueType == null) {
				ViolationBuilder.to(validationErrors).withProperty(entityType, p)
					.add(MetaModelValidationViolationType.PROPERTY_TYPE_COLLECTION_ELEMENT_TYPE_NULL, 
						"Type '" + entityType.getTypeSignature() + "' contains property '" + p.getName() + 
						"' of map type, which value type is null");
			} else if (valueType instanceof GmCollectionType) {
				ViolationBuilder.to(validationErrors).withProperty(entityType, p)
					.add(MetaModelValidationViolationType.PROPERTY_TYPE_COLLECTION_ELEMENT_TYPE_COLLECTION, 
						"Type '" + entityType.getTypeSignature() + "' contains property '" + p.getName() + 
						"' of map type, which value type is also a collection");
			}
		}
	}

	private void checkConsistencyAcrossDifferrentInheritanceLines(Map<String, GmProperty> allSupertypeProperties,
			GmEntityType entityType, List<MetaModelValidationViolation> validationErrors) {
		
		//check inherited properties compatibilities - across different inheritance lines
		Map<String, GmProperty> superPropertiesByName = new HashMap<String, GmProperty>();
		for (GmProperty prop : allSupertypeProperties.values()) {
			if (prop != null) {
				GmProperty sameNameProp = superPropertiesByName.get(prop.getName());
				if (sameNameProp != null) {
					if (sameNameProp.getType() != null && prop.getType() != null &&
							!sameNameProp.getType().getTypeSignature().equals(prop.getType().getTypeSignature())) {
						
						ViolationBuilder.to(validationErrors).withProperty(entityType, prop)
							.add(MetaModelValidationViolationType.PROPERTY_INHERITED_FROM_DIFFERRENT_SUPERTYPES_CLASH_BY_TYPE, 
								"EntityType '" + entityType.getTypeSignature() + "' inherits properties with name '" + prop.getName() + 
								"' from differrent inheritance lines, but they clash by types: '" + prop.getType().getTypeSignature() + 
								"', '" + sameNameProp.getType().getTypeSignature() + "'");
					}
				} else {
					superPropertiesByName.put(prop.getName(), prop);
				}
			}
		}
	}
}
