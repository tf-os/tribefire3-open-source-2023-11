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
package com.braintribe.model.processing.itw.synthesis.gm;

import static com.braintribe.model.processing.itw.synthesis.gm.GmtsHelper.getEntityTypeName;
import static com.braintribe.model.processing.itw.synthesis.gm.GmtsHelper.getPlainClassName;
import static com.braintribe.model.processing.itw.synthesis.gm.GmtsHelper.getPropertyClassName;
import static com.braintribe.model.processing.itw.synthesis.gm.GmtsHelper.getWeakIfaceName;
import static com.braintribe.model.processing.itw.synthesis.gm.GmtsHelper.toArrayOrNull;
import static com.braintribe.model.processing.itw.tools.ItwTools.cast;
import static com.braintribe.model.processing.itw.tools.ItwTools.extractStaticValue;
import static com.braintribe.model.processing.itw.tools.ItwTools.getAnnotation;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static java.util.Collections.emptyMap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.annotation.Transient;
import com.braintribe.model.generic.reflection.AbstractProperty;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityInitializer;
import com.braintribe.model.generic.reflection.EntityInitializerImpl;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GmtsTransientProperty;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TransientProperty;
import com.braintribe.model.generic.reflection.type.custom.AbstractEntityType;
import com.braintribe.model.generic.value.NullDescriptor;
import com.braintribe.model.processing.itw.analysis.BeanPropertyScan;
import com.braintribe.model.processing.itw.analysis.protomodel.ProtoGmTypeImpl;
import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmClassPool;
import com.braintribe.model.processing.itw.asm.AsmExistingClass;
import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.asm.ClassBuilder;
import com.braintribe.model.processing.itw.asm.InterfaceBuilder;
import com.braintribe.model.processing.itw.synthesis.gm.asm.DefaultMethodsSupport;
import com.braintribe.model.processing.itw.synthesis.gm.asm.EnhancedEntityImplementer;
import com.braintribe.model.processing.itw.synthesis.gm.asm.EntityTypeImplementer;
import com.braintribe.model.processing.itw.synthesis.gm.asm.GmClassPool;
import com.braintribe.model.processing.itw.synthesis.gm.asm.PlainEntityImplementer;
import com.braintribe.model.processing.itw.synthesis.gm.asm.PropertyImplementer;
import com.braintribe.model.processing.itw.synthesis.gm.experts.AccessorFactory;
import com.braintribe.model.processing.itw.synthesis.java.JavaTypeSynthesis;
import com.braintribe.model.processing.itw.synthesis.java.JavaTypeSynthesisException;
import com.braintribe.model.processing.itw.synthesis.java.PropertyAnalysis.PropertyDescription;
import com.braintribe.model.processing.itw.synthesis.java.TmpJtsTracker;
import com.braintribe.model.processing.itw.tools.ItwTools;
import com.braintribe.model.weaving.ProtoGmCollectionType;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmEnumType;
import com.braintribe.model.weaving.ProtoGmLinearCollectionType;
import com.braintribe.model.weaving.ProtoGmListType;
import com.braintribe.model.weaving.ProtoGmMapType;
import com.braintribe.model.weaving.ProtoGmMetaModel;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.ProtoGmSetType;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.info.ProtoGmPropertyInfo;
import com.braintribe.processing.async.api.AsyncCallback;

/* NOTE regarding "ensure" method names. Some have regular names, some other start with an underscore. Those with
 * normal names are public methods that also do validation, underscore methods are private and do not require
 * validation. */
public class GenericModelTypeSynthesis extends JavaTypeSynthesis {

	private static final GenericModelTypeSynthesis instance = new GenericModelTypeSynthesis();

	private final GmClassPool gcp;
	private final Map<String, PreliminaryEntityType> newEntityTypes = newMap();
	private final Map<String, Map<String, AsmClass>> transientPropertiesForType = newMap();

	private final Stack<ProtoGmEntityType> entityCreationStack = new Stack<>();

	private final DefaultMethodsSupport defaultMethodsSupport = new DefaultMethodsSupport();

	private static final Logger log = Logger.getLogger(GenericModelTypeSynthesis.class);

	private GenericModelTypeSynthesis() {
		super();
		gcp = new GmClassPool(asmClassPool);
	}

	/**
	 * Why this method? So that you read this and realize you probably don't want to create your own {@link GenericModelTypeSynthesis} to ensure your
	 * model types, you just want to call {@link ProtoGmMetaModel#deploy()}.
	 * 
	 * But why? Well, it's really a bad idea. There are blocks that are synchronized and the lock is only valid for one instance (as it is an instance
	 * variable). For now, I'm not making it static, to avoid unpredictable errors, so I'm moving to this method to ensure nobody calls the
	 * constructor, unless he knows what he's doing.
	 */
	public static GenericModelTypeSynthesis newInstance() {
		return new GenericModelTypeSynthesis();
	}

	public static GenericModelTypeSynthesis standardInstance() {
		return instance;
	}

