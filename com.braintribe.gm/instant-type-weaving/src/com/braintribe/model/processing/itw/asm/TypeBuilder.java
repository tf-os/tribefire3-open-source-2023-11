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

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.asm.AnnotationVisitor;
import com.braintribe.asm.ClassWriter;
import com.braintribe.asm.FieldVisitor;
import com.braintribe.asm.MethodVisitor;
import com.braintribe.asm.Opcodes;
import com.braintribe.asm.Type;
import com.braintribe.model.generic.annotation.meta.api.synthesis.ClassReference;

/**
 *
 */
public abstract class TypeBuilder implements Opcodes {

	/* If there is a problem with stack/locals size, let ClassWriter compute them for you (use ClassWriter.COMPUTE_MAXS as constructor argument). The
	 * performance difference is not that big. */
	private final ClassWriter cw = new ClassWriter(0);
	public FieldVisitor fv;
	public MethodVisitor mv;

	protected AsmClassPool classPool;
	protected AsmNewClass asmClass;

	protected TypeBuilder declaringClassBuilder;

	protected TypeBuilder(AsmClassPool classPool, String typeSignature, AsmClass superClass, List<AsmClass> superInterfaces) {
		this.classPool = classPool;
		this.asmClass = new AsmNewClass(typeSignature, superClass, superInterfaces, classPool);

		this.classPool.registerPreliminaryClass(asmClass);
	}

	public AsmClassPool getClassPool() {
		return classPool;
	}

	protected String[] toInternalNames(List<AsmClass> classes) {
		if (classes == null) {
			return null;
		}

		String[] result = new String[classes.size()];

		int counter = 0;
		for (AsmClass ac : classes) {
			result[counter++] = ac.getInternalName();
		}

		return result;
	}

	public AsmNewClass getPreliminaryClass() {
		return asmClass;
	}

	public AsmNewClass build() {
		checkNoMethodBeingBuild();
		checkNoFieldBeingBuild();

		cw.visitEnd();
		asmClass.bytes = cw.toByteArray();
		classPool.registerFinishedNewClass(asmClass);

		return asmClass;
	}

	protected String getGetterName(String propertyName) {
		return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
	}

	protected String getSetterName(String propertyName) {
		return "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
	}

	public void addAbstractMethod(String name, AsmType returnType, AsmType... params) {
		addAbstractMethod(name, Collections.<AsmAnnotationInstance> emptyList(), returnType, params);
	}

	public void addAbstractMethod(String name, List<AsmAnnotationInstance> annotations, AsmType returnType, AsmType... params) {
		mv = visitMethodWithGenerics(Modifier.PUBLIC | Modifier.ABSTRACT, name, returnType, params);
		addMethodAnnotations(annotations);
		mv.visitEnd();
		notifyMethodFinished();
	}

	protected void addMethodAnnotations(List<AsmAnnotationInstance> annotations) {
		for (AsmAnnotationInstance aai : nullSafe(annotations))
			addMethodAnnotation(aai);
	}

	protected void addMethodAnnotation(AsmAnnotationInstance aai) {
		addMethodAnnotation(aai.getAnnotationClass().getInternalNameLonger(), aai.getAnnotationValues());
	}

	private void addMethodAnnotation(String annotationClassInternalNameLonger, Map<String, Object> annotationValues) {
		AnnotationVisitor av0 = mv.visitAnnotation(annotationClassInternalNameLonger, true);
		finalizeAnnotation(av0, annotationValues);
	}

	protected void addFieldAnnotations(List<AsmAnnotationInstance> annotations) {
		for (AsmAnnotationInstance aai : nullSafe(annotations))
			addFieldAnnotation(aai);
	}

	protected void addFieldAnnotation(AsmAnnotationInstance aai) {
		addFieldAnnotation(aai.getAnnotationClass().getInternalNameLonger(), aai.getAnnotationValues());
	}

	private void addFieldAnnotation(String annotationClassInternalNameLonger, Map<String, Object> annotationValues) {
		AnnotationVisitor av0 = fv.visitAnnotation(annotationClassInternalNameLonger, true);
		finalizeAnnotation(av0, annotationValues);
	}

