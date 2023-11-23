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

import static com.braintribe.model.processing.itw.asm.ImplementationTypeBuilder.DelegationType.invokeOnThis;
import static com.braintribe.model.processing.itw.asm.ImplementationTypeBuilder.DelegationType.passThisAsFirstParam;
import static com.braintribe.model.processing.itw.asm.ImplementationTypeBuilder.DelegationType.straightForward;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.braintribe.asm.Label;
import com.braintribe.asm.MethodVisitor;
import com.braintribe.asm.Opcodes;
import com.braintribe.model.processing.itw.synthesis.java.EmtpyDebugInfoProvider;

/**
 *
 */
public class ImplementationTypeBuilder extends TypeBuilder {

	protected DebugInfoProvider debugInfoProvider = EmtpyDebugInfoProvider.INSTANCE;
	private Map<AsmMethod, AsmMethod> staticSyntheticDelegators;

	public ImplementationTypeBuilder(AsmClassPool classPool, String typeSignature, AsmClass superClass, List<AsmClass> superInterfaces) {
		super(classPool, typeSignature, superClass, superInterfaces);
	}

	public ImplementationTypeBuilder withDebugInfo(DebugInfoProvider debugInfoProvider) {
		this.debugInfoProvider = debugInfoProvider;

		if (debugInfoProvider.hasInfoFor(asmClass.getName()))
			visitSource(asmClass.getSimpleName() + ".java");

		return this;
	}

	public AsmField addField(String name, AsmType fieldType, int modifier) {
		fv = visitField(modifier, name, fieldType);
		return notifyNewField();
	}

	public AsmMethod addGetter(AsmField field, String getterName) {
		return addGetter(field.getName(), field.getType(), getterName);
	}

	private AsmMethod addGetter(String fieldName, AsmType fieldType, String getterName) {
		return addGetter(fieldName, fieldType, getterName, Collections.<AsmAnnotationInstance> emptyList());
	}

	private AsmMethod addGetter(String fieldName, AsmType fieldType, String getterName, List<AsmAnnotationInstance> annotations) {
		Integer line = debugInfoProvider.getMethodLine(asmClass.getName(), getterName);

		mv = visitMethodWithGenerics(ACC_PUBLIC, getterName, fieldType);

		addMethodAnnotations(annotations);

		AsmClass fieldRawType = fieldType.getRawType();

		mv.visitCode();
		// <DEBUG>
		Label l0 = null;
		if (line != null) {
			l0 = visitNewLabel();
			mv.visitLineNumber(line, l0);
		}
		// </DEBUG>
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, asmClass.getInternalName(), fieldName, fieldRawType.getInternalNameLonger());
		mv.visitInsn(AsmUtils.getReturnOpCode(fieldRawType));
		// <DEBUG>
		if (line != null) {
			Label l1 = visitNewLabel();
			mv.visitLocalVariable("this", asmClass.getInternalNameLonger(), null, l0, l1, 0);
		}
		// </DEBUG>
		mv.visitMaxs(AsmUtils.getSize(fieldRawType), 1);
		mv.visitEnd();

