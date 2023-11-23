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
package com.braintribe.gwt.customization.client.cmd;

import static com.braintribe.gwt.customization.client.cmd.GmMappers.typeOverrideToSignature;
import static com.braintribe.gwt.customization.client.cmd.GmMappers.typeToSignature;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.meta.override.GmCustomTypeOverride;
import com.braintribe.model.meta.override.GmEntityTypeOverride;
import com.braintribe.model.meta.override.GmEnumConstantOverride;
import com.braintribe.model.meta.override.GmEnumTypeOverride;
import com.braintribe.model.meta.override.GmPropertyOverride;

/**
 * 
 */
public class MetaModelEditor {

	protected MetaModelEditorRegistry metaModelEditorRegistry;

	protected GmMetaModel metaModel;
	protected Set<GmCustomTypeOverride> modelTypeOverrides;
	protected Set<MetaData> modelMetaData;
	protected Set<MetaData> modelEnumTypeMetaData;
	protected Set<MetaData> modelEnumConstantMetaData;

	protected GmEntityType currentEntity;
	protected GmEntityTypeOverride currentEntityOverride;
	// entityInfo is either currentEntity or currentEntityOverride
	protected Set<MetaData> entityInfoMetaData;
	protected Set<MetaData> entityInfoPropertyMetaData;

	protected final Map<String, GmType> types;
	protected final Map<String, GmCustomTypeOverride> customTypeOverrides;

	protected GmProperty currentProperty;
	protected GmPropertyOverride currentPropertyOverride;
	protected Set<MetaData> propertyInfoMetaData;

	protected GmEnumType currentEnum;
	protected GmEnumTypeOverride currentEnumOverride;
	protected Set<MetaData> enumInfoMetaData;
	protected Set<MetaData> enumInfoConstantMetaData;

	protected GmEnumConstant currentConstant;
	protected GmEnumConstantOverride currentConstantOverride;
	protected Set<MetaData> constantInfoMetaData;

	public MetaModelEditor(GmMetaModel metaModel) {
		this(metaModel, null);
	}

	public MetaModelEditor(GmMetaModel metaModel, MetaModelEditorRegistry metaModelEditorRegistry) {
		this.metaModel = metaModel;
		this.modelTypeOverrides = metaModel.getTypeOverrides();
		this.modelMetaData = metaModel.getMetaData();
		this.modelEnumTypeMetaData = metaModel.getEnumTypeMetaData();
		this.modelEnumConstantMetaData = metaModel.getEnumConstantMetaData();

		this.types = nullSafe(metaModel.getTypes()).stream().collect(Collectors.toMap(typeToSignature, Function.identity()));
		this.customTypeOverrides = nullSafe(metaModel.getTypeOverrides()).stream()
				.collect(Collectors.toMap(typeOverrideToSignature, Function.identity()));

		this.metaModelEditorRegistry = ensureRegistry(metaModelEditorRegistry);
	}

	private MetaModelEditorRegistry ensureRegistry(MetaModelEditorRegistry registry) {
		return registry != null ? registry : new MetaModelEditorRegistry(this, metaModel);
	}

	// #############################################################
	// ## . . . . . . . . . . . Model . . . . . . . . . . . . . . ##
	// #############################################################

	public GmMetaModel metaModel() {
		return metaModel;
	}

	public MetaModelEditor acquireEditorFor(GmMetaModel relatedModel) {

		return metaModelEditorRegistry.acquireEditorFor(relatedModel);
	}

	public MetaModelEditor acquireEditorForModel(String modelName) {
		return metaModelEditorRegistry.acquireEditorForModel(modelName);
	}

	public void addModelMetaData(MetaData mmd) {
		if (modelMetaData == null) {
			modelMetaData = newSet();
			metaModel.setMetaData(modelMetaData);
		}

		modelMetaData.add(mmd);
	}

	public void addEnumMetaDataOnModel(MetaData mmd) {
		if (modelEnumTypeMetaData == null) {
			modelEnumTypeMetaData = newSet();
			metaModel.setEnumTypeMetaData(modelEnumTypeMetaData);
		}

		modelEnumTypeMetaData.add(mmd);
	}

	public void addConstantMetaDataOnModel(MetaData mmd) {
		if (modelEnumConstantMetaData == null) {
			modelEnumConstantMetaData = newSet();
			metaModel.setEnumConstantMetaData(modelEnumConstantMetaData);
		}

		modelEnumConstantMetaData.add(mmd);
	}