	public void ensureModelTypes(ProtoGmMetaModel gmModel) throws GenericModelTypeSynthesisException {
		GmtsMetaModelValidator.validate(gmModel);

		try {
			_tryEnsuringModelTypes(gmModel, newSet());

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException(
					"Error while ensuring types for model with name: " + (gmModel != null ? gmModel.getName() : "null"), e);
		}
	}

	public void ensureModelTypes(ProtoGmMetaModel gmModel, AsyncCallback<Void> asyncCallback) {
		try {
			ensureModelTypes(gmModel);
			asyncCallback.onSuccess(null);

		} catch (GenericModelTypeSynthesisException e) {
			asyncCallback.onFailure(e);
		}
	}

	private void _tryEnsuringModelTypes(ProtoGmMetaModel gmModel, Set<ProtoGmMetaModel> ensuredModels) throws GenericModelTypeSynthesisException {
		if (!ensuredModels.add(gmModel))
			return;

		for (ProtoGmMetaModel dependency : nullSafe(gmModel.getDependencies()))
			_tryEnsuringModelTypes(dependency, ensuredModels);

		Set<? extends ProtoGmType> types = gmModel.getTypes();

		/* IMPORTANT: We have to ensure enums first, because the initializers (default values) for enum properties of entities might have these enum
		 * constants set. So they have to be accessible via reflection by the initializerString parser */

		// ensure enum types to exist and being reflectable
		List<ProtoGmType> enumTypes = types.stream() //
				.filter(gmType -> gmType instanceof ProtoGmEnumType) //
				.collect(Collectors.toList());

		for (ProtoGmType enumType : enumTypes)
			_ensureEnumType((ProtoGmEnumType) enumType);

		// ensure entity types to exist and being reflectable
		List<ProtoGmType> entityTypes = types.stream() //
				.filter(gmType -> gmType instanceof ProtoGmEntityType) //
				.collect(Collectors.toList());

		for (ProtoGmType entityType : entityTypes)
			_ensureEntityType((ProtoGmEntityType) entityType);
	}

	private final Object ENUM_TYPE_LOCK = new Object();

	/** Returns a fully-initialized {@link EnumType} which corresponds to given {@link ProtoGmEnumType}; */
	private EnumType _ensureEnumType(ProtoGmEnumType gmEnumType) throws GenericModelTypeSynthesisException {
		String typeSignature = gmEnumType.getTypeSignature();

		try {
			EnumType enumType = (EnumType) typeReflection.getDeployedType(typeSignature);
			if (enumType != null)
				return enumType;

			GmtsMetaModelValidator.validate(gmEnumType);

			/* we are synchronizing for all equal strings, so creating two different enums at the concurrently is possible */
			synchronized (ENUM_TYPE_LOCK) {
				enumType = (EnumType) typeReflection.getDeployedType(typeSignature);
				if (enumType != null)
					return enumType;

				AsmClass enumAsmClass = ensureEnumClass(gmEnumType);
				Class<? extends Enum<?>> enumClass = getJavaClass(enumAsmClass);

				return typeReflection.deployEnumType(enumClass);
			}

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException("error while creating enum " + typeSignature, e);
		}
	}

	/* This method should not be called recursively as it would be doing unnecessary extra validation */
	/** Returns a fully-initialized {@link GenericModelType} which corresponds to given {@link ProtoGmType}; */
	public GenericModelType ensureType(ProtoGmType gmType) throws GenericModelTypeSynthesisException {
		GmtsMetaModelValidator.validate(gmType);
		return _ensureType(gmType);

	}

	private GenericModelType _ensureType(ProtoGmType gmType) throws GenericModelTypeSynthesisException {
		// first check custom type that must be created on demand
		if (gmType instanceof ProtoGmCollectionType)
			return _ensureCollectionType((ProtoGmCollectionType) gmType);

		else if (gmType instanceof ProtoGmEntityType)
			return _ensureEntityType((ProtoGmEntityType) gmType);

		else if (gmType instanceof ProtoGmEnumType)
			return _ensureEnumType((ProtoGmEnumType) gmType);

		else
			// any other type should be already present in the type reflection
			return typeReflection.getType(gmType.getTypeSignature());
	}

	private CollectionType _ensureCollectionType(ProtoGmCollectionType gmCollectionType) throws GenericModelTypeSynthesisException {
		if (gmCollectionType instanceof ProtoGmSetType) {
			ProtoGmSetType gmSetType = (ProtoGmSetType) gmCollectionType;
			GenericModelType elementType = _ensureType(gmSetType.getElementType());
			return typeReflection.getCollectionType(Set.class, new GenericModelType[] { elementType });

		} else if (gmCollectionType instanceof ProtoGmListType) {
			ProtoGmListType gmListType = (ProtoGmListType) gmCollectionType;
			GenericModelType elementType = _ensureType(gmListType.getElementType());
			return typeReflection.getCollectionType(List.class, new GenericModelType[] { elementType });

		} else if (gmCollectionType instanceof ProtoGmMapType) {
			ProtoGmMapType gmMapType = (ProtoGmMapType) gmCollectionType;
			GenericModelType keyType = _ensureType(gmMapType.getKeyType());
			GenericModelType valueType = _ensureType(gmMapType.getValueType());
			return typeReflection.getCollectionType(Map.class, new GenericModelType[] { keyType, valueType });

		} else {
			throw new UnsupportedOperationException("unsupported collection type " + gmCollectionType.getTypeSignature());
		}
	}

