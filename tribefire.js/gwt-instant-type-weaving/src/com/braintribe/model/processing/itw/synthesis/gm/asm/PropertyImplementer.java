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
package com.braintribe.model.processing.itw.synthesis.gm.asm;

import static com.braintribe.model.processing.itw.asm.AsmClassPool.booleanType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.objectType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.stringType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.voidType;

import com.braintribe.asm.Opcodes;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmClassPool;
import com.braintribe.model.processing.itw.asm.AsmField;
import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.asm.AsmUtils;
import com.braintribe.model.processing.itw.asm.ClassBuilder;
import com.braintribe.model.processing.itw.synthesis.gm.PreliminaryEntityType;
import com.braintribe.model.processing.itw.synthesis.gm.PreliminaryProperty;
import com.braintribe.model.processing.itw.tools.ItwTools;
import com.braintribe.model.weaving.ProtoGmProperty;

/**
 * Implements non-trivial methods defined by {@link EnhancedEntity} interfaces. (Non-trivial means it is not a simple getter/setter of some
 * field). It also implements the {@link #toString()} method for enhanced entity.
 */
public class PropertyImplementer extends AbstractClassBuilder {

	public static final String SINGLETON_NAME = "INSTANCE";

	private static String superConstructorSignature;

	public PropertyImplementer(ClassBuilder b, GmClassPool gcp) {
		super(b, gcp);
	}

	public void addConstructor(PreliminaryEntityType pet, PreliminaryProperty pp) {
		mv = b.visitMethod(ACC_PRIVATE, "<init>", voidType);
		mv.visitCode();

		AsmNewClass entityTypeClass = pet.entityTypeImplementer.getPreliminaryClass();

		// super(${EntityType}.INSTANCE, null, ${propertyName}, isId, isNullable, null)
		mv.visitVarInsn(ALOAD, 0); // this ('super')
		AsmField declaringEntityTypeField = entityTypeClass.getDeclaredField(EntityTypeImplementer.SINGLETON_NAME);
		getStaticField(declaringEntityTypeField);
		mv.visitLdcInsn(pp.propertyName);
		pushConstant(pp.nullable);
		pushConstant(pp.confidential);
		mv.visitMethodInsn(INVOKESPECIAL, gcp.jvmPropertyType.getInternalName(), "<init>", superConstructorSignature(), false);
		mv.visitInsn(RETURN);

		mv.visitMaxs(5, 1);
		mv.visitEnd();
	}

	private String superConstructorSignature() {
		if (superConstructorSignature == null)
			superConstructorSignature = AsmUtils.createMethodSignature(voidType, gcp.entityTypeType, stringType, booleanType, booleanType);

		return superConstructorSignature;
	}

	public void addStaticSingletonField() {
		// public static final ${TypeOfPropertyClass} INSTANCE
		b.addField(SINGLETON_NAME, asmClass, ACC_PUBLIC + ACC_FINAL + ACC_STATIC);
	}

	public void addClassInitialization() {
		mv = b.visitMethod(Opcodes.ACC_STATIC, "<clinit>", AsmClassPool.voidType);
		mv.visitCode();

		// INSTANCE = new ${TypeOfPropertyClass}();
		newInstance_DefaultConstructor(asmClass);
		mv.visitFieldInsn(PUTSTATIC, asmClass.getInternalName(), SINGLETON_NAME, asmClass.getInternalNameLonger());
		mv.visitInsn(RETURN);

		mv.visitMaxs(2, 0);
		mv.visitEnd();
	}

	public void addGetDirectUnsafe(ProtoGmProperty gmProperty, AsmClass weakInterface) {
		// method: Object getDirectUnsafe(GenericEntity entity)
		mv = b.visitMethod(ACC_PUBLIC, "getDirectUnsafe", objectType, gcp.genericEntityType);
		mv.visitCode();

		// return (T) ((${WeakInterface})entity).${readerName}();
		mv.visitVarInsn(ALOAD, 1);
		checkCast(weakInterface);
		invokeMethod(weakInterface.getMethod(ItwTools.getReaderName(gmProperty), objectType));
		mv.visitInsn(ARETURN);

		mv.visitMaxs(1, 2);
		mv.visitEnd();
		b.notifyMethodFinished();
	}

	public void addSetDirectUnsafe(ProtoGmProperty gmProperty, AsmClass weakInterface) {
		// method: void setDirectUnsafe(GenericEntity entity, Object value)
		mv = b.visitMethod(ACC_PUBLIC, "setDirectUnsafe", voidType, gcp.genericEntityType, objectType);
		mv.visitCode();

		// ((${WeakInterface}) entity).${writerName}(value);
		mv.visitVarInsn(ALOAD, 1);
		checkCast(weakInterface);
		mv.visitVarInsn(ALOAD, 2);
		invokeMethod(weakInterface.getMethod(ItwTools.getWriterName(gmProperty), voidType, objectType));
		mv.visitInsn(RETURN);

		mv.visitMaxs(2, 3);
		mv.visitEnd();
		b.notifyMethodFinished();
	}

}
