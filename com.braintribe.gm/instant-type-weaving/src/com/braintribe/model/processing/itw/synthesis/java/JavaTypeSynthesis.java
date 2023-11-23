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
package com.braintribe.model.processing.itw.synthesis.java;

import static com.braintribe.model.processing.itw.asm.AsmClassPool.abstractType;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.iteratorAtTheEndOf;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.braintribe.model.access.ClassDataStorage;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.annotation.meta.api.synthesis.AnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.api.synthesis.RepeatedAnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.api.synthesis.SingleAnnotationDescriptor;
import com.braintribe.model.generic.annotation.meta.synthesis.MdaSynthesis;
import com.braintribe.model.generic.reflection.ItwTypeReflection;
import com.braintribe.model.processing.itw.asm.AsmAnnotationInstance;
import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmClassPool;
import com.braintribe.model.processing.itw.asm.AsmEnumConstant;
import com.braintribe.model.processing.itw.asm.AsmField;
import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.asm.AsmType;
import com.braintribe.model.processing.itw.asm.ClassBuilder;
import com.braintribe.model.processing.itw.asm.DebugInfoProvider;
import com.braintribe.model.processing.itw.asm.EnumBuilder;
import com.braintribe.model.processing.itw.asm.GenericAsmClass;
import com.braintribe.model.processing.itw.asm.InterfaceBuilder;
import com.braintribe.model.processing.itw.asm.NotFoundException;
import com.braintribe.model.processing.itw.asm.TypeBuilder;
import com.braintribe.model.processing.itw.synthesis.java.PropertyAnalysis.PropertyDescription;
import com.braintribe.model.processing.itw.synthesis.java.PropertyAnalysis.SetterGetterAchievement;
import com.braintribe.model.processing.itw.synthesis.java.asm.DeclarationInfaceImplementer;
import com.braintribe.model.processing.itw.synthesis.java.asm.EnumBaseEnumBuilder;
import com.braintribe.model.processing.itw.synthesis.java.clazz.ClassStorageManager;
import com.braintribe.model.processing.itw.synthesis.java.clazz.FileSystemClassDataStorage;
import com.braintribe.model.weaving.ProtoGmBaseType;
import com.braintribe.model.weaving.ProtoGmCollectionType;
import com.braintribe.model.weaving.ProtoGmEntityType;
import com.braintribe.model.weaving.ProtoGmEnumConstant;
import com.braintribe.model.weaving.ProtoGmEnumType;
import com.braintribe.model.weaving.ProtoGmLinearCollectionType;
import com.braintribe.model.weaving.ProtoGmListType;
import com.braintribe.model.weaving.ProtoGmMapType;
import com.braintribe.model.weaving.ProtoGmProperty;
import com.braintribe.model.weaving.ProtoGmSetType;
import com.braintribe.model.weaving.ProtoGmSimpleType;
import com.braintribe.model.weaving.ProtoGmType;
import com.braintribe.model.weaving.data.ProtoHasMetaData;
import com.braintribe.model.weaving.info.ProtoGmPropertyInfo;
import com.braintribe.model.weaving.override.ProtoGmPropertyOverride;

public class JavaTypeSynthesis {

	public static final ItwTypeReflection typeReflection = GMF.getTypeReflection();

	protected final DebugInfoProvider debugInfoProvider;
	protected final AsmClassPool asmClassPool;

	protected final PropertyAnalyzer propertyAnalyzer;

	private final Map<String, Map<String, ProtoGmPropertyInfo[]>> mergedProperties = new ConcurrentHashMap<>();

	public JavaTypeSynthesis() {
		this(EmtpyDebugInfoProvider.INSTANCE, true);
	}

	public JavaTypeSynthesis(DebugInfoProvider debugInfoProvider, boolean considerClassPath) {
		TmpJtsTracker.INSTANCE.onNewJts();
		this.debugInfoProvider = debugInfoProvider;
		this.asmClassPool = new AsmClassPool(considerClassPath);
		this.propertyAnalyzer = new PropertyAnalyzer(this);
	}

	public void setClassOutputFolder(File folder) {
		setClassDataStorage(folder == null ? null : new FileSystemClassDataStorage(folder));
	}

