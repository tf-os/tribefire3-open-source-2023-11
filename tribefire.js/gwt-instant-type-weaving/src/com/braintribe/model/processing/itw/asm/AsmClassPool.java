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
package com.braintribe.model.processing.itw.asm;

import static com.braintribe.model.processing.itw.tools.ItwTools.findClass;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.emptyList;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.itw.synthesis.java.JavaTypeSynthesisException;
import com.braintribe.model.processing.itw.synthesis.java.clazz.ClassStorageManager;
import com.braintribe.model.processing.itw.synthesis.java.clazz.FolderClassLoader;
import com.braintribe.model.processing.itw.tools.ItwTools;

/**
 *
 */
public class AsmClassPool {

	private static final Map<String, AsmExistingClass> staticCache = newMap();

	public static final AsmExistingClass voidType = getStatic(void.class);

	public static final AsmExistingClass classType = getStatic(Class.class);

	public static final AsmExistingClass byteType = getStatic(byte.class);
	public static final AsmExistingClass shortType = getStatic(short.class);
	public static final AsmExistingClass intType = getStatic(int.class);
	public static final AsmExistingClass longType = getStatic(long.class);
	public static final AsmExistingClass floatType = getStatic(float.class);
	public static final AsmExistingClass doubleType = getStatic(double.class);
	public static final AsmExistingClass booleanType = getStatic(boolean.class);
	public static final AsmExistingClass charType = getStatic(char.class);
	public static final AsmExistingClass byteObjectType = getStatic(Byte.class);
	public static final AsmExistingClass shortObjectType = getStatic(Short.class);
	public static final AsmExistingClass intObjectType = getStatic(Integer.class);
	public static final AsmExistingClass longObjectType = getStatic(Long.class);
	public static final AsmExistingClass floatObjectType = getStatic(Float.class);
	public static final AsmExistingClass doubleObjectType = getStatic(Double.class);
	public static final AsmExistingClass booleanObjectType = getStatic(Boolean.class);
	public static final AsmExistingClass charObjectType = getStatic(Character.class);
	public static final AsmExistingClass objectType = getStatic(Object.class);
	public static final AsmExistingClass stringType = getStatic(String.class);
	public static final AsmExistingClass dateType = getStatic(Date.class);
	public static final AsmExistingClass bigDecimalType = getStatic(BigDecimal.class);

	public static final AsmExistingClass listType = getStatic(List.class);
	public static final AsmExistingClass setType = getStatic(Set.class);
	public static final AsmExistingClass mapType = getStatic(Map.class);

	public static final AsmExistingClass enumType = getStatic(Enum.class);

	public static final AsmExistingClass genericModelTypeType = getStatic(GenericModelType.class);
	public static final AsmExistingClass entityTypeType = getStatic(EntityType.class);
	public static final AsmExistingClass entityTypesType = getStatic(EntityTypes.class);
	public static final AsmExistingClass enumBaseType = getStatic(EnumBase.class);
	public static final AsmExistingClass enumTypeType = getStatic(EnumType.class);
	public static final AsmExistingClass enumTypesType = getStatic(EnumTypes.class);
	public static final AsmExistingClass evalContextType = getStatic(EvalContext.class);
	public static final AsmExistingClass evaluatorType = getStatic(Evaluator.class);

	public static final AsmExistingClass initializerType = getStatic(Initializer.class);
	public static final AsmExistingClass abstractType = getStatic(Abstract.class);

	private static AsmExistingClass getStatic(Class<?> clazz) {
		if (staticCache.containsKey(clazz.getName()))
			throw new RuntimeException("Wrong class initialization. Classs " + clazz.getName() + " is being cached twice");

		AsmExistingClass result = new AsmExistingClass(clazz, null);
		staticCache.put(clazz.getName(), result);

		return result;
	}

	private final boolean considerClassPath; // if false, it does not try to find class on the classpath

	private final AsmClassLoading classLoading = newClassLoading() ;
	private final Map<String, AsmClass> classCache = newMap();

	protected ClassStorageManager classStorageManager;

	public AsmClassPool(boolean considerClassPath) {
		this.considerClassPath = considerClassPath;
	}

	private static AsmClassLoading newClassLoading() {
		return ItwTools.useOwnCl() ? new AsmDirectClassLoader() : new AsmClassLoaderWrapper();
	}

	public ClassLoader getItwClassLoader() {
		return classLoading.getItwClassLoader();
	}

	public void setClassStorageManager(ClassStorageManager classStorageManager) {
		this.classStorageManager = classStorageManager;
	}

	public AsmExistingClass get(Class<?> clazz) {
		String name = clazz.getName();

		if (staticCache.containsKey(name))
			return staticCache.get(name);

		if (classCache.containsKey(name))
			return (AsmExistingClass) classCache.get(clazz.getName());

		return addCacheEntryFor(clazz);
	}

	public AsmClass get(String className) throws NotFoundException {
		AsmClass asmClass = getIfPresent(className);

		if (asmClass == null)
			throw new NotFoundException("AsmClass not found: " + className);

		return asmClass;
	}

	/**
	 * Special method to acquire AsmClass instance for an array. These are handled specially since they should not be registered at
	 * {@link AsmDirectClassLoader} (as long as any ClassLoader is able to load the component type of some array, it is also able to load the array
	 * itself).
	 */
	public AsmClass acquireArrayClass(AsmClass componentType) {
		String arrayName = AsmArray.createArrayClassName(componentType);

		if (classCache.containsKey(arrayName))
			return classCache.get(arrayName);

		AsmArray result = new AsmArray(componentType);
		classCache.put(arrayName, result);

		return result;
	}

