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

import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * Context which {@link SelectorExpert}s are using to access relevant information. The information type is specified as a {@link Class}
 * instance of given {@link SelectorContextAspect} (e.g. {@code EntityAspect} -&gt; {@code EntityAspect.class}).
 */
public interface SelectorContext {

	ModelOracle getModelOracle();
	
	/** Retrieves a value for given aspect. The value may be <tt>null</tt>. */
	<T, A extends SelectorContextAspect<T>> T get(Class<A> aspect) throws CascadingMetaDataException;

	/**
	 * Retrieves a value for given aspect. If the value is not in the context, exception is thrown (i.e. <tt>null</tt> is never returned as
	 * a result).
	 */
	<T, A extends SelectorContextAspect<T>> T getNotNull(Class<A> aspect) throws CascadingMetaDataException;

}
