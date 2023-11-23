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

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.lang.reflect.Modifier.PUBLIC;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.base.EntityBase;
import com.braintribe.model.generic.base.GenericBase;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmExistingClass;
import com.braintribe.model.processing.itw.asm.AsmExistingMethod;
import com.braintribe.model.processing.itw.asm.ClassBuilder;

/**
 * TERMINOLOGY NOTE: plural of "analysis" is "analyses" (see {@link "https://en.wiktionary.org/wiki/analyses"}).
 * 
 * @author peter.gazdik
 */
public class DefaultMethodsSupport {

	private final Map<AsmExistingClass, DefaultMethodAnalysis> analyses = newMap();

	// TODO do not override methods from superType with most properties, if same implementation
	public void implementDefaultMethodsIfEligible(ClassBuilder classBuilder, AsmExistingClass declaredInterface) {
		DefaultMethodAnalysis analysis = acquireAnalysis(declaredInterface);
		if (analysis == null)
			return;

		for (DefaultMethodDescriptor dmd : analysis.defaultMethods)
			classBuilder.addStaticDelegatorMethod(PUBLIC, dmd.implMethod.getName(), dmd.implMethod, dmd.withEntity);
	}

	private DefaultMethodAnalysis acquireAnalysis(AsmExistingClass javaInterface) {
		DefaultMethodAnalysis result = analyses.get(javaInterface);

		if (result == null) {
			result = createAnalysis(javaInterface);
			analyses.put(javaInterface, result);
		}

		return result;
	}

	private DefaultMethodAnalysis createAnalysis(AsmExistingClass javaInterface) {
		List<DefaultMethodAnalysis> superAnalyses = findAllSuperDescriptors(javaInterface);
		AsmExistingClass defaultMethodsAsmClass = findDefaultMethodsClass(javaInterface);

		if (superAnalyses.isEmpty() && defaultMethodsAsmClass == null)
			return null;

		DefaultMethodAnalysis result = new DefaultMethodAnalysis(javaInterface, defaultMethodsAsmClass);

		javaInterface.getMethods().forEach(ifaceMethod -> {
			Method existingMethod = ifaceMethod.getExistingMethod();
			if (!isGetterOrSetter(existingMethod) && !isEvalMethod(existingMethod, Evaluator.class)) {
				DefaultMethodDescriptor dmd = createDescriptorFor(ifaceMethod, result.dmClassIndex, superAnalyses);
				if (dmd != null)
					result.defaultMethods.add(dmd);
			}
		});

		return result;
	}

	/** For every direct superType this returns the complete List of super {@link DefaultMethodDescriptor}s. */
	private List<DefaultMethodAnalysis> findAllSuperDescriptors(AsmExistingClass javaInterface) {
		List<DefaultMethodAnalysis> allSuperDescriptors = newList();

		for (AsmExistingClass superInterface : javaInterface.getInterfaces()) {
			DefaultMethodAnalysis superAnalysis = acquireAnalysis(superInterface);
			if (superAnalysis != null)
				allSuperDescriptors.add(superAnalysis);
		}
		return allSuperDescriptors;
	}

	private DefaultMethodDescriptor createDescriptorFor(AsmExistingMethod ifaceMethod, DefaultMethodClassIndex dmClassIndex,
			List<DefaultMethodAnalysis> superAnalyses) {

		DefaultMethodDescriptor result = dmClassIndex.createDescriptorIfPossibleFor(ifaceMethod);
		if (result != null)
			return result;

		for (DefaultMethodAnalysis superAnalysis : superAnalyses) {
			for (DefaultMethodDescriptor dmd : superAnalysis.defaultMethods)
				if (dmd.isCompatibleWith(ifaceMethod))
					return dmd;
		}

		if (isGmMethod(ifaceMethod))
			return null;

		if (isAbstract(dmClassIndex.javaInterface))
			return null;

		if (ifaceMethod.isDefault())
			return null;

		throw new RuntimeException(
				"Default method implementation not found for: " + ifaceMethod + " for class: " + dmClassIndex.javaInterface.getSimpleName());
	}

	private boolean isAbstract(AsmExistingClass javaInterface) {
		return javaInterface.getAnnotation(Abstract.class) != null;
	}

	private boolean isGmMethod(AsmExistingMethod ifaceMethod) {
		Class<?> declaringClass = ifaceMethod.getExistingMethod().getDeclaringClass();
		return declaringClass == EntityBase.class || declaringClass == GenericBase.class;
	}

	// ###################################################
	// ## . . . . . . Static Helper Classes . . . . . . ##
	// ###################################################

