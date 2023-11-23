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
package com.braintribe.cartridge.common.processing.deployment;

import com.braintribe.wire.api.space.WireSpace;

/**
 * Use this marker interface on your {@link WireSpace}  
 * to support deployment reflection (lookup a BeanHolder for given instance) for all of its beans that are not automatically
 * reflected because of being scoped with the {@link DeploymentScope}
 *  
 * @author Dirk Scheffler
 *
 */
public interface ReflectBeansForDeployment {
	// intentionally left blank as this is a marker interface
}
