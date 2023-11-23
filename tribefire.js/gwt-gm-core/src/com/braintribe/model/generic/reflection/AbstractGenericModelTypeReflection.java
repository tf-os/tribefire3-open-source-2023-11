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
package com.braintribe.model.generic.reflection;

import static com.braintribe.utils.lcd.CollectionTools2.newIdentityMap;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmfException;
import com.braintribe.model.generic.base.GenericBase;
import com.braintribe.model.generic.reflection.CollectionType.CollectionKind;
import com.braintribe.model.generic.reflection.type.collection.ListTypeImpl;
import com.braintribe.model.generic.reflection.type.collection.MapTypeImpl;
import com.braintribe.model.generic.reflection.type.collection.SetTypeImpl;
import com.braintribe.model.generic.reflection.type.custom.EnumTypeImpl;
import com.braintribe.model.generic.tools.GwtCompatibilityUtils;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.Weavable;
import com.braintribe.processing.async.api.AsyncCallback;

@SuppressWarnings("unusable-by-js")
public abstract class AbstractGenericModelTypeReflection implements ItwTypeReflection {

	// ###############################################
	// ## . . . . . Static Initialization . . . . . ##
	// ###############################################

	protected final Set<SimpleType> simpleTypes = newSet(TYPES_SIMPLE);
	protected final Map<Class<?>, SimpleType> simpleTypeMap = newIdentityMap();
	protected final Map<String, SimpleType> simpleTypeNameMap = newMap();

	protected final Map<String, GenericModelType> signatureMap = GwtCompatibilityUtils.newConcurrentMap();
	/** The key would actually be {@link Type}, if GWT supported it... */
	protected final Map<Object, GenericModelType> javaTypeMap = GwtCompatibilityUtils.newConcurrentMap();

	protected final Map<Class<?>, GenericModelType> knownEssentialTypes = newIdentityMap();

	// ###############################################
	// ## . . . . . Instance Initialization . . . . ##
	// ###############################################

	public AbstractGenericModelTypeReflection() {
		for (SimpleType type : TYPES_SIMPLE) {
			simpleTypeMap.put(type.getJavaType(), type);
			simpleTypeNameMap.put(type.getTypeName(), type);
		}

		for (GenericModelType type : TYPES_ESSENTIAL) {
			registerGenericModelType(type.getJavaType(), type);
			knownEssentialTypes.put(type.getJavaType(), type);
		}

		for (CollectionType cType : TYPES_COLLECTION) {
			CollectionTypeKey key = new CollectionTypeKey(cType.getJavaType(), cType.getParameterization());
			javaTypeMap.put(key, cType);
		}

		javaTypeMap.put(boolean.class, TYPE_BOOLEAN);
		javaTypeMap.put(int.class, TYPE_INTEGER);
		javaTypeMap.put(long.class, TYPE_LONG);
		javaTypeMap.put(float.class, TYPE_FLOAT);
		javaTypeMap.put(double.class, TYPE_DOUBLE);

		knownEssentialTypes.put(java.sql.Date.class, TYPE_DATE);
		knownEssentialTypes.put(java.sql.Time.class, TYPE_DATE);
		knownEssentialTypes.put(java.sql.Timestamp.class, TYPE_DATE);

		knownEssentialTypes.put(java.util.ArrayList.class, TYPE_LIST);
		knownEssentialTypes.put(java.util.LinkedList.class, TYPE_LIST);

		knownEssentialTypes.put(java.util.HashSet.class, TYPE_SET);
		knownEssentialTypes.put(java.util.LinkedHashSet.class, TYPE_SET);
		knownEssentialTypes.put(java.util.TreeSet.class, TYPE_SET);

		knownEssentialTypes.put(java.util.HashMap.class, TYPE_MAP);
		knownEssentialTypes.put(java.util.LinkedHashMap.class, TYPE_MAP);
		knownEssentialTypes.put(java.util.IdentityHashMap.class, TYPE_MAP);
		knownEssentialTypes.put(java.util.TreeMap.class, TYPE_MAP);
	}

	@Override
	public abstract void deploy(Weavable weavable) throws GmfException;

	@Override
	public abstract void deploy(Weavable weavable, AsyncCallback<Void> asyncCallback);

	protected abstract Class<?> getClassForName(String qualifiedEntityTypeName, boolean require) throws GenericModelException;

	protected abstract <T extends GenericEntity> EntityType<T> createEntityType(Class<?> entityClass) throws GenericModelException;

	protected abstract <T extends GenericModelType> T createCustomType(Class<?> classType);

