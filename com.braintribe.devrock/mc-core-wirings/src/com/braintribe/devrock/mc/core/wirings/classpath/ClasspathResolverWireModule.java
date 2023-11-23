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
package com.braintribe.devrock.mc.core.wirings.classpath;

import java.util.List;

import com.braintribe.devrock.mc.core.resolver.classpath.BasicClasspathDependencyResolver;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.util.Lists;

/**
 * This {@link WireTerminalModule} configures a {@link BasicClasspathDependencyResolver} along
 * with its precursors to be functional for transitive classpath Java dependency resolution.
 * 
 * @author Dirk Scheffler
 *
 */
public enum ClasspathResolverWireModule implements WireTerminalModule<ClasspathResolverContract> {
	INSTANCE;
	
	@Override
	public List<WireModule> dependencies() {
		return Lists.list(TransitiveResolverWireModule.INSTANCE);
	}
}
