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
package tribefire.extension.audit.processing;

import java.util.function.Supplier;

import com.braintribe.common.attribute.TypeSafeAttribute;
import com.braintribe.utils.collection.impl.AttributeContexts;

public class AttributeContextValueSupplier<T, A extends TypeSafeAttribute<T>> implements Supplier<T> {
	
	private Class<A> attribute;

	public AttributeContextValueSupplier(Class<A> attribute) {
		super();
		this.attribute = attribute;
	}
	
	public static <T, A extends TypeSafeAttribute<T>> AttributeContextValueSupplier<T, A> of(Class<A> attribute) {
		return new AttributeContextValueSupplier<T, A>(attribute);
	}

	@Override
	public T get() {
		return AttributeContexts.peek().findOrNull(attribute);
	}
}
