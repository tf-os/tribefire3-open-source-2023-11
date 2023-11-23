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
import java.util.List;

import com.braintribe.model.processing.itw.asm.AsmClassPool;
import com.braintribe.model.processing.itw.asm.AsmField;
import com.braintribe.model.processing.itw.asm.AsmMethod;
import com.braintribe.model.processing.itw.asm.AsmType;
import com.braintribe.model.processing.itw.asm.AsmUtils;
import com.braintribe.model.processing.itw.asm.ClassBuilder;
import com.braintribe.model.processing.itw.synthesis.gm.PreliminaryProperty;
import com.braintribe.model.processing.itw.synthesis.java.PropertyAnalysis.PropertyDescription;
import com.braintribe.model.processing.itw.tools.ItwTools;

public class EnhancedEntityImplementer extends AbstractGenericEntityImplementer {

	public EnhancedEntityImplementer(ClassBuilder b, GmClassPool gcp) {
		super(b, gcp);
	}

	public void addBooleanConstructor() {
		mv = b.visitConstructor(ACC_PUBLIC, AsmClassPool.booleanType);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ILOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, asmClass.getSuperClass().getInternalName(), "<init>", "(Z)V", false);
		mv.visitInsn(RETURN);

		mv.visitMaxs(2, 2);
		mv.visitEnd();
	}

	// public void addPaiConstructor() {
	// mv = b.visitConstructor(ACC_PUBLIC, gcp.propertyAccessInterceptorType);
	// mv.visitCode();
	//
	// mv.visitVarInsn(ALOAD, 0);
	// mv.visitVarInsn(ALOAD, 1);
	// mv.visitMethodInsn(INVOKESPECIAL, asmClass.getSuperClass().getInternalName(), "<init>",
	// "(Lcom/braintribe/model/generic/reflection/PropertyAccessInterceptor;)V", false);
	// mv.visitInsn(RETURN);
	//
	// mv.visitMaxs(2, 2);
	// mv.visitEnd();
	// }
	//
	public void addAopAroundGetterSetter(String propertyClassName, PreliminaryProperty pp, PropertyDescription pd) {
		pp.aopGetter = addAopAroundGetter(propertyClassName, pd);
		pp.aopSetter = addAopAroundSetter(propertyClassName, pd);
	}

	private AsmMethod addAopAroundGetter(String propertyClassName, PropertyDescription pd) {
		AsmType accessPropertyClass = pd.accessPropertyClass;
		AsmType actualPropertyClass = pd.actualPropertyClass;

		// method: %{accessPropertyClass} get${PropertyName}()
		mv = b.visitMethodWithGenerics(ACC_PUBLIC, pd.getterName, accessPropertyClass);
		mv.visitCode();

		// return (${actualPropertyClass}) pai.getProperty(${PropertyClass}.INSTANCE, this);
		mv.visitVarInsn(ALOAD, 0);
		getInstanceField(gcp.paiField);
		getStaticField(propertyClassName, PropertyImplementer.SINGLETON_NAME, propertyClassName);
		mv.visitVarInsn(ALOAD, 0);
		pushConstant(false);
		invokeMethod(gcp.pai_getPropertyMethod);
		checkCast(actualPropertyClass);
		addConversionToPrimitiveIfEligible(accessPropertyClass);
		addReturn(accessPropertyClass);

		mv.visitMaxs(4, 1);
		mv.visitEnd();

		return b.notifyMethodFinished();
	}

	private AsmMethod addAopAroundSetter(String propertyClassName, PropertyDescription pd) {
		AsmType accessPropertyClass = pd.accessPropertyClass;

		// method: void set${PropertyName}(%{accessPropertyClass} value)
		mv = b.visitMethodWithGenerics(ACC_PUBLIC, pd.setterName, AsmClassPool.voidType, accessPropertyClass);
		mv.visitCode();

		// pai.setProperty(${PropertyClass}.INSTANCE, this, value);
		mv.visitVarInsn(ALOAD, 0);
		getInstanceField(gcp.paiField);
		getStaticField(propertyClassName, PropertyImplementer.SINGLETON_NAME, propertyClassName);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(AsmUtils.getLoadOpCode(accessPropertyClass), 1);
		addConversionFromPrimitiveIfNecessary(accessPropertyClass);
		pushConstant(false);
		invokeMethod(gcp.pai_setPropertyMethod);
		mv.visitInsn(POP);
		mv.visitInsn(RETURN);

		mv.visitMaxs(4 + AsmUtils.getSize(accessPropertyClass), 1 + AsmUtils.getSize(accessPropertyClass));
		mv.visitEnd();

		return b.notifyMethodFinished();
	}

	public void createAndStorePropertyField(PropertyDescription pd) {
		pd.enhancedPropertyField = b.addField(pd.getFieldName(), objectType, Modifier.PROTECTED);
	}

	public void addEnhancedRead(PropertyDescription pd) {
		AsmField f = pd.enhancedPropertyField;

		// method: public Object readProperty()
		mv = b.visitMethod(ACC_PUBLIC, ItwTools.getReaderName(pd.property), objectType);
		mv.visitCode();

		// return this.property;
		mv.visitVarInsn(ALOAD, 0);
		getInstanceField(f);
		mv.visitInsn(ARETURN);

		mv.visitMaxs(1, 1);
		mv.visitEnd();
		b.notifyMethodFinished();
	}

	public void addEnhancedWrite(PropertyDescription pd) {
		AsmField f = pd.enhancedPropertyField;

		// method: public void writeProperty(Object value)
		mv = b.visitMethod(ACC_PUBLIC, ItwTools.getWriterName(pd.property), voidType, objectType);
		mv.visitCode();

		// this.property = (${propertyFieldType}) value;
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		putInstanceField(f);

		// return
		mv.visitInsn(RETURN);

		mv.visitMaxs(2, 2);
		mv.visitEnd();
		b.notifyMethodFinished();
	}

	public void addInitializePrimitiveFields(List<PropertyDescription> primitiveProps) {
		mv = b.visitMethod(ACC_PROTECTED, "initializePrimitiveFields", voidType);
		mv.visitCode();

		for (PropertyDescription pd : primitiveProps) {
			mv.visitVarInsn(ALOAD, 0);
			pushDefault(pd.accessPropertyClass);
			addConversionFromPrimitiveIfNecessary(pd.accessPropertyClass);
			mv.visitFieldInsn(PUTFIELD, asmClass.getInternalName(), pd.fieldName, "Ljava/lang/Object;");
		}

		mv.visitInsn(RETURN);
		mv.visitMaxs(3, 1);
		mv.visitEnd();
		b.notifyMethodFinished();
	}

	@Override
	protected AsmMethod getter(PreliminaryProperty pp) {
		return pp.aopGetter;
	}

	@Override
	protected AsmMethod setter(PreliminaryProperty pp) {
		return pp.aopSetter;
	}

}
