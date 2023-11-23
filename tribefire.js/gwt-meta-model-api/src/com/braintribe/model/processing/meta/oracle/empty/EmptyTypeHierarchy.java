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
package com.braintribe.model.processing.meta.oracle.empty;

import java.util.Collections;
import java.util.Set;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.TypeHierarchy;

/**
 * @author peter.gazdik
 */
@SuppressWarnings("unusable-by-js")
public class EmptyTypeHierarchy implements TypeHierarchy {

	public static final EmptyTypeHierarchy INSTANCE = new EmptyTypeHierarchy();

	private EmptyTypeHierarchy() {
	}

	@Override
	public TypeHierarchy transitive() {
		return this;
	}

	@Override
	public TypeHierarchy includeSelf() {
		return this;
	}

	@Override
	public TypeHierarchy includeSelfForce() {
		return this;
	}

	@Override
	public TypeHierarchy onlyInstantiable() {
		return this;
	}

	@Override
	public TypeHierarchy onlyAbstract() {
		return this;
	}

	@Override
	public TypeHierarchy includeBaseType() {
		return this;
	}

	@Override
	public TypeHierarchy sorted(Order order) {
		return this;
	}

	@Override
	public <T extends GmType> Set<T> asGmTypes() {
		return Collections.emptySet();
	}

	@Override
	public <T extends GenericModelType> Set<T> asTypes() {
		return Collections.emptySet();
	}

	@Override
	public Set<EntityTypeOracle> asEntityTypeOracles() {
		return Collections.emptySet();
	}

}
