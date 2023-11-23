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
package com.braintribe.devrock.zarathud.runner.wire.contract;

import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.zarathud.model.ClassesProcessingRunnerContext;
import com.braintribe.devrock.zarathud.model.JarProcessingRunnerContext;
import com.braintribe.devrock.zarathud.model.ResolvingRunnerContext;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zarathud.wirings.core.context.CoreContext;
import com.braintribe.wire.api.space.WireSpace;

/**
 * the different setups to run zed with 
 * @author pit
 *
 */
public interface ZedRunnerContract extends WireSpace {

	/**
	 * uses mc-core-wirings to get a {@link ClasspathDependencyResolver} via environment sensitive configuration 
	 * @param context - {@link ResolvingRunnerContext}
	 * @return - a fully configured {@link ZedWireRunner}
	 */	
	ZedWireRunner resolvingRunner(ResolvingRunnerContext context);
	
	/**
	 * uses the pre-configured resolver to build the classpath 
	 * @param context - the {@link ResolvingRunnerContext}
	 * @param resolver - a {@link ClasspathDependencyResolver} preconfigured
	 * @return - a fully configured {@link ZedWireRunner}
	 */
	ZedWireRunner preconfiguredResolvingRunner(ResolvingRunnerContext context, ClasspathDependencyResolver resolver);
	
	/**
	 * based on the ant-integration's way to do things 
	 * @param context - the {@link JarProcessingRunnerContext}
	 * @return - a fully configured {@link ZedWireRunner}
	 */
	ZedWireRunner jarRunner(JarProcessingRunnerContext context);

	/**
	 * based on the Eclipse integration's way to do things:
	 * may have additional folders for classes of projects rather than jars 
	 * @param context - the {@link ClassesProcessingRunnerContext}
	 * @return - a fully configured {@link ZedWireRunner}
	 */
	ZedWireRunner classesRunner(ClassesProcessingRunnerContext context);
	
	/**
	 * a generic {@link ZedWireRunner} with the base {@link CoreContext}
	 * @param coreContext - the basic {@link CoreContext}
	 * @return - a fully configured {@link ZedWireRunner}
	 */
	ZedWireRunner coreRunner( CoreContext coreContext);
		
}