	private final Object ENTITY_TYPE_LOCK = new Object();

	private Throwable throwable;

	public <T extends GenericEntity> EntityType<T> ensureEntityType(ProtoGmEntityType gmEntityType) throws GenericModelTypeSynthesisException {
		GmtsMetaModelValidator.validate(gmEntityType);
		return _ensureEntityType(gmEntityType);
	}

	private <T extends GenericEntity> EntityType<T> _ensureEntityType(ProtoGmEntityType gmEntityType) throws GenericModelTypeSynthesisException {
		String entityTypeName = gmEntityType.getTypeSignature();
		EntityType<T> entityType = typeReflection.getDeployedType(entityTypeName);

		if (entityType != null) {
			ensureEntityClassIfClassStorageConfigured(gmEntityType);
			return entityType;
		}

		synchronized (ENTITY_TYPE_LOCK) {
			try {
				TmpJtsTracker.INSTANCE.checkCreatinoStackIsEmpty(entityCreationStack, gmEntityType, throwable);

				return cast(ensureEntityTypeHelper(gmEntityType));

			} catch (ClassCastException e) {
				return TmpJtsTracker.INSTANCE.<EntityType<T>> handleClassCastException(e);

			} catch (Throwable t) {
				if (throwable == null)
					throwable = t;
				throw t;
			}
		}
	}

	/**
	 * This is actually a bug-fix. People might call ensureEntityType with something else in mind - forcing the changed {@link ProtoGmEntityType}
	 * bytecode to be stored. Therefore, in case this possibility exists, we make sure bytecode is generated!
	 */
	private void ensureEntityClassIfClassStorageConfigured(ProtoGmEntityType gmEntityType) {
		try {
			if (asmClassPool.hasClassStorageManager())
				ensureEntityIface(gmEntityType);

		} catch (JavaTypeSynthesisException e) {
			log.error("Something bad happend while creating entity class for the purpose of storing the bytecode."
					+ " No exception will be thrown now, as this does necessaryli mean the system cannot work correctly now.", e);
		}
	}

	// #############################################
	// ## . . . . . Ensuring EntityType . . . . . ##
	// #############################################

	private ItwEntityType ensureEntityTypeHelper(ProtoGmEntityType gmEntityType) throws GenericModelTypeSynthesisException {
		String typeSignature = gmEntityType.getTypeSignature(); // Do not inline! Makes debugging easier ;)

		ItwEntityType entityType = findExistingItwType(typeSignature);
		if (entityType != null)
			return entityType;

		gmEntityType = replaceWithJtaIfRelevant(gmEntityType);

		// There used to be try-finally block, but that is useless, any error and ITW is wasted, so let's just make the code simpler
		entityCreationStack.push(gmEntityType);

		PreliminaryEntityType pet = weaveNewEntityType(gmEntityType);

		entityCreationStack.pop();

		if (entityCreationStack.isEmpty()) {
			finalizeEntitiesRegistration();
			newEntityTypes.clear();
			preliminaryTypes.clear();

			return cast(pet.entityType);
		}

		return cast(pet);
	}

	private ProtoGmEntityType replaceWithJtaIfRelevant(ProtoGmEntityType gmEntityType) {
		if (gmEntityType instanceof ProtoGmTypeImpl)
			return gmEntityType;

		ProtoGmEntityType jtaType = typeReflection.findProtoGmEntityType(gmEntityType.getTypeSignature());

		return jtaType == null ? gmEntityType : jtaType;
	}

	private ItwEntityType findExistingItwType(String typeSignature) {
		ItwEntityType entityType = cast(typeReflection.getDeployedType(typeSignature));

		return entityType != null ? entityType : newEntityTypes.get(typeSignature);
	}

	// #############################################
	// ## . . . . . Creating EntityType . . . . . ##
	// #############################################

	private final Map<String, PreliminaryEntityType> preliminaryTypes = newMap();

