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
package com.braintribe.devrock.eclipse.model.ve;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * a override of either an environment variable or a system property 
 * @author pit
 *
 */
public interface EnvironmentOverride extends GenericEntity {
	
	final EntityType<EnvironmentOverride> T = EntityTypes.T(EnvironmentOverride.class);
	
	String name = "name";
	String value = "value";
	String overrideNature = "overrideNature";
	String active = "active";

	/**
	 * @return - the name of the env-variable or system-property
	 */
	String getName();
	void setName( String name);
	
	/**
	 * @return - the value that should be returned 
	 */
	String getValue();
	void setValue( String value);
	
	/**
	 * @return - the {@link OverrideType}, either env-variable or system-property
	 */
	OverrideType getOverrideNature();
	void setOverrideNature( OverrideType overrideType);
	
	/**
	 * @return - whether the override is active or not
	 */
	boolean getActive();
	void setActive( boolean active);
}
