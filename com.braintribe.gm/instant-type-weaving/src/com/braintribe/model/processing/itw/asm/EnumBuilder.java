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

import java.util.List;

import com.braintribe.asm.MethodVisitor;
import com.braintribe.asm.Type;

/**
 * 
 */
public class EnumBuilder extends TypeBuilder {

	private static final String ENUM$VALUES = "ENUM$VALUES";
	private final AsmClass valuesClass;
	private final String internalName;
	private final String internalNameLonger;

	public EnumBuilder(AsmClassPool classPool, String typeSignature, List<AsmEnumConstant> constants, List<AsmClass> superInterfaces) {
		super(classPool, typeSignature, AsmClassPool.enumType, superInterfaces);
		String internalName = AsmUtils.toInternalName(typeSignature);
		String signature = "Ljava/lang/Enum<L" + internalName + ";>;";
		visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER + ACC_ENUM, internalName, signature, "java/lang/Enum", toInternalNames(superInterfaces));

		this.valuesClass = classPool.acquireArrayClass(asmClass);
		this.internalName = asmClass.getInternalName();
		this.internalNameLonger = asmClass.getInternalNameLonger();

		setConstants(constants);
	}

	private void setConstants(List<AsmEnumConstant> constants) {
		declareFields(constants);
		initializeFields(constants);
		addPrivateConstructor();
		implementValuesMethod();
		implementValueOfMethod();
	}

	private void declareFields(List<AsmEnumConstant> constants) {
		for (AsmEnumConstant constant : constants) {
			visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM, constant.name, asmClass);
			addFieldAnnotations(constant.annotations);
			notifyNewField();
		}

		visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC + ACC_SYNTHETIC, ENUM$VALUES, valuesClass);
		notifyNewField();
	}

	protected void initializeFields(List<AsmEnumConstant> constants) {
		mv = visitMethod(ACC_STATIC, "<clinit>", AsmClassPool.voidType);
		mv.visitCode();
		int counter = 0;
		for (AsmEnumConstant constant : constants) {
			mv.visitTypeInsn(NEW, internalName);
			mv.visitInsn(DUP);
			mv.visitLdcInsn(constant.name);
			pushConstant(counter++);
			mv.visitMethodInsn(INVOKESPECIAL, internalName, "<init>", "(Ljava/lang/String;I)V", false);
			mv.visitFieldInsn(PUTSTATIC, internalName, constant.name, internalNameLonger);
		}

		pushConstant(counter);
		mv.visitTypeInsn(ANEWARRAY, internalName);

		counter = 0;
		for (AsmEnumConstant constant : constants) {
			mv.visitInsn(DUP);
			pushConstant(counter++);
			mv.visitFieldInsn(GETSTATIC, internalName, constant.name, internalNameLonger);
			mv.visitInsn(AASTORE);
		}

		mv.visitFieldInsn(PUTSTATIC, internalName, ENUM$VALUES, valuesClass.getInternalNameLonger());

		initializeNonConstantFields(mv);

		mv.visitInsn(RETURN);
		mv.visitMaxs(clinitMaxStacks(), clinitMaxLocals());
		mv.visitEnd();
	}

	/**
	 * @param mv
	 *            visitor for the {@code <clinit>} method.
	 */
	protected void initializeNonConstantFields(@SuppressWarnings("unused") MethodVisitor mv) {
		// might be overridden by sub-type
	}

	/** Sub-types implementing {@link #initializeNonConstantFields(MethodVisitor)} might override the default this way */
	protected int clinitMaxStacks() {
		return 4;
	}

	/** Sub-types implementing {@link #initializeNonConstantFields(MethodVisitor)} might override the default this way */
	protected int clinitMaxLocals() {
		return 0;
	}

	private void addPrivateConstructor() {
		mv = visitMethod(ACC_PUBLIC, "<init>", AsmClassPool.voidType, AsmClassPool.stringType, AsmClassPool.intType);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ILOAD, 2);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Enum", "<init>", "(Ljava/lang/String;I)V", false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(3, 3);
		mv.visitEnd();
	}

	private void implementValuesMethod() {
		mv = visitMethod(ACC_PUBLIC + ACC_STATIC, "values", valuesClass);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, internalName, ENUM$VALUES, valuesClass.getInternalNameLonger());
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 0);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ARRAYLENGTH);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ISTORE, 1);
		mv.visitTypeInsn(ANEWARRAY, internalName);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 2);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ILOAD, 1);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(5, 3);
		mv.visitEnd();
		notifyMethodFinished();
	}

	private void implementValueOfMethod() {
		mv = visitMethod(ACC_PUBLIC + ACC_STATIC, "valueOf", asmClass, AsmClassPool.stringType);
		mv.visitCode();
		mv.visitLdcInsn(Type.getType(internalNameLonger));
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Enum", "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", false);
		mv.visitTypeInsn(CHECKCAST, internalName);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
		notifyMethodFinished();
	}

}
