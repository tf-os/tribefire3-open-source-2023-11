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
package com.braintribe.model.artifact.processing.cfg.env;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * base for both {@link OverridingEnvironmentVariable} and {@link OverridingSystemProperty}<br/>
 * defines a name value pair to override a system property or an environment
 * @author pit
 *
 */
@Abstract
public interface Override extends GenericEntity{
	
	final EntityType<Override> T = EntityTypes.T(Override.class);

	/**
	 * @param name - the name of the {@link Override}, i.e. environment-variable or system-property name
	 */
	@Mandatory
	void setName(String name);
	/**
	 * @return - the name of the {@link Override}, i.e. environment-variable or system-property name
	 */
	String getName();
	
	/**
	 * @param value - the {@link String} value of the {@link Override}
	 */
	void setValue(String value);
	/**
	 * @return - the {@link String} value of the {@link Override}
	 */
	String getValue();
	
	
}
