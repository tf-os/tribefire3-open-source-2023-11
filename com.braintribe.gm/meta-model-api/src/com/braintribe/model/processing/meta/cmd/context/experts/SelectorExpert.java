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
package com.braintribe.model.processing.meta.cmd.context.experts;

import java.util.Collection;

import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.meta.cmd.context.SelectorContext;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;

/**
 * Represent a class that is able to evaluate a {@link MetaDataSelector} of some type. Information needed for evaluation
 * (if any) may be retrieved from {@link SelectorContext}, the type of information is specified by {@link Class}
 * instance of given {@link SelectorContextAspect}.
 * <p>
 * The expert also must be able to tell what aspects it may need ( {@link #getRelevantAspects(MetaDataSelector)}. This
 * information is then used for caching optimization of the meta data resolver.
 */
public interface SelectorExpert<T extends MetaDataSelector> {

	Collection<Class<? extends SelectorContextAspect<?>>> getRelevantAspects(T selector) throws Exception;

	boolean matches(T selector, SelectorContext context) throws Exception;

}
