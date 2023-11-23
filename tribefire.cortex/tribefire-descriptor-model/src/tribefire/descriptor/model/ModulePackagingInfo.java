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
package tribefire.descriptor.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Wrapper for all the information about a module that is contained inside the module's jar file.
 * <p>
 * For now it only contains {@link #getWireModule() wire module} information, which is used for module loading.
 * <p>
 * The information is stored in a file called `packaging-info.yml`
 * 
 * @author peter.gazdik
 */
public interface ModulePackagingInfo extends GenericEntity {

	EntityType<ModulePackagingInfo> T = EntityTypes.T(ModulePackagingInfo.class);

	/** Name of the WireModule class of given tribefire module. */
	String getWireModule();
	void setWireModule(String wireModule);

}
