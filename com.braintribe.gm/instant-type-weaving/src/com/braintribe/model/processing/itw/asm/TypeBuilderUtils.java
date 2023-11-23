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

import static com.braintribe.model.processing.itw.asm.AsmClassPool.booleanType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.doubleType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.floatType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.intType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.longType;

import com.braintribe.asm.MethodVisitor;
import com.braintribe.asm.Opcodes;

/**
 *
 */
public class TypeBuilderUtils implements Opcodes {

	public static void newInstance_DefaultConstructor(MethodVisitor mv, AsmClass type) {
		mv.visitTypeInsn(NEW, type.getInternalName());
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, type.getInternalName(), "<init>", "()V", false);
	}

	public static void addConversionFromPrimitiveIfNecessary(MethodVisitor mv, AsmType primitive) {
		if (primitive.isPrimitive()) {
			AsmClass object = AsmUtils.primitiveToObjectType(primitive);
			// like e.g. (I)Ljava.lang.Integer;
			String signature = AsmUtils.createMethodSignature(object, primitive);
			mv.visitMethodInsn(INVOKESTATIC, object.getInternalName(), "valueOf", signature, false);
		}
	}

	public static void addConversionToPrimitiveIfEligible(MethodVisitor mv, AsmType primitive) {
		if (primitive.isPrimitive()) {
			AsmClass object = AsmUtils.primitiveToObjectType(primitive);
			// like e.g. ()I
			String signature = AsmUtils.createMethodSignature(primitive);
			// like e.g. intValue
			String methodName = ((AsmExistingClass) primitive).getJavaType().getSimpleName() + "Value";
			mv.visitMethodInsn(INVOKEVIRTUAL, object.getInternalName(), methodName, signature, false);
		}
	}

	public static void pushDefault(MethodVisitor mv, AsmClass primitiveClazz) {
		if (primitiveClazz == intType || primitiveClazz == booleanType) {
			mv.visitInsn(ICONST_0);

		} else if (primitiveClazz == longType) {
			mv.visitInsn(LCONST_0);

		} else if (primitiveClazz == floatType) {
			mv.visitInsn(FCONST_0);

		} else if (primitiveClazz == doubleType) {
			mv.visitInsn(DCONST_0);

		} else {
			throw new UnsupportedOperationException("Unsupported primitive type: " + primitiveClazz.getName());
		}
	}

	public static void pushConstant(MethodVisitor mv, int value) {
		if (-128 <= value && value <= 127) {
			mv.visitIntInsn(BIPUSH, value);

		} else if (-32768 <= value && value <= 32767) {
			mv.visitIntInsn(SIPUSH, value);

		} else {
			mv.visitLdcInsn(value);
		}
	}

	public static void pushConstant(MethodVisitor mv, boolean value) {
		mv.visitInsn(value ? ICONST_1 : ICONST_0);
	}

	public static void invokeMethod(MethodVisitor mv, AsmMethod asmMethod) {
		AsmClass declaringClass = asmMethod.getDeclaringClass();
		if (declaringClass.isInterface()) {
			mv.visitMethodInsn(INVOKEINTERFACE, declaringClass.getInternalName(), asmMethod.getName(), asmMethod.getSignature(), true);
		} else {
			mv.visitMethodInsn(INVOKEVIRTUAL, declaringClass.getInternalName(), asmMethod.getName(), asmMethod.getSignature(), false);
		}
	}

	public static void invokePrivateMethod(MethodVisitor mv, AsmMethod asmMethod) {
		mv.visitMethodInsn(INVOKESPECIAL, asmMethod.getDeclaringClass().getInternalName(), asmMethod.getName(), asmMethod.getSignature(),
				false);
	}

	/**
	 * Adds an instruction that retrieves an instance (i.e. not static) field.
	 */
	public static void getInstanceField(MethodVisitor mv, AsmField field) {
		mv.visitFieldInsn(GETFIELD, field.getDeclaringClass().getInternalName(), field.getName(),
				field.getRawType().getInternalNameLonger());
	}

	/**
	 * Adds and instruction that stores an instance (i.e. not static) field.
	 */
	public static void putInstanceField(MethodVisitor mv, AsmField field) {
		mv.visitFieldInsn(PUTFIELD, field.getDeclaringClass().getInternalName(), field.getName(),
				field.getRawType().getInternalNameLonger());
	}

	/**
	 * Adds an instruction that retrieves a static field.
	 */
	public static void getStaticField(MethodVisitor mv, AsmField field) {
		mv.visitFieldInsn(GETSTATIC, field.getDeclaringClass().getInternalName(), field.getName(),
				field.getRawType().getInternalNameLonger());
	}

	/**
	 * Adds an instruction that retrieves a static field. Does not work with primitive types.
	 */
	public static void getStaticField(MethodVisitor mv, String declaringClass, String fieldName, String fieldType) {
		mv.visitFieldInsn(GETSTATIC, AsmUtils.toInternalName(declaringClass), fieldName, AsmUtils.toInternalNameLonger(fieldType));
	}

	/**
	 * Adds and instruction that stores a static field.
	 */
	public static void putStaticField(MethodVisitor mv, AsmField field) {
		mv.visitFieldInsn(PUTSTATIC, field.getDeclaringClass().getInternalName(), field.getName(),
				field.getRawType().getInternalNameLonger());
	}

	/**
	 * Adds checkCast instruction.
	 */
	public static void checkCast(MethodVisitor mv, AsmClass asmClass) {
		mv.visitTypeInsn(CHECKCAST, asmClass.getInternalName());
	}

}
