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
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 
 */
public class AsmNewClass extends AsmLoadableClass {

	private final AsmClass superClass;
	private final List<AsmClass> interfaces;
	private List<Class<? extends AsmRuntimeAnnotation>> runtimeAnnotations;

	AsmNewClass declaringClass;
	List<AsmClass> declaredClasses;

	private Map<MethodSignature, AsmNewMethod> methods = Collections.EMPTY_MAP;
	private Map<String, AsmField> fields = Collections.EMPTY_MAP;

	AsmNewClass(String name, AsmClass superClass, List<AsmClass> interfaces, AsmClassPool classPool) {
		super(name, classPool);

		if (classPool == null)
			throw new IllegalArgumentException("Classpool cannot be null for new class!");

		this.superClass = superClass != null ? superClass : AsmClassPool.objectType;
		this.interfaces = (interfaces != null && !interfaces.isEmpty()) ? interfaces : Collections.<AsmClass> emptyList();
		this.runtimeAnnotations = Collections.EMPTY_LIST;
		this.declaredClasses = Collections.EMPTY_LIST;
	}

	public void setModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	@Override
	public AsmMethod getMethod(String name, AsmClass returnType, AsmClass... params) {
		return getMethod(new MethodSignature(name, params), returnType);
	}

	@Override
	protected AsmMethod getMethod(MethodSignature ms, AsmClass returnType) {
		if (methods.containsKey(ms))
			return getMethodByReturnType(methods.get(ms), returnType);
		else
			return getMethodFromSuperType(ms, returnType);
	}

	private AsmMethod getMethodFromSuperType(MethodSignature ms, AsmClass returnType) {
		AsmMethod method = superClass.getMethod(ms, returnType);

		if (method != null)
			return method;

		for (AsmClass iface : interfaces) {
			method = iface.getMethod(ms, returnType);
			if (method != null)
				break;
		}

		return method;
	}

	private AsmMethod getMethodByReturnType(AsmMethod asmMethod, AsmClass returnType) {
		return returnType.getInternalName().equals(asmMethod.getReturnTypeName()) ? asmMethod : null;
	}

	@Override
	public AsmClass getDeclaringClass() {
		return declaringClass;
	}

	public void registerNewInnerClass(AsmNewClass newInnerClass) {
		if (declaredClasses == null)
			declaredClasses = newList();

		declaredClasses.add(newInnerClass);
		newInnerClass.declaringClass = this;
	}

	@Override
	public List<AsmClass> getDeclaredClasses() {
		return declaredClasses;
	}

	AsmField notifyNewField(AsmField field) {
		if (fields == Collections.EMPTY_MAP)
			fields = newMap();

		if (fields.put(field.getName(), field) != null)
			throw new IllegalArgumentException("Class '" + name + "' already has a field called: " + field.getName());

		return field;
	}

	AsmNewMethod notifyNewMethod(AsmNewMethod method) {
		MethodSignature ms = new MethodSignature(method.getName(), method.getParams());
		if (methods == Collections.EMPTY_MAP)
			methods = newMap();

		if (methods.put(ms, method) != null)
			throw new IllegalArgumentException(
					"Method overloading not supported. Class '" + name + "' already has a method called: " + method.getName());

		return method;
	}

	public String makeUniqueName(String prefix) {
		Set<String> keys = getMemberNames();

		if (!keys.contains(prefix))
			return prefix;

		int i = 100;
		String name;
		do {
			if (i > 999)
				throw new RuntimeException("too many unique name");

			name = prefix + i++;
		} while (keys.contains(name));

		return name;
	}

	private Set<String> getMemberNames() {
		Set<String> members = newSet();
		addMemberNamesTo(members);

		return members;
	}

	@Override
	public AsmClass getSuperClass() {
		return superClass;
	}

	@Override
	public List<AsmClass> getInterfaces() {
		return interfaces;
	}

	@Override
	public Stream<? extends AsmMethod> getMethods() {
		return methods.values().stream();
	}

	@Override
	public Stream<AsmField> getFields() {
		return fields.values().stream();
	}

	/**
	 * Returns field for given name defined for this class.
	 */
	public AsmField getDeclaredField(String name) {
		return fields.get(name);
	}

	public void addRuntimeAnnotation(Class<? extends AsmRuntimeAnnotation> annotation) {
		if (runtimeAnnotations == Collections.EMPTY_LIST)
			runtimeAnnotations = newList();

		runtimeAnnotations.add(annotation);
	}

	public boolean hasRuntimeAnnotation(Class<? extends AsmRuntimeAnnotation> annotation) {
		return runtimeAnnotations.contains(annotation);
	}
}
