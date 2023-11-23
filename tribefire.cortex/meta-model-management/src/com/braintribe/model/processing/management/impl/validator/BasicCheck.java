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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.model.management.MetaModelValidationViolation;
import com.braintribe.model.management.MetaModelValidationViolationType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

public class BasicCheck implements ValidatorCheck {

	private final Set<String> encounteredTypeSignatures = new HashSet<String>();

	@Override
	public boolean check(GmMetaModel metaModel, List<MetaModelValidationViolation> validationErrors) {
		if (metaModel == null) {
			ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_METAMODEL_NULL, "metaModel is null");
			return false;
		}	
		
		encounteredTypeSignatures.clear();
		
		if (!NamesHelper.validMetaModelName(metaModel.getName())) {
			ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_METAMODEL_NAME_INVALID, 
					"metaModel.name '" + metaModel.getName() + "' is invalid");
		}
		if (!NamesHelper.validVersion(metaModel.getVersion())) {
			ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_METAMODEL_VERSION_INVALID, 
					"metaModel.version '" + metaModel.getVersion() + "' is invalid");
		}

//		if (metaModel.getBaseType() == null) {
//			ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_BASETYPE_NULL, "metaModel.baseType is null");
//		} else {
//			if (!MetaModelConst.BASE_TYPE_SIGNATURE.equals(metaModel.getBaseType().getTypeSignature())) {
//				ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_BASETYPE_INVALID, 
//						"metaModel.baseType.typeSignature should be '" + MetaModelConst.BASE_TYPE_SIGNATURE + 
//						"' but is '" + metaModel.getBaseType().getTypeSignature() + "'");
//			}
//		}
		
