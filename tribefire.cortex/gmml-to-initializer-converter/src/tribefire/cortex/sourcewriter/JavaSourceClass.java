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
package tribefire.cortex.sourcewriter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author peter.gazdik
 */
public class JavaSourceClass {

	public static final JavaSourceClass StringJsc = JavaSourceClass.create(String.class);
	public static final JavaSourceClass IntegerJsc = JavaSourceClass.create(Integer.class);
	public static final JavaSourceClass LongJsc = JavaSourceClass.create(Long.class);
	public static final JavaSourceClass FloatJsc = JavaSourceClass.create(Float.class);
	public static final JavaSourceClass DoubleJsc = JavaSourceClass.create(Double.class);
	public static final JavaSourceClass BooleanJsc = JavaSourceClass.create(Boolean.class);
	public static final JavaSourceClass DateJsc = JavaSourceClass.create(Date.class);
	public static final JavaSourceClass BigDecimalJsc = JavaSourceClass.create(BigDecimal.class);

	public final String packageName;
	public final String simpleName;
	public final boolean isInterface;
	public final boolean isAnnotation;

	public static JavaSourceClass create(Class<?> clazz) {
		return new JavaSourceClass(clazz.getPackage().getName(), clazz.getSimpleName(), clazz.isInterface(), clazz.isAnnotation());
	}

	public static JavaSourceClassBuilder build(String fullName) {
		int i = fullName.lastIndexOf('.');
		String packageName = fullName.substring(0, i);
		String shortName = fullName.substring(i + 1);
		return build(packageName, shortName);
	}

	public static JavaSourceClassBuilder build(String packageName, String shortName) {
		return new JavaSourceClassBuilder(packageName, shortName);
	}

	public static class JavaSourceClassBuilder {
		private final String packageName;
		private final String shortName;
		private boolean isInterface;
		private boolean isAnnotation;

		public JavaSourceClassBuilder(String packageName, String shortName) {
			this.packageName = packageName;
			this.shortName = shortName;
		}

		public JavaSourceClassBuilder isInterface(boolean isInterface) {
			this.isInterface = isInterface;
			return this;
		}

		public JavaSourceClassBuilder isAnnotation(boolean isAnnotation) {
			this.isAnnotation = isAnnotation;
			return this;
		}

		public JavaSourceClass please() {
			return new JavaSourceClass(packageName, shortName, isInterface, isAnnotation);
		}
	}

	private JavaSourceClass(String packageName, String shortName, boolean isInterface, boolean isAnnotation) {
		this.packageName = packageName;
		this.simpleName = shortName;
		this.isInterface = isInterface;
		this.isAnnotation = isAnnotation;
	}

	public boolean requiresImport() {
		return !isJavaLang();
	}

	public boolean isJavaLang() {
		return "java.lang".equals(packageName);
	}

	public String fullName() {
		return packageName + "." + simpleName;
	}

}
