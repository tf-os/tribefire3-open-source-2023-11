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
package com.braintribe.gwt.ioc.gme.client;

import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gm.model.persistence.reflection.api.GetModelAndWorkbenchEnvironment;
import com.braintribe.gwt.ioc.gme.client.expert.bootstrapping.BootstrappingRequest;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.gwt.utils.client.FastSet;
import com.braintribe.model.workbench.KnownWorkenchPerspective;
import com.braintribe.provider.SingletonBeanProvider;

public class Requests {
	
	protected static Supplier<BootstrappingRequest> bootstrappingRequest = new SessionScopedBeanProvider<BootstrappingRequest>() {
		@Override
		public BootstrappingRequest create() throws Exception {
			BootstrappingRequest bean = publish(new BootstrappingRequest());
			bean.setRpcEvaluator(GmRpc.serviceRequestEvaluator.get());
			bean.setModelEnvironmentRequest(modelEnvironmentRequest.get());
			return bean;
		}
	};
	
	private static Supplier<GetModelAndWorkbenchEnvironment> modelEnvironmentRequest = new SessionScopedBeanProvider<GetModelAndWorkbenchEnvironment>() {
		@Override
		public GetModelAndWorkbenchEnvironment create() throws Exception {
			GetModelAndWorkbenchEnvironment bean = GetModelAndWorkbenchEnvironment.T.create();
			bean.setFoldersByPerspective(perspectivesSet.get());
			return bean;
		}
	};
	
	private static Supplier<Set<String>> perspectivesSet = new SingletonBeanProvider<Set<String>>() {
		@Override
		public Set<String> create() throws Exception {
			Set<String> bean = new FastSet();
			for (KnownWorkenchPerspective known : KnownWorkenchPerspective.values())
				bean.add(known.toString());
			return bean;
		}
	};

}
