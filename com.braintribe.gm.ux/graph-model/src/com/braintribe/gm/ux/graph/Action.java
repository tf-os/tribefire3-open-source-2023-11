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
package com.braintribe.gm.ux.graph;

import java.util.Set;

import com.braintribe.gm.ux.decorator.Decorator;
import com.braintribe.model.descriptive.HasDescription;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.HasServiceRequest;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * An action is representing executable functionality on for example {@link GraphElement graph elements}.
 * 
 * <p>
 * It holds a {@link ServiceRequest} and can be further qualified by a {@link HasName name} and {@link HasDescription description}.
 * 
 */
public interface Action extends HasName, HasDescription, HasServiceRequest {

	EntityType<Action> T = EntityTypes.T(Action.class);
	
	Set<Decorator> getDecorators();
	void setDecorators(Set<Decorator> decorators);
	
}