	/**
	 * The EntityType is built in two phases - first we start building it thus crating the corresponding {@link AsmNewClass} (in
	 * {@link GenericModelTypeSynthesis#startBuildingEntityType(ProtoGmEntityType)}). We then proceed to build
	 */
	private PreliminaryEntityType weaveNewEntityType(ProtoGmEntityType gmEntityType) throws GenericModelTypeSynthesisException {
		String entityTypeName = gmEntityType.getTypeSignature();

		try {
			// first create incomplete type and register it to allow recursive registration with cyclic type references
			PreliminaryEntityType pet = ensurePet(gmEntityType);
			newEntityTypes.put(entityTypeName, pet);

			pet.superTypes = ensureSuperTypes(gmEntityType);

			for (ProtoGmPropertyInfo[] propertyLineage : pet.mergedProtoGmProperties.values()) {
				ProtoGmProperty gmProperty = propertyLineage[0].relatedProperty();
				ensurePreliminaryType(gmProperty.getType());

				pet.createPreliminaryProperty(gmProperty, propertyLineage);
			}

			if (pet.getDeclaredEvaluatesTo() != null)
				ensurePreliminaryType(pet.getDeclaredEvaluatesTo());

			pet.entityIface = ensureEntityIface(gmEntityType);

			pet.propertyAnalysis = propertyAnalyzer.analyzeProperties(pet.entityIface, pet.mergedProtoGmProperties);
			pet.toStringAnnotation = getAnnotation(pet.entityIface, ToStringInformation.class);
			pet.selectiveInformationAnnotation = getAnnotation(pet.entityIface, SelectiveInformation.class);

			/* The toSelectiveInformation methods (for both plain/enhanced) can only be implemented iff all the properties are at least started, which
			 * only happens after we are done with the entire assembly. So i will only get the implementer here, and finish in that finalize thing.
			 * But the properties can definitely be implemented right away. */
			finalizeWeakInterface(gmEntityType, pet);
			finalizePlainClass(gmEntityType, pet);
			finalizeEnhancedEntityClass(gmEntityType, pet);

			pet.entityType = finalizeEntityTypeImplementation(pet);

			return pet;

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException("error while creating preliminary entity type for " + gmEntityType.getTypeSignature(), e);
		}
	}

	private PreliminaryEntityType ensurePet(ProtoGmEntityType gmEntityType) throws JavaTypeSynthesisException {
		String typeSignature = gmEntityType.getTypeSignature();

		PreliminaryEntityType pet = preliminaryTypes.get(typeSignature);
		if (pet != null)
			return pet;

		if (typeReflection.getDeployedType(typeSignature) != null)
			return null;

		pet = new PreliminaryEntityType(gmEntityType);
		preliminaryTypes.put(typeSignature, pet);

		for (ProtoGmEntityType gmSuperType : nullSafe(gmEntityType.getSuperTypes()))
			ensurePet(gmSuperType);

		pet.implSuperType = findSuperTypeWithMostProperties(gmEntityType);
		pet.implSuperTypeProps = getMergedPropertiesFromTypeHierarchy(pet.implSuperType).keySet();
		pet.mergedProtoGmProperties = getMergedPropertiesFromTypeHierarchy(gmEntityType);

		pet.entityIface = ensureEntityIface(gmEntityType);

		pet.allPreliminaryTransientPropsToImpl = getTransientPropertiesFor(gmEntityType);
		pet.preliminaryTransientPropsToImpl = getTransientPropertiesToImplement(gmEntityType, pet.implSuperType);

		pet.weakInterfaceImplementer = startBuildingWeakInterface(gmEntityType);
		pet.weakInterface = pet.weakInterfaceImplementer.getPreliminaryClass();

		pet.entityTypeImplementer = startBuildingEntityType(gmEntityType);
		pet.entityTypeClass = pet.entityTypeImplementer.getPreliminaryClass();

		pet.plainEntityImplementer = startBuildingPlainClass(gmEntityType, pet);
		pet.plainClass = pet.plainEntityImplementer.getPreliminaryClass();

		pet.enhancedEntityImplementer = startBuildingEnhancedEntityClass(gmEntityType, pet);
		pet.enhancedClass = pet.enhancedEntityImplementer.getPreliminaryClass();

		return pet;
	}

	private Map<String, AsmClass> getTransientPropertiesToImplement(ProtoGmEntityType gmEntityType, ProtoGmEntityType implSuperType) {
		Map<String, AsmClass> transientProperties = getTransientPropertiesFor(gmEntityType);
		Map<String, AsmClass> inheritedTransientProperties = getTransientPropertiesFor(implSuperType);

		Map<String, AsmClass> result = newMap(transientProperties);
		result.keySet().removeAll(inheritedTransientProperties.keySet());

		return result;
	}

	private Map<String, AsmClass> getTransientPropertiesFor(ProtoGmEntityType gmEntityType) {
		if (gmEntityType == null) // superType of GenericEntity
			return emptyMap();
		else
			return transientPropertiesForType.computeIfAbsent(gmEntityType.getTypeSignature(), s -> scanTransientProperties(gmEntityType));
	}

	private Map<String, AsmClass> scanTransientProperties(ProtoGmEntityType gmEntityType) {
		Map<String, AsmClass> result = newMap();

		for (ProtoGmEntityType superType : nullSafe(gmEntityType.getSuperTypes()))
			result.putAll(getTransientPropertiesFor(superType));

		Class<?> javaInteface = ItwTools.findClass(gmEntityType.getTypeSignature());
		if (javaInteface == null)
			return result;

		for (Method method : javaInteface.getDeclaredMethods()) {
			if (!isTransient(method))
				continue;

			Boolean isGetter = BeanPropertyScan.isGetter(method);
			if (Boolean.TRUE != isGetter)
				continue;

			String propertyName = BeanPropertyScan.getPropertyName(method);
			AsmExistingClass propertyType = asmClassPool.get(method.getReturnType());

			result.put(propertyName, propertyType);
		}

		return result;
	}

