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

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.processing.meta.cmd.context.aspects.EntityAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmEntityTypeAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.GmPropertyAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;
import com.braintribe.model.processing.meta.cmd.context.scope.CmdScope;
import com.braintribe.model.processing.meta.cmd.context.scope.ScopedAspect;

import jsinterop.annotations.JsType;

/**
 * Represents some type of information that may be contained in the {@link SelectorContext}. There are some pre-defined types which are used
 * by the resolver itself, but in general it is up to the client (user of the resolver) to create his own custom aspects (by extending this
 * interface).
 * <p>
 * E.g. if someone wants to put some date in the context, one could create a date aspect like this:
 * {@code public interface DateAspect extends SelectorContextAspect<Date>}.
 * <p>
 * One may also annotate a given aspect with a scope, indicating how stable (or likely to be changed) a given aspect is, using the
 * {@link ScopedAspect} annotation. It is recommended to annotate the aspect with the most stable scope possible, since this information is
 * used for caching.
 * 
 * @param <T>
 *            type for given aspect
 * 
 * @see CmdScope
 * @see EntityAspect
 * @see GmEntityTypeAspect
 * @see GmPropertyAspect
 * @see RoleAspect
 */
@JsType (namespace = GmCoreApiInteropNamespaces.metadata)
public interface SelectorContextAspect<T> {
	// blank
}
