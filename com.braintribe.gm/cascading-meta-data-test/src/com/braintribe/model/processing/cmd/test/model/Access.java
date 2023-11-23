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
package com.braintribe.model.processing.cmd.test.model;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.selector.AccessSelector;
import com.braintribe.model.processing.meta.cmd.context.experts.AccessSelectorExpert;

/**
 * This is here for testing the {@link AccessSelector} and {@link AccessSelectorExpert}. In real life we would have e.g.
 * an instance of a SmoodAccess (the denotation type, not the actual implementation of course).
 */
public interface Access extends AccessSelector {

	EntityType<Access> T = EntityTypes.T(Access.class);

}
