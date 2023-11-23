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

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 */
public class AsmExistingClass extends AsmClass {

	private final Class<?> clazz;
	private AsmClass superClass;
	private List<AsmExistingClass> interfaces;
	private List<AsmExistingMethod> methods;
	private Map<Method, AsmExistingMethod> methodIndex;
	private List<AsmField> fields;
	private List<AsmExistingClass> declaredClasses;

	AsmExistingClass(Class<?> clazz, AsmClassPool classPool) {
		super(clazz.getName(), classPool);

		this.clazz = clazz;
		this.modifiers = clazz.getModifiers();

		if (classPool == null && clazz.getDeclaringClass() != null)
			throw new IllegalArgumentException("Nested class cannot be defined without class pool. Class: " + clazz.getName());

		loadMethods();
	}

	private void loadMethods() {
		Method[] ms = clazz.getMethods();
		methods = new ArrayList<>(ms.length);
		methodIndex = newMap();
		for (Method m : ms) {
			AsmExistingMethod asmExistingMethod = new AsmExistingMethod(classPool, m);
			methods.add(asmExistingMethod);
			methodIndex.put(m, asmExistingMethod);
		}
	}

	/** Returns a public method for given return type and parameters. */
	@Override
	public AsmExistingMethod getMethod(String name, AsmClass returnType, AsmClass... params) {
		Method m = getMethodBySignature(name, params);
		if (m == null)
			return null;

		AsmExistingMethod asmMethod = methodIndex.get(m);
		if (asmMethod == null)
			return null;

		return returnType == asmMethod.getReturnType() ? asmMethod : null;
	}

	@Override
	protected AsmExistingMethod getMethod(MethodSignature ms, AsmClass returnType) {
		return getMethod(ms.name, returnType, ms.params);
	}

	private Method getMethodBySignature(String name, AsmClass[] params) {
		/* The method can only be found iff all the params are existing classes, since if the class did not exist
		 * before, an existing class cannot have a method with such parameter */
		Class<?>[] params0 = new Class<?>[params.length];

		int counter = 0;
		for (AsmClass param : params) {
			if (!(param instanceof AsmExistingClass))
				return null;

			params0[counter++] = ((AsmExistingClass) param).clazz;
		}

		try {
			return clazz.getMethod(name, params0);

		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Class<?> getJavaType() {
		return clazz;
	}
	
	public Class<?> getExistingClass() {
		return clazz;
	}

	@Override
	public boolean isPrimitive() {
		return clazz.isPrimitive();
	}

	@Override
	public AsmClass getSuperClass() {
		if (superClass == null) {
			Class<?> sc = clazz.getSuperclass();
			superClass = sc == null ? null : classPool.get(sc);
		}

		return superClass;
	}

	@Override
	public List<AsmExistingClass> getInterfaces() {
		if (interfaces == null) {
			Class<?>[] ifs = clazz.getInterfaces();
			interfaces = Arrays.asList(AsmUtils.toAsmClasses(classPool, ifs));
		}

		return interfaces;
	}

	@Override
	public Stream<AsmExistingMethod> getMethods() {
		return methods.stream();
	}

	@Override
	public Stream<AsmField> getFields() {
		if (fields == null) {
			Field[] fs = clazz.getFields();
			fields = new ArrayList<AsmField>(fs.length);
			for (Field f : fs)
				fields.add(newExistingField(f));
		}

		return fields.stream();
	}

	public AsmField getDeclaredField(String name) {
		try {
			Field f = clazz.getDeclaredField(name);
			return newExistingField(f);

		} catch (Exception e) {
			throw new RuntimeException("No field found: " + clazz.getName() + "#" + name);
		}
	}

	private AsmField newExistingField(Field f) {
		AsmClass type = classPool.get(f.getType());
		return AsmField.newExistingField(this, f.getName(), type);
	}

	@Override
	public AsmClass getDeclaringClass() {
		return clazz.getDeclaringClass() != null ? classPool.get(clazz.getDeclaringClass()) : null;
	}

	@Override
	public List<AsmExistingClass> getDeclaredClasses() {
		if (declaredClasses == null) {
			Class<?>[] dcs = clazz.getDeclaredClasses();

			if (dcs.length > 0) {
				declaredClasses = newList();
				for (Class<?> dc : dcs)
					declaredClasses.add(classPool.get(dc));

			} else {
				declaredClasses = Collections.emptyList();
			}
		}

		return declaredClasses;
	}

	@Override
	public String toString() {
		return clazz.toString();
	}

	public boolean isAssignableFrom(AsmClass asmClass) {
		if (this == asmClass)
			return true;

		if (asmClass == null)
			return false;

		if (isInterface())
			for (AsmClass iClass : asmClass.getInterfaces())
				if (isAssignableFrom(iClass))
					return true;

		return isAssignableFrom(asmClass.getSuperClass());
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return clazz.getAnnotation(annotationClass);
	}
}
