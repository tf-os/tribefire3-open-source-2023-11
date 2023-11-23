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
package com.braintribe.devrock.zarathud.runner.wire.space;

import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.zarathud.model.ClassesProcessingRunnerContext;
import com.braintribe.devrock.zarathud.model.JarProcessingRunnerContext;
import com.braintribe.devrock.zarathud.model.ResolvingRunnerContext;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zarathud.runner.impl.BasicZedWireRunner;
import com.braintribe.devrock.zarathud.runner.impl.builders.ClassesCoreContextBuilder;
import com.braintribe.devrock.zarathud.runner.impl.builders.JarCoreContextBuilder;
import com.braintribe.devrock.zarathud.runner.impl.builders.PreconfiguredResolvingCoreContextBuilder;
import com.braintribe.devrock.zarathud.runner.impl.builders.ResolvingCoreContextBuilder;
import com.braintribe.devrock.zarathud.runner.wire.contract.ZedRunnerContract;
import com.braintribe.devrock.zarathud.wirings.core.context.CoreContext;
import com.braintribe.provider.Holder;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;

@Managed
public class ZedRunnerSpace implements ZedRunnerContract {
			
	
	
	@Override
	@Managed(Scope.prototype)
	public ZedWireRunner resolvingRunner(ResolvingRunnerContext context) {
		ZedWireRunner bean = new BasicZedWireRunner( new ResolvingCoreContextBuilder(context));
		return bean;
	}
	
		
	@Override
	@Managed(Scope.prototype)
	public ZedWireRunner preconfiguredResolvingRunner(ResolvingRunnerContext context, ClasspathDependencyResolver resolver) {
		ZedWireRunner bean = new BasicZedWireRunner( new PreconfiguredResolvingCoreContextBuilder(context, resolver));
		return bean;
	}


	@Override
	@Managed(Scope.prototype)
	public ZedWireRunner jarRunner(JarProcessingRunnerContext context) {
		ZedWireRunner bean = new BasicZedWireRunner( new JarCoreContextBuilder(context));		
		return bean;
	}
	
	@Override
	@Managed(Scope.prototype)
	public ZedWireRunner classesRunner(ClassesProcessingRunnerContext context) {
		ZedWireRunner bean = new BasicZedWireRunner( new ClassesCoreContextBuilder(context));		
		return bean;
	}

	@Override
	@Managed(Scope.prototype)
	public ZedWireRunner coreRunner(CoreContext coreContext) {		
		Holder<CoreContext> holder = new Holder<CoreContext>( coreContext);
		ZedWireRunner wireRunner = new BasicZedWireRunner( holder);			
		return wireRunner;
	}
	

	
}