	@Override
	@Deprecated
	public final GenericModelType getGenericModelType(Type type) throws GenericModelException {
		return getType(type);
	}

	@Override
	public abstract <T extends GenericModelType> T getType(Type type) throws GenericModelException;

	// ###############################################
	// ## . . . . . . . . Public API . . . . . . . .##
	// ###############################################

	@Override
	public BaseType getBaseType() {
		return BaseType.INSTANCE;
	}

	@Override
	public SimpleType getSimpleType(Class<?> javaType) {
		return getSimpleTypeMap().get(javaType);
	}

	@Override
	@Deprecated
	public List<SimpleType> getSimpleTypes() {
		return TYPES_SIMPLE;
	}

	@Override
	@Deprecated
	public Map<Class<?>, SimpleType> getSimpleTypeMap() {
		return simpleTypeMap;
	}

	@Override
	@Deprecated
	public Map<String, SimpleType> getSimpleTypeNameMap() {
		return simpleTypeNameMap;
	}

	@Override
	@Deprecated
	public Set<Class<?>> getSimpleTypeClasses() {
		return getSimpleTypeMap().keySet();
	}

	@Override
	@Deprecated
	public Set<String> getSimpleTypeNames() {
		return getSimpleTypeNameMap().keySet();
	}

	@Override
	@Deprecated
	public EnumType getEnumType(Enum<?> enumConstant) {
		Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) enumConstant.getClass();
		return getEnumType(enumClass);
	}

	@Override
	public EnumType getEnumType(String typeSignature) {
		return getType(typeSignature);
	}

	@Override
	public EnumType findEnumType(String typeSignature) {
		return findType(typeSignature);
	}

	@Override
	@Deprecated
	public EnumType getEnumType(String typeName, boolean require) {
		EnumType type = findEnumType(typeName);
		if (require && type == null)
			throw new GenericModelException("no enum type found for typeSignature " + typeName);

		return type;
	}

	@Override
	public EnumType getEnumType(Class<? extends Enum<?>> enumClass) {
		EnumType type = (EnumType) javaTypeMap.get(enumClass);
		if (type != null)
			return type;

		if (isFromAnotherClassLoader(enumClass))
			return null;

		return deployEnumType(enumClass);
	}

	@Override
	public EnumType deployEnumType(Class<? extends Enum<?>> enumClass) {
		EnumType type = new EnumTypeImpl(enumClass);
		registerGenericModelType(enumClass, type);

		return type;
	}

	@Override
	public void deployEntityType(EntityType<?> entityType) {
		registerGenericModelType(entityType.getJavaType(), entityType);
	}

	protected void registerGenericModelType(Object javaType, GenericModelType type) {
		signatureMap.put(type.getTypeSignature(), type);
		javaTypeMap.put(javaType, type);
	}

	@Override
	public ListType getListType(GenericModelType elementType) {
		return getCollectionType(List.class, elementType);
	}

	@Override
	public SetType getSetType(GenericModelType elementType) {
		return getCollectionType(Set.class, elementType);
	}

	@Override
	public MapType getMapType(GenericModelType keyType, GenericModelType valueType) {
		return getCollectionType(Map.class, keyType, valueType);
	}

	@Override
	public <T extends CollectionType> T getCollectionType(String typeName, GenericModelType... parameterization) {
		Class<?> collectionClass = getCollectionClass(typeName);
		return getCollectionType(collectionClass, parameterization);
	}

	@Override
	public <T extends CollectionType> T getCollectionType(Class<?> collectionClass, GenericModelType... parameterization) {
		CollectionTypeKey key = new CollectionTypeKey(collectionClass, parameterization);

		T type = (T) javaTypeMap.get(key);
		if (type != null)
			return type;

		type = (T) newCollectionType(collectionClass, parameterization);
		registerGenericModelType(key, type);

		return type;
	}

	private CollectionType newCollectionType(Class<?> javaType, GenericModelType[] parameterization) {
		try {
			return newCollectionTypeHelper(javaType, parameterization);

		} catch (Exception e) {
			throw new GenericModelException(
					"Error while creating a collection of type '" + javaType + "', with parameterization: " + Arrays.toString(parameterization), e);
		}
	}

	private CollectionType newCollectionTypeHelper(Class<?> javaType, GenericModelType[] parameterization) {
		if (javaType == Map.class)
			return new MapTypeImpl(parameterization[0], parameterization[1]);
		else if (javaType == List.class)
			return new ListTypeImpl(parameterization[0]);
		else if (javaType == Set.class)
			return new SetTypeImpl(parameterization[0]);

		throw new GenericModelException("Unsupported java type: " + javaType);
	}

	private Class<?> getCollectionClass(String collectionTypeName) {
		switch (CollectionKind.valueOf(collectionTypeName)) {
			case list:
				return List.class;
			case set:
				return Set.class;
			case map:
				return Map.class;
		}
		throw new GenericModelException("Unsupported collection type: " + collectionTypeName);
	}

	private static class CollectionTypeKey {
		public Class<?> collectionClass;
		public GenericModelType parameterization[];

		public CollectionTypeKey(Class<?> collectionClass, GenericModelType[] parameterization) {
			this.collectionClass = collectionClass;
			this.parameterization = parameterization;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof CollectionTypeKey))
				return false;

			CollectionTypeKey other = (CollectionTypeKey) obj;

			if (collectionClass != other.collectionClass)
				return false;

			int length = parameterization.length;
			for (int i = 0; i < length; i++)
				if (parameterization[i] != other.parameterization[i])
					return false;

			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(parameterization);
			result = prime * result + collectionClass.hashCode();

			return result;
		}
	}

	/**
	 * @deprecated call {@link GenericEntity#reference()} directly
	 */
	@Override
	@Deprecated
	public <T extends GenericEntity> EntityReference getEntityReference(T entity) throws GenericModelException {
		return entity.reference();
	}

	/**
	 * @deprecated call {@link GenericEntity#entityType()} directly
	 */
	@Override
	@Deprecated
	public <T extends GenericEntity, T1 extends GenericEntity> EntityType<T1> getEntityType(T entity) throws GenericModelException {
		return entity.entityType();
	}

	@Override
	public <T extends GenericEntity> EntityType<T> getEntityType(Class<? extends GenericEntity> declarationIface) throws GenericModelException {
		EntityType<T> result = (EntityType<T>) javaTypeMap.get(declarationIface);
		if (result != null)
			return result;

		if (!declarationIface.isInterface() && !declarationIface.isEnum())
			throw new RuntimeException("No. You cannot do this! Only interface Class-object is a valid parameter here." + " Given class: "
					+ declarationIface.getName() + ". If this is a valid class of an entity, it was probably retrieved"
					+ " from an instance. Simply call type() method on that instance.");

		if (isFromAnotherClassLoader(declarationIface))
			return null;

		return createEntityType(declarationIface);
	}

	/**
	 * We do this check when resolving {@link EntityType} or {@link EnumType} for a given class. In case given class
	 * comes from a different {@link ClassLoader} (than the one which loaded this class and thus also the entities), we
	 * have to return <tt>null</tt>, as this indicates we are analyzing some jar files which are not on the "main"
	 * classpath. This code is then reachable for example when examining enums with JTA, as the
	 * {@link Class#getEnumConstants()} triggers class initialization, which calls {@link EnumTypes#T(Class)} and so on.
	 * 
	 * Note that this method is overridden in the JVM implementation.
	 */
	protected boolean isFromAnotherClassLoader(@SuppressWarnings("unused") Class<?> entityOrEnumClass) {
		return false;
	}

	@Override
	@Deprecated
	public <T extends GenericEntity> EntityType<T> getEntityType(String typeSignature, boolean require) throws GenericModelException {
		return (EntityType<T>) (require ? getType(typeSignature) : findType(typeSignature));
	}

	@Override
	public <T extends GenericEntity> EntityType<T> getEntityType(String typeSignature) throws GenericModelException {
		return getType(typeSignature);
	}

	@Override
	public <T extends GenericEntity> EntityType<T> findEntityType(String typeSignature) {
		return findType(typeSignature);
	}

	public GenericModelType getParameterizedType(String typeName, GenericModelType[] parameterization) throws GenericModelException {
		return getCollectionType(typeName, parameterization);
	}

	public GenericModelType getCustomType(String typeName) throws GenericModelException {
		GenericModelType type = getType(typeName);
		return type instanceof CustomType ? type : null;
	}

	public GenericModelType getNoneParameterizedType(String typeName) throws GenericModelException {
		GenericModelType type = getType(typeName);
		return type.isCollection() ? null : type;
	}

	@Deprecated
	@Override
	public GenericModelType getRegisteredType(String typeSignature) throws GenericModelException {
		return getDeployedType(typeSignature);
	}

	@Override
	public <T extends GenericModelType> T getDeployedType(String typeSignature) {
		return (T) signatureMap.get(typeSignature);
	}

	/** Returns the type for the type signature or creates it if possible, otherwise returns null. */
	@Override
	public <T extends GenericModelType> T getType(String typeSignature) throws GenericModelException {
		T result = findType(typeSignature);
		if (result == null)
			throw new GenericModelException("Type not found: " + typeSignature);

		return result;
	}

	/** Returns the type only if already registered. No types will be created. */
	@Override
	public <T extends GenericModelType> T findType(String typeSignature) {
		T genericModelType = (T) signatureMap.get(typeSignature);

		if (genericModelType == null) {

			int lastCharIndex = typeSignature.length() - 1;
			if (typeSignature.charAt(lastCharIndex) == '>') {
				int parameterizationIndex = typeSignature.indexOf('<');
				String typeName = typeSignature.substring(0, parameterizationIndex).trim();
				String parameterization = typeSignature.substring(parameterizationIndex + 1, lastCharIndex);

				String parameterTypeSignatures[] = 'm' == typeName.charAt(0) ? parameterization.split(",") : new String[] { parameterization };

				GenericModelType parameterTypes[] = new GenericModelType[parameterTypeSignatures.length];

				for (int i = 0; i < parameterTypeSignatures.length; i++) {
					GenericModelType genericTypeParameter = findType(parameterTypeSignatures[i]);
					if (genericTypeParameter == null)
						return null;

					parameterTypes[i] = genericTypeParameter;
				}

				genericModelType = createCollectionType(typeName, parameterTypes).cast();

			} else {
				// we know this is a custom type
				Class<?> classType = getClassForName(typeSignature, false);
				return classType == null ? null : this.<T> createCustomType(classType);
			}
		}

		return genericModelType;
	}

	private CollectionType createCollectionType(String typeName, GenericModelType... parameterization) {
		Class<?> collectionClass = getCollectionClass(typeName);
		CollectionTypeKey key = new CollectionTypeKey(collectionClass, parameterization);
		CollectionType type = newCollectionType(collectionClass, parameterization);

		registerGenericModelType(key, type);

		return type;
	}

	@Override
	public <T extends GenericModelType> T getType(Object value) {
		return (T) getTypeHelper(value);
	}

	private GenericModelType getTypeHelper(Object value) {
		GenericModelType type;

		if (value == null)
			return TYPE_OBJECT;
		else if (value instanceof GenericBase)
			return ((GenericBase) value).type();
		else if (value.getClass().isEnum())
			return getEnumType((Class<? extends Enum<?>>) value.getClass());
		else if ((type = knownEssentialTypes.get(value.getClass())) != null)
			return type;
		else if (value instanceof List<?>)
			return TYPE_LIST;
		else if (value instanceof Set<?>)
			return TYPE_SET;
		else if (value instanceof Map<?, ?>)
			return TYPE_MAP;
		else if (value instanceof Date)
			return TYPE_DATE;

		return null;
	}

	@Override
	@Deprecated
	public GenericModelType getGenericModelType(Class<?> declarationIface) throws GenericModelException {
		return getType(declarationIface);
	}

	@Override
	public <T extends GenericModelType> T getType(Class<?> declarationIface) throws GenericModelException {
		T type = (T) javaTypeMap.get(declarationIface);
		if (type != null)
			return type;

		// Temporary code until we fix possible problems
		if (!declarationIface.isInterface() && !declarationIface.isEnum())
			throw new RuntimeException("No. You cannot do this! Only interface Class-object is a valid parameter here." + " Given class: "
					+ declarationIface.getName() + ". If this is a valid class of an entity, it was probably retrieved"
					+ " from an instance. Simply call type() method on that instance.");

		return createCustomType(declarationIface);
	}

	@Override
	@Deprecated
	public GenericModelType getObjectType(Object value) {
		return getType(value);
	}

	@Override
	@Deprecated
	public String getTypeSignature(Object value) throws GenericModelException {
		return getType(value).getTypeSignature();
	}

	@Override
	public Model getModel(String modelName) {
		Model result = findModel(modelName);
		if (result == null) {
			throw new GenericModelException("Model not found '" + modelName
					+ "'. Did you really use the qualified model name? Also make sure you have the model-declaration.xml on the classpath."
					+ " For troubleshooting see log entries starting with '[PACKAGED MODELS]' prefix.");
		}

		return result;
	}

	@Override
	public Model getModelForType(String customTypeSignature) {
		return null;
	}

	@Override
	public Collection<? extends Model> getPackagedModels() {
		throw new UnsupportedOperationException();
	}
}