	public void addAnnotation(AsmClass annotationClass) {
		AnnotationVisitor av = cw.visitAnnotation(annotationClass.getInternalNameLonger(), true);
		av.visitEnd();
	}

	public void addAnnotation(AsmAnnotationInstance aai) {
		addAnnotation(aai.getAnnotationClass().getInternalNameLonger(), aai.getAnnotationValues());
	}

	private void addAnnotation(String annotationClassInternalNameLonger, Map<String, Object> annotationValues) {
		AnnotationVisitor av1 = cw.visitAnnotation(annotationClassInternalNameLonger, true);
		finalizeAnnotation(av1, annotationValues);
	}

	private void finalizeAnnotation(AnnotationVisitor av1, Map<String, Object> annotationValues) {
		for (Entry<String, Object> entry : annotationValues.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();

			if (value == null)
				continue;

			if (!value.getClass().isArray())
				visitValue(av1, name, value);

			else {
				AnnotationVisitor av2 = av1.visitArray(name);
				for (Object o : (Object[]) value)
					visitValue(av2, null, o);
				av2.visitEnd();
			}
		}

		av1.visitEnd();
	}

	private void visitValue(AnnotationVisitor av1, String name, Object value) {
		if (value instanceof AsmAnnotationInstance) {
			/* reachable if we are visiting a container for a Repeatable annotation, in which case name="value" */
			AsmAnnotationInstance aai = (AsmAnnotationInstance) value;

			AnnotationVisitor av2 = av1.visitAnnotation(null, aai.getAnnotationClass().getInternalNameLonger());
			finalizeAnnotation(av2, aai.getAnnotationValues());

			return;
		}

		if (value instanceof ClassReference) {
			ClassReference castedValue = (ClassReference) value;
			value = Type.getObjectType(AsmUtils.toInternalName(castedValue.className));
		}

		av1.visit(name, value);
	}

	protected void visitSource(String fileName) {
		cw.visitSource(fileName, null);
	}

