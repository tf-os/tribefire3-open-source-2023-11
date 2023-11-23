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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 */
public abstract class AsmClass implements AsmType {

	protected String name;
	protected int modifiers;
	protected AsmClassPool classPool;

	public AsmClass(String name, AsmClassPool classPool) {
		this.name = name;
		this.classPool = classPool;
	}

	public AsmClassPool getClassPool() {
		return classPool;
	}

	public String getName() {
		return name;
	}

	public String getSimpleName() {
		int index = name.lastIndexOf(".");
		return index >= 0 ? name.substring(index + 1) : name;
	}

	public int getModifiers() {
		return modifiers;
	}

	// ////////////////////

	public abstract AsmMethod getMethod(String name, AsmClass returnType, AsmClass... params);

	protected abstract AsmMethod getMethod(MethodSignature ms, AsmClass returnType);

	private String internalName;
	private String internalNameLonger;

	public String getInternalName() {
		if (internalName == null) {
			internalName = AsmUtils.getInternalName(this);
		}

		return internalName;
	}

	@Override
	public String genericSignatureOrNull() {
		return null;
	}

	/** Internal name including the L and semicolon, if necessary (object) */
	@Override
	public String getInternalNameLonger() {
		if (internalNameLonger == null) {
			internalNameLonger = AsmUtils.getInternalNameLonger(this);
		}

		return internalNameLonger;
	}

	@Override
	public AsmClass getRawType() {
		return this;
	}

	public boolean isInterface() {
		return Modifier.isInterface(modifiers);
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(modifiers);
	}

	public abstract AsmClass getDeclaringClass();

	public abstract List<? extends AsmClass> getDeclaredClasses();

	@Override
	public abstract String toString();

	@Override
	public abstract boolean isPrimitive();

	@Override
	public boolean isArray() {
		return false;
	}

	protected void addMemberNamesTo(Set<String> names) {
		if (isAbstract()) {
			for (AsmClass iface : getInterfaces()) {
				if (iface != null) {
					iface.addMemberNamesTo(names);
				}
			}
		}

		AsmClass s = getSuperClass();
		if (s != null) {
			s.addMemberNamesTo(names);
		}

		getMethods().forEach( m -> names.add(m.getName()));

		getFields().forEach(f -> names.add(f.getName()));
	}

	public abstract AsmClass getSuperClass();

	public abstract List<? extends AsmClass> getInterfaces();

	public abstract Stream<? extends AsmMethod> getMethods();

	public abstract Stream<AsmField> getFields();

	protected static final class MethodSignature {
		protected String name;
		protected AsmClass[] params;

		public MethodSignature(String name, AsmClass[] params) {
			this.name = name;
			this.params = params;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + name.hashCode();
			result = prime * result + params.length;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof MethodSignature)) {
				return false;
			}

			MethodSignature ms = (MethodSignature) obj;

			return name.equals(ms.name) && Arrays.equals(params, ms.params);
		}

		@Override
		public String toString() {
			return name + "(..)";
		}
	}
}
