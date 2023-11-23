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
package com.braintribe.zarathud.model.data.natures;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents the feature of having the capability to be generic
 * @author pit
 *
 */
@Abstract
public interface HasGenericNature extends GenericEntity {
	
	EntityType<HasGenericNature> T = EntityTypes.T(HasGenericNature.class);
	
	String genericNature = "genericNature";
	
	/**
	 * true : is generic,
	 * false : isn't generic,
	 * null : hasn't been determined yet 
	 * 
	 * @return - whether it's a generic entity or not, or whether it's not known yet
	 */
	Boolean getGenericNature();
	void setGenericNature( Boolean value);
}
