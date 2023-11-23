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
package com.braintribe.model.version;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * something that can deliver major and minor version parts
 * @author pit/dirk
 *
 */
@Abstract
public interface HasMajorMinor extends GenericEntity {
	
	EntityType<HasMajorMinor> T = EntityTypes.T(HasMajorMinor.class);
	static final String major = "major";
	static final String minor = "minor";

	/**
	 * @return - the major value
	 */
	int getMajor();
	void setMajor( int major);
	
	/**
	 * @return - the minor value
	 */
	Integer getMinor();
	void setMinor( Integer minor);
	
	default int minor() {
		Integer rv = getMinor();
		if (rv != null) {
			return rv;
		}
		return 0;
	}
}
