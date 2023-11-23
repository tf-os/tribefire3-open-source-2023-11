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

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.cmd.CascadingMetaDataException;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.cmd.builders.MdResolver;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;

/**
 * Extension of {@link SelectorContext} used exclusively internally by {@link CmdResolverImpl} (i.e. not given to
 * {@link SelectorExpert}s).
 */
public interface ExtendedSelectorContext extends SelectorContext {

	/**
	 * Returns an object that somewhat identifies the session. The only purpose of this object is for caching purposes - as long as we are
	 * within the same session, the returned object should be the same (thus serving as key in the cache).
	 */
	Object provideSession() throws CascadingMetaDataException;

	/** Returns <tt>true</tt> if resolving with {@link MdResolver#ignoreSelectors()} flag. */
	boolean shouldIgnoreSelectors();

	Set<EntityType<?>> notIgnoredSelectors();

}
