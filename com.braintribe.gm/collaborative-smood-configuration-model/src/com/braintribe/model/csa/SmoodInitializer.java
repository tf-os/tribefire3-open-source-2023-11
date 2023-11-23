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
package com.braintribe.model.csa;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Represents a configuration entry for a single persistence initializer of the collaborative smood access (CSA). When
 * initializing CSA, a list of SmoodInitalizers is given, each is resolved to the actual initializer, which is then
 * executed..
 * 
 * @see ManInitializer
 * 
 * @author peter.gazdik
 */
@Abstract
public interface SmoodInitializer extends GenericEntity {

	EntityType<SmoodInitializer> T = EntityTypes.T(SmoodInitializer.class);

	@Mandatory
	String getName();
	void setName(String name);

	/** If true, this initializer is not executed on bootstrap */
	boolean getSkip();
	void setSkip(boolean skip);

	default void normalize() {
		// noop
	}

}