	public void setClassDataStorage(ClassDataStorage classDataStorage) {
		asmClassPool.setClassStorageManager(classDataStorage == null ? null : new ClassStorageManager(classDataStorage));
	}

	public ClassLoader getItwClassLoader() {
		return asmClassPool.getItwClassLoader();
	}

	/**
	 * Note that the order of iteration through the values determines the indices of the properties, and this information is accessed via this method
	 * more than once. It is therefore necessary that the value returned for the same {@link ProtoGmEntityType} is really always the same.
	 * <p>
	 * Using arrays rather than lists for less memory.
	 */
	public Map<String, ProtoGmPropertyInfo[]> getMergedPropertiesFromTypeHierarchy(ProtoGmEntityType gmEntityType) throws JavaTypeSynthesisException {
		if (gmEntityType == null)
			return Collections.emptyMap();
		else
			return getMergedPropertiesFromTypeHierarchy(gmEntityType, newSet());
	}

	private Map<String, ProtoGmPropertyInfo[]> getMergedPropertiesFromTypeHierarchy(ProtoGmEntityType gmEntityType,
			Set<ProtoGmEntityType> visitedTypes) throws JavaTypeSynthesisException {

		if (visitedTypes.contains(gmEntityType))
			throw new JavaTypeSynthesisException("Seems like type '" + gmEntityType.getTypeSignature() + "' is it's own sub-type!");

		Map<String, ProtoGmPropertyInfo[]> result = mergedProperties.get(gmEntityType.getTypeSignature());

		if (result == null) {
			Map<String, List<ProtoGmPropertyInfo>> mps = new TreeMap<>();

			for (ProtoGmProperty gmProperty : nullSafe(gmEntityType.getProperties()))
				acquireList(mps, gmProperty.getName()).add(gmProperty);

			for (ProtoGmPropertyOverride gmPropertyOverride : nullSafe(gmEntityType.getPropertyOverrides()))
				acquireList(mps, gmPropertyOverride.getProperty().getName()).add(gmPropertyOverride);

			visitedTypes.add(gmEntityType);
			for (ProtoGmEntityType superType : nullSafe(gmEntityType.getSuperTypes())) {
				Map<String, ProtoGmPropertyInfo[]> mergedSuperProperties = getMergedPropertiesFromTypeHierarchy(superType, visitedTypes);
				for (Entry<String, ProtoGmPropertyInfo[]> entry : mergedSuperProperties.entrySet()) {
					String propertyName = entry.getKey();
					ProtoGmPropertyInfo[] properties = entry.getValue();

					acquireList(mps, propertyName).addAll(Arrays.asList(properties));
				}
			}
			visitedTypes.remove(gmEntityType);

			mergedProperties.put(gmEntityType.getTypeSignature(), result = normalizeMergedProperties(mps));
		}

		return result;
	}

	private Map<String, ProtoGmPropertyInfo[]> normalizeMergedProperties(Map<String, List<ProtoGmPropertyInfo>> mps) {
		Map<String, ProtoGmPropertyInfo[]> result = new TreeMap<>();
		for (Entry<String, List<ProtoGmPropertyInfo>> e : mps.entrySet()) {
			List<ProtoGmPropertyInfo> ps = e.getValue();
			removeDuplicates(ps);

			result.put(e.getKey(), ps.toArray(new ProtoGmPropertyInfo[ps.size()]));
		}

		return result;
	}

	private void removeDuplicates(List<ProtoGmPropertyInfo> allDefinedProperties) {
		Set<ProtoGmEntityType> visitedTypes = newSet();

		ListIterator<ProtoGmPropertyInfo> it = iteratorAtTheEndOf(allDefinedProperties);
		while (it.hasPrevious()) {
			ProtoGmEntityType declaringType = (ProtoGmEntityType) it.previous().declaringTypeInfo();

			if (!visitedTypes.add(declaringType))
				it.remove();
		}
	}

