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
package com.braintribe.model.processing.core.commons;

import java.util.function.Supplier;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.provider.Holder;


public class AssemblyEnhancer<T> implements Supplier<T> {

	protected Supplier<T> provider = null;


	@Override
	public T get() throws RuntimeException {
		if (this.provider == null) {
			return null;
		}
		
		T assembly = this.provider.get();
		
		T clonedAssembly = (T) GMF.getTypeReflection().getBaseType().clone(assembly, null, StrategyOnCriterionMatch.reference);
		
		return clonedAssembly;
	}

	public void setProvider(Supplier<T> provider) {
		this.provider = provider;
	}
	public void setAssembly(T assembly) {
		this.provider = new Holder<T>(assembly);
	}

}
