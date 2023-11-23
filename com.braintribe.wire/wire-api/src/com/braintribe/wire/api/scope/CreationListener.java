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
package com.braintribe.wire.api.scope;

import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

/**
 * CreationListener are used to intercept the whole creation of a bean including post construction and conditional logic before the actual instantiation.
 * It uses the before and after pointcut AOP logic projected on the methods of CreationListener.
 * 
 * @see WireContextConfiguration#addCreationListener(CreationListener)
 * @author dirk.scheffler
 * @author peter.gazdik
 */
public interface CreationListener {
	/**
	 * This method will be called at the beginning of a {@link Managed managed} method.
	 */
	void onBeforeCreate(InstanceHolder instanceHolder);

	/**
	 * This method will be called right before any exit (i.e. return or uncaught exception) from a {@link Managed managed} method.
	 */
	void onAfterCreate(InstanceHolder instanceHolder);
}
