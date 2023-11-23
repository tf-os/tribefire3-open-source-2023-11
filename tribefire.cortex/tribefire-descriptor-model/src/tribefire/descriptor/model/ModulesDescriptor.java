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

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface ModulesDescriptor extends GenericEntity {

	EntityType<ModulesDescriptor> T = EntityTypes.T(ModulesDescriptor.class);

	/**
	 * Each {@link ModuleDescriptor} contains an information necessary to load that module, and the order in this list determines the order in which
	 * them modules are loaded. In general, if a module A depends on module B, then B must be loaded before A.
	 */
	List<ModuleDescriptor> getModules();
	void setModules(List<ModuleDescriptor> modules);

}