	private static class DefaultMethodAnalysis {
		public DefaultMethodClassIndex dmClassIndex;
		public List<DefaultMethodDescriptor> defaultMethods = newList();

		public DefaultMethodAnalysis(AsmExistingClass javaInterface, AsmExistingClass defaultMethodsAsmClass) {
			this.dmClassIndex = new DefaultMethodClassIndex(javaInterface, defaultMethodsAsmClass);
		}
	}

	private static class DefaultMethodClassIndex {
		public final AsmExistingClass javaInterface;
		public final AsmExistingClass defaultMethodsAsmClass; // may be null, it just means no DefaultMethods defined on
																// this level

		public DefaultMethodClassIndex(AsmExistingClass javaInterface, AsmExistingClass defaultMethodsAsmClass) {
			this.javaInterface = javaInterface;
			this.defaultMethodsAsmClass = defaultMethodsAsmClass;
		}

		public DefaultMethodDescriptor createDescriptorIfPossibleFor(AsmExistingMethod ifaceMethod) {
			if (defaultMethodsAsmClass == null)
				return null;

			AsmClass[] ifaceMethodParams = ifaceMethod.getParams();
			AsmExistingMethod implMethod = defaultMethodsAsmClass.getMethod(ifaceMethod.getName(), ifaceMethod.getReturnType(), ifaceMethodParams);

			if (isNotNullAndStatic(implMethod))
				return new DefaultMethodDescriptor(implMethod, false);

			AsmClass[] extendedParams = getExtendedParamsFor(ifaceMethodParams);
			implMethod = defaultMethodsAsmClass.getMethod(ifaceMethod.getName(), ifaceMethod.getReturnType(), extendedParams);

			if (isNotNullAndStatic(implMethod))
				return new DefaultMethodDescriptor(implMethod, true);

			return null;
		}

		private AsmClass[] getExtendedParamsFor(AsmClass[] ifaceMethodParams) {
			AsmClass[] result = new AsmClass[ifaceMethodParams.length + 1];
			result[0] = javaInterface;
			System.arraycopy(ifaceMethodParams, 0, result, 1, ifaceMethodParams.length);

			return result;
		}

		private boolean isNotNullAndStatic(AsmExistingMethod m) {
			return m != null && Modifier.isStatic(m.getModifiers());
		}
	}

	private static class DefaultMethodDescriptor {
		public final AsmExistingMethod implMethod;
		public final boolean withEntity;

		public DefaultMethodDescriptor(AsmExistingMethod implMethod, boolean withEntity) {
			this.implMethod = implMethod;
			this.withEntity = withEntity;
		}

		public boolean isCompatibleWith(AsmExistingMethod otherIfaceMethod) {
			if (!implMethod.getName().equals(otherIfaceMethod.getName()) || implMethod.getReturnType() != otherIfaceMethod.getReturnType())
				return false;

			AsmClass[] implParams = implMethod.getParams();
			AsmClass[] otherIfaceParams = otherIfaceMethod.getParams();

			int implIndex = withEntity ? 1 : 0;
			int otherIfaceIndex = 0;

			if (!(implParams.length - implIndex == otherIfaceParams.length))
				return false;

			while (implIndex < implParams.length)
				if (implParams[implIndex++] != otherIfaceParams[otherIfaceIndex++])
					return false;

			return true;
		}
	}

	// ###################################################
	// ## . . . . . . Static Helper Methods . . . . . . ##
	// ###################################################

	public static boolean isEvalMethod(Method method, Class<?> evaluatorClass) {
		if (!method.getName().equals("eval"))
			return false;

		Class<?>[] params = method.getParameterTypes();
		if (params.length != 1)
			return false;

		return params[0] == evaluatorClass;
	}

	private static boolean isGetterOrSetter(Method m) {
		return isGetter(m) || isSetter(m);
	}

	private static boolean isGetter(Method m) {
		return m.getReturnType() != void.class && m.getParameterTypes().length == 0 && hasRightName(m, "get");
	}

	private static boolean isSetter(Method m) {
		return m.getReturnType() == void.class && m.getParameterTypes().length == 1 && hasRightName(m, "set");
	}

	private static boolean hasRightName(Method m, String prefix) {
		String name = m.getName();
		return name.startsWith(prefix) && name.length() > 3 && !Character.isLowerCase(name.charAt(3));
	}

	private static AsmExistingClass findDefaultMethodsClass(AsmExistingClass javaInterface) {
		for (AsmExistingClass nestedClass : javaInterface.getDeclaredClasses())
			if (nestedClass.getSimpleName().equals(javaInterface.getSimpleName() + "$DefaultMethods"))
				return nestedClass;

		return null;
	}

}