	// #############################################################
	// ## . . . . . . . . . . . Property . . . . . . . . . . . . .##
	// #############################################################

	public GmProperty loadProperty(EntityType<?> entityType, String propertyName) {
		loadEntityType(entityType);
		return loadProperty(propertyName);
	}

	public GmProperty loadProperty(String propertyName) {
		currentProperty = getProperty(propertyName);
		if (currentProperty.getMetaData() == null) {
			currentProperty.setMetaData(newSet());
		}
		propertyInfoMetaData = currentProperty.getMetaData();

		return currentProperty;
	}

	public GmProperty getProperty(String propertyName) {
		return getProperty(currentEntity, propertyName);
	}

	public static GmProperty getProperty(GmEntityType gmEntityType, String propertyName) {
		for (GmProperty gmProperty: gmEntityType.getProperties()) {
			if (gmProperty.getName().equals(propertyName)) {
				return gmProperty;
			}
		}

		throw new IllegalArgumentException("Property '" + propertyName + "' not found for entity: " + gmEntityType.getTypeSignature());
	}

	// #############################################################
	// ## . . . . . . . . . Property Overrides . . . . . . . . . .##
	// #############################################################

	/**
	 * NOTE: This method only works if given entityType declares given property. Otherwise use the
	 * {@link #acquirePropertyOverride(EntityType, EntityType, String)} method and provide the declaring type explicitly.
	 */
	public GmPropertyOverride acquirePropertyOverride(EntityType<?> entityType, String propertyName) {
		acquireEntityOverride(entityType);
		return acquirePropertyOverrideHelper(entityType, propertyName);
	}

	public <T extends GenericEntity> GmPropertyOverride acquirePropertyOverride(EntityType<T> entityType,
			EntityType<? super T> declaringType, String propertyName) {
		acquireEntityOverride(entityType);
		return acquirePropertyOverrideHelper(declaringType, propertyName);
	}

	private GmPropertyOverride acquirePropertyOverrideHelper(EntityType<?> declaringType, String propertyName) {
		currentPropertyOverride = findPropertyOverride(propertyName);
		if (currentPropertyOverride == null) {
			currentPropertyOverride = GmPropertyOverride.T.create();
			currentPropertyOverride.setProperty(metaModelEditorRegistry.getProperty(declaringType.getTypeSignature(), propertyName));
			currentPropertyOverride.setDeclaringTypeInfo(currentEntityOverride);
			currentEntityOverride.getPropertyOverrides().add(currentPropertyOverride);
		}
		if (currentPropertyOverride.getMetaData() == null) {
			currentPropertyOverride.setMetaData(newSet());
		}
		propertyInfoMetaData = currentPropertyOverride.getMetaData();

		return currentPropertyOverride;
	}

	public GmPropertyOverride findPropertyOverride(String propertyName) {
		for (GmPropertyOverride gmProperty: currentEntityOverride.getPropertyOverrides()) {
			if (gmProperty.getProperty().getName().equals(propertyName)) {
				return gmProperty;
			}
		}

		return null;
	}

	// #############################################################
	// ## . . . . . . . . . . Property Info . . . . . . . . . . . ##
	// #############################################################

	public void addPropertyMetaData(MetaData pmd) {
		propertyInfoMetaData.add(pmd);
	}

	// #############################################################
	// ## . . . . . . . . . . . . Entity . . . . . . . . . . . . .##
	// #############################################################

	public GmEntityType loadEntityType(EntityType<?> et) {
		return loadEntityType(et.getTypeSignature());
	}

	public GmEntityType loadEntityType(String typeSignature) {
		currentEntity = getType(typeSignature);
		loadEntityTypeInfo(currentEntity);

		return currentEntity;
	}

	public GmEntityType getEntityType(EntityType<?> entityType) {
		return getType(entityType);
	}

	public GmEntityType getEntityType(String typeSignature) {
		return getType(typeSignature);
	}

	// #############################################################
	// ## . . . . . . . . . Entity Overrides . . . . . . . . . . .##
	// #############################################################

	public GmEntityTypeOverride acquireEntityOverride(EntityType<?> entityType) {
		return acquireEntityOverride(entityType.getTypeSignature());
	}

