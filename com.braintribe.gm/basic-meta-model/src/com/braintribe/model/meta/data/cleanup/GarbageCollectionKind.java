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
package com.braintribe.model.meta.data.cleanup;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * Used to specify how GC handles entities of the respective type.
 * 
 * @author michael.lafite
 */
public enum GarbageCollectionKind implements EnumBase {

	/**
	 * This setting marks entities from where the GC starts its reachability walks. <code>anchor</code> entities will
	 * never be {@link #collect collected} by the GC (i.e. same as {@link #hold}).
	 */
	anchor,
	/**
	 * The GC <i>holds</i> all the <code>hold</code> entities, i.e. they will never be {@link #collect collected} by
	 * the GC.
	 */
	hold,
	/**
	 * The GC <i>collects</i> (i.e. removes) all <code>collect</code> entities, unless they are reachable (directly or
	 * indirectly) from one of the {@link #anchor} entities
	 */
	collect;

	public static final EnumType T = EnumTypes.T(GarbageCollectionKind.class);
	
	@Override
	public EnumType type() {
		return T;
	}
}