	private boolean isTransient(Method method) {
		return method.getDeclaredAnnotation(Transient.class) != null;
	}

	private InterfaceBuilder startBuildingWeakInterface(ProtoGmEntityType gmEntityType) throws GenericModelTypeSynthesisException {
		try {
			String weakIfaceName = getWeakIfaceName(gmEntityType);

			List<AsmClass> superIfaces = newList();
			for (ProtoGmEntityType superType : gmEntityType.getSuperTypes()) {
				String superSignature = getWeakIfaceName(superType);
				superIfaces.add(findAsmClass(superSignature));
			}

			return asmClassPool.makeInterface(weakIfaceName, superIfaces);

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException("Error while building weak inteface for " + gmEntityType.getTypeSignature(), e);
		}
	}

	private PlainEntityImplementer startBuildingPlainClass(ProtoGmEntityType gmEntityType, PreliminaryEntityType pet)
			throws GenericModelTypeSynthesisException {

		try {
			String plainClassName = getPlainClassName(gmEntityType);
			String superPlainName = pet.implSuperType != null ? getPlainClassName(pet.implSuperType) : null;
			AsmClass superClass = superPlainName != null ? asmClassPool.get(superPlainName) : gcp.gmtsPlainEntityStubType;

			ClassBuilder classBuilder = makeClass(plainClassName, pet.isAbstract(), superClass, pet.entityIface, pet.weakInterface);
			classBuilder.addDefaultConstructor();

			return new PlainEntityImplementer(classBuilder, gcp);

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException("Error while building plain class for: " + gmEntityType.getTypeSignature(), e);
		}
	}

	private EnhancedEntityImplementer startBuildingEnhancedEntityClass(ProtoGmEntityType gmEntityType, PreliminaryEntityType pet)
			throws GenericModelTypeSynthesisException {

		try {
			String enhancedName = GmtsHelper.getEnhancedClassName(gmEntityType);
			String superEnhancedName = pet.implSuperType != null ? GmtsHelper.getEnhancedClassName(pet.implSuperType) : null;
			AsmClass superClass = superEnhancedName != null ? asmClassPool.get(superEnhancedName) : gcp.gmtsEnhancedEntityStubType;

			ClassBuilder classBuilder = makeClass(enhancedName, pet.isAbstract(), superClass, pet.entityIface, pet.weakInterface,
					gcp.enhancedEntityType);
			classBuilder.addDefaultConstructor();

			return new EnhancedEntityImplementer(classBuilder, gcp);

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException("Error while building enhanced class for: " + gmEntityType.getTypeSignature(), e);
		}
	}

	private void ensurePreliminaryType(ProtoGmType type) throws GenericModelTypeSynthesisException {
		if (type instanceof ProtoGmEntityType) {
			ensureEntityTypeHelper((ProtoGmEntityType) type);

		} else if (type instanceof ProtoGmCollectionType) {
			ensureCollectionTypeHelper((ProtoGmCollectionType) type);

		} else {

			_ensureType(type);
		}
	}

	private void ensureCollectionTypeHelper(ProtoGmCollectionType gmCollectionType) throws GenericModelTypeSynthesisException {
		if (gmCollectionType instanceof ProtoGmLinearCollectionType) {
			ProtoGmLinearCollectionType gmSetType = (ProtoGmLinearCollectionType) gmCollectionType;
			ensurePreliminaryType(gmSetType.getElementType());

		} else if (gmCollectionType instanceof ProtoGmMapType) {
			ProtoGmMapType gmMapType = (ProtoGmMapType) gmCollectionType;
			ensurePreliminaryType(gmMapType.getKeyType());
			ensurePreliminaryType(gmMapType.getValueType());

		} else {
			throw new UnsupportedOperationException("unsupported collection type " + gmCollectionType.getTypeSignature());
		}
	}

	private ProtoGmEntityType findSuperTypeWithMostProperties(ProtoGmEntityType gmEntityType) throws JavaTypeSynthesisException {
		ProtoGmEntityType result = null;
		int maxCount = 0;

		for (ProtoGmEntityType superType : gmEntityType.getSuperTypes()) {
			int propCount = getMergedPropertiesFromTypeHierarchy(superType).size();

			if (propCount > maxCount) {
				propCount = maxCount;
				result = superType;
			}
		}

		return result;
	}

	private List<ItwEntityType> ensureSuperTypes(ProtoGmEntityType gmEntityType) throws JavaTypeSynthesisException {
		List<ItwEntityType> result = newList();

		// ensure super types and double link them
		for (ProtoGmEntityType gmSuperType : nullSafe(gmEntityType.getSuperTypes())) {
			ItwEntityType superType = ensureEntityTypeHelper(gmSuperType);
			result.add(superType);
		}

		return result;
	}

	private EntityTypeImplementer startBuildingEntityType(ProtoGmEntityType gmEntityType) {
		String entityTypeName = getEntityTypeName(gmEntityType);

		ClassBuilder cb = makeClass(entityTypeName, false, gcp.jvmEntityTypeType);
		EntityTypeImplementer entityTypeImplementer = new EntityTypeImplementer(cb, gcp);

		entityTypeImplementer.addConstructor();
		entityTypeImplementer.addStaticSingletonField();
		entityTypeImplementer.addClassInitialization();

		return entityTypeImplementer;
	}

