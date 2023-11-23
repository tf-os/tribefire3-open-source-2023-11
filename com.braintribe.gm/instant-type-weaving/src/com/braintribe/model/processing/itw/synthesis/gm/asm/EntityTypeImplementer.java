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

import static com.braintribe.model.processing.itw.asm.AsmClassPool.stringType;

import com.braintribe.asm.Opcodes;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmClassPool;
import com.braintribe.model.processing.itw.asm.AsmMethod;
import com.braintribe.model.processing.itw.asm.AsmUtils;
import com.braintribe.model.processing.itw.asm.ClassBuilder;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesis;
import com.braintribe.model.processing.itw.synthesis.gm.PreliminaryEntityType;
import com.braintribe.model.processing.itw.synthesis.gm.template.Template;
import com.braintribe.model.processing.itw.synthesis.gm.template.VariableResolver;

public class EntityTypeImplementer extends AbstractClassBuilder {

	public static final String SINGLETON_NAME = "INSTANCE";

	public EntityTypeImplementer(ClassBuilder b, GmClassPool gcp) {
		super(b, gcp);
	}

	public void addConstructor() {
		b.addDefaultConstructor();
	}

	public void addStaticSingletonField() {
		// public static final ${TypeOfEntityType} INSTANCE
		b.addField(SINGLETON_NAME, asmClass, ACC_PUBLIC + ACC_FINAL + ACC_STATIC);
	}

	public void addClassInitialization() {
		mv = b.visitMethod(Opcodes.ACC_STATIC, "<clinit>", AsmClassPool.voidType);
		mv.visitCode();

		// INSTANCE = new ${TypeOfEntityType}();
		newInstance_DefaultConstructor(asmClass);
		mv.visitFieldInsn(PUTSTATIC, asmClass.getInternalName(), SINGLETON_NAME, asmClass.getInternalNameLonger());
		mv.visitInsn(RETURN);

		mv.visitMaxs(2, 0);
		mv.visitEnd();
	}

	public void addCreatePlainRaw_WithBridge(AsmClass plainClass) {
		// method: ${PlainClass} createPlainRaw()
		mv = b.visitMethod(ACC_PUBLIC, "createPlainRaw", plainClass);
		mv.visitCode();

		// return new ${PlainClass}
		newInstance_DefaultConstructor(plainClass);
		mv.visitInsn(ARETURN);

		mv.visitMaxs(2, 1);
		mv.visitEnd();

		AsmMethod createPlainRawMethod = b.notifyMethodFinished();

		/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

		// method: GenericEntity createPlainRaw()
		mv = b.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "createPlainRaw", gcp.genericEntityType);
		mv.visitCode();

		// return this.createPlainRaw();
		mv.visitVarInsn(ALOAD, 0);
		invokeMethod(createPlainRawMethod);
		mv.visitInsn(ARETURN);

		mv.visitMaxs(1, 1);
		mv.visitEnd();

		b.notifyNonReflectableMethodFinished();
	}

	public void addCreateRaw_WithBridge(AsmClass enhancedClass) {
		// method: ${EnhancedClass} createRaw()
		mv = b.visitMethod(ACC_PUBLIC, "createRaw", enhancedClass);
		mv.visitCode();

		// return new ${EnhancedClass}(pai);
		mv.visitTypeInsn(NEW, enhancedClass.getInternalName());
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_0);
		mv.visitMethodInsn(INVOKESPECIAL, enhancedClass.getInternalName(), "<init>",
				AsmUtils.createMethodSignature(AsmClassPool.voidType, AsmClassPool.booleanType), false);
		mv.visitInsn(ARETURN);

		mv.visitMaxs(3, 1);
		mv.visitEnd();
		AsmMethod createRawMethod = b.notifyMethodFinished();

		/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

		// method: GenericEntity createRaw()
		mv = b.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "createRaw", gcp.genericEntityType);
		mv.visitCode();

		// return createRaw(pai);
		mv.visitVarInsn(ALOAD, 0);
		invokeMethod(createRawMethod);
		mv.visitInsn(ARETURN);

		mv.visitMaxs(2, 1);
		mv.visitEnd();
		b.notifyNonReflectableMethodFinished();
	}

	public void addIsInstanceMethod(AsmClass entityIface) {
		// method: boolean isInstance(Object value);
		mv = b.visitMethod(ACC_PUBLIC, "isInstance", AsmClassPool.booleanType, AsmClassPool.objectType);
		mv.visitCode();

		// return value instanceof ${EntityIface}
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(INSTANCEOF, entityIface.getInternalName());
		mv.visitInsn(IRETURN);

		mv.visitMaxs(1, 2);
		mv.visitEnd();
		b.notifyMethodFinished();
	}

	// ###########################################
	// ## . . . . . . . ToSting . . . . . . . . ##
	// ###########################################

	public void addToStringMethodIfEligible(GenericModelTypeSynthesis gmts, PreliminaryEntityType pet) {
		if (pet.toStringAnnotation == null)
			return;

		mv = b.visitMethod(ACC_PUBLIC, "toString", stringType, gcp.genericEntityType);
		weaveStringifier(gmts, pet, pet.toStringAnnotation.value(), false);
	}

	public void addToSelectiveInformationIfEligible(GenericModelTypeSynthesis gmts, PreliminaryEntityType pet) {
		SelectiveInformation sia = pet.selectiveInformationAnnotation;
		if (sia == null)
			return;

		mv = b.visitMethod(ACC_PROTECTED, "getSelectiveInformationFor", stringType, gcp.genericEntityType);
		weaveStringifier(gmts, pet, sia.value(), true);
	}

	private void weaveStringifier(GenericModelTypeSynthesis gmts, PreliminaryEntityType pet, String templateSource, boolean selective) {
		mv.visitCode();
		mv.visitTypeInsn(NEW, "com/braintribe/model/processing/itw/tools/ItwStringBuilder");
		mv.visitInsn(DUP);
		mv.visitInsn(selective ? ICONST_1 : ICONST_0);
		mv.visitMethodInsn(INVOKESPECIAL, "com/braintribe/model/processing/itw/tools/ItwStringBuilder", "<init>", "(Z)V", false);

		Template template = Template.parse(gmts, pet.gmEntityType, templateSource);
		template.merge(mv, new VariableResolver(pet, gcp));

		mv.visitMethodInsn(INVOKEVIRTUAL, "com/braintribe/model/processing/itw/tools/ItwStringBuilder", "toString", "()Ljava/lang/String;", false);

		mv.visitInsn(ARETURN);
		/* There is always 2 variables -> this (EntityType) and entity (GenericEntity)
		 * 
		 * The max number for stack is 6, before property is retrieved: StringBuilder, entity, Array, Array, arrayIndex, Property.INSTANCE */
		mv.visitMaxs(6, 2);
		mv.visitEnd();
		b.notifyMethodFinished();
	}

}