	/* This method must never be invoked recursively!!! See comment note in GenericModelTypeSynthesis */
	public AsmClass ensureClass(ProtoGmType gmType) throws JavaTypeSynthesisException {
		try {
			// TODO replace with switch
			// first check custom type that must be created on demand
			if (gmType instanceof ProtoGmCollectionType)
				return ensureCollectionParameterizationClasses((ProtoGmCollectionType) gmType);
			else if (gmType instanceof ProtoGmEntityType)
				return ensureEntityIface((ProtoGmEntityType) gmType);
			else if (gmType instanceof ProtoGmEnumType)
				return ensureEnumClass((ProtoGmEnumType) gmType);
			else if (gmType instanceof ProtoGmBaseType)
				return AsmClassPool.objectType;
			else if (gmType instanceof ProtoGmSimpleType)
				// simple type are already present in the type reflection and the java class system
				return asmClassPool.get(typeReflection.getType(gmType.getTypeSignature()).getJavaType().getName());
			else
				throw new JavaTypeSynthesisException("unkown ProtoGmType derivate " + gmType.getClass());

		} catch (NotFoundException e) {
			throw new JavaTypeSynthesisException("error while finding or creating required class for " + gmType.getTypeSignature(), e);
		}
	}

	protected AsmClass ensureCollectionParameterizationClasses(ProtoGmCollectionType gmCollectionType) throws JavaTypeSynthesisException {
		if (gmCollectionType instanceof ProtoGmLinearCollectionType) {
			ProtoGmLinearCollectionType linearCollectionType = (ProtoGmLinearCollectionType) gmCollectionType;
			ensureClass(linearCollectionType.getElementType());
			if (gmCollectionType instanceof ProtoGmListType)
				return AsmClassPool.listType;
			else if (gmCollectionType instanceof ProtoGmSetType)
				return AsmClassPool.setType;

		} else if (gmCollectionType instanceof ProtoGmMapType) {
			ProtoGmMapType mapType = (ProtoGmMapType) gmCollectionType;
			ensureClass(mapType.getKeyType());
			ensureClass(mapType.getValueType());
			return AsmClassPool.mapType;
		}

		throw new JavaTypeSynthesisException("unknown collection type " + gmCollectionType.getClass());
	}

	private final Object ENUM_CLASS_LOCK = new Object();

	protected AsmClass ensureEnumClass(ProtoGmEnumType gmEnumType) throws JavaTypeSynthesisException {
		String typeSignature = gmEnumType.getTypeSignature();
		AsmClass existingEntityClass = findAsmClass(typeSignature);

		if (existingEntityClass != null)
			return existingEntityClass;

		synchronized (ENUM_CLASS_LOCK) {
			existingEntityClass = findAsmClass(typeSignature);
			if (existingEntityClass != null)
				return existingEntityClass;

			List<AsmEnumConstant> values = getValues(gmEnumType);

			return createEnumClass(gmEnumType, values);
		}
	}

	private List<AsmEnumConstant> getValues(ProtoGmEnumType gmEnumType) {
		List<AsmEnumConstant> result = newList();

		for (ProtoGmEnumConstant ec : nullSafe(gmEnumType.getConstants())) {
			List<AsmAnnotationInstance> annos = MdaSynthesis.synthesizeMetaDataAnnotations(ec.getGlobalId(), ec.getMetaData()).stream() //
					.map(this::toAsmAnnotationInstance) //
					.collect(Collectors.toList());

			result.add(new AsmEnumConstant(ec.getName(), annos));
		}

		return result;
	}

	private AsmClass createEnumClass(ProtoGmEnumType gmEnumType, List<AsmEnumConstant> constants) {
		EnumBuilder enumBuilder = asmClassPool.makeEnum( //
				EnumBaseEnumBuilder::new, gmEnumType.getTypeSignature(), constants, AsmClassPool.enumBaseType);

		implementCustomTypeMetaDataAnnotationsIfEligible(enumBuilder, gmEnumType);

		return finishBuild(enumBuilder);
	}

	private final Object ENTITY_CLASS_LOCK = new Object();

	protected AsmClass ensureEntityIface(ProtoGmEntityType gmEntityType) throws JavaTypeSynthesisException {
		synchronized (ENTITY_CLASS_LOCK) {
			return ensureEntityInterfaceHelper(gmEntityType);
		}
	}