	private void finalizeWeakInterface(ProtoGmEntityType gmEntityType, PreliminaryEntityType pet) throws JavaTypeSynthesisException {
		try {
			InterfaceBuilder ib = pet.weakInterfaceImplementer;

			for (PreliminaryProperty pp : pet.preliminaryProperties.values()) {
				if (pp.isIntroducedAt(gmEntityType)) {
					ib.addAbstractMethod(ItwTools.getReaderName(pp.gmProperty), AsmClassPool.objectType);
					ib.addAbstractMethod(ItwTools.getWriterName(pp.gmProperty), AsmClassPool.voidType, AsmClassPool.objectType);
				}
			}

			ib.build();

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException("Error while building weak inteface for " + gmEntityType.getTypeSignature(), e);
		}
	}

	private AsmClass finalizePlainClass(ProtoGmEntityType gmEntityType, PreliminaryEntityType pet) throws JavaTypeSynthesisException {
		try {
			PlainEntityImplementer pei = pet.plainEntityImplementer;

			// Implement properties
			for (PropertyDescription pd : pet.propertyAnalysis) {
				if (pet.mustImplement(pd.getName())) {
					PreliminaryProperty pp = pet.getPreliminaryProperty(pd.getName());

					pei.createAndStorePropertyField(pd);
					pei.createSetterGetterImplementation(pp, pd);
					pei.addPlainRead(pd);
					pei.addPlainWrite(pd);
				}
			}

			// Transient properties
			for (Entry<String, AsmClass> entry : pet.preliminaryTransientPropsToImpl.entrySet())
				pei.addTransientGetterSetter(entry.getKey(), entry.getValue());

			// Implement defaultMethods
			if (pet.entityIface instanceof AsmExistingClass) {
				defaultMethodsSupport.implementDefaultMethodsIfEligible(pei.getDelegate(), (AsmExistingClass) pet.entityIface);
			}

			// Implement typeReflection methods (EntityType<?> type() method, but implemented twice, due to covariant
			// overriding)
			pei.addType_WithBridge(pet);

			return pei.build();

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException("Error while building plain class for: " + gmEntityType.getTypeSignature(), e);
		}
	}

	private AsmClass finalizeEnhancedEntityClass(ProtoGmEntityType gmEntityType, PreliminaryEntityType pet)
			throws GenericModelTypeSynthesisException {

		try {
			EnhancedEntityImplementer eei = pet.enhancedEntityImplementer;
			eei.addBooleanConstructor();

			List<PropertyDescription> primitiveProps = newList();
			// Implement properties
			for (PropertyDescription pd : pet.propertyAnalysis) {
				String propertyName = pd.getName();
				if (pet.mustImplement(propertyName)) {
					PreliminaryProperty pp = pet.getPreliminaryProperty(propertyName);

					eei.createAndStorePropertyField(pd);
					eei.addAopAroundGetterSetter(pet.getPropertyClassName(propertyName), pp, pd);
					eei.addPlainRead(pd);
					eei.addPlainWrite(pd);

				}

				if (pd.isPrimitive())
					primitiveProps.add(pd);
			}

			// Transient properties
			for (Entry<String, AsmClass> entry : pet.preliminaryTransientPropsToImpl.entrySet())
				eei.addTransientGetterSetter(entry.getKey(), entry.getValue());

			// If primitive fields were added, we have to initialize them
			if (!primitiveProps.isEmpty())
				eei.addInitializePrimitiveFields(primitiveProps);

			// Implement default methods
			if (pet.entityIface instanceof AsmExistingClass)
				defaultMethodsSupport.implementDefaultMethodsIfEligible(eei.getDelegate(), (AsmExistingClass) pet.entityIface);

			// Implement typeReflection methods (EntityType<?> type() method, but implemented twice, due to covariant
			// overriding)
			eei.addType_WithBridge(pet);

			return eei.build();

		} catch (

		Exception e) {
			throw new GenericModelTypeSynthesisException("Error while building enhanced class for: " + gmEntityType.getTypeSignature(), e);
		}
	}

	private <T extends GenericEntity> JvmEntityType<T> finalizeEntityTypeImplementation(PreliminaryEntityType pet)
			throws GenericModelTypeSynthesisException {

		// Finish implementation of EntityType for our entity
		EntityTypeImplementer eti = pet.entityTypeImplementer;
		if (!pet.isAbstract()) {
			eti.addCreatePlainRaw_WithBridge(pet.plainClass);
			eti.addCreateRaw_WithBridge(pet.enhancedClass);
		}
		eti.addIsInstanceMethod(pet.entityIface);
		eti.addToStringMethodIfEligible(this, pet);
		eti.addToSelectiveInformationIfEligible(this, pet);

		// Return reference on the singleton instance of our EntityType via java reflection
		AsmNewClass asmClass = eti.getDelegate().build();
		Class<?> jvmClass = getJavaClass(asmClass);

		JvmEntityType<T> result = extractStaticValue(jvmClass, EntityTypeImplementer.SINGLETON_NAME);
		result.setIsAbstract(pet.isAbstract());
		result.setHasExplicitSelectiveInformation(pet.selectiveInformationAnnotation != null);
		return result;
	}