	public GmEntityTypeOverride acquireEntityOverride(String typeSignature) {
		currentEntityOverride = (GmEntityTypeOverride) customTypeOverrides.get(typeSignature);
		if (currentEntityOverride == null) {
			GmEntityType overriddenType = metaModelEditorRegistry.getType(typeSignature);

			currentEntityOverride = GmEntityTypeOverride.T.create();
			currentEntityOverride.setEntityType(overriddenType);
			currentEntityOverride.setDeclaringModel(metaModel);
			addEntityOverride(typeSignature, currentEntityOverride);
		}

		loadEntityTypeInfo(currentEntityOverride);

		return currentEntityOverride;
	}

	private void addEntityOverride(String typeSignature, GmEntityTypeOverride typeOverride) {
		if (modelTypeOverrides == null) {
			modelTypeOverrides = newSet();
			metaModel.setTypeOverrides(modelTypeOverrides);
		}

		modelTypeOverrides.add(typeOverride);
		customTypeOverrides.put(typeSignature, typeOverride);
	}

	// #############################################################
	// ## . . . . . . . . . . Entity Info . . . . . . . . . . . . ##
	// #############################################################

	public void addEntityMetaData(MetaData emd) {
		entityInfoMetaData.add(emd);
	}

	public void addPropertyMetaDataOnEntity(MetaData pmd) {
		entityInfoPropertyMetaData.add(pmd);
	}

	private void loadEntityTypeInfo(GmEntityTypeInfo currentEntityInfo) {
		if (currentEntityInfo.getMetaData() == null) {
			currentEntityInfo.setMetaData(newSet());
		}
		if (currentEntityInfo.getPropertyMetaData() == null) {
			currentEntityInfo.setPropertyMetaData(newSet());
		}
		entityInfoMetaData = currentEntityInfo.getMetaData();
		entityInfoPropertyMetaData = currentEntityInfo.getPropertyMetaData();
	}

	// #############################################################
	// ## . . . . . . . . . . . . Enum . . . . . . . . . . . . . .##
	// #############################################################

	public GmEnumType loadEnumType(EnumType et) {
		return loadEnumType(et.getTypeSignature());
	}

	public GmEnumType loadEnumType(Class<? extends Enum<?>> clazz) {
		return loadEnumType(clazz.getName());
	}

	public GmEnumType loadEnumType(String typeSignature) {
		currentEnum = getType(typeSignature);
		loadEnumTypeInfo(currentEnum);

		return currentEnum;
	}

	public GmEnumType getEnumType(Class<? extends Enum<?>> clazz) {
		return getType(clazz.getName());
	}

	public GmEnumType getEnumType(EnumType enumType) {
		return getType(enumType);
	}

	public GmEnumType getEnumType(String typeSignature) {
		return getType(typeSignature);
	}

	// #############################################################
	// ## . . . . . . . . . . Enum Overrides . . . . . . . . . . .##
	// #############################################################

	public GmEnumTypeOverride acquireEnumOverride(Class<? extends Enum<?>> enumClass) {
		return acquireEnumOverride(enumClass.getName());
	}

	public GmEnumTypeOverride acquireEnumOverride(EnumType enumType) {
		return acquireEnumOverride(enumType.getTypeSignature());
	}

	public GmEnumTypeOverride acquireEnumOverride(String typeSignature) {
		currentEnumOverride = (GmEnumTypeOverride) customTypeOverrides.get(typeSignature);
		if (currentEnumOverride == null) {
			GmEnumType overriddenType = metaModelEditorRegistry.getType(typeSignature);

			currentEnumOverride = GmEnumTypeOverride.T.create();
			currentEnumOverride.setEnumType(overriddenType);
			currentEnumOverride.setDeclaringModel(metaModel);
			addEnumOverride(typeSignature, currentEnumOverride);
		}

		loadEnumTypeInfo(currentEnumOverride);

		return currentEnumOverride;
	}

	private void addEnumOverride(String typeSignature, GmEnumTypeOverride typeOverride) {
		if (modelTypeOverrides == null) {
			modelTypeOverrides = newSet();
			metaModel.setTypeOverrides(modelTypeOverrides);
		}

		modelTypeOverrides.add(typeOverride);
		customTypeOverrides.put(typeSignature, typeOverride);
	}

