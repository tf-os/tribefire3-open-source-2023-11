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
package com.braintribe.model.processing.meta.cmd.context;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.builders.MdResolver;

/**
 * @see SelectorContext
 */
public interface MutableSelectorContext extends ExtendedSelectorContext {

	/**
	 * Puts new value for given aspect into the {@link SelectorContext}.
	 * <p>
	 * Value must not be {@code null} Implementation should throw an exception if caller tries to put a {@code null} in the context.
	 */
	<T, A extends SelectorContextAspect<? super T>> void put(Class<A> aspect, T value);

	/** Sets the {@link MdResolver#ignoreSelectors()} flag on the context. */
	void ignoreSelectors(EntityType<?>... exceptions);

	MutableSelectorContext copy();

}