//		if (CollectionTools.isEmpty(metaModel.getSimpleTypes())) {
//			ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_SIMPLETYPES_NULL, 
//					"metaModel.simpleTypes is null");
//		} else {
//			HashSet<String> exsitingSimpleTypeSignatures = new HashSet<String>();
//			for (GmSimpleType simpleType : metaModel.getSimpleTypes()) {
//				if (simpleType == null) {
//					ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_SIMPLETYPES_CONTAIN_NULL, 
//							"metaModel.simpleTypes contain null");
//				} else {
//					exsitingSimpleTypeSignatures.add(simpleType.getTypeSignature());
//				}
//			}
//			
//			//check if all expected simple types are listed
//			for (String expectedSimpleTypeSignature : MetaModelConst.SIMPLE_TYPE_SIGNATURES) {
//				boolean existed = exsitingSimpleTypeSignatures.remove(expectedSimpleTypeSignature);
//				if (!existed) {
//					ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_SIMPLETYPE_MISSING, 
//							"metaModel.simpleTypes does not contain '" + expectedSimpleTypeSignature + "'");
//				}
//			}
//			
//			//check if there are any remaining types in simple types, which should not be there
//			for (String sig : exsitingSimpleTypeSignatures) {
//				ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_SIMPLETYPE_UNEXPECTED, 
//						"metaModel.simpleTypes contain unexpected '" + sig + "'");
//			}
//		}
		
		
		for (GmType type : metaModel.getTypes()) {
			if (type == null) {
				ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_TYPES_CONTAIN_NULL, 
						"metaModel.types contain null");
			} else {
				checkValidAndUniqueTypeSignature(type, "metaModel.types[]", validationErrors);

				
				if (type instanceof GmCustomType) {
					GmCustomType customType = (GmCustomType) type;
					if (customType.getDeclaringModel() == null) {
						ViolationBuilder.to(validationErrors).withCustomType(customType)
						.add(MetaModelValidationViolationType.BASIC_DECLARINGMODEL_NULL, 
								"metaModel.entityTypes(" + customType.getTypeSignature() + ").declaringModel is null");
					}
				
				}
				if (type instanceof GmEntityType) {
					GmEntityType entityType = (GmEntityType) type;
					if (entityType.getSuperTypes() != null) {
						for (GmEntityType supertype : entityType.getSuperTypes()) {
							if (supertype == null) {
								ViolationBuilder.to(validationErrors).withEntityType(entityType)
								.add(MetaModelValidationViolationType.BASIC_ENTITYTYPE_SUPERTYPES_CONTAIN_NULL, 
										"metaModel.entityTypes(" + entityType.getTypeSignature() + ").superTypes contain null");
							}
						}
					}
					
					checkProperties(entityType, validationErrors);
				}
				if (type instanceof GmEnumType) {
					GmEnumType enumType = (GmEnumType) type;
					checkConstants(enumType, validationErrors);
					
				}
				
			}
		}
			
		return true;
	}

	private void checkProperties(GmEntityType entityType, List<MetaModelValidationViolation> validationErrors) {
		if (entityType.getProperties() != null) {
			Set<String> encounteredNames = new HashSet<String>();
			for (GmProperty property : entityType.getProperties()) {
				if (property == null) {
					ViolationBuilder.to(validationErrors).withEntityType(entityType)
						.add(MetaModelValidationViolationType.BASIC_ENTITYTYPE_PROPERTIES_CONTAIN_NULL, 
								"metaModel.entityTypes[" + entityType.getTypeSignature() + "].properties contain null");
				} else {
					String name = property.getName();
					boolean valid = NamesHelper.validPropertyOrConstantName(name, entityType);
					
					if (!valid) {
						ViolationBuilder.to(validationErrors).withProperty(entityType, property)
							.add(MetaModelValidationViolationType.BASIC_ENTITYTYPE_PROPERTY_NAME_INVALID, 
									"metaModel.entityTypes[" + entityType.getTypeSignature() + "].properties[" + name +
									"] property name is not valid");
					}
					
					//check name uniqueness within entity
					boolean wasUnique = encounteredNames.add(name);
					if (!wasUnique) {
						ViolationBuilder.to(validationErrors).withProperty(entityType, property)
							.add(MetaModelValidationViolationType.BASIC_ENTITYTYPE_PROPERTY_NAME_NOT_UNIQUE, 
									"metaModel.entityTypes[" + entityType.getTypeSignature() + "].properties[" + name +
									"] property name is not unique within entityType");
					}
					
					//check property back-reference to entityType
					if (property.getDeclaringType() == null) {
						ViolationBuilder.to(validationErrors).withProperty(entityType, property)
							.add(MetaModelValidationViolationType.BASIC_ENTITYTYPE_PROPERTY_BACKREFERENCE_NULL, 
									"EntityType '" + entityType.getTypeSignature() + "' contains GmProperty with name '" + 
									property.getName() + "' that has entityType null");
					} else if (property.getDeclaringType() != entityType) {
						ViolationBuilder.to(validationErrors).withProperty(entityType, property)
							.add(MetaModelValidationViolationType.BASIC_ENTITYTYPE_PROPERTY_BACKREFERENCE_NO_MATCH, 
									"EntityType '" + entityType.getTypeSignature() + "' contains GmProperty with name '" + 
									property.getName() + "' that has entityType that does not match containing entityType");
					}
				}
			}
		}
	}

	private void checkConstants(GmEnumType enumType, List<MetaModelValidationViolation> validationErrors) {
		if (enumType.getConstants() != null) {
			Set<String> encounteredNames = new HashSet<String>();
			for (GmEnumConstant constant : enumType.getConstants()) {
				if (constant == null) {
					ViolationBuilder.to(validationErrors).withEnumType(enumType)
						.add(MetaModelValidationViolationType.BASIC_ENUMTYPE_CONSTANTS_CONTAIN_NULL, 
							"metaModel.enumTypes[" + enumType.getTypeSignature() + "].constants contain null");
				} else {
					String name = constant.getName();
					boolean valid = NamesHelper.validPropertyOrConstantName(name, enumType);
					
					if (!valid) {
						ViolationBuilder.to(validationErrors).withEnumConstant(enumType, constant)
							.add(MetaModelValidationViolationType.BASIC_ENUMTYPE_CONSTANT_NAME_INVALID,
									"metaModel.enumTypes[" + enumType.getTypeSignature() + "].constants[" + name +
									"] constant name is not valid");
					}
					
					//check name uniqueness within entity
					boolean wasUnique = encounteredNames.add(name);
					if (!wasUnique) {
						ViolationBuilder.to(validationErrors).withEnumConstant(enumType, constant)
							.add(MetaModelValidationViolationType.BASIC_ENUMTYPE_CONSTANT_NAME_NOT_UNIQUE, 
									"metaModel.enumTypes[" + enumType.getTypeSignature() + "].constants[" + name +
									"] constant name is not unique within enumType");
					}
					
					//check constant back-reference to enumType
					if (constant.getDeclaringType() == null) {
						ViolationBuilder.to(validationErrors).withEnumConstant(enumType, constant)
							.add(MetaModelValidationViolationType.BASIC_ENUMTYPE_CONSTANT_BACKREFERENCE_NULL, 
									"EnumType '" + enumType.getTypeSignature() + "' contains constant with name '" + 
									constant.getName() + "' that has enumType null");
					} else if (constant.getDeclaringType() != enumType) {
						ViolationBuilder.to(validationErrors).withEnumConstant(enumType, constant)
							.add(MetaModelValidationViolationType.BASIC_ENUMTYPE_CONSTANT_BACKREFERENCE_NO_MATCH, 
									"EnumType '" + enumType.getTypeSignature() + "' contains constant with name '" + 
									constant.getName() + "' that has enumType that does not match containing enumType");
					}
				}
			}
		}
	}

	private void checkValidAndUniqueTypeSignature(GmType type, String messageHint, List<MetaModelValidationViolation> validationErrors) {
		String typeSignature = type.getTypeSignature();
		boolean valid = NamesHelper.validTypeSignature(typeSignature, type);
		
		if (!valid) {
			ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_TYPESIGNATURE_INVALID, "typeSignature '" + typeSignature + "' in " + messageHint + " is not valid");
		}
		
		//check signature uniqueness
		boolean wasUnique = encounteredTypeSignatures.add(typeSignature);
		if (!wasUnique) {
			ViolationBuilder.to(validationErrors).add(MetaModelValidationViolationType.BASIC_TYPESIGNATURE_NOT_UNIQUE, "typeSignature '" + typeSignature + "' is not unique");
		}
	}
}