	// #############################################################
	// ## . . . . . . . . . . . Enum Info . . . . . . . . . . . . ##
	// #############################################################

	public void addEnumMetaData(MetaData emd) {
		enumInfoMetaData.add(emd);
	}

	public void addConstantMetaDataOnEnum(MetaData pmd) {
		enumInfoConstantMetaData.add(pmd);
	}

	private void loadEnumTypeInfo(GmEnumTypeInfo currentEnumInfo) {
		if (currentEnumInfo.getMetaData() == null) {
			currentEnumInfo.setMetaData(newSet());
		}
		if (currentEnumInfo.getEnumConstantMetaData() == null) {
			currentEnumInfo.setEnumConstantMetaData(newSet());
		}
		enumInfoMetaData = currentEnumInfo.getMetaData();
		enumInfoConstantMetaData = currentEnumInfo.getEnumConstantMetaData();
	}

	// #############################################################
	// ## . . . . . . . . . . Enum Constant . . . . . . . . . . . ##
	// #############################################################

	public void loadConstant(Enum<?> enumValue) {
		loadEnumType(enumValue.getDeclaringClass());
		loadConstant(enumValue.name());
	}

	public GmEnumConstant loadConstant(String constantName) {
		currentConstant = getConstant(constantName);
		if (currentConstant.getMetaData() == null) {
			currentConstant.setMetaData(newSet());
		}
		constantInfoMetaData = currentConstant.getMetaData();

		return currentConstant;
	}

	public GmEnumConstant getConstant(String constantName) {
		return getConstant(currentEnum, constantName);
	}

	public static GmEnumConstant getConstant(GmEnumType gmEnumType, String constantName) {
		for (GmEnumConstant gmEnumConstant: gmEnumType.getConstants()) {
			if (gmEnumConstant.getName().equals(constantName)) {
				return gmEnumConstant;
			}
		}

		throw new RuntimeException("No enum constant of type '" + gmEnumType.getTypeSignature() + "' found for name: " + constantName);
	}

	public void addConstantMetaData(MetaData ecmd) {
		constantInfoMetaData.add(ecmd);
	}

	// #############################################################
	// ## . . . . . . . . Enum Constant Overrides. . . . . . . . .##
	// #############################################################
	public GmEnumConstantOverride acquireEnumConstantOverride(Enum<?> enumValue) {
		acquireEnumOverride(enumValue.getDeclaringClass());
		return acquireEnumConstantOverrideHelper(enumValue);
	}

	private GmEnumConstantOverride acquireEnumConstantOverrideHelper(Enum<?> enumValue) {
		currentConstantOverride = findEnumConstantOverride(enumValue.name());
		if (currentConstantOverride == null) {
			currentConstantOverride = GmEnumConstantOverride.T.create();
			currentConstantOverride.setEnumConstant(metaModelEditorRegistry.getConstant(enumValue.getDeclaringClass().getName(), enumValue.name()));
			currentConstantOverride.setDeclaringTypeOverride(currentEnumOverride);
			currentEnumOverride.getConstantOverrides().add(currentConstantOverride);
		}
		if (currentConstantOverride.getMetaData() == null) {
			currentConstantOverride.setMetaData(newSet());
		}
		constantInfoMetaData = currentConstantOverride.getMetaData();

		return currentConstantOverride;
	}

	public GmEnumConstantOverride findEnumConstantOverride(String constantName) {
		for (GmEnumConstantOverride gmEnumConstantOverride: currentEnumOverride.getConstantOverrides()) {
			if (gmEnumConstantOverride.getEnumConstant().getName().equals(constantName)) {
				return gmEnumConstantOverride;
			}
		}

		return null;
	}

	// #############################################################
	// ## . . . . . . . . . . . . Other . . . . . . . . . . . . . ##
	// #############################################################

	public GmSimpleType getSimpleType(String typeSignature) {
		return getType(typeSignature);
	}

	public <T extends GmType> T getType(GenericModelType type) {
		return getType(type.getTypeSignature());
	}

	public <T extends GmType> T getType(String typeSignature) {
		GmType result = types.get(typeSignature);
		if (result == null) {
			throw new IllegalArgumentException("Wrong entity signature: " + typeSignature);
		}

		return (T) result;
	}

	public GmType findType(String typeSignature) {
		return types.get(typeSignature);
	}

}