	private GenericModelType resolveType(ProtoGmType type) throws GenericModelTypeSynthesisException {
		if (type instanceof ProtoGmEntityType) {
			return resolveEntityType((ProtoGmEntityType) type);

		} else if (type instanceof ProtoGmCollectionType) {
			return resolveCollectionType((ProtoGmCollectionType) type);

		} else {
			return _ensureType(type);
		}
	}

	private GenericModelType resolveEntityType(ProtoGmEntityType type) throws GenericModelTypeSynthesisException {
		ItwEntityType et = ensureEntityTypeHelper(type);

		if (et instanceof EntityType)
			return cast(et);
		else
			return ((PreliminaryEntityType) et).entityType;
	}

	private CollectionType resolveCollectionType(ProtoGmCollectionType gmCollectionType) throws GenericModelTypeSynthesisException {
		if (gmCollectionType instanceof ProtoGmSetType) {
			ProtoGmSetType gmSetType = (ProtoGmSetType) gmCollectionType;
			GenericModelType elementType = resolveType(gmSetType.getElementType());
			return typeReflection.getCollectionType(Set.class, new GenericModelType[] { elementType });

		} else if (gmCollectionType instanceof ProtoGmListType) {
			ProtoGmListType gmListType = (ProtoGmListType) gmCollectionType;
			GenericModelType elementType = resolveType(gmListType.getElementType());
			return typeReflection.getCollectionType(List.class, new GenericModelType[] { elementType });

		} else if (gmCollectionType instanceof ProtoGmMapType) {
			ProtoGmMapType gmMapType = (ProtoGmMapType) gmCollectionType;
			GenericModelType keyType = resolveType(gmMapType.getKeyType());
			GenericModelType valueType = resolveType(gmMapType.getValueType());
			return typeReflection.getCollectionType(Map.class, new GenericModelType[] { keyType, valueType });

		} else {
			throw new UnsupportedOperationException("unsupported collection type " + gmCollectionType.getTypeSignature());
		}
	}

	private void finalizeEntitiesRegistration() throws GenericModelTypeSynthesisException {
		/* first publish all new EntityTypes */
		for (PreliminaryEntityType pet : newEntityTypes.values())
			setJavaTypesForEntityType(pet);

		for (PreliminaryEntityType pet : newEntityTypes.values()) {
			EntityType<?> et = finalizeEntityType(pet);
			typeReflection.deployEntityType(et);
		}

		/* now add each new EntityType under his super-type (now it is safe, all EntityTypes are already registered) */
		for (PreliminaryEntityType pet : newEntityTypes.values()) {
			EntityType<?> entityType = pet.entityType;

			for (EntityType<?> superType : entityType.getSuperTypes()) {
				AbstractEntityType<?> castedSuperType = cast(superType);
				castedSuperType.addSubType(entityType);
			}
		}
	}

	// #############################################
	// ## . . . . EntityType finalization . . . . ##
	// #############################################
	private void setJavaTypesForEntityType(PreliminaryEntityType pet) throws GenericModelTypeSynthesisException {
		JvmEntityType<GenericEntity> result = pet.entityType;
		if (result.getJavaType() != null)
			return;

		for (ItwEntityType superType : pet.superTypes) {
			if (superType instanceof PreliminaryEntityType)
				setJavaTypesForEntityType((PreliminaryEntityType) superType);
		}

		// ############################
		// ## . . . JVM classes . . .##
		// ############################

		Class<GenericEntity> javaIface = getJavaClass(pet.entityIface);
		result.setJavaType(javaIface);
		result.setPlainClass(this.<GenericEntity> getJavaClass(pet.plainClass));
		result.setEnhancedClass(this.<GenericEntity> getJavaClass(pet.enhancedClass));

		for (PreliminaryProperty pp : pet.preliminaryProperties.values())
			if (pp.isIntroducedAt(pet.gmEntityType))
				setJavaTypesForDependedEntityTypes(pp.gmProperty.getType());
	}

