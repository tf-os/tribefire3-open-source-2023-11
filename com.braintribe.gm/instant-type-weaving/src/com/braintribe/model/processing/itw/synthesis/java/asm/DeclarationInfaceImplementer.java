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
package com.braintribe.model.processing.itw.synthesis.java.asm;

import com.braintribe.asm.Opcodes;
import com.braintribe.asm.Type;
import com.braintribe.model.processing.itw.asm.AsmClassPool;
import com.braintribe.model.processing.itw.asm.AsmField;
import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.asm.AsmType;
import com.braintribe.model.processing.itw.asm.GenericAsmClass;
import com.braintribe.model.processing.itw.asm.InterfaceBuilder;

/**
 * @author peter.gazdik
 */
public class DeclarationInfaceImplementer extends AbstractTypeBuilder<InterfaceBuilder> {

	public DeclarationInfaceImplementer(InterfaceBuilder b, AsmClassPool asmClassPool) {
		super(b, asmClassPool);
	}

	public void implementEntityTypeLiteral(AsmNewClass declarationIface) {
		AsmType type = new GenericAsmClass(AsmClassPool.entityTypeType, declarationIface);

		b.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "T", type, null);
		AsmField entityTypeField = b.notifyNewField();

		mv = b.visitMethod(Opcodes.ACC_STATIC, "<clinit>", AsmClassPool.voidType);
		mv.visitCode();

		mv.visitLdcInsn(Type.getType(declarationIface.getInternalNameLonger()));
		mv.visitMethodInsn(INVOKESTATIC, "com/braintribe/model/generic/reflection/EntityTypes", "T",
				"(Ljava/lang/Class;)Lcom/braintribe/model/generic/reflection/EntityType;", false);
		putStaticField(entityTypeField);

		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 0);
		mv.visitEnd();
	}

}