	private AsmClass ensureEntityInterfaceHelper(ProtoGmEntityType gmEntityType) throws JavaTypeSynthesisException {
		String typeSignature = gmEntityType.getTypeSignature();
		AsmClass existingEntityClass = findAsmClass(typeSignature);
		if (existingEntityClass != null)
			return existingEntityClass;

		List<AsmClass> superInterfaces = newList();

		for (ProtoGmEntityType superType : nullSafe(gmEntityType.getSuperTypes())) {
			AsmClass superClassOrInterface = ensureEntityInterfaceHelper(superType);
			if (superClassOrInterface.isInterface())
				superInterfaces.add(superClassOrInterface);
			else
				throw new JavaTypeSynthesisException("Classes are not supported any more! Class: " + superClassOrInterface);
		}

		/* It may happen, that examining superTypes caused this entity to be build (through properties), so we have to check it one more time */
		if ((existingEntityClass = findAsmClass(typeSignature)) != null)
			return existingEntityClass;

		return createEntityInterface(gmEntityType, superInterfaces);
	}

	/** Creates the class without any AOP logic just with property support */
	private AsmNewClass createEntityInterface(ProtoGmEntityType gmEntityType, List<AsmClass> superInterfaces) throws JavaTypeSynthesisException {
		try {
			String typeSignature = gmEntityType.getTypeSignature();

			InterfaceBuilder ib = asmClassPool.makeInterface(typeSignature, superInterfaces);
			if (isAbstract(gmEntityType))
				ib.addAnnotation(abstractType);

			Map<String, ProtoGmPropertyInfo[]> properties = getMergedPropertiesFromTypeHierarchy(gmEntityType);
			PropertyAnalysis propAnalysis = propertyAnalyzer.analyzeProperties(ib, properties);

			implementCustomTypeMetaDataAnnotationsIfEligible(ib, gmEntityType);
			implementEntityTypeLiteral(ib);
			implementIfaceProperties(ib, propAnalysis);
			implementPropertyNameLiterals(ib, propAnalysis);
			implementEvalIfEligible(ib, gmEntityType);

			return finishBuild(ib);

		} catch (Exception e) {
			throw new JavaTypeSynthesisException("error while building AsmClass for " + gmEntityType.getTypeSignature(), e);
		}
	}

	private void implementCustomTypeMetaDataAnnotationsIfEligible(TypeBuilder tb, ProtoHasMetaData gmCustomType) {
		Collection<AnnotationDescriptor> annotationDescriptors = MdaSynthesis.synthesizeMetaDataAnnotations(gmCustomType.getGlobalId(), gmCustomType.getMetaData());

		for (AnnotationDescriptor annotationDescriptor : annotationDescriptors)
			tb.addAnnotation(toAsmAnnotationInstance(annotationDescriptor));
	}

	private AsmNewClass finishBuild(TypeBuilder tb) {
		AsmNewClass result = tb.build();
		asmClassPool.onClassCreated(result);

		return result;
	}

	protected boolean isAbstract(ProtoGmEntityType gmEntityType) {
		return Boolean.TRUE.equals(gmEntityType.getIsAbstract());
	}

	protected AsmClass findAsmClass(String typeSignature) throws JavaTypeSynthesisException {
		AsmClass result = asmClassPool.getIfPresent(typeSignature);

		if (result != null && !Modifier.isPublic(result.getModifiers()))
			throw new JavaTypeSynthesisException("Illegal class '" + typeSignature + "' Only public classes are supported by ITW.");

		return result;
	}

	private void implementEntityTypeLiteral(InterfaceBuilder ib) {
		new DeclarationInfaceImplementer(ib, asmClassPool).implementEntityTypeLiteral(ib.getPreliminaryClass());
	}

	private void implementIfaceProperties(InterfaceBuilder ib, PropertyAnalysis propAnalysis) {
		for (PropertyDescription description : propAnalysis.propertyDescriptions)
			createSetterGetter(ib, description);
	}