	protected void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		cw.visit(version, access, name, signature, superName, interfaces);
		asmClass.setModifiers(access);
	}

	protected void visitInnerClass(String name, String outerName, String innerName, int access) {
		cw.visitInnerClass(name, outerName, innerName, access);
	}

	private MethodDescriptor visitedMethod;
	private FieldDescriptor visitedField;

	public MethodVisitor visitConstructor(int access, AsmClass... params) {
		return cw.visitMethod(access, "<init>", AsmUtils.createMethodSignature(AsmClassPool.voidType, params), null, null);
	}

	public MethodVisitor visitMethod(int access, String name, AsmClass returnType, AsmClass... params) {
		return visitMethod(access, name, null, returnType, params);
	}

	public MethodVisitor visitMethodWithGenerics(int access, String name, AsmType returnType, AsmType... params) {
		String signature = AsmUtils.createMethodSignature(returnType, params);
		AsmClass asmReturnType = returnType.getRawType();
		AsmClass[] asmParams = AsmUtils.toAsmClasses(params);
		return visitMethod(access, name, signature, asmReturnType, asmParams);
	}

	private MethodVisitor visitMethod(int access, String name, String signature, AsmClass returnType, AsmClass... params) {
		checkNoMethodBeingBuild();
		visitedMethod = methodNeedsRegistration(name) ? new MethodDescriptor(access, name, returnType, params) : null;
		String desc = AsmUtils.createMethodSignature(returnType, params);
		return cw.visitMethod(access, name, desc, signature, null);
	}

	private void checkNoMethodBeingBuild() {
		if (visitedMethod != null) {
			throw new RuntimeException("Method '" + visitedMethod.name
					+ "' was not registered yet! Make sure the builder invokes 'notifyMethodFinished' after the code for given method is ready.");
		}
	}

	private boolean methodNeedsRegistration(String name) {
		return !(name.equals("<init>") || name.equals("<clinit>"));
	}

	String sss;

	/** Creates a <code>public static final</code> field according to given parameters. */
	public AsmField addConstantField(AsmClass type, String name, Object value) {
		visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, name, type, value);
		return notifyNewField();
	}

	public FieldVisitor visitField(int access, String name, AsmType type) {
		return visitField(access, name, type, null);
	}

	public FieldVisitor visitField(int access, String name, AsmType type, Object value) {
		checkNoFieldBeingBuild();
		visitedField = new FieldDescriptor(name, type);
		String desc = type.getRawType().getInternalNameLonger();
		String signature = type.genericSignatureOrNull();
		return fv = cw.visitField(access, name, desc, signature, value);
	}

	private void checkNoFieldBeingBuild() {
		if (visitedField != null) {
			throw new RuntimeException("Field '" + visitedField.name + "' was not registered yet!");
		}
	}

	public AsmMethod notifyMethodFinished() {
		if (visitedMethod == null) {
			throw new RuntimeException("No method is being built right now!");
		}

		MethodDescriptor vm = visitedMethod;
		AsmNewMethod result = AsmNewMethod.newRegisteredMethod(asmClass, vm.name, vm.returnType, vm.params, vm.access);
		visitedMethod = null;
		return result;
	}

	/** Non-Reflectable methods are for example (and for now noting else) bridge methods. */
	public void notifyNonReflectableMethodFinished() {
		if (visitedMethod == null) {
			throw new RuntimeException("No method is being built right now!");
		}
		visitedMethod = null;
	}

	public AsmField notifyNewField() {
		fv.visitEnd();

		FieldDescriptor fd = visitedField;
		AsmField result = AsmField.newRegisteredField(asmClass, fd.name, fd.type);
		visitedField = null;
		return result;
	}

	private static class MethodDescriptor {
		private final int access;
		private final String name;
		AsmClass returnType;
		AsmClass[] params;

		public MethodDescriptor(int access, String name, AsmClass returnType, AsmClass... params) {
			super();
			this.access = access;
			this.name = name;
			this.returnType = returnType;
			this.params = params;
		}
	}

	private static class FieldDescriptor {
		private final String name;
		private final AsmType type;

		public FieldDescriptor(String name, AsmType type) {
			super();
			this.name = name;
			this.type = type;
		}
	}

	protected void pushConstant(int value) {
		TypeBuilderUtils.pushConstant(mv, value);
	}

	public ClassBuilder makeNestedClass(String simpleName, AsmClass superClass, List<AsmClass> superInterfaces) {
		String name = asmClass.getName();
		String internaleName = asmClass.getInternalName();

		visitInnerClass(internaleName + "$" + simpleName, internaleName, simpleName, ACC_PUBLIC + ACC_STATIC);

		ClassBuilder result = new ClassBuilder(classPool, name + "$" + simpleName, false, superClass, superInterfaces);
		result.visitInnerClass(internaleName + "$" + simpleName, internaleName, simpleName, ACC_PUBLIC + ACC_STATIC);
		result.declaringClassBuilder = this;

		asmClass.registerNewInnerClass(result.asmClass);

		return result;
	}

	public InterfaceBuilder makeNestedInterface(String simpleName, List<AsmClass> superInterfaces) {
		String name = asmClass.getName();
		String internaleName = asmClass.getInternalName();

		visitInnerClass(internaleName + "$" + simpleName, internaleName, simpleName, ACC_PUBLIC + ACC_STATIC);

		for (AsmClass si : superInterfaces) {
			// sd = superDeclaring
			AsmClass sd = si.getDeclaringClass();
			if (sd != null) {
				// TODO test
				String superSimple = si.getName().substring(sd.getName().length() + 1);
				visitInnerClass(si.getInternalName(), sd.getInternalName(), superSimple, ACC_PUBLIC + ACC_STATIC);
			}
		}

		InterfaceBuilder result = new InterfaceBuilder(classPool, name + "$" + simpleName, superInterfaces);
		result.visitInnerClass(internaleName + "$" + simpleName, internaleName, simpleName, ACC_PUBLIC + ACC_STATIC);
		result.declaringClassBuilder = this;

		asmClass.registerNewInnerClass(result.asmClass);

		return result;
	}
}
