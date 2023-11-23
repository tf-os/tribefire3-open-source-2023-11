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
package com.braintribe.model.meta.data.prompt;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.EntityTypeMetaData;

/**
 * This meta data denotes entity types that actually stand for other by wrapping them based on a property they have.
 * This wrapping is normally used to adapt to other type hierarchies. Unfortunately the wrapping leads to a lack of
 * expressiveness. By actually denoting this wrapping a display and editing software like the GME can restore the
 * expressiveness for the user by using delegate editors on the actual wrapped property.
 * 
 * @author dirk.scheffler
 */
public interface Wrapped extends EntityTypeMetaData {

	EntityType<Wrapped> T = EntityTypes.T(Wrapped.class);

	/** the name of the property which holds the actual value that is wrapped by the wrapper type */
	void setWrapperPropertyName(String wrapperPropertyName);
	String getWrapperPropertyName();

}