	private void createSetterGetter(InterfaceBuilder ib, PropertyDescription pd) {
		AsmClass asmClass = ib.getPreliminaryClass();
		// do we need an implementation or just a declaration
		// declaration is needed only if not already present or we re-declare the initializer
		if (pd.achievement == SetterGetterAchievement.missing || isInterfaceWithInitializer(asmClass, pd)) {
			AsmType propertyClass = pd.getPropertyType();
			List<AsmAnnotationInstance> as = propertyAnalyzer.getAnnotations(pd);
			ib.addAbstractMethod(pd.getterName, as, propertyClass);
			ib.addAbstractMethod(pd.setterName, AsmClassPool.voidType, propertyClass);
		}
	}

	/**
	 * As we will soon only support interfaces, I do not want to support the use-case that we have an abstract class which inherits some method (which
	 * may be declared or implemented) with an initializer. For interfaces it is easy, we simply re-declare the method with the new initializer.
	 */
	private boolean isInterfaceWithInitializer(AsmClass asmClass, PropertyDescription pd) {
		ProtoGmProperty p = pd.property;
		return asmClass.getName().equals(p.getDeclaringType().getTypeSignature()) && p.getInitializer() != null;
	}

	private void implementPropertyNameLiterals(InterfaceBuilder tb, PropertyAnalysis propAnalysis) {
		for (PropertyDescription description : propAnalysis.propertyDescriptions)
			if (description.achievement == SetterGetterAchievement.missing)
				tb.addConstantField(AsmClassPool.stringType, description.getName(), description.getName());
	}

	protected AsmField createUniqueField(AsmClass type, String desiredName, ClassBuilder classBuilder) {
		String name = classBuilder.getPreliminaryClass().makeUniqueName(desiredName);

		return classBuilder.addField(name, type, Modifier.PRIVATE);
	}

	private void implementEvalIfEligible(InterfaceBuilder ib, ProtoGmEntityType gmEntityType) throws JavaTypeSynthesisException {
		ProtoGmType evaluatesTo = gmEntityType.getEvaluatesTo();
		if (evaluatesTo != null) {
			AsmType asmEvaluatesTo = ensureClassAsGenericIfNeeded(evaluatesTo);

			GenericAsmClass returnType = new GenericAsmClass(AsmClassPool.evalContextType, asmEvaluatesTo);
			GenericAsmClass argType = new GenericAsmClass(AsmClassPool.evaluatorType, ib.getPreliminaryClass());

			ib.addAbstractMethod("eval", returnType, argType);
		}
	}

	public AsmType ensureClassAsGenericIfNeeded(ProtoGmType type) throws JavaTypeSynthesisException {
		AsmClass rawType = ensureClass(type);
		if (!(type instanceof ProtoGmCollectionType))
			return rawType;

		AsmType params[] = null;

		if (type instanceof ProtoGmLinearCollectionType) {
			params = new AsmType[1];
			params[0] = ensureClass(((ProtoGmLinearCollectionType) type).getElementType());

		} else if (type instanceof ProtoGmMapType) {
			ProtoGmMapType mapType = (ProtoGmMapType) type;
			params = new AsmType[2];
			params[0] = ensureClass(mapType.getKeyType());
			params[1] = ensureClass(mapType.getValueType());

		} else {
			throw new JavaTypeSynthesisException("unknown CollectionType " + type.getClass());
		}

		return new GenericAsmClass(rawType, params);
	}

	protected AsmAnnotationInstance toAsmAnnotationInstance(AnnotationDescriptor annotationDescriptor) {
		AsmClass annotationClass = asmClassPool.get(annotationDescriptor.getAnnotationClass());

		if (annotationDescriptor instanceof SingleAnnotationDescriptor) {
			SingleAnnotationDescriptor sad = (SingleAnnotationDescriptor) annotationDescriptor;
			return new AsmAnnotationInstance(annotationClass, sad.getAnnotationValues());

		} else {
			RepeatedAnnotationDescriptor rad = (RepeatedAnnotationDescriptor) annotationDescriptor;
			AsmAnnotationInstance[] nestedAnnotations = rad.getNestedAnnotations().stream() //
					.map(this::toAsmAnnotationInstance) //
					.toArray(AsmAnnotationInstance[]::new);

			return new AsmAnnotationInstance(annotationClass, "value", nestedAnnotations);
		}
	}

}
