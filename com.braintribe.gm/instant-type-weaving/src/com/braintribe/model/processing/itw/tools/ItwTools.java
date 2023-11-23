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
package com.braintribe.model.processing.itw.tools;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.itw.asm.AsmClass;
import com.braintribe.model.processing.itw.asm.AsmClassLoading;
import com.braintribe.model.processing.itw.asm.AsmExistingClass;
import com.braintribe.model.processing.itw.asm.AsmNewClass;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesisException;
import com.braintribe.model.processing.itw.synthesis.java.JavaTypeSynthesisException;
import com.braintribe.model.weaving.ProtoGmProperty;

/**
 * 
 */
public class ItwTools {

	private static final String GM_SYSTEM_CL_CLASSPATH_RESOURCE = "META-INF/gm.systemCl";

	public static boolean useOwnCl() {
		return !forceSystemCl_SystemProperty() && !forceSystemCl_ClassPath();
	}

	private static boolean forceSystemCl_SystemProperty() {
		String property = System.getProperty("gm.ownCl");

		return "false".equalsIgnoreCase(property);
	}

	/** The mere presence of {@value ItwTools#GM_SYSTEM_CL_CLASSPATH_RESOURCE} on classpath indicates we want to use own {@link ClassLoader}. */
	private static boolean forceSystemCl_ClassPath() {
		try (InputStream is = GenericEntity.class.getClassLoader().getResourceAsStream(GM_SYSTEM_CL_CLASSPATH_RESOURCE)) {
			return is != null;

		} catch (Exception e) {
			return false;
		}
	}

	public static Class<?> findClass(String className) {
		try {
			return Class.forName(className, false, AsmClassLoading.contextClassLoader());

		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static String getGetterName(ProtoGmProperty property) {
		return getAccessorName("get", property);
	}

	public static String getGetterName(String propertyName) {
		return getAccessorName("get", propertyName);
	}

	public static String getSetterName(ProtoGmProperty property) {
		return getAccessorName("set", property);
	}

	public static String getSetterName(String propertyName) {
		return getAccessorName("set", propertyName);
	}

	public static String getReaderName(ProtoGmProperty property) {
		return getAccessorName("read", property);
	}

	public static String getWriterName(ProtoGmProperty property) {
		return getAccessorName("write", property);
	}

	private static String getAccessorName(String prefix, ProtoGmProperty property) {
		return getAccessorName(prefix, property.getName());
	}

	private static String getAccessorName(String prefix, String propertyName) {
		return prefix + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
	}

	public static <T> T extractStaticValue(Class<?> jvmClass, String fieldName) throws GenericModelTypeSynthesisException {
		try {
			Field singletonField = jvmClass.getField(fieldName);
			return cast(singletonField.get(null));

		} catch (Exception e) {
			throw new GenericModelTypeSynthesisException("Error while retrieving static field '" + fieldName + "' of: " + jvmClass.getName(), e);
		}
	}

	public static <T extends Annotation> T getAnnotation(Class<?> jvmClass, Class<T> annotationClass) {
		Class<?> superClass = jvmClass.getSuperclass();

		T annotation = jvmClass.getAnnotation(annotationClass);

		if (annotation == null && superClass != null)
			annotation = getAnnotation(superClass, annotationClass);

		if (annotation != null)
			return annotation;

		for (Class<?> iface : jvmClass.getInterfaces()) {
			annotation = getAnnotation(iface, annotationClass);
			if (annotation != null)
				break;
		}

		return annotation;
	}

	public static <T extends Annotation> T getAnnotation(AsmClass asmClass, Class<T> annotationClass) throws JavaTypeSynthesisException {
		if (asmClass instanceof AsmNewClass) {
			AsmNewClass asmNewClass = (AsmNewClass) asmClass;
			AsmClass superClass = asmNewClass.getSuperClass();

			T annotation = null;

			if (superClass != null)
				annotation = getAnnotation(superClass, annotationClass);

			if (annotation != null)
				return annotation;

			for (AsmClass iface : asmNewClass.getInterfaces()) {
				annotation = getAnnotation(iface, annotationClass);
				if (annotation != null)
					break;
			}

			return annotation;

		}

		if (asmClass instanceof AsmExistingClass)
			return getAnnotation(((AsmExistingClass) asmClass).getExistingClass(), annotationClass);

		return null;
	}

	public static <T> T cast(Object o) {
		return (T) o;
	}

	public static String getFieldName(String propertyName) {
		return " " + propertyName;
	}

}
