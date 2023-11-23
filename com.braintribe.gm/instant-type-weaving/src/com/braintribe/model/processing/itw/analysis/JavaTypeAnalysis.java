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
package com.braintribe.model.processing.itw.analysis;

import static com.braintribe.model.processing.itw.analysis.meta.MetaDataAnnotationAnalyzers.analyzeMetaDataAnnotations;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmBaseType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmBooleanType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmDateType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmDecimalType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmDoubleType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmEntityType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmEnumConstant;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmEnumType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmFloatType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmIntegerType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmListType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmLongType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmMapType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmProperty;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmPropertyOverride;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmSetType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmStringType;
import static com.braintribe.model.processing.itw.analysis.protomodel.EagerMetaModelTypes.eagerProtoGmTypeRestriction;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.GlobalId;
import com.braintribe.model.generic.annotation.Transient;
import com.braintribe.model.generic.annotation.TypeRestriction;
import com.braintribe.model.generic.annotation.meta.api.analysis.MdaAnalysisContext;
import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.tools.GmValueCodec.EnumParsingMode;
import com.braintribe.model.generic.value.ValueDescriptor;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmModels;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.itw.InitializerTools;
import com.braintribe.model.processing.itw.InitializerTools.EnumHint;
import com.braintribe.model.processing.itw.analysis.api.EagerType;
import com.braintribe.model.weaving.ProtoGmBaseType;
import com.braintribe.model.weaving.ProtoGmBooleanType;
import com.braintribe.model.weaving.ProtoGmCollectionType;
import com.braintribe.model.weaving.ProtoGmDateType;
import com.braintribe.model.weaving.ProtoGmDecimalType;
import com.braintribe.model.weaving.ProtoGmDoubleType;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmEnumConstant;
import com.braintribe.model.weaving.ProtoGmEnumType;
import com.braintribe.model.weaving.ProtoGmFloatType;
import com.braintribe.model.weaving.ProtoGmIntegerType;
import com.braintribe.model.weaving.ProtoGmLinearCollectionType;
import com.braintribe.model.weaving.ProtoGmListType;
import com.braintribe.model.weaving.ProtoGmLongType;
import com.braintribe.model.weaving.ProtoGmMapType;
import com.braintribe.model.weaving.ProtoGmModelElement;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.ProtoGmSetType;
import com.braintribe.model.weaving.ProtoGmStringType;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.data.ProtoHasMetaData;
import com.braintribe.model.weaving.info.ProtoGmPropertyInfo;
import com.braintribe.model.weaving.override.ProtoGmPropertyOverride;
import com.braintribe.model.weaving.restriction.ProtoGmTypeRestriction;
import com.braintribe.utils.lcd.StringTools;

public final class JavaTypeAnalysis {

	private volatile Map<Type, ProtoGmType> typeMap; // may contain not-yet-finished GmTypes
	private final Object typeMapLock = new Object();
	private final Map<Type, ProtoGmType> typeCache = new ConcurrentHashMap<>(); // contains only finished GmTypes

	private ClassLoader classLoader = null;
	private boolean proto;
	private boolean enhanced;
	private final JtaClasses jtaClasses = new JtaClasses();
	protected final ThreadLocal<List<Runnable>> postProcessors = new ThreadLocal<>();
	private boolean requireEnumBase = true;

	public static final class JtaClasses {
		public Class<?> genericEntityClass;
		public Class<?> enumBaseClass;
		public Class<?> evaluatorClass;
		public Class<?> evalContextClass;
		public Class<? extends Annotation> abstractAnnotationClass;
		public Class<? extends Annotation> transientAnnotationClass;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void setEnhanced(boolean enhanced) {
		this.enhanced = enhanced;
	}

	@Deprecated
	public void setPseudo(boolean pseudo) {
		setProto(pseudo);
	}

	public void setProto(boolean proto) {
		this.proto = proto;
	}

	public void setRequireEnumBase(boolean requireEnumBase) {
		this.requireEnumBase = requireEnumBase;
	}