	public AsmClass getIfPresent(String className) {
		if (staticCache.containsKey(className))
			return staticCache.get(className);

		if (classCache.containsKey(className))
			return classCache.get(className);

		Class<?> clazz = findClass(className);

		if (clazz == null)
			return null;

		if (classMayBeIgnored(clazz) && (!considerClassPath || wasInjectedByItw(clazz.getName())))
			return null;

		return addCacheEntryFor(clazz);
	}

	private boolean classMayBeIgnored(Class<?> clazz) {
		return GenericEntity.class.isAssignableFrom(clazz) || (Enum.class.isAssignableFrom(clazz) && Enum.class != clazz);
	}

	private AsmExistingClass addCacheEntryFor(Class<?> clazz) {
		AsmExistingClass asmClass = new AsmExistingClass(clazz, this);
		classCache.put(clazz.getName(), asmClass);

		return asmClass;
	}

	public ClassBuilder makeClass(String typeSignature, boolean isAbstract, AsmClass superClass, List<AsmClass> superInterfaces) {
		return new ClassBuilder(this, typeSignature, isAbstract, superClass, superInterfaces);
	}

	public ClassBuilder makeClass(String typeSignature, boolean isAbstract, AsmClass superClass, AsmClass... superInterfaces) {
		List<AsmClass> list = superInterfaces == null ? emptyList() : Arrays.asList(superInterfaces);
		return new ClassBuilder(this, typeSignature, isAbstract, superClass, list);
	}

	public InterfaceBuilder makeInterface(String typeSignature, List<AsmClass> superInterfaces) {
		return new InterfaceBuilder(this, typeSignature, superInterfaces);
	}

	public <T extends EnumBuilder> T makeEnum(EnumBuilderFactory<T> enumBuilderFactory, String typeSignature, List<AsmEnumConstant> constants,
			AsmClass... superInterfaces) {

		List<AsmClass> list = superInterfaces == null ? emptyList() : Arrays.asList(superInterfaces);
		return enumBuilderFactory.create(this, typeSignature, constants, list);
	}

	public void registerPreliminaryClass(AsmLoadableClass asmClass) {
		String className = asmClass.getName();

		if (classCache.containsKey(className))
			throw new IllegalArgumentException("Class was already registered: " + className);

		classCache.put(className, asmClass);
	}

	/**
	 * Registers a newly-created class inside this class-pool. After this method is called, there should be no changes made to the provided
	 * {@link AsmLoadableClass} instance.
	 */
	public void registerFinishedNewClass(AsmLoadableClass asmClass) {
		String className = asmClass.getName();

		if (asmClass.getBytes() == null)
			throw new IllegalArgumentException("Cannot register new asm class (" + className
					+ "). This class has no bytecode, so it was either loaded already, or is yet to be built!");

		if (!classCache.containsKey(className))
			// This should be totally impossible as the preliminary registration is done inside AsmNewClass constructor
			throw new IllegalArgumentException("Class was not registered as preliminary: " + className);

		if (!wasInjectedByItw(className))
			classLoading.register(asmClass);
	}

	private final Map<AsmClass, Class<?>> loadedClasses = newMap();

	public <T> Class<T> getJvmClass(AsmClass entityClass) throws JavaTypeSynthesisException {
		if (entityClass instanceof AsmExistingClass)
			return (Class<T>) ((AsmExistingClass) entityClass).getJavaType();

		String className = entityClass.getName();
		if (wasInjectedByItw(className))
			return classStorageManager.getLoadedClass(className);

		Class<?> result = loadedClasses.get(entityClass);

		if (result == null) {
			result = getJvmClass(className);
			loadedClasses.put(entityClass, result);
		}

		return (Class<T>) result;
	}

	/**
	 * Returns <tt>true</tt> iff given class comes from the configured class storage and it was already loaded with the system class-loader (probably
	 * using {@link FolderClassLoader}). This is useful for following situations:
	 * <ol>
	 * <li>In the {@link #getIfPresent(String)} method, when our {@link AsmClass} does not exist yet, we make sure that we always return <tt>null</tt>
	 * for classes injected by ITW (thus achieving that new bytecode will be created)</li>
	 * <li>In the {@link #registerFinishedNewClass(AsmLoadableClass)} method, we make sure we do not register a already-loaded class for the
	 * class-loader, because loading would fail (class already loaded). This is a fix for previous point, where we cause new class to be created for
	 * one that is already loaded.</li>
	 * <li>In the {@link #getJvmClass(AsmClass)} method, when looking for {@link Class} object for given {@link AsmClass}, we have to provide the
	 * existing instance. Because of first point we created a new class (so our ASM class instance is not a {@link AsmExistingClass}) and due to
	 * second point our class was not loaded, so it is not part of {@link #loadedClasses} map.</li>
	 * </ol>
	 */
	private boolean wasInjectedByItw(String className) {
		return classStorageManager != null && classStorageManager.containsLoadedClass(className);
	}

	private <T> Class<T> getJvmClass(String name) throws JavaTypeSynthesisException {
		try {
			return classLoading.getJvmClass(name);

		} catch (Exception e) {
			throw new JavaTypeSynthesisException("error while loading class: " + name, e);
		}
	}

	public void onClassCreated(AsmNewClass newClass) {
		if (classStorageManager != null)
			classStorageManager.onClassCreated(newClass);
	}

	public boolean hasClassStorageManager() {
		return classStorageManager != null;
	}
}
