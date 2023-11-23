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

import static com.braintribe.model.processing.itw.tools.ItwTools.getGetterName;
import static com.braintribe.model.processing.itw.tools.ItwTools.getSetterName;

import java.lang.reflect.Modifier;

import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmField;
import com.braintribe.model.processing.itw.asm.AsmMethod;
import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.asm.ClassBuilder;
import com.braintribe.model.processing.itw.synthesis.gm.PreliminaryEntityType;
import com.braintribe.model.processing.itw.synthesis.gm.PreliminaryProperty;
import com.braintribe.model.processing.itw.tools.ItwTools;

/**
 * @author peter.gazdik
 */
public abstract class AbstractGenericEntityImplementer extends AbstractClassBuilder {

	public AbstractGenericEntityImplementer(ClassBuilder b, GmClassPool gcp) {
		super(b, gcp);
	}

	public void addType_WithBridge(PreliminaryEntityType pet) {
		AsmNewClass entityTypeClass = pet.entityTypeImplementer.getPreliminaryClass();
		AsmField singletonField = entityTypeClass.getDeclaredField(EntityTypeImplementer.SINGLETON_NAME);

		// method: EntityType<?> type()
		mv = b.visitMethod(ACC_PUBLIC, "type", gcp.entityTypeType);
		mv.visitCode();

		// return ${EntitySignature}-et.INSTANCE;
		getStaticField(singletonField);
		mv.visitInsn(ARETURN);

		mv.visitMaxs(1, 1);
		mv.visitEnd();
		AsmMethod typeMethod = b.notifyMethodFinished();

		/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

		// method: GenericModelType type()
		mv = b.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "type", gcp.genericModelTypeType);
		mv.visitCode();

		// return this.type(); // this is the type method defined above
		mv.visitVarInsn(ALOAD, 0);
		invokeMethod(typeMethod);
		mv.visitInsn(ARETURN);

		mv.visitMaxs(1, 1);
		mv.visitEnd();
		b.notifyNonReflectableMethodFinished();
	}

	protected abstract AsmMethod getter(PreliminaryProperty pp);
	
	protected abstract AsmMethod setter(PreliminaryProperty pp);

	public void addTransientGetterSetter(String propertyName, AsmClass propertyType) {
		AsmField field = b.addField(ItwTools.getFieldName(propertyName), propertyType, Modifier.PUBLIC);

		b.addGetter(field, getGetterName(propertyName));
		b.addSetter(field, getSetterName(propertyName));
	}

}
