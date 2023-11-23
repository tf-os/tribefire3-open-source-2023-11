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
package com.braintribe.model.util.meta;

import static com.braintribe.model.generic.reflection.GenericModelTypeReflection.rootModelName;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.StandardIntegerIdentifiable;
import com.braintribe.model.generic.StandardStringIdentifiable;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmBooleanType;
import com.braintribe.model.meta.GmDateType;
import com.braintribe.model.meta.GmDecimalType;
import com.braintribe.model.meta.GmDoubleType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmFloatType;
import com.braintribe.model.meta.GmIntegerType;
import com.braintribe.model.meta.GmLinearCollectionType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmLongType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.meta.info.GmPropertyInfo;
import com.braintribe.model.meta.override.GmPropertyOverride;

/**
 * Utility class for building correct {@link GmMetaModel} with dependencies and everything.
 * 
 * Note that this it not really compatible with JavaTypeAnalysis. This ignores all meta-data annotations which are not available via GM reflection,
 * and we don't use Java reflection here as the code is also GWT compatible. Currently there is only one MD annotation reflected, namely
 * {@link com.braintribe.model.generic.annotation.meta.Confidential}.
 * 
 * @see #withValidation()
 * @see #buildMetaModel(String, Collection)
 * @see #buildMetaModel(String, Collection, List)
 */
public class NewMetaModelGeneration {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private final Function<EntityType<?>, GenericEntity> entityFactory;

	private final Map<GenericModelType, GmType> typeMap = newMap();
	private final Set<GmType> newGmTypes = newSet();
	private final Set<GmType> allowedGmTypes = newSet();

	private boolean validate;

	private GmMetaModel rootMetaModel;
	private GmMetaModel currentlyBuildMetaModel;

	private Confidential confidential;

	/** Constructor that is only aware of the "RootModel"; */
	public NewMetaModelGeneration() {
		this(EntityType::create);
	}

	public NewMetaModelGeneration(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		initRootModel();
	}

	private void initRootModel() {
		putType(BaseType.INSTANCE, GmBaseType.T);

		putType(SimpleTypes.TYPE_STRING, GmStringType.T);
		putType(SimpleTypes.TYPE_FLOAT, GmFloatType.T);
		putType(SimpleTypes.TYPE_DOUBLE, GmDoubleType.T);
		putType(SimpleTypes.TYPE_BOOLEAN, GmBooleanType.T);
		putType(SimpleTypes.TYPE_INTEGER, GmIntegerType.T);
		putType(SimpleTypes.TYPE_LONG, GmLongType.T);
		putType(SimpleTypes.TYPE_DATE, GmDateType.T);
		putType(SimpleTypes.TYPE_DECIMAL, GmDecimalType.T);

		List<GmType> simpleAndBaseTypes = newList(typeMap.values());

		rootMetaModel = buildMetaModel(rootModelName, asList(GenericEntity.T, StandardIdentifiable.T, StandardStringIdentifiable.T, StandardIntegerIdentifiable.T), null);
		rootMetaModel.getTypes().addAll(simpleAndBaseTypes);
	}

	private void putType(GenericModelType type, EntityType<? extends GmType> gmTypeEntityType) {
		typeMap.put(type, create(gmTypeEntityType));
	}

	/**
	 * Constructor that is aware of all given models. Root model must be one of them, otherwise it throws an exception.
	 */
	public NewMetaModelGeneration(Iterable<Model> initModels) {
		this.entityFactory = EntityType::create;

		for (Model model : initModels) {
			GmMetaModel metaModel = model.getMetaModel();
			index(metaModel);

			if (rootModelName.equals(metaModel.getName()))
				rootMetaModel = metaModel;
		}

		if (!typeMap.containsKey(BaseType.INSTANCE))
			throw new RuntimeException("Incorrect initialization. BaseType not found. Make sure to provide RootModel as an intial model!");
	}

	/**
	 * Constructor that is aware of all given meta models. Root model must be one of them, otherwise it throws an exception.
	 *
	 * @param ignoredFlag
	 *            this is there just to create different signature from {@link #NewMetaModelGeneration(Iterable)}
	 */
	public NewMetaModelGeneration(Iterable<GmMetaModel> initMetaModels, boolean ignoredFlag) {
		this.entityFactory = EntityType::create;

		for (GmMetaModel metaModel : initMetaModels) {
			index(metaModel);

			if (rootModelName.equals(metaModel.getName()))
				rootMetaModel = metaModel;
		}

		if (!typeMap.containsKey(BaseType.INSTANCE))
			throw new RuntimeException("Incorrect initialization. BaseType not found. Make sure to provide RootModel as an intial model!");
	}

	private void index(GmMetaModel metaModel) {
		for (GmType gmType : nullSafe(metaModel.getTypes()))
			typeMap.put(typeReflection.getType(gmType.getTypeSignature()), gmType);
	}

	public GmMetaModel rootMetaModel() {
		return rootMetaModel;
	}