	private Class<? extends Annotation> getSafeAnnotationClass(Class<? extends Annotation> annotationClass) {
		if (classLoader == null) {
			return annotationClass;
		} else {
			try {
				return Class.forName(annotationClass.getName(), false, classLoader).asSubclass(Annotation.class);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private <T> Class<T> getSafeClass(Class<T> systemClass) {
		if (classLoader == null) {
			return systemClass;
		} else {
			try {
				return (Class<T>) Class.forName(systemClass.getName(), false, classLoader);

			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Lazy getter for the type map that maps from java {@link Type} to {@link ProtoGmType}. It will be lazy initialized with all simple types and the
	 * base type
	 */
	private Map<Type, ProtoGmType> getTypeMap() {
		if (typeMap != null)
			return typeMap;

		/* PGA: right now, this sync is not necessary as we only reach this block when inside #getProtoGmTypeSynced(..) */
		synchronized (typeMapLock) {
			if (typeMap == null) {
				final Map<Type, ProtoGmType> _typeMap = new ConcurrentHashMap<>();

				ProtoGmBaseType gmBaseType = instantiate(eagerProtoGmBaseType);
				_typeMap.put(Object.class, gmBaseType);

				ProtoGmBooleanType booleanType = instantiate(eagerProtoGmBooleanType);
				_typeMap.put(Boolean.class, booleanType);
				_typeMap.put(boolean.class, booleanType);

				ProtoGmIntegerType integerType = instantiate(eagerProtoGmIntegerType);
				_typeMap.put(Integer.class, integerType);
				_typeMap.put(int.class, integerType);

				ProtoGmLongType longType = instantiate(eagerProtoGmLongType);
				_typeMap.put(Long.class, longType);
				_typeMap.put(long.class, longType);

				ProtoGmFloatType floatType = instantiate(eagerProtoGmFloatType);
				_typeMap.put(Float.class, floatType);
				_typeMap.put(float.class, floatType);

				ProtoGmDoubleType doubleType = instantiate(eagerProtoGmDoubleType);
				_typeMap.put(Double.class, doubleType);
				_typeMap.put(double.class, doubleType);

				ProtoGmDecimalType decimalType = instantiate(eagerProtoGmDecimalType);
				_typeMap.put(BigDecimal.class, decimalType);

				ProtoGmStringType stringType = instantiate(eagerProtoGmStringType);
				_typeMap.put(String.class, stringType);

				ProtoGmDateType dateType = instantiate(eagerProtoGmDateType);
				_typeMap.put(Date.class, dateType);

				typeMap = _typeMap;
			}
		}

		return typeMap;
	}

	public GmType getGmTypeUnchecked(Type type) {
		try {
			return getGmType(type);
		} catch (JavaTypeAnalysisException e) {
			throw Exceptions.unchecked(e, "Error while analyzing type: " + type.getTypeName());
		}
	}

	/** This method cannot be invoked recursively!!! */
	public GmType getGmType(Type type) throws JavaTypeAnalysisException {
		try {
			return (GmType) getProtoGmType(type);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while analyzing " + type);
		}
	}

	public ProtoGmType getProtoGmType(Type type) throws JavaTypeAnalysisException { 
		ProtoGmType gmType = typeCache.get(type);
		if (gmType != null)
			return gmType;

		ensureClassObjects();

		ProtoGmType result;
		synchronized (typeMapLock) {
			result = getProtoGmTypeSynced(type);
		}

		typeCache.put(type, result);
		return result;
	}

	private void ensureClassObjects() {
		if (jtaClasses.genericEntityClass != null)
			return;

		jtaClasses.enumBaseClass = getSafeClass(EnumBase.class);
		jtaClasses.evaluatorClass = getSafeClass(Evaluator.class);
		jtaClasses.evalContextClass = getSafeClass(EvalContext.class);
		jtaClasses.abstractAnnotationClass = getSafeAnnotationClass(Abstract.class);
		jtaClasses.transientAnnotationClass = getSafeAnnotationClass(Transient.class);
		jtaClasses.genericEntityClass = getSafeClass(GenericEntity.class);
	}

	/** This method cannot be invoked recursively!!! */
	private ProtoGmType getProtoGmTypeSynced(Type type) throws JavaTypeAnalysisException {
		ProtoGmType result;

		try {
			result = _getProtoGmType(type);

			List<Runnable> localPostProcessors = postProcessors.get();
			if (localPostProcessors != null) {
				for (Runnable postProcessor : localPostProcessors) {
					postProcessor.run();
				}
			}

		} finally {
			postProcessors.remove();
		}

		return result;
	}

	private ProtoGmType _getProtoGmType(Type type) throws JavaTypeAnalysisException {
		ProtoGmType gmType = getCachedGmType(type);
		if (gmType != null)
			return gmType;

		type = sanitizeType(type);

		if (type instanceof ParameterizedType) {
			// collections
			ParameterizedType parameterizedType = (ParameterizedType) type;

			Type rawType = parameterizedType.getRawType();

			Class<?> rawClass = (Class<?>) rawType;

			if (rawClass == Set.class) {
				return registerProtoGmType(parameterizedType, setTypeBuilder);
			} else if (rawClass == List.class) {
				return registerProtoGmType(parameterizedType, listTypeBuilder);
			} else if (rawClass == Map.class) {
				return registerProtoGmType(parameterizedType, mapTypeBuilder);
			}

		} else if (type instanceof Class<?>) {
			// entity or enum
			Class<?> candidateClass = (Class<?>) type;
			if (isAssignableToGenericEntity(candidateClass)) {
				Class<? extends GenericEntity> entityClass = (Class<? extends GenericEntity>) candidateClass;
				return registerProtoGmType(entityClass, entityTypeBuilder);

			} else if (candidateClass.isEnum()) {
				Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) candidateClass;
				return registerProtoGmType(enumClass, enumTypeBuilder);
			}
		}

		throw new JavaTypeAnalysisException("unsupported java type " + type);
	}

	/* package */ static Type sanitizeType(Type type) throws JavaTypeAnalysisException {
		if (!(type instanceof TypeVariable<?>)) {
			return type;
		}
		// Any type, but getter has an auto-cast. Examples:
		// <T> T getId() or <T extends Manipulation> M getManipulation()
		TypeVariable<?> typeVar = (TypeVariable<?>) type;
		Type[] bounds = typeVar.getBounds();

		if (bounds.length > 1)
			throw new JavaTypeAnalysisException("unsupported java type variables bounds " + Arrays.asList(bounds));

		return bounds[0];
	}

	private boolean isAssignableToGenericEntity(Class<?> candidateClass) {
		return jtaClasses.genericEntityClass.isAssignableFrom(candidateClass);
	}

	private <T extends Type, G extends ProtoGmType> G registerProtoGmType(T type, ProtoGmTypeBuilder<T, G> gmTypeBuilder)
			throws JavaTypeAnalysisException {

		G gmType = (G) getCachedGmType(type);
		if (gmType != null)
			return gmType;

		gmType = (G) typeMap.get(type);
		if (gmType == null) {
			/* Ensuring superTypes might trigger ensuring our type, if it is reachable via properties. */
			gmTypeBuilder.ensureSuperTypes(type);
			gmType = (G) typeMap.get(type);
		}
		if (gmType == null) {
			gmTypeBuilder.ensureSuperTypes(type);
			gmType = gmTypeBuilder.buildWirableInstance(type);
			typeMap.put(type, gmType);
			gmTypeBuilder.completeWiredInstance(type, gmType);
		}

		return gmType;
	}

	private ProtoGmType getCachedGmType(Type type) {
		return getTypeMap().get(type);
	}

	public abstract class ProtoGmTypeBuilder<T extends Type, G extends ProtoGmType> {

		/**
		 * See override in {@link JavaTypeAnalysis#entityTypeBuilder}
		 */
		@SuppressWarnings("unused")
		public void ensureSuperTypes(T type) throws JavaTypeAnalysisException {
			// do nothing
		}

		public abstract G buildWirableInstance(T type) throws JavaTypeAnalysisException;

		@SuppressWarnings("unused")
		public void completeWiredInstance(T type, G instance) throws JavaTypeAnalysisException {
			// empty
		}
	}

	private final ProtoGmTypeBuilder<Class<? extends GenericEntity>, ProtoGmEntityType> entityTypeBuilder = new ProtoGmTypeBuilder<Class<? extends GenericEntity>, ProtoGmEntityType>() {

		private final Map<String, BeanPropertyScan> scans = newMap();

		/** We must ensure super-types here to avoid a cycle. */
		@Override
		public void ensureSuperTypes(Class<? extends GenericEntity> type) throws JavaTypeAnalysisException {
			for (Class<? extends GenericEntity> superType : getSuperTypes(type)) {
				_getProtoGmType(superType);
			}
		}

		@Override
		public ProtoGmEntityType buildWirableInstance(Class<? extends GenericEntity> type) throws JavaTypeAnalysisException {
			ProtoGmEntityType gmEntityType = instantiate(eagerProtoGmEntityType);
			gmEntityType.setGlobalId(resolveGlobalId(type));
			gmEntityType.setTypeSignature(type.getName());
			gmEntityType.setIsAbstract(type.isAnnotationPresent(jtaClasses.abstractAnnotationClass));

			setSuperTypes(type, gmEntityType);
			prepareProperties(type, gmEntityType);

			return gmEntityType;
		}

		private void setSuperTypes(Class<? extends GenericEntity> type, ProtoGmEntityType gmEntityType) throws JavaTypeAnalysisException {
			List<Class<? extends GenericEntity>> superTypes = getSuperTypes(type);
			List<ProtoGmEntityType> gmSuperTypes = newList();

			for (Type superType : superTypes) {
				ProtoGmEntityType gmSuperType = (ProtoGmEntityType) _getProtoGmType(superType);
				gmSuperTypes.add(gmSuperType);
			}

			eagerProtoGmEntityType.setSuperTypes(gmEntityType, gmSuperTypes, proto);
		}

		private void prepareProperties(Class<? extends GenericEntity> type, ProtoGmEntityType gmEntityType) throws JavaTypeAnalysisException {
			BeanPropertyScan propertyScan = new BeanPropertyScan(type, jtaClasses, requireEnumBase);
			List<ProtoGmProperty> properties = newList();
			List<ProtoGmPropertyOverride> propertyOverrides = newList();

			for (ScannedProperty scannedProperty : propertyScan.getScannedProperties()) {
				scannedProperty.validate(jtaClasses);

				String propertyName = scannedProperty.propertyName;

				ProtoGmProperty superProperty = findSuperProperty(gmEntityType, propertyName);

				if (superProperty != null) {
					ProtoGmPropertyOverride gmPropertyOverride = instantiate(eagerProtoGmPropertyOverride);
					gmPropertyOverride.setGlobalId(resolvePropertyOverrideGlobalId(scannedProperty));
					eagerProtoGmPropertyOverride.setDeclaringTypeInfo(gmPropertyOverride, gmEntityType, proto);
					eagerProtoGmPropertyOverride.setProperty(gmPropertyOverride, superProperty, proto);
					gmPropertyOverride.setInitializer(getInitializer(scannedProperty, gmPropertyOverride));

					propertyOverrides.add(gmPropertyOverride);
					propertyScan.gmPropertyInfos.put(propertyName, gmPropertyOverride);

				} else {
					ProtoGmProperty gmProperty = instantiate(eagerProtoGmProperty);
					gmProperty.setGlobalId(resolvePropertyGlobalId(scannedProperty));
					eagerProtoGmProperty.setDeclaringType(gmProperty, gmEntityType, proto);
					gmProperty.setName(propertyName);
					// gmProperty.setType(_getProtoGmType(setterType));
					// gmProperty.setTypeRestriction(getProtoGmTypeRestriction(gmProperty, scannedProperty));
					gmProperty.setNullable(!scannedProperty.getPropertyRawType().isPrimitive());
					gmProperty.setInitializer(getInitializer(scannedProperty, gmProperty));

					properties.add(gmProperty);
					propertyScan.gmPropertyInfos.put(propertyName, gmProperty);
				}

			}

			eagerProtoGmEntityType.setProperties(gmEntityType, properties, proto);
			eagerProtoGmEntityType.setPropertyOverrides(gmEntityType, propertyOverrides, proto);

			scans.put(gmEntityType.getTypeSignature(), propertyScan);
		}

		private ProtoGmProperty findSuperProperty(ProtoGmEntityType gmEntityType, String propertyName) {
			for (ProtoGmEntityType superType : gmEntityType.getSuperTypes()) {
				ProtoGmPropertyInfo info = scans.get(superType.getTypeSignature()).gmPropertyInfos.get(propertyName);
				if (info != null)
					return info.relatedProperty();

				ProtoGmProperty property = findSuperProperty(superType, propertyName);
				if (property != null)
					return property;
			}

			return null;
		}

		@Override
		public void completeWiredInstance(Class<? extends GenericEntity> type, ProtoGmEntityType instance) throws JavaTypeAnalysisException {
			BeanPropertyScan propertyScan = scans.get(instance.getTypeSignature());

			for (ScannedProperty scannedProperty : propertyScan.getScannedProperties()) {
				Type propertyType = scannedProperty.getPropertyType();

				ProtoGmPropertyInfo gmPropertyInfo = propertyScan.gmPropertyInfos.get(scannedProperty.propertyName);
				if (gmPropertyInfo instanceof ProtoGmProperty) {
					ProtoGmProperty gmProperty = (ProtoGmProperty) gmPropertyInfo;
					eagerProtoGmProperty.setType(gmProperty, _getProtoGmType(propertyType), proto);
					eagerProtoGmProperty.setTypeRestriction(gmProperty, getProtoGmTypeRestriction(gmProperty, scannedProperty), proto);
				}

				analyzeMetaDataAnnotations(scannedProperty.getter.getAnnotations(), new MetaDataAnnotationAnalyzerContextImpl(gmPropertyInfo));
			}

			if (propertyScan.getEvalType() != null)
				eagerProtoGmEntityType.setEvaluatesTo(instance, _getProtoGmType(propertyScan.getEvalType()), proto);

			analyzeMetaDataAnnotations(type.getAnnotations(), new MetaDataAnnotationAnalyzerContextImpl(instance));
		}

		private ProtoGmTypeRestriction getProtoGmTypeRestriction(ProtoGmProperty gmProperty, ScannedProperty scannedProperty)
				throws JavaTypeAnalysisException {
			// This does not really work with TR from given class-loader, as we need to access the methods of TR
			TypeRestriction annotation = scannedProperty.getter.getAnnotation(TypeRestriction.class);
			if (annotation == null)
				return null;

			Class<?>[] values = annotation.value();
			Class<?>[] keys = annotation.key();

			boolean hasKeys = keys.length > 0;

			if (hasKeys && values.length != keys.length) {
				throw new JavaTypeAnalysisException("Number of values and keys is not the same for: " + gmProperty);
			}

			ProtoGmTypeRestriction result = instantiate(eagerProtoGmTypeRestriction);
			result.setGlobalId(resolveTypeRestrictionGlobalId(annotation, gmProperty));
			result.setAllowVd(annotation.allowVd());
			result.setAllowKeyVd(annotation.allowKeyVd());

			eagerProtoGmTypeRestriction.setTypes(result, toProtoGmTypes(values), proto);
			eagerProtoGmTypeRestriction.setKeyTypes(result, toProtoGmTypes(keys), proto);

			return result;
		}

		private String resolveTypeRestrictionGlobalId(TypeRestriction annotation, ProtoGmProperty gmProperty) {
			String globalId = annotation.globalId();
			if (StringTools.isEmpty(globalId)) {
				globalId = "typeRestriction:" + gmProperty.getGlobalId();
			}
			return globalId;
		}

		private List<ProtoGmType> toProtoGmTypes(Class<?>[] classes) throws JavaTypeAnalysisException {
			List<ProtoGmType> result = newList();
			for (int i = 0; i < classes.length; i++) {
				result.add(_getProtoGmType(classes[i]));
			}
			return result;
		}

		private Object getInitializer(ScannedProperty scannedProperty, ProtoGmPropertyInfo gmPropertyInfo) {
			EnumHint[] enumHints = resolveEnumHints(scannedProperty);
			Object parsedValue = InitializerTools.parseInitializer(scannedProperty.getInitializerString(), EnumParsingMode.enumAsReference,
					enumHints);
			if (parsedValue instanceof ValueDescriptor)
				((ValueDescriptor) parsedValue).setGlobalId("initializer:" + gmPropertyInfo.getGlobalId());

			return parsedValue;
		}

		private EnumHint[] resolveEnumHints(ScannedProperty scannedProperty) {
			EnumHint[] result = resolveEnumHintsHelper(scannedProperty);
			return result == null || (result[0] == null && result[1] == null) ? null : result;
		}

		private EnumHint[] resolveEnumHintsHelper(ScannedProperty scannedProperty) {
			Class<?> rawType = scannedProperty.getPropertyRawType();

			if (rawType.isEnum())
				return new EnumHint[] { null, newEnumHint(rawType) };

			Type pt = scannedProperty.getPropertyType();

			if (rawType == List.class || rawType == Set.class)
				return new EnumHint[] { null, getEnumHintForTypeArgument(pt, 0) };

			if (rawType == Map.class)
				return new EnumHint[] { getEnumHintForTypeArgument(pt, 0), getEnumHintForTypeArgument(pt, 1) };

			return null;
		}

		private EnumHint getEnumHintForTypeArgument(Type genericType, int index) {
			if (!(genericType instanceof ParameterizedType))
				return null;

			ParameterizedType pt = (ParameterizedType) genericType;
			Type paramType = pt.getActualTypeArguments()[index];

			if (!(paramType instanceof Class<?>))
				return null;

			Class<?> clazz = (Class<?>) paramType;

			return newEnumHint(clazz);
		}

		private EnumHint newEnumHint(Class<?> rawType) {
			return rawType.isEnum() ? new EnumHint(rawType.getName(), getConstantNames(rawType)) : null;
		}

	};

	private class MetaDataAnnotationAnalyzerContextImpl implements MdaAnalysisContext {

		private final ProtoHasMetaData target;

		public MetaDataAnnotationAnalyzerContextImpl(ProtoHasMetaData target) {
			this.target = target;
		}

		@Override
		public ProtoHasMetaData getTarget() {
			return target;
		}

		@Override
		public boolean isProto() {
			return proto;
		}

		@Override
		public ProtoGmType getGmType(Type type) {
			try {
				return JavaTypeAnalysis.this._getProtoGmType(type);
			} catch (JavaTypeAnalysisException e) {
				throw new IllegalArgumentException("Cannot find GmType for: " + type, e);
			}
		}

		@Override
		public void addPostProcessor(Runnable postProcessor) {
			List<Runnable> localPostProcessors = postProcessors.get();
			if (localPostProcessors == null) {
				localPostProcessors = newList();
				postProcessors.set(localPostProcessors);
			}
			localPostProcessors.add(postProcessor);
		}

	}

	private List<Class<? extends GenericEntity>> getSuperTypes(Class<? extends GenericEntity> entityClass) {
		return Stream.of(entityClass.getInterfaces())//
				.filter(this::isAssignableToGenericEntity) //
				.map(c -> (Class<? extends GenericEntity>) c) //
				.collect(Collectors.toList());
	}

	private final ProtoGmTypeBuilder<Class<? extends Enum<?>>, ProtoGmEnumType> enumTypeBuilder = new ProtoGmTypeBuilder<Class<? extends Enum<?>>, ProtoGmEnumType>() {
		@Override
		public ProtoGmEnumType buildWirableInstance(Class<? extends Enum<?>> type) throws JavaTypeAnalysisException {
			ProtoGmEnumType gmEnumType = instantiate(eagerProtoGmEnumType);
			gmEnumType.setGlobalId(resolveGlobalId(type));
			gmEnumType.setTypeSignature(type.getName());

			Set<String> constantNames = getConstantNames(type);

			List<ProtoGmEnumConstant> constants = new ArrayList<>(constantNames.size());

			for (Field field : type.getFields()) {
				// Other fields, like the T literal, are not constantNames and have to be skipped
				if (!constantNames.contains(field.getName()))
					continue;

				ProtoGmEnumConstant constant = instantiate(eagerProtoGmEnumConstant);
				constant.setGlobalId(resolveEnumConstantGlobalId(field));
				eagerProtoGmEnumConstant.setDeclaringType(constant, gmEnumType, proto);
				constant.setName(field.getName());
				constants.add(constant);

				analyzeMetaDataAnnotations(field.getAnnotations(), new MetaDataAnnotationAnalyzerContextImpl(constant));
			}

			eagerProtoGmEnumType.setConstatns(gmEnumType, constants, proto);

			analyzeMetaDataAnnotations(type.getAnnotations(), new MetaDataAnnotationAnalyzerContextImpl(gmEnumType));

			return gmEnumType;
		}

	};

	// We avoid calling type.getEnumConstants() as that triggers class initialization, which is sometimes not desired
	private static Set<String> getConstantNames(Class<?> enumClass) {
		return Stream.of(enumClass.getFields()) //
				.filter(Field::isEnumConstant) //
				.map(Field::getName) //
				.collect(Collectors.toSet());
	}

	private abstract class ProtoGmCollectionTypeBuilder<C extends ProtoGmCollectionType> extends ProtoGmTypeBuilder<ParameterizedType, C> {
		@Override
		public final void completeWiredInstance(ParameterizedType type, C instance) throws JavaTypeAnalysisException {
			Type[] types = type.getActualTypeArguments();
			ProtoGmType gmTypes[] = new ProtoGmType[types.length];

			StringJoiner typeSignatureBuilder = new StringJoiner(",", getSimpleTypeName() + "<", ">");
			for (int i = 0; i < types.length; i++) {
				ProtoGmType gmType = _getProtoGmType(types[i]);
				gmTypes[i] = gmType;

				typeSignatureBuilder.add(gmType.getTypeSignature());
			}

			String typeSignature = typeSignatureBuilder.toString();
			instance.setTypeSignature(typeSignature);
			instance.setGlobalId(typeGlobalId(typeSignature));

			completeWiredProtoGmCollectionTypeInstance(gmTypes, instance);
		}

		protected abstract String getSimpleTypeName();

		/**
		 * @param gmTypes
		 *            collection parameter types
		 * @param instance
		 *            actual {@link ProtoGmCollectionType} instance
		 */
		protected abstract void completeWiredProtoGmCollectionTypeInstance(ProtoGmType gmTypes[], C instance);
	}

	private abstract class ProtoGmLinearCollectionTypeBuilder<C extends ProtoGmLinearCollectionType> extends ProtoGmCollectionTypeBuilder<C> {
		// empty
	}

	private final ProtoGmTypeBuilder<ParameterizedType, ProtoGmSetType> setTypeBuilder = new ProtoGmLinearCollectionTypeBuilder<ProtoGmSetType>() {
		@Override
		public ProtoGmSetType buildWirableInstance(ParameterizedType type) throws JavaTypeAnalysisException {
			return instantiate(eagerProtoGmSetType);
		}

		@Override
		protected String getSimpleTypeName() {
			return "set";
		}

		@Override
		public void completeWiredProtoGmCollectionTypeInstance(ProtoGmType gmTypes[], ProtoGmSetType instance) {
			eagerProtoGmSetType.setElementType(instance, gmTypes[0], proto);
		}
	};

	private final ProtoGmTypeBuilder<ParameterizedType, ProtoGmListType> listTypeBuilder = new ProtoGmLinearCollectionTypeBuilder<ProtoGmListType>() {
		@Override
		public ProtoGmListType buildWirableInstance(ParameterizedType type) throws JavaTypeAnalysisException {
			return instantiate(eagerProtoGmListType);
		}

		@Override
		protected String getSimpleTypeName() {
			return "list";
		}

		@Override
		public void completeWiredProtoGmCollectionTypeInstance(ProtoGmType gmTypes[], ProtoGmListType instance) {
			eagerProtoGmListType.setElementType(instance, gmTypes[0], proto);
		}
	};

	private final ProtoGmTypeBuilder<ParameterizedType, ProtoGmMapType> mapTypeBuilder = new ProtoGmCollectionTypeBuilder<ProtoGmMapType>() {
		@Override
		public ProtoGmMapType buildWirableInstance(ParameterizedType type) throws JavaTypeAnalysisException {
			return instantiate(eagerProtoGmMapType);
		}

		@Override
		protected String getSimpleTypeName() {
			return "map";
		}

		@Override
		public void completeWiredProtoGmCollectionTypeInstance(ProtoGmType gmTypes[], ProtoGmMapType instance) {
			eagerProtoGmMapType.setKeyType(instance, gmTypes[0], proto);
			eagerProtoGmMapType.setValueType(instance, gmTypes[1], proto);
		}
	};

	private <T extends ProtoGmModelElement, E extends GenericEntity> T instantiate(EagerType<T, E> et) {
		try {
			return proto ? et.createPseudo() : (T) (enhanced ? et.entityType().create() : et.entityType().createPlain());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String resolveGlobalId(Class<?> entityOrEnumType) {
		GlobalId globalId = entityOrEnumType.getAnnotation(GlobalId.class);
		return globalId != null ? globalId.value() : typeGlobalId(entityOrEnumType.getName());
	}

	/**
	 * Returns the standard globalId to be assigned to the {@link GmEntityType} corresponding to given typeSignature.
	 * <p>
	 * WARNING: Do not use this for querying!!! The actual globalId might be different, e.g. if {@link GlobalId} annotation was used. If possible, use
	 * {@link #resolveGlobalId(Class)}, otherwise make a query by typeSignature.
	 */
	public static String typeGlobalId(String typeSignature) {
		return GmModels.typeGlobalId(typeSignature);
	}

	private static String resolveEnumConstantGlobalId(Field enumConstantField) {
		GlobalId globalId = enumConstantField.getAnnotation(GlobalId.class);
		return globalId != null ? globalId.value() : constantGlobalId(enumConstantField.getDeclaringClass().getName(), enumConstantField.getName());
	}

	public static String constantGlobalId(String typeSignature, String constantName) {
		return GmModels.constantGlobalId(typeSignature, constantName);
	}

	private static String resolvePropertyGlobalId(ScannedProperty scannedProperty) {
		GlobalId globalId = scannedProperty.getter.getAnnotation(GlobalId.class);
		return globalId != null ? globalId.value() : propertyGlobalId(scannedProperty.entityClass.getName(), scannedProperty.propertyName);
	}

	public static String propertyGlobalId(String typeSignature, String propertyName) {
		return GmModels.propertyGlobalId(typeSignature, propertyName);
	}

	private static String resolvePropertyOverrideGlobalId(ScannedProperty scannedProperty) {
		GlobalId globalId = scannedProperty.getter.getAnnotation(GlobalId.class);
		return globalId != null ? globalId.value() : propertyOverrideGlobalId(scannedProperty.entityClass.getName(), scannedProperty.propertyName);
	}

	public static String propertyOverrideGlobalId(String typeSignature, String propertyName) {
		return GmModels.propertyOverrideGlobalId(typeSignature, propertyName);
	}

}
