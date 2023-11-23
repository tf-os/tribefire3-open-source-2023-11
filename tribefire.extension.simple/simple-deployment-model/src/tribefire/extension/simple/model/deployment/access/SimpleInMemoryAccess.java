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
package tribefire.extension.simple.model.deployment.access;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The <code>SimpleInMemoryAccess</code> is a simple access implementation that stores entities from the <code>simple-data-model</code> in memory.
 * <p>
 * This entity type is a so-called <i>denotation type</i> which represents the respective <code>SimpleInMemoryAccess</code> implementation.<br>
 * Denotation types are used to assign {@link Deployable}s to a cartridge and to configure and deploy/undeploy them. All this can be done via the
 * Control Center (or programmatically).<br>
 * During deployment the denotation type instance is passed to the cartridge. The cartridge may then access the denotation type's properties to
 * configure the respective deployable. Denotation types may have any number (i.e. <code>0..n</code>) of configuration properties.
 * <p>
 * Since this is just an example, this denotation type only adds a {@link #getInitializeWithExampleData() single property} to the (standard)
 * {@link IncrementalAccess} properties.
 *
 * @author michael.lafite
 */
public interface SimpleInMemoryAccess extends IncrementalAccess {

	EntityType<SimpleInMemoryAccess> T = EntityTypes.T(SimpleInMemoryAccess.class);

	/**
	 * Specifies whether or not the access shall be initialized with example data (during deployment). This option is enabled by default.
	 */
	@Initializer("true")
	boolean getInitializeWithExampleData();
	void setInitializeWithExampleData(boolean initializeWithExampleData);
}
