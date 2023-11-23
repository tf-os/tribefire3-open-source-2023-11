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
package tribefire.platform.impl.thread;

import java.util.function.Supplier;

import com.braintribe.utils.collection.api.MinimalStack;

public class StandardThreadContextScopeSupplier <T> implements Supplier<StandardThreadContextScope<T>> {

	private Supplier<T> contextSupplier;
	private MinimalStack<T> contextConsumer;
	
	public void setContextSupplier(Supplier<T> contextSupplier) {
		this.contextSupplier = contextSupplier;
	}

	public void setContextConsumer(MinimalStack<T> contextConsumer) {
		this.contextConsumer = contextConsumer;
	}
	
	@Override
	public StandardThreadContextScope<T> get() {
		T context = contextSupplier.get();
		return new StandardThreadContextScope<T>(context, contextConsumer);
	}

}
