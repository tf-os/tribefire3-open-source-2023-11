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
package com.braintribe.model.generic.enhance;

import com.braintribe.model.generic.annotation.GmSystemInterface;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.session.SessionAttachable;

/**
 * Enhanced entity is the standard entity implementation we use in GM, and it supports {@link PropertyAccessInterceptor}
 * driven cross-cutting concerns. This enables features like manipulation-tracking, lazy-loading, or ensuring reading a
 * collection property never results in <tt>null</tt> being returned. <br>
 * The counterpart of an enhanced entity is called a plain entity, which is implemented as a POJO, which provides a
 * minor advantage in smaller memory impact.
 */
@SuppressWarnings("deprecation")
@GmSystemInterface
public interface EnhancedEntity extends SessionAttachable {

	/**
	 * Returns the flags of given instance.
	 * 
	 * @see EntityFlags
	 */
	int flags();

	void assignFlags(int flags);

	/** Adds a {@link PropertyAccessInterceptor} (PAI) to the beginning of the PAI chain for this entity. */
	void pushPai(PropertyAccessInterceptor pai);

	/**
	 * Removes and returns the first {@link PropertyAccessInterceptor} (PAI) from the entity's PAI chain. This should
	 * only be called to remove a PAI which was previously pushed by the same client code.
	 */
	PropertyAccessInterceptor popPai();

	/**
	 * Set's this entity's {@link PropertyAccessInterceptor} (PAI) to the given value. Any previously set PAI is lost.
	 */
	void assignPai(PropertyAccessInterceptor pai);
}
