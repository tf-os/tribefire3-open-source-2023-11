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

import static com.braintribe.model.processing.itw.asm.AsmClassPool.objectType;
import static com.braintribe.model.processing.itw.asm.AsmClassPool.voidType;

import java.lang.reflect.Modifier;

import com.braintribe.model.processing.itw.asm.AsmField;
import com.braintribe.model.processing.itw.asm.AsmMethod;
import com.braintribe.model.processing.itw.asm.AsmUtils;
import com.braintribe.model.processing.itw.asm.ClassBuilder;
import com.braintribe.model.processing.itw.synthesis.gm.PreliminaryProperty;
import com.braintribe.model.processing.itw.synthesis.java.PropertyAnalysis.PropertyDescription;
import com.braintribe.model.processing.itw.tools.ItwTools;

public class PlainEntityImplementer extends AbstractGenericEntityImplementer {

	public PlainEntityImplementer(ClassBuilder b, GmClassPool gcp) {
		super(b, gcp);
	}

	public void createAndStorePropertyField(PropertyDescription pd) {
		pd.plainPropertyField = b.addField(pd.getFieldName(), pd.accessPropertyClass, Modifier.PRIVATE);
	}

	public void createSetterGetterImplementation(PreliminaryProperty pp, PropertyDescription pd) {
		pp.plainGetter = b.addGetter(pd.plainPropertyField, pd.getterName);
		pp.plainSetter = b.addSetter(pd.plainPropertyField, pd.setterName);
	}

	public void addPlainRead(PropertyDescription pd) {
		AsmField f = pd.plainPropertyField;

		// method: public Object readProperty()
		mv = b.visitMethod(ACC_PUBLIC, ItwTools.getReaderName(pd.property), objectType);
		mv.visitCode();

		// return this.property;
		mv.visitVarInsn(ALOAD, 0);
		getInstanceField(f);
		addConversionFromPrimitiveIfNecessary(f.getType());
		mv.visitInsn(ARETURN);

		mv.visitMaxs(AsmUtils.getSize(f.getType()), 1);
		mv.visitEnd();
		b.notifyMethodFinished();
	}

	public void addPlainWrite(PropertyDescription pd) {
		AsmField f = pd.plainPropertyField;

		// method: public void writeProperty(Object value)
		mv = b.visitMethod(ACC_PUBLIC, ItwTools.getWriterName(pd.property), voidType, objectType);
		mv.visitCode();

		// this.property = (${propertyFieldType}) value;
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		checkCast(pd.actualPropertyClass);
		addConversionToPrimitiveIfEligible(f.getType());
		putInstanceField(f);

		// result;
		mv.visitInsn(RETURN);

		mv.visitMaxs(1 + AsmUtils.getSize(f.getType()), 2);
		mv.visitEnd();
		b.notifyMethodFinished();
	}

	@Override
	protected AsmMethod getter(PreliminaryProperty pp) {
		return pp.plainGetter;
	}

	@Override
	protected AsmMethod setter(PreliminaryProperty pp) {
		return pp.plainSetter;
	}

}
