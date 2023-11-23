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

import static com.braintribe.model.processing.itw.asm.AsmClassPool.booleanObjectType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.booleanType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.byteObjectType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.byteType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.charObjectType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.charType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.doubleObjectType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.doubleType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.floatObjectType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.floatType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.intObjectType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.intType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.longObjectType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.longType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.shortObjectType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.shortType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.voidType;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.asm.Opcodes;
import com.braintribe.asm.Type;

/**
 * 
 */
public class AsmUtils {

	private static final Map<AsmClass, Type> primitiveTypes = new HashMap<AsmClass, Type>();
	private static final Map<AsmClass, AsmClass> primitiveToObjectTypeMapping = new HashMap<AsmClass, AsmClass>();

	static {
		primitiveTypes.put(byteType, Type.BYTE_TYPE);
		primitiveTypes.put(shortType, Type.SHORT_TYPE);
		primitiveTypes.put(intType, Type.INT_TYPE);
		primitiveTypes.put(longType, Type.LONG_TYPE);
		primitiveTypes.put(floatType, Type.FLOAT_TYPE);
		primitiveTypes.put(doubleType, Type.DOUBLE_TYPE);
		primitiveTypes.put(booleanType, Type.BOOLEAN_TYPE);
		primitiveTypes.put(charType, Type.CHAR_TYPE);
		primitiveTypes.put(voidType, Type.VOID_TYPE);

		primitiveToObjectTypeMapping.put(byteType, byteObjectType);
		primitiveToObjectTypeMapping.put(shortType, shortObjectType);
		primitiveToObjectTypeMapping.put(intType, intObjectType);
		primitiveToObjectTypeMapping.put(longType, longObjectType);
		primitiveToObjectTypeMapping.put(floatType, floatObjectType);
		primitiveToObjectTypeMapping.put(doubleType, doubleObjectType);
		primitiveToObjectTypeMapping.put(booleanType, booleanObjectType);
		primitiveToObjectTypeMapping.put(charType, charObjectType);
	}

	private static final Type OBJECT_TYPE = Type.getObjectType(Object.class.getName());

	public static int getReturnOpCode(AsmClass asmClass) {
		return getType(asmClass).getOpcode(Opcodes.IRETURN);
	}

	public static int getLoadOpCode(AsmType asmType) {
		return getLoadOpCode(asmType.getRawType());
	}

	public static int getLoadOpCode(AsmClass asmClass) {
		return getType(asmClass).getOpcode(Opcodes.ILOAD);
	}

	public static Type getType(AsmClass asmClass) {
		if (asmClass.isPrimitive())
			return primitiveTypes.get(asmClass);
		else if (asmClass instanceof AsmArray)
			throw new UnsupportedOperationException("Array type is not supported!");
		else
			return OBJECT_TYPE;
	}

	public static int getVirtualInvocationCode(AsmClass asmClass) {
		return asmClass.isInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL;
	}

	public static String toInternalNameLonger(String className) {
		return wrapInternalName(toInternalName(className));
	}

	public static String toInternalName(String className) {
		return className.replace('.', '/');
	}

	public static String fromInternalName(String className) {
		return className.replace('/', '.');
	}

	public static String createMethodSignature(AsmType returnType, AsmType... params) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		if (params != null)
			for (AsmType ac : params)
				sb.append(ac.getInternalNameLonger());

		sb.append(")");
		sb.append(returnType.getInternalNameLonger());
		return sb.toString();
	}

	public static String getInternalName(AsmClass asmClass) {
		if (asmClass.isPrimitive())
			return AsmUtils.getPrimitiveInternalName(asmClass);
		else if (asmClass.isArray())
			return asmClass.getName();
		else
			return toInternalName(asmClass.getName());
	}

	/** including the L and semicolon, if necessary (object) */
	public static String getInternalNameLonger(AsmClass asmClass) {
		if (asmClass.isPrimitive())
			return AsmUtils.getPrimitiveInternalName(asmClass);
		else if (asmClass.isArray())
			return asmClass.getName();
		else
			return toInternalNameLonger(asmClass.getName());
	}

	public static String wrapInternalName(String internaleName) {
		return "L" + internaleName + ";";
	}

	public static String getPrimitiveInternalName(AsmClass asmClass) {
		return primitiveTypes.get(asmClass).getDescriptor();
	}

	public static AsmClass primitiveToObjectType(AsmType primitive) {
		return primitiveToObjectTypeMapping.get(primitive);
	}

	public static AsmExistingClass[] toAsmClasses(AsmClassPool cp, Class<?>... classes) {
		AsmExistingClass[] result = new AsmExistingClass[classes.length];

		for (int i = 0; i < classes.length; i++) {
			AsmExistingClass clazz = cp.get(classes[i]);
			result[i] = clazz;
		}

		return result;
	}

	public static AsmClass[] toAsmClasses(AsmType[] typeDescriptions) {
		AsmClass[] result = new AsmClass[typeDescriptions.length];

		int i = 0;
		for (AsmType td : typeDescriptions)
			result[i++] = td.getRawType();

		return result;
	}

	public static String getSrcClassReference(AsmClass clazz) {
		AsmClass declaringClass = clazz.getDeclaringClass();
		if (declaringClass == null)
			return clazz.getName();

		String declaringClassName = declaringClass.getName();
		String className = clazz.getName();

		String simpleClassName = className.substring(declaringClassName.length() + 1);
		String qualifiedName = getSrcClassReference(declaringClass) + "." + simpleClassName;
		return qualifiedName;
	}

	public static int getSize(AsmType asmType) {
		if (asmType.isPrimitive())
			if (asmType.equals(AsmClassPool.doubleType) || (asmType.equals(AsmClassPool.longType)))
				return 2;
		return 1;
	}

}