	// ONLY IN JVM
	public static Model rootModel() {
		return GMF.getTypeReflection().getModel(rootModelName);
	}

	/**
	 * Enables the validation flag. Validation means we check that every reachable from the model we are building was either explicitly stated (i.e.
	 * was in the input for the build method), or is part of the stated dependencies.
	 * 
	 * NOTE that validation is only meant to be used for debugging. If a validation fails, this instance is no longer in a consistent state and should
	 * not be used further.
	 */
	public NewMetaModelGeneration withValidation() {
		validate = true;
		return this;
	}

	public GmMetaModel buildMetaModel(String name, Collection<EntityType<?>> entityTypes) {
		return buildMetaModel(name, entityTypes, asList(rootMetaModel));
	}

	public GmMetaModel buildMetaModel(String name, Collection<EntityType<?>> entityTypes, List<GmMetaModel> dependencies) {
		initializeAllowedGmTypesIfWithValidation(dependencies);

		currentlyBuildMetaModel = create(GmMetaModel.T);
		currentlyBuildMetaModel.setGlobalId(Model.modelGlobalId(name));
		currentlyBuildMetaModel.setName(name);
		currentlyBuildMetaModel.setDependencies(dependencies);

		newGmTypes.clear();

		for (EntityType<?> entityType : nullSafe(entityTypes))
			aquireGmType(entityType);

		currentlyBuildMetaModel.getTypes().addAll(newGmTypes);

		return currentlyBuildMetaModel;
	}

	// ###################################################
	// ## . . . . . . Dependency validation . . . . . . ##
	// ###################################################

	private void initializeAllowedGmTypesIfWithValidation(List<GmMetaModel> dependencies) {
		if (!validate)
			return;

		allowedGmTypes.clear();
		Set<GmMetaModel> visited = newSet();
		fillAllowedTypes(visited, dependencies);
	}

	private void fillAllowedTypes(Set<GmMetaModel> visited, List<GmMetaModel> dependencies) {
		if (isEmpty(dependencies))
			return;

		for (GmMetaModel dependencyModel : dependencies)
			if (visited.add(dependencyModel)) {
				allowedGmTypes.addAll(dependencyModel.getTypes());
				fillAllowedTypes(visited, dependencyModel.getDependencies());
			}
	}

	// ###################################################
	// ## . . . . . . . . Type acquiring . . . . . . . .##
	// ###################################################

	private <T extends GmType> T aquireGmType(GenericModelType type) {
		GmType gmType = typeMap.get(type);
		if (gmType != null) {
			if (validate && gmType.isGmEntity() && !allowedGmTypes.contains(gmType))
				throw new GenericModelException("The newly built model '" + currentlyBuildMetaModel.getName() + "' has a dependency on '" + type
						+ "', but this type is not reachable via provided model dependencies.");

			return (T) gmType;
		}

		switch (type.getTypeCode()) {
			case entityType: {
				GmEntityType gmEntityType = registerNewType(GmEntityType.T, type);
				initialize((EntityType<?>) type, gmEntityType);
				return (T) gmEntityType;
			}
			case enumType: {
				GmEnumType gmEnumType = registerNewType(GmEnumType.T, type);
				initialize((EnumType) type, gmEnumType);
				return (T) gmEnumType;
			}
			case listType: {
				GmListType gmListType = registerNewType(GmListType.T, type);
				initialize((CollectionType) type, gmListType);
				return (T) gmListType;
			}
			case setType: {
				GmSetType gmSetType = registerNewType(GmSetType.T, type);
				initialize((CollectionType) type, gmSetType);
				return (T) gmSetType;
			}
			case mapType: {
				GmMapType gmMapType = registerNewType(GmMapType.T, type);
				initialize((MapType) type, gmMapType);
				return (T) gmMapType;
			}
			default:
				throw new IllegalArgumentException("Unsupported type: " + type.getTypeSignature());
		}
	}

	private <T extends GmType> T registerNewType(EntityType<T> gmTypeType, GenericModelType type) {
		T gmType = create(gmTypeType);
		gmType.setTypeSignature(type.getTypeSignature());
		gmType.setGlobalId(typeGlobalId(type.getTypeSignature()));

		typeMap.put(type, gmType);
		if (!type.isCollection())
			newGmTypes.add(gmType);
		if (validate)
			allowedGmTypes.add(gmType);

		return gmType;
	}