	private EntityType<?> finalizeEntityType(PreliminaryEntityType pet) throws GenericModelTypeSynthesisException {
		JvmEntityType<GenericEntity> result = pet.entityType;
		if (result.getProperties() != null)
			return result;

		for (ItwEntityType superType : pet.superTypes) {
			EntityType<?> superEntType = finalizeSuperTypeIfNeeded(superType);
			result.getSuperTypes().add(superEntType);
		}

		// #############################################
		// ## . . . properties and initializers . . . ##
		// #############################################

		List<EntityInitializer> initializers = newList();

		Property[] properties = new Property[pet.preliminaryProperties.size()];
		int index = 0;
		for (PreliminaryProperty pp : pet.preliminaryProperties.values()) {
			Property property = pp.findPropertyFromSuperIsPossible();
			if (property == null) {
				property = createProperty(pet, pp);
				pet.addIntroducedProperty(property);
			}

			properties[index++] = property;

			if (pp.initializer != null && !(pp.initializer instanceof NullDescriptor))
				initializers.add(EntityInitializerImpl.newInstance(property, pp.initializer));
		}

		result.setProperties(properties);
		result.setInitializers(toArrayOrNull(initializers));

		// #############################################
		// ## . . . . . transient properties . . . . .##
		// #############################################

		List<TransientProperty> transientProperties = newList(pet.allPreliminaryTransientPropsToImpl.size());
		for (Entry<String, AsmClass> e : pet.allPreliminaryTransientPropsToImpl.entrySet()) {
			String name = e.getKey();
			AsmClass type = e.getValue();

			TransientProperty tp = pet.findTransientProperty(name);
			if (tp == null) {
				tp = createTransientProperty(pet, name, type);
				pet.addIntroducedTransientProperty(tp);
			}

			transientProperties.add(tp);
		}

		pet.entityType.setTransientProperties(transientProperties);
		if (pet.getDeclaredEvaluatesTo() != null)
			result.setEvaluatesTo(resolveType(pet.getDeclaredEvaluatesTo()));

		return result;
	}

	private EntityType<?> finalizeSuperTypeIfNeeded(ItwEntityType superType) throws GenericModelTypeSynthesisException {
		if (superType instanceof PreliminaryEntityType)
			return finalizeEntityType((PreliminaryEntityType) superType);
		else
			return (EntityType<?>) superType;
	}

	private AbstractProperty createProperty(PreliminaryEntityType pet, PreliminaryProperty pp) throws GenericModelTypeSynthesisException {
		try {
			String propertyClassName = getPropertyClassName(pet.gmEntityType, pp.propertyName);

			ClassBuilder cb = makeClass(propertyClassName, false, gcp.jvmPropertyType);
			PropertyImplementer propertyImplementer = new PropertyImplementer(cb, gcp);

			propertyImplementer.addConstructor(pet, pp);
			propertyImplementer.addStaticSingletonField();
			propertyImplementer.addClassInitialization();
			propertyImplementer.addGetDirectUnsafe(pp.gmProperty, pet.weakInterface);
			propertyImplementer.addSetDirectUnsafe(pp.gmProperty, pet.weakInterface);

			AsmNewClass asmClass = cb.build();
			Class<?> jvmClass = getJavaClass(asmClass);

			// finish property initialization
			AbstractProperty result = extractStaticValue(jvmClass, PropertyImplementer.SINGLETON_NAME);
			result.setInitializer(pp.initializer);
			result.setPropertyType(resolveType(pp.gmProperty.getType()));

			return result;

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException("Error while creating property '" + pp.propertyName + "' of: " + pet.entityTypeName, e);
		}
	}

	private TransientProperty createTransientProperty(PreliminaryEntityType pet, String name, AsmClass type)
			throws GenericModelTypeSynthesisException {

		try {
			Class<?> ifaceType = getJavaClass(pet.entityIface);
			Class<?> propertyType = getJavaClass(type);

			Function<GenericEntity, Object> getter = AccessorFactory.getter(ifaceType, ItwTools.getGetterName(name), propertyType);
			BiConsumer<GenericEntity, Object> setter = AccessorFactory.setter(ifaceType, ItwTools.getSetterName(name), propertyType);

			return new GmtsTransientProperty(pet.entityType, name, propertyType, getter, setter);

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException("Error while creating preliminary property '" + name + "' of: " + pet.entityTypeName, e);
		}
	}

	private void setJavaTypesForDependedEntityTypes(ProtoGmType type) throws GenericModelTypeSynthesisException {
		if (type instanceof ProtoGmEntityType) {
			PreliminaryEntityType pet = newEntityTypes.get(type.getTypeSignature());
			if (pet != null)
				setJavaTypesForEntityType(pet);

		} else if (type instanceof ProtoGmCollectionType) {

			if (type instanceof ProtoGmLinearCollectionType) {
				ProtoGmType elementType = ((ProtoGmLinearCollectionType) type).getElementType();
				setJavaTypesForDependedEntityTypes(elementType);

			} else if (type instanceof ProtoGmMapType) {
				ProtoGmMapType mapType = (ProtoGmMapType) type;

				setJavaTypesForDependedEntityTypes(mapType.getKeyType());
				setJavaTypesForDependedEntityTypes(mapType.getValueType());
			}
		}
	}

	// #############################################
	// ## . . . . . . Various helpers . . . . . . ##
	// #############################################

	private ClassBuilder makeClass(String className, boolean isAbstract, AsmClass superClass, AsmClass... superInterfaces) {
		return asmClassPool.makeClass(className, isAbstract, superClass, superInterfaces);
	}

	private <T> Class<T> getJavaClass(AsmClass asmClass) throws GenericModelTypeSynthesisException {
		try {
			return asmClassPool.getJvmClass(asmClass);

		} catch (JavaTypeSynthesisException e) {
			throw new GenericModelTypeSynthesisException("error while getting actual class for AsmClass " + asmClass, e);
		}
	}

}