		return notifyMethodFinished();
	}

	private Label visitNewLabel() {
		Label l = new Label();
		mv.visitLabel(l);

		return l;
	}

	public AsmMethod addSetter(AsmField field, String setterName) {
		return addSetter(field.getName(), field.getType(), setterName);
	}

	private AsmMethod addSetter(String fieldName, AsmType fieldType, String setterName) {
		return addSetter(fieldName, fieldType, setterName, Collections.<AsmAnnotationInstance> emptyList());
	}

	private AsmMethod addSetter(String fieldName, AsmType fieldType, String setterName, List<AsmAnnotationInstance> annotations) {
		Integer line = debugInfoProvider.getMethodLine(asmClass.getName(), setterName);

		mv = visitMethodWithGenerics(ACC_PUBLIC, setterName, AsmClassPool.voidType, fieldType);

		addMethodAnnotations(annotations);

		AsmClass fieldRawType = fieldType.getRawType();

		mv.visitCode();
		// <DEBUG>
		Label l0 = null;
		if (line != null) {
			l0 = visitNewLabel();
			mv.visitLineNumber(line, l0);
		}
		// </DEBUG>
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(AsmUtils.getLoadOpCode(fieldRawType), 1);
		mv.visitFieldInsn(PUTFIELD, asmClass.getInternalName(), fieldName, fieldRawType.getInternalNameLonger());
		// <DEBUG>
		if (line != null) {
			Label l1 = visitNewLabel();
			mv.visitLineNumber(line + 1, l1);
		}
		// </DEBUG>
		mv.visitInsn(RETURN);
		// <DEBUG>
		if (line != null) {
			String setterParamName = debugInfoProvider.getSetterParameterName(asmClass.getName(), setterName);

			Label l2 = visitNewLabel();
			mv.visitLocalVariable("this", asmClass.getInternalNameLonger(), null, l0, l2, 0);
			mv.visitLocalVariable(setterParamName, fieldRawType.getInternalNameLonger(), null, l0, l2, 1);
		}
		// </DEBUG>
		int max = 1 + AsmUtils.getSize(fieldRawType);
		mv.visitMaxs(max, max);
		mv.visitEnd();

		return notifyMethodFinished();
	}

	// probably unused
	public void invokePrivateOfDeclaring(MethodVisitor mv, AsmMethod privateMethod) {
		if (!Modifier.isPrivate(privateMethod.getModifiers()))
			throw new IllegalArgumentException("Method is not private: " + privateMethod);

		if (declaringClassBuilder == null)
			throw new RuntimeException("This is not an inner class!");

		if (!(declaringClassBuilder instanceof ImplementationTypeBuilder))
			throw new RuntimeException("The outer type is interface, so it is not expected to have private members!");

		ImplementationTypeBuilder declaringImplBuilder = (ImplementationTypeBuilder) declaringClassBuilder;
		AsmMethod m = declaringImplBuilder.acquireStaticSynteticDelegatorForPrivate(privateMethod);
		mv.visitMethodInsn(INVOKESTATIC, m.getDeclaringClass().getInternalName(), m.getName(), m.getSignature(), false);
	}

	private AsmMethod acquireStaticSynteticDelegatorForPrivate(AsmMethod privateMethod) {
		if (privateMethod.getDeclaringClass() != asmClass)
			throw new IllegalArgumentException(
					"Method was not decalred in current class (" + asmClass + "), but in: " + privateMethod.getDeclaringClass());

		if (staticSyntheticDelegators == null)
			staticSyntheticDelegators = newMap();

		AsmMethod result = staticSyntheticDelegators.get(privateMethod);
		if (result == null) {
			result = createStaticSyntheticDelegatorFor(privateMethod);
			staticSyntheticDelegators.put(privateMethod, result);
		}

		return result;
	}

	private AsmMethod createStaticSyntheticDelegatorFor(AsmMethod privateMethod) {
		String name = "access$" + staticSyntheticDelegators.size();
		int modifier = ACC_STATIC + ACC_SYNTHETIC;

		return addStaticDelegatorMethod(modifier, INVOKESPECIAL, name, privateMethod);
	}

	private AsmMethod addStaticDelegatorMethod(int modifier, int code, String methodName, AsmMethod delegateMethod) {
		AsmClass[] delegParams = delegateMethod.getParams();
		AsmClass[] params = new AsmClass[delegParams.length + 1];
		params[0] = asmClass;
		System.arraycopy(delegParams, 0, params, 1, delegParams.length);

		return addDelegatorMethodHelper(modifier, code, methodName, delegateMethod, params, straightForward);
	}

	// Probably unused
	public AsmMethod addDelegatorMethod(int modifier, int code, String methodName, AsmMethod delegateMethod) {
		return addDelegatorMethodHelper(modifier, code, methodName, delegateMethod, delegateMethod.getParams(), invokeOnThis);
	}

	public AsmMethod addStaticDelegatorMethod(int modifier, String methodName, AsmMethod delegateMethod, boolean firstDelegateParamIsThis) {
		DelegationType dt = firstDelegateParamIsThis ? passThisAsFirstParam : straightForward;

		return addDelegatorMethodHelper(modifier, Opcodes.INVOKESTATIC, methodName, delegateMethod, delegateMethod.getParams(), dt);
	}

	public static enum DelegationType {
		straightForward,
		invokeOnThis,
		passThisAsFirstParam,
	}

	private AsmMethod addDelegatorMethodHelper(int modifier, int code, String name, AsmMethod delegateMethod, AsmClass[] delegateParams,
			DelegationType delegationType) {
		int max = 0;

		AsmClass[] thisMethodParams = delegationType == passThisAsFirstParam ? Arrays.copyOfRange(delegateParams, 1, delegateParams.length)
				: delegateParams;
		mv = visitMethod(modifier, name, delegateMethod.getReturnType(), thisMethodParams);
		mv.visitCode();
		int counter = 0;
		if (delegationType == invokeOnThis) {
			mv.visitVarInsn(ALOAD, 0);
			counter = 1;
			max++;
		}

		for (AsmClass param : delegateParams) {
			mv.visitVarInsn(AsmUtils.getLoadOpCode(param), counter++);
			max += AsmUtils.getSize(param);
		}

		invokeMethod(code, delegateMethod);

		if (delegateMethod.getReturnType() != null) {
			mv.visitInsn(AsmUtils.getReturnOpCode(delegateMethod.getReturnType()));
			max = Math.max(max, AsmUtils.getSize(delegateMethod.getReturnType()));

		} else {
			mv.visitInsn(RETURN);
		}

		mv.visitMaxs(max, max);
		mv.visitEnd();

		return notifyMethodFinished();
	}

	private void invokeMethod(int code, AsmMethod m) {
		boolean itf = code == Opcodes.INVOKEINTERFACE;
		mv.visitMethodInsn(code, m.getDeclaringClass().getInternalName(), m.getName(), m.getSignature(), itf);
	}

}