	private void initialize(EntityType<?> entityType, GmEntityType gmEntityType) {
		gmEntityType.setIsAbstract(entityType.isAbstract());
		gmEntityType.setDeclaringModel(currentlyBuildMetaModel);

		List<EntityType<?>> superEntityTypes = entityType.getSuperTypes();
		List<GmEntityType> gmSuperEntityTypes = newList(superEntityTypes.size());
		gmEntityType.setSuperTypes(gmSuperEntityTypes);

		for (EntityType<?> superEntityType : superEntityTypes) {
			GmEntityType gmSuperEntityType = aquireGmType(superEntityType);
			gmSuperEntityTypes.add(gmSuperEntityType);
		}

		List<GmProperty> gmProperties = gmEntityType.getProperties();
		if (gmProperties == null)
			gmEntityType.setProperties(gmProperties = newList());

		List<GmPropertyOverride> gmPropertyOverrides = gmEntityType.getPropertyOverrides();
		if (gmPropertyOverrides == null)
			gmEntityType.setPropertyOverrides(gmPropertyOverrides = newList());

		for (Property property : entityType.getProperties()) {
			if (isFirstDeclaredProperty(property, entityType)) {
				GmProperty gmProperty = create(GmProperty.T);
				gmProperty.setGlobalId(resolvePropertyGlobalId(property));
				gmProperty.setName(property.getName());
				gmProperty.setDeclaringType(gmEntityType);
				gmProperty.setNullable(property.isNullable());
				gmProperty.setInitializer(property.getInitializer());

				GenericModelType propertyType = property.getType();
				GmType gmPropertyType = aquireGmType(propertyType);
				gmProperty.setType(gmPropertyType);

				addMetaData(property, gmProperty);

				gmProperties.add(gmProperty);

			} else if (isDeclaredProperty(property, entityType)) {
				GmPropertyOverride gmPropOverride = create(GmPropertyOverride.T);
				gmPropOverride.setGlobalId(resolvePropertyGlobalId(property));
				gmPropOverride.setDeclaringTypeInfo(gmEntityType);
				gmPropOverride.setInitializer(property.getInitializer());
				gmPropOverride.setProperty(findGmPropertyForOverride(property));

				if (property.isConfidential())
					gmPropOverride.getMetaData().add(confidential());

				gmPropertyOverrides.add(gmPropOverride);
			}
		}

		if (entityType.getEvaluatesTo() != null)
			gmEntityType.setEvaluatesTo(aquireGmType(entityType.getEvaluatesTo()));
	}

	private GmProperty findGmPropertyForOverride(Property property) {
		EntityType<?> fdt = property.getFirstDeclaringType();
		GmEntityType gmType = aquireGmType(fdt);

		return gmType.getProperties().stream() //
				.filter(p -> p.getName().equals(property.getName())) //
				.findFirst() //
				.orElseThrow(() -> new IllegalStateException(
						"Property [" + property.getName() + "] not found on it's first declaring type: " + fdt.getTypeSignature()));
	}

	private boolean isDeclaredProperty(Property property, EntityType<?> entityType) {
		return property.getDeclaringType() == entityType;
	}

	private boolean isFirstDeclaredProperty(Property property, EntityType<?> entityType) {
		return property.getFirstDeclaringType() == entityType;
	}

	private void addMetaData(Property property, GmPropertyInfo gmPropInfo) {
		if (property.isConfidential())
			gmPropInfo.getMetaData().add(confidential());
	}

	private void initialize(EnumType enumType, GmEnumType gmEnumType) {
		gmEnumType.setDeclaringModel(currentlyBuildMetaModel);

		List<GmEnumConstant> constants = gmEnumType.getConstants();
		for (Enum<? extends Enum<?>> value : enumType.getEnumValues()) {
			GmEnumConstant gmEnumConstant = create(GmEnumConstant.T);
			gmEnumConstant.setDeclaringType(gmEnumType);
			gmEnumConstant.setName(value.name());

			gmEnumConstant.setGlobalId(resolveEnumConstantGlobalId(value));
			constants.add(gmEnumConstant);
		}
	}

	private void initialize(MapType mapType, GmMapType gmMapType) {
		gmMapType.setKeyType(aquireGmType(mapType.getKeyType()));
		gmMapType.setValueType(aquireGmType(mapType.getValueType()));
	}

	private void initialize(CollectionType collectionType, GmLinearCollectionType gmLinearCollectionType) {
		gmLinearCollectionType.setElementType(aquireGmType(collectionType.getCollectionElementType()));
	}

	public static String typeGlobalId(String typeSignature) {
		return "type:" + typeSignature;
	}

	private String resolveEnumConstantGlobalId(Enum<? extends Enum<?>> value) {
		Class<? extends Enum<?>> enumClass = value.getDeclaringClass();
		String name = value.name();

		return "enum:" + enumClass.getName() + "/" + name;
	}

	private static String resolvePropertyGlobalId(Property property) {
		EntityType<?> declaringType = property.getDeclaringType();
		String propertyName = property.getName();
		Class<?> javaType = declaringType.getJavaType();

		return "property:" + javaType.getCanonicalName() + "/" + propertyName;
	}

	private Confidential confidential() {
		if (confidential == null)
			confidential = create(Confidential.T);

		return confidential;
	}

	private <T extends GenericEntity> T create(EntityType<T> type) {
		return (T) entityFactory.apply(type);
	}

}
