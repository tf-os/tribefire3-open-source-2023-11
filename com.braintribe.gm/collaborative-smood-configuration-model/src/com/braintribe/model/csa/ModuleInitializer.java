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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface ModuleInitializer extends CustomInitializer {

	EntityType<ModuleInitializer> T = EntityTypes.T(ModuleInitializer.class);

	/** globalId of the relevant module */
	String getModuleId();
	void setModuleId(String moduleId);

	/**
	 * In case of an accessId redirect, this holds the value of the original access id, which is important for the
	 * resolution of the initializer on CSA bootstrap.
	 * <p>
	 * For information on redirect search for QualifiedStoragePriming.
	 */
	String getRedirectedAccessId();
	void setRedirectedAccessId(String redirectedAccessId);

}